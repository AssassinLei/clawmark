import { request } from '../utils/request'

export const couplesApi = {
  createInvite() {
    return request('/couples/invite', 'POST')
  },
  bind(invite_code) {
    return request('/couples/bind', 'POST', { invite_code })
  },
  unbind() {
    return request('/couples/bind', 'DELETE')
  }
}
