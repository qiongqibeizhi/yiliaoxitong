const TOKEN_KEY = 'medical_token'
const USER_KEY = 'medical_user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function getUser() {
  const raw = localStorage.getItem(USER_KEY)
  try {
    return raw ? JSON.parse(raw) : null
  } catch (e) {
    return null
  }
}

export function saveSession(loginData) {
  localStorage.setItem(TOKEN_KEY, loginData.token)
  localStorage.setItem(
    USER_KEY,
    JSON.stringify({
      username: loginData.username,
      role: loginData.role,
      realName: loginData.realName,
      patientId: loginData.patientId,
      doctorId: loginData.doctorId
    })
  )
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export const ROLE_TEXT = {
  PATIENT: '患者',
  DOCTOR: '医生',
  ADMIN: '管理员'
}
