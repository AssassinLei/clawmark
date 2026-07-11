import { request } from '../utils/request'

export const authApi = {
  login(payload) {
    return request('/auth/login', 'POST', payload)
  },
  refresh(access_token) {
    return request('/auth/refresh', 'POST', { access_token })
  }
}
