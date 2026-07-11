import { request } from '../utils/request'

export const usersApi = {
  me() {
    return request('/users/me', 'GET')
  },
  update(payload) {
    return request('/users/me', 'PUT', payload)
  }
}
