import request from '../utils/request'

export function pagePrescriptions(params) {
  return request.get('/api/prescriptions', { params })
}

export function getPrescription(id) {
  return request.get(`/api/prescriptions/${id}`)
}

export function getPrescriptionItems(id) {
  return request.get(`/api/prescriptions/${id}/items`)
}

export function createPrescription(data) {
  return request.post('/api/prescriptions', data)
}
