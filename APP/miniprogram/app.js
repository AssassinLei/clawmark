import { env } from './config/env'
import { getToken } from './utils/auth'

App({
  globalData: {
    apiBaseUrl: env.apiBaseUrl,
    token: getToken() || ''
  },

  onLaunch() {
    const token = getToken()
    if (token) {
      this.globalData.token = token
    }
  },

  onShow() {
    const token = getToken()
    this.globalData.token = token || ''
    if (!token) {
      const pages = getCurrentPages()
      const current = pages.length > 0 ? pages[pages.length - 1].route : ''
      if (current && current !== 'pages/auth-login/index') {
        wx.reLaunch({ url: '/pages/auth-login/index' })
      }
    }
  }
})

