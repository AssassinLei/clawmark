import { photosApi } from '../../services/photos'
import { usersApi } from '../../services/users'
import { ensureLogin } from '../../utils/page-guard'

Page({
  data: {
    loading: false,
    loadingMore: false,
    albumId: '',
    albumTitle: '',
    cityCode: '',
    year: '',
    photos: [],
    hasMore: false,
    nextCursor: '',
    error: ''
  },

  onLoad(options) {
    const albumId = decodeURIComponent((options && options.albumId) || '')
    const albumTitle = decodeURIComponent((options && options.albumTitle) || '')
    const cityCode = decodeURIComponent((options && options.cityCode) || '')

    this.setData({ albumId, albumTitle, cityCode })
    wx.setNavigationBarTitle({ title: albumTitle || '相册照片' })
  },

  async onShow() {
    await this.reload()
  },

  async onPullDownRefresh() {
    await this.reload()
    wx.stopPullDownRefresh()
  },

  async onReachBottom() {
    if (this.data.loadingMore || !this.data.hasMore) return
    await this.loadPhotos(false)
  },

  onYearInput(e) {
    this.setData({ year: (e.detail.value || '').trim() })
  },

  async onApplyFilter() {
    await this.reload()
  },

  async onClearFilter() {
    this.setData({ year: '' })
    await this.reload()
  },

  async reload() {
    this.setData({ photos: [], nextCursor: '', hasMore: false })
    await this.loadPhotos(true)
  },

  async loadPhotos(reset) {
    if (!ensureLogin()) return
    const stageKey = reset ? 'loading' : 'loadingMore'
    this.setData({ [stageKey]: true, error: '' })

    try {
      const me = await usersApi.me()
      const coupleId = me.couple_id || ''
      if (!coupleId || !this.data.albumId) {
        this.setData({ photos: [], hasMore: false, nextCursor: '' })
        return
      }

      const params = {
        album_id: this.data.albumId,
        city_code: this.data.cityCode || undefined,
        year: this.data.year || undefined,
        cursor: reset ? '' : this.data.nextCursor,
        limit: 20
      }

      const resp = await photosApi.list(coupleId, params)
      const incoming = (resp && resp.photos) || []
      const photos = reset ? incoming : this.data.photos.concat(incoming)

      this.setData({
        photos,
        nextCursor: (resp && resp.next_cursor) || '',
        hasMore: !!(resp && resp.has_more)
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
