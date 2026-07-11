import { getToken } from './auth'

export function ensureLogin() {
  const token = getToken()
  if (!token) {
    const pages = getCurrentPages()
    const current = pages.length > 0 ? pages[pages.length - 1].route : ''
    if (current !== 'pages/auth-login/index') {
      wx.reLaunch({ url: '/pages/auth-login/index' })
    }
    return false
  }
  return true
}

