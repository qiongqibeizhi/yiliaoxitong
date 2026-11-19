import { createRouter, createWebHistory } from 'vue-router'
import { getToken, getUser } from '../utils/auth'

const routes = [
  { path: '/', redirect: '/departments' },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/Login.vue'),
    meta: { public: true, title: '登录' }
  },
  // ---- 患者端 ----
  {
    path: '/departments',
    name: 'departments',
    component: () => import('../views/DepartmentList.vue'),
    meta: { title: '科室导航' }
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
  },
  // ---- 医生 / 管理员端 ----
  {
    path: '/registrations',
    name: 'registrationManage',
    component: () => import('../views/RegistrationManage.vue'),
    meta: { title: '挂号记录', roles: ['DOCTOR', 'ADMIN'] }
  },
  {
    path: '/prescriptions',
    name: 'prescriptionManage',
    component: () => import('../views/PrescriptionManage.vue'),
    meta: { title: '处方管理', roles: ['DOCTOR', 'ADMIN'] }
  },
  {
    path: '/patients',
    name: 'patientManage',
    component: () => import('../views/PatientManage.vue'),
    meta: { title: '患者档案', roles: ['DOCTOR', 'ADMIN'] }
  },
  // ---- 管理员端 ----
  {
    path: '/manage/departments',
    name: 'departmentManage',
    component: () => import('../views/DepartmentManage.vue'),
    meta: { title: '科室管理', roles: ['ADMIN'] }
  },
  {
    path: '/manage/doctors',
    name: 'doctorManage',
    component: () => import('../views/DoctorManage.vue'),
    meta: { title: '医生管理', roles: ['ADMIN'] }
  },
  {
    path: '/manage/schedules',
    name: 'scheduleManage',
    component: () => import('../views/ScheduleManage.vue'),
    meta: { title: '排班管理', roles: ['ADMIN'] }
  },
  // ---- 全角色 ----
  {
    path: '/drugs',
    name: 'drugList',
    component: () => import('../views/DrugList.vue'),
    meta: { title: '药品库' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫：登录校验 + 角色校验
router.beforeEach((to) => {
  document.title = `${to.meta.title || '医疗系统'} · 仁和医疗`
  if (to.meta.public) {
    return true
  }
  const user = getUser()
  if (!getToken() || !user) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.roles && !to.meta.roles.includes(user.role)) {
    // 角色不匹配时回到各自首页：患者去科室导航，其他去挂号记录
    return user.role === 'PATIENT' ? '/departments' : '/registrations'
  }
  return true
})

// 登录后的默认首页按角色区分
export function homePathByRole(role) {
  return role === 'PATIENT' ? '/departments' : '/registrations'
}

export default router
