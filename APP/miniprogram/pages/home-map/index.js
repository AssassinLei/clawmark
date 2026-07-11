import { PROVINCE_LAYOUT } from '../../mock/map'
import { mapApi } from '../../services/map'
import { usersApi } from '../../services/users'
import { ensureLogin } from '../../utils/page-guard'

const provinceAlias = {
  内蒙古: '内蒙古自治区',
  广西: '广西壮族自治区',
  西藏: '西藏自治区',
  宁夏: '宁夏回族自治区',
  新疆: '新疆维吾尔自治区',
  香港: '香港特别行政区',
  澳门: '澳门特别行政区'
}

function normalizeProvinceName(name) {
  const raw = String(name || '').trim()
  if (!raw) return ''
  return provinceAlias[raw] || raw
}

function buildProvinceMap(cities) {
  const byName = {}
  cities.forEach((city) => {
    const provinceName = normalizeProvinceName(city.province)
    if (!provinceName) return
    if (!byName[provinceName]) {
      byName[provinceName] = { cityCount: 0, latestDate: '' }
    }
    byName[provinceName].cityCount += 1
    if ((city.latest_date || '') > byName[provinceName].latestDate) {
      byName[provinceName].latestDate = city.latest_date || ''
    }
  })

  return PROVINCE_LAYOUT.map((p) => {
    const visit = byName[p.name]
    return {
      ...p,
      visited: !!visit,
      cityCount: (visit && visit.cityCount) || 0,
      latestDate: (visit && visit.latestDate) || ''
    }
  })
}

Page({
  data: {
    loading: false,
    keyword: '',
    provinces: [],
    visibleProvinces: [],
    groups: [],
    visitedProvinceCount: 0,
    visitedCityCount: 0,
    error: ''
  },

  async onShow() {
    await this.loadMapData()
  },

  async onPullDownRefresh() {
    await this.loadMapData()
    wx.stopPullDownRefresh()
  },

  onKeywordInput(e) {
    const keyword = (e.detail.value || '').trim()
    this.setData({ keyword })
    this.applyFilter()
  },

  clearKeyword() {
    this.setData({ keyword: '' })
    this.applyFilter()
  },

  onTapProvince(e) {
    const { code, name } = e.currentTarget.dataset || {}
    if (!code || !name) return
    wx.navigateTo({
      url: `/pages/province-albums/index?provinceCode=${encodeURIComponent(code)}&provinceName=${encodeURIComponent(name)}`
    })
  },

  async loadMapData() {
    if (!ensureLogin()) return
    this.setData({ loading: true, error: '' })
    try {
      const me = await usersApi.me()
      const coupleId = me.couple_id || ''
      if (!coupleId) {
        this.setData({ provinces: [], visibleProvinces: [], visitedProvinceCount: 0, visitedCityCount: 0, groups: [] })
        return
      }

      const resp = await mapApi.cities(coupleId)
      const cities = (resp && resp.cities) || []
      const provinces = buildProvinceMap(cities)

      const visitedProvinceCount = provinces.filter((p) => p.visited).length
      const visitedCityCount = cities.length

      this.setData({
        provinces,
        visitedProvinceCount,
        visitedCityCount
      })
      this.applyFilter(provinces)
    } catch (e) {
      const message = e instanceof Error ? e.message : '地图加载失败'
      this.setData({ error: message })
      wx.showToast({ title: message, icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  applyFilter(sourceProvinces) {
    const keyword = this.data.keyword.toLowerCase()
    const source = sourceProvinces || this.data.provinces
    const visibleProvinces = source.filter((p) => {
      if (!keyword) return true
      return [p.name, p.shortName, p.code].some((v) => v.toLowerCase().includes(keyword))
    })

    const visited = visibleProvinces
      .filter((p) => p.visited)
      .sort((a, b) => (b.latestDate || '').localeCompare(a.latestDate || ''))

    const groups = [
      {
        province: '已点亮省份',
        visitedCount: visited.length,
        cities: visited.map((p) => ({
          code: p.code,
          shortName: p.shortName,
          cityCount: p.cityCount,
          latestDate: p.latestDate
        }))
      }
    ]

    this.setData({ visibleProvinces, groups })
  }
})
