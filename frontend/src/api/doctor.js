import request from '../utils/request'

export function pageDoctors(params) {
  return request.get('/api/doctors', { params })
}

export function getDoctor(id) {
  return request.get(`/api/doctors/${id}`)
}
