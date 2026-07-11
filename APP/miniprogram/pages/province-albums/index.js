import { albumsApi } from '../../services/albums'
import { usersApi } from '../../services/users'
import { isCityInProvince } from '../../mock/map'
import { ensureLogin } from '../../utils/page-guard'

async function fetchAllAlbums(coupleId) {
  let page = 1
  const pageSize = 50
  let total = 0
  let all = []

  do {
    const resp = await albumsApi.list(coupleId, 'all', page, pageSize)
    const list = (resp && resp.albums) || []
    total = Number(resp && resp.total) || 0
    all = all.concat(list)
    page += 1
    if (list.length === 0) break
  } while (all.length < total && page <= 20)

  return all
}

Page({
  data: {
    loading: false,
    provinceCode: '',
    provinceName: '',
    albums: [],
    total: 0,
    error: ''
  },

  onLoad(options) {
    const provinceCode = decodeURIComponent((options && options.provinceCode) || '')
    const provinceName = decodeURIComponent((options && options.provinceName) || '')
    this.setData({ provinceCode, provinceName })
    wx.setNavigationBarTitle({ title: `${provinceName || '省份'}相册` })
  },

  async onShow() {
    await this.loadProvinceAlbums()
  },

  async onPullDownRefresh() {
    await this.loadProvinceAlbums()
    wx.stopPullDownRefresh()
  },

  async loadProvinceAlbums() {
    if (!ensureLogin()) return

    this.setData({ loading: true, error: '' })
    try {
      const me = await usersApi.me()
      const coupleId = me.couple_id || ''
      if (!coupleId) {
        this.setData({ albums: [], total: 0 })
        return
      }

      // TODO: 后端部署后建议新增接口：GET /couples/{id}/albums?province_code=xx
      const allAlbums = await fetchAllAlbums(coupleId)
      const provinceCode = this.data.provinceCode
      const filtered = allAlbums
        .filter((a) => a.type === 'city' && isCityInProvince(a.city_code, provinceCode))
        .sort((a, b) => String(b.created_at || '').localeCompare(String(a.created_at || '')))

      this.setData({ albums: filtered, total: filtered.length })
    } catch (e) {
      const message = e instanceof Error ? e.message : '加载失败'
      this.setData({ error: message })
      wx.showToast({ title: message, icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  onTapAlbum(e) {
    const { id, title, citycode } = e.currentTarget.dataset || {}
    if (!id) return
    wx.navigateTo({
      url: `/pages/album-photos/index?albumId=${encodeURIComponent(id)}&albumTitle=${encodeURIComponent(title || '')}&cityCode=${encodeURIComponent(citycode || '')}`
    })
  }
})
