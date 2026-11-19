import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/auth'

const routes = [
  { path: '/', redirect: '/departments' },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/Login.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/departments',
    name: 'departments',
    component: () => import('../views/DepartmentList.vue'),
    meta: { title: '科室列表' }
  },
  {
    path: '/departments/:departmentId/doctors',
    name: 'doctors',
    component: () => import('../views/DoctorList.vue'),
    meta: { title: '医生列表' }
  },
  {
    path: '/doctors/:id',
    name: 'doctorDetail',
    component: () => import('../views/DoctorDetail.vue'),
    meta: { title: '医生详情' }
  },
  {
    path: '/booking',
    name: 'booking',
    component: () => import('../views/Booking.vue'),
    meta: { title: '挂号预约' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫：除登录页外都要求先登录，避免未带令牌裸调接口
router.beforeEach((to) => {
  document.title = `${to.meta.title || '医疗系统'} · 仁和医疗`
  if (to.meta.public) {
    return true
  }
  if (!getToken()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
