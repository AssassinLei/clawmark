import { request } from '../utils/request'

export const albumsApi = {
  list(coupleId, type, page = 1, pageSize = 20) {
    return request(`/couples/${coupleId}/albums?type=${type}&page=${page}&page_size=${pageSize}`, 'GET')
  },
  create(coupleId, payload) {
    return request(`/couples/${coupleId}/albums`, 'POST', payload)
  },
  delete(coupleId, albumId) {
    return request(`/couples/${coupleId}/albums/${albumId}`, 'DELETE')
  }
}
