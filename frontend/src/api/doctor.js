import request from '../utils/request'

export function pageDoctors(params) {
  return request.get('/api/doctors', { params })
}

export function getDoctor(id) {
  return request.get(`/api/doctors/${id}`)
}

export function createDoctor(data) {
  return request.post('/api/doctors', data)
}

export function updateDoctor(id, data) {
  return request.put(`/api/doctors/${id}`, data)
}

export function deleteDoctor(id) {
  return request.delete(`/api/doctors/${id}`)
}
