import { albumsApi } from '../../services/albums'
import { usersApi } from '../../services/users'
import { ensureLogin } from '../../utils/page-guard'

const typeOptions = ['all', 'city', 'custom']

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
    error: ''
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
    if (!this.data.coupleId || this.data.creating) return

    const modal = await wx.showModal({
      title: '新建相册',
      editable: true,
      placeholderText: '请输入相册名称（1-30字）'
    })
    if (!modal.confirm) return

    const title = (modal.content || '').trim()
    if (!title) {
      wx.showToast({ title: '请输入相册名称', icon: 'none' })
      return
    }

    this.setData({ creating: true })
    try {
      await albumsApi.create(this.data.coupleId, { title })
      wx.showToast({ title: '创建成功', icon: 'success' })
      await this.reload()
    } catch (e) {
      const message = e instanceof Error ? e.message : '创建失败'
      wx.showToast({ title: message, icon: 'none' })
    } finally {
      this.setData({ creating: false })
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
      let coupleId = this.data.coupleId
      if (!coupleId || reset) {
        const me = await usersApi.me()
        coupleId = me.couple_id || ''
      }
      if (!coupleId) {
        this.setData({ albums: [], total: 0, coupleId: '', hasMore: false })
        return
      }

      const type = this.data.typeOptions[this.data.typeIndex]
      const targetPage = reset ? 1 : this.data.page + 1
      const resp = await albumsApi.list(coupleId, type, targetPage, this.data.pageSize)
      const incoming = resp.albums || []
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
