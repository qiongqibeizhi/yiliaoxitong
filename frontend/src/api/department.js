import request from '../utils/request'

export function pageDepartments(params) {
  return request.get('/api/departments', { params })
}

export function getDepartment(id) {
  return request.get(`/api/departments/${id}`)
}

export function createDepartment(data) {
  return request.post('/api/departments', data)
}

export function updateDepartment(id, data) {
  return request.put(`/api/departments/${id}`, data)
}

export function deleteDepartment(id) {
  return request.delete(`/api/departments/${id}`)
}
