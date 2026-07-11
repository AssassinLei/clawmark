import { request } from '../utils/request'

export const photosApi = {
  list(coupleId, params = {}) {
    const query = []
    if (params.album_id) query.push(`album_id=${encodeURIComponent(params.album_id)}`)
    if (params.city_code) query.push(`city_code=${encodeURIComponent(params.city_code)}`)
    if (params.year) query.push(`year=${encodeURIComponent(params.year)}`)
    if (params.cursor) query.push(`cursor=${encodeURIComponent(params.cursor)}`)
    if (params.limit) query.push(`limit=${encodeURIComponent(params.limit)}`)
    const suffix = query.length ? `?${query.join('&')}` : ''
    return request(`/couples/${coupleId}/photos${suffix}`, 'GET')
  }
}
