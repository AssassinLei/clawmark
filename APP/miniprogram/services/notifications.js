import { request } from '../utils/request'

export const notificationsApi = {
  subscribe(templateIds) {
    return request('/notifications/subscribe', 'POST', {
      template_ids: templateIds
    })
  }
}
