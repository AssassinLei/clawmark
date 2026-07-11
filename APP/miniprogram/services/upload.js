import { request } from '../utils/request'

export const uploadApi = {
  getToken(payload) {
    return request('/upload/token', 'POST', payload)
  }
}
