import { albumsApi } from '../../services/albums'
import { usersApi } from '../../services/users'
import { ensureLogin } from '../../utils/page-guard'

const typeOptions = ['all', 'custom']

function normalizeCoupleId(raw) {

  if (raw === undefined || raw === null) return ''
  const value = String(raw).trim()
  return value
}

function formatCreatedAt(value) {
  const raw = String(value || '').trim()
  if (!raw) return ''

  const date = new Date(raw)
  if (!Number.isNaN(date.getTime())) {
    const pad = (n) => String(n).padStart(2, '0')
    const yyyy = date.getFullYear()
    const MM = pad(date.getMonth() + 1)
    const dd = pad(date.getDate())
    const HH = pad(date.getHours())
    const mm = pad(date.getMinutes())
    return `${yyyy}-${MM}-${dd} ${HH}:${mm}`
  }

  return raw.replace('T', ' ').replace('Z', '').slice(0, 16)
}

Page({
  data: {
    loading: false,
    loadingMore: false,
    creating: false,
    albums: [],
    total: 0,
    page: 1,
    pageSize: 20,
    hasMore: true,
    typeIndex: 0,
    typeOptions,
    coupleId: '',
    error: '',
    showCreateForm: false,
    draftTitle: '',
    draftCityName: '',
    deletingAlbumId: ''
  },

  async onShow() {
    await this.reload()
  },

  async onPullDownRefresh() {
    await this.reload()
    wx.stopPullDownRefresh()
  },

  async onReachBottom() {
    if (!this.data.hasMore || this.data.loadingMore || this.data.loading) return
    await this.loadAlbums(false)
  },

  async onTypeChange(e) {
    const typeIndex = Number(e.detail.value || 0)
    this.setData({ typeIndex })
    await this.reload()
  },

  async onCreateAlbum() {
    if (this.data.creating || !ensureLogin()) return

    let coupleId = normalizeCoupleId(this.data.coupleId)
    if (!coupleId) {
      try {
        const me = await usersApi.me()
        coupleId = normalizeCoupleId(me.couple_id)
      } catch (e) {}
    }
    if (!coupleId) {
      coupleId = '0'
    }
    this.setData({
      coupleId,
      showCreateForm: true,
      draftTitle: '',
      draftCityName: ''
    })
  },

  onDraftTitleInput(e) {
    this.setData({ draftTitle: (e.detail.value || '').trim() })
  },

  onDraftCityInput(e) {
    this.setData({ draftCityName: (e.detail.value || '').trim() })
  },

  onCancelCreateAlbum() {
    if (this.data.creating) return
    this.setData({ showCreateForm: false, draftTitle: '', draftCityName: '' })
  },

  async onConfirmCreateAlbum() {
    if (this.data.creating) return

    const coupleId = normalizeCoupleId(this.data.coupleId) || '0'
    const title = (this.data.draftTitle || '').trim()
    const cityName = (this.data.draftCityName || '').trim()

    if (!title) {
      wx.showToast({ title: '请输入相册名称', icon: 'none' })
      return
    }
    if (!cityName) {
      wx.showToast({ title: '请输入城市名', icon: 'none' })
      return
    }

    this.setData({ creating: true })
    try {
      await albumsApi.create(coupleId, { title, city_name: cityName })
      wx.showToast({ title: '创建成功', icon: 'success' })
      this.setData({ showCreateForm: false, draftTitle: '', draftCityName: '' })
      await this.reload()
    } catch (e) {
      const message = e instanceof Error ? e.message : '创建失败'
      wx.showToast({ title: message, icon: 'none' })
    } finally {
      this.setData({ creating: false })
    }
  },

  onTapAlbum(e) {
    const { id, title, type, citycode } = e.currentTarget.dataset || {}
    if (!id) return
    wx.navigateTo({
      url: `/pages/album-photos/index?albumId=${encodeURIComponent(id)}&albumTitle=${encodeURIComponent(title || '')}&albumType=${encodeURIComponent(type || '')}&cityCode=${encodeURIComponent(citycode || '')}`
    })
  },

  async onDeleteAlbum(e) {
    const albumId = (e.currentTarget.dataset && e.currentTarget.dataset.id) || ''
    if (!albumId || this.data.creating || this.data.deletingAlbumId) return
    if (!ensureLogin()) return

    const confirm = await wx.showModal({
      title: '删除相册',
      content: '确认删除该相册吗？若该相册中的照片不在其它相册中，这些照片也会被删除。',
      confirmColor: '#d03050'
    })
    if (!confirm.confirm) return

    this.setData({ deletingAlbumId: albumId })
    try {
      let coupleId = normalizeCoupleId(this.data.coupleId)
      if (!coupleId) {
        const me = await usersApi.me()
        coupleId = normalizeCoupleId(me.couple_id)
      }
      if (!coupleId) {
        throw new Error('未找到情侣信息')
      }

      await albumsApi.delete(coupleId, albumId)
      const nextAlbums = this.data.albums.filter((a) => a.album_id !== albumId)
      const nextTotal = Math.max(0, Number(this.data.total || 0) - 1)
      this.setData({
        albums: nextAlbums,
        total: nextTotal,
        hasMore: nextAlbums.length < nextTotal
      })
      wx.showToast({ title: '删除成功', icon: 'success' })
    } catch (e1) {
      const message = e1 instanceof Error ? e1.message : '删除失败'
      wx.showToast({ title: message, icon: 'none' })
    } finally {
      this.setData({ deletingAlbumId: '' })
    }
  },

  async reload() {
    this.setData({ page: 1, albums: [], hasMore: true })
    await this.loadAlbums(true)
  },

  async loadAlbums(reset) {
    if (!ensureLogin()) return
    if (reset) {
      this.setData({ loading: true, error: '' })
    } else {
      this.setData({ loadingMore: true, error: '' })
    }

    try {
      let coupleId = normalizeCoupleId(this.data.coupleId)
      if (!coupleId || reset) {
        const me = await usersApi.me()
        coupleId = normalizeCoupleId(me.couple_id)
      }
      if (!coupleId) {
        coupleId = '0'
      }


      const type = this.data.typeOptions[this.data.typeIndex]
      const targetPage = reset ? 1 : this.data.page + 1
      const resp = await albumsApi.list(coupleId, type, targetPage, this.data.pageSize)
      const incoming = (resp.albums || []).map((item) => ({
        ...item,
        created_at_text: formatCreatedAt(item.created_at)
      }))
      const albums = reset ? incoming : this.data.albums.concat(incoming)

      this.setData({
        albums,
        total: resp.total || 0,
        coupleId,
        page: targetPage,
        hasMore: albums.length < (resp.total || 0)
      })
    } catch (e) {
      const message = e instanceof Error ? e.message : '加载失败'
      this.setData({ error: message })
      wx.showToast({ title: message, icon: 'none' })
    } finally {
      this.setData({ loading: false, loadingMore: false })
    }
  }
})
