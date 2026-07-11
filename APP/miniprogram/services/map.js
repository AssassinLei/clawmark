import { request } from '../utils/request'

export const mapApi = {
  cities(coupleId) {
    return request(`/couples/${coupleId}/map/cities`, 'GET')
  }
}
