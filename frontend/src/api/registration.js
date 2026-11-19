import request from '../utils/request'

export function pageRegistrations(params) {
  return request.get('/api/registrations', { params })
}

export function register(data) {
  return request.post('/api/registrations', data)
}

export function cancelRegistration(id) {
  return request.put(`/api/registrations/${id}/cancel`)
}
