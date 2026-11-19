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

/** 处方应付金额（明细单价 × 数量之和） */
export function getPrescriptionAmount(id) {
  return request.get(`/api/prescriptions/${id}/amount`)
}

/** 患者凭余额支付处方药费 */
export function payPrescription(id) {
  return request.put(`/api/prescriptions/${id}/pay`)
}
