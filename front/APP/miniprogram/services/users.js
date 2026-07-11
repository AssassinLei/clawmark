import { request } from '../utils/request'

function getAppSafe() {
  try {
    return getApp()
  } catch (e) {
    return null
  }
}

export const usersApi = {
  requestMe() {
    return request('/users/me', 'GET')
  },

  async me(force = false) {
    const app = getAppSafe()
    const cached = app && app.globalData && app.globalData.currentUser
    if (!force && cached) {
      return cached
    }
    const me = await this.requestMe()
    if (app && app.setCurrentUser) {
      app.setCurrentUser(me)
    }
    return me
  },

  async update(payload) {
    const user = await request('/users/me', 'PUT', payload)
    const app = getAppSafe()
    if (app && app.setCurrentUser) {
      const current = app.globalData.currentUser || {}
      app.setCurrentUser({ ...current, ...user })
    }
    return user
  },

  clearCache() {
    const app = getAppSafe()
    if (app && app.clearCurrentUser) {
      app.clearCurrentUser()
    }
  }
}

