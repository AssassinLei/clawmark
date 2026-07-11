import { authApi } from '../../services/auth'
import { getToken, setToken } from '../../utils/auth'


Page({
  data: {
    loading: false
  },

  async handleWxLogin() {
    if (this.data.loading) return
    this.setData({ loading: true })
    try {
      const loginRes = await wxLogin()
      if (!loginRes || !loginRes.code) {
        throw new Error('未获取到微信登录凭证')
      }
      const result = await authApi.login({ code: loginRes.code })
      setToken(result.access_token)
      const app = getApp()
      app.globalData.token = result.access_token
      wx.switchTab({ url: '/pages/home-map/index' })
    } catch (e) {
      const message = e instanceof Error ? e.message : '登录失败'
      wx.showToast({ title: message, icon: 'none' })
    } finally {

      this.setData({ loading: false })
    }
  }
})

function wxLogin() {
  return new Promise((resolve, reject) => {
    wx.login({
      success: resolve,
      fail: reject
    })
  })
}
