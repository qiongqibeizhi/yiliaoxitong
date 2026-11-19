import request from '../utils/request'

export function pageDepartments(params) {
  return request.get('/api/departments', { params })
}

export function getDepartment(id) {
  return request.get(`/api/departments/${id}`)
}
