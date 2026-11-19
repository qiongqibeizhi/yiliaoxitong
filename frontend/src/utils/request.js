import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearSession, getToken } from './auth'

// 后端地址：开发环境默认 8080，可用 .env 的 VITE_API_BASE 覆盖
const baseURL = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

const request = axios.create({
  baseURL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json;charset=UTF-8' }
})

// 请求拦截：自动携带登录令牌，前端业务代码不必关心鉴权头
request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截：统一解包 Result{code,message,data}，把异常转成可读提示
request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && body.code === 200) {
      return body.data
    }
    const message = (body && body.message) || '请求失败'
    ElMessage.error(message)
    if (body && body.code === 401) {
      clearSession()
      window.location.href = '/login'
    }
    return Promise.reject(new Error(message))
  },
  (error) => {
    const status = error.response && error.response.status
    const body = error.response && error.response.data
    let message = (body && body.message) || error.message || '网络异常'
    if (status === 401) {
      message = (body && body.message) || '未登录或登录已过期，请重新登录'
      clearSession()
      window.location.href = '/login'
    } else if (status === 403) {
      message = (body && body.message) || '当前角色无权访问该资源'
    } else if (status === 404) {
      message = (body && body.message) || '资源不存在'
    } else if (status >= 500) {
      message = '服务器开小差了，请稍后再试'
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
