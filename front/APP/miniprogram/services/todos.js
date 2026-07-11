import { request } from '../utils/request'

export const todosApi = {
  list(coupleId, type, status, page = 1, pageSize = 20) {
    return request(`/couples/${coupleId}/todos?type=${type}&status=${status}&page=${page}&page_size=${pageSize}`, 'GET')
  },
  create(coupleId, payload) {
    return request(`/couples/${coupleId}/todos`, 'POST', payload)
  },
  update(coupleId, todoId, payload) {
    return request(`/couples/${coupleId}/todos/${todoId}`, 'PUT', payload)
  },
  delete(coupleId, todoId) {
    return request(`/couples/${coupleId}/todos/${todoId}`, 'DELETE')
  },
  complete(coupleId, todoId) {
    return request(`/couples/${coupleId}/todos/${todoId}/complete`, 'PATCH')
  },
  urge(coupleId, todoId) {
    return request(`/couples/${coupleId}/todos/${todoId}/urge`, 'POST')
  }
}
