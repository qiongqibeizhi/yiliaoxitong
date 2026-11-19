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

/** 完成就诊（医生/管理员） */
export function completeRegistration(id) {
  return http.put(`/registrations/${id}/complete`)
}
