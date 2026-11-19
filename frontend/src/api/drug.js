import request from '../utils/request'

export function pageDrugs(params) {
  return request.get('/api/drugs', { params })
}

export function getDrug(id) {
  return request.get(`/api/drugs/${id}`)
}
