const TOKEN_KEY = 'ACCESS_TOKEN'

export function setToken(token) {
  wx.setStorageSync(TOKEN_KEY, token)
}

export function getToken() {
  return wx.getStorageSync(TOKEN_KEY) || ''
}

export function clearToken() {
  wx.removeStorageSync(TOKEN_KEY)
}
