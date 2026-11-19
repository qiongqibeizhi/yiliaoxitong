import request from '../utils/request'

export function login(data) {
  return request.post('/api/auth/login', data)
}

export function logout() {
  return request.post('/api/auth/logout')
}

export function getProfile() {
  return request.get('/api/auth/me')
}

/** 患者自助注册：建档 + 开通账号，返回登录态 */
export function register(data) {
  return request.post('/api/auth/register', data)
}
