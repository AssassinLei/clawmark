import { env } from './config/env'
import { usersApi } from './services/users'
import { getToken } from './utils/auth'

App({
  globalData: {
    apiBaseUrl: env.apiBaseUrl,
    token: getToken() || '',
    currentUser: null,
    currentUserLoadedAt: 0
  },

  async onLaunch() {
    const token = getToken()
    if (token) {
      this.globalData.token = token
      await this.fetchCurrentUser(false)
    }
  },

  async onShow() {
    const token = getToken()
    this.globalData.token = token || ''
    if (!token) {
      this.clearCurrentUser()
      const pages = getCurrentPages()
      const current = pages.length > 0 ? pages[pages.length - 1].route : ''
      if (current && current !== 'pages/auth-login/index') {
        wx.reLaunch({ url: '/pages/auth-login/index' })
      }
      return
    }

    if (!this.globalData.currentUser) {
      await this.fetchCurrentUser(false)
    }
  },

  setCurrentUser(user) {
    this.globalData.currentUser = user || null
    this.globalData.currentUserLoadedAt = user ? Date.now() : 0
  },

  clearCurrentUser() {
    this.globalData.currentUser = null
    this.globalData.currentUserLoadedAt = 0
  },

  async fetchCurrentUser(force = false) {
    if (!this.globalData.token) return null
    if (!force && this.globalData.currentUser) return this.globalData.currentUser
    try {
      const me = await usersApi.requestMe()
      this.setCurrentUser(me)
      return me
    } catch (e) {
      return null
    }
  }
})


