import request from '../utils/request'

export function pageSchedules(params) {
  return request.get('/api/schedules', { params })
}

export function getSchedule(id) {
  return request.get(`/api/schedules/${id}`)
}
