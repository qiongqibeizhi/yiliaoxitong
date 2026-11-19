import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 前端独立端口 5173，后端接口 8080：这里不配代理，让浏览器真实发起跨域请求，
// 由后端 CorsRegistry 放行，正好验证实验五要求的 CORS 配置。
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    host: '127.0.0.1'
  }
})
