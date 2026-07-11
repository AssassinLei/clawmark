import { env } from '../config/env'
import { clearToken, getToken } from './auth'


function unwrapBody(body, resolve, reject) {
  if (!body) {
    reject(new Error('空响应'))
    return
  }
  if (body.code !== 0) {
    reject(new Error(body.message || '请求失败'))
    return
  }
  resolve(body.data)
}

export function request(url, method = 'GET', data) {
  return new Promise((resolve, reject) => {
    const token = getToken()

    wx.request({
      url: `${env.apiBaseUrl}${url}`,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      success: (res) => {
        const body = res.data
        if (body && body.code === 1002) {
          clearToken()
          const pages = getCurrentPages()
          const current = pages.length > 0 ? pages[pages.length - 1].route : ''
          if (current !== 'pages/auth-login/index') {
            wx.reLaunch({ url: '/pages/auth-login/index' })
          }
          reject(new Error(body.message || '登录已失效，请重新登录'))
          return
        }
        unwrapBody(body, resolve, reject)
      },
      fail: (err) => reject(err)
    })
  })
}

