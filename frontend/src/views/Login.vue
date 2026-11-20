<template>
  <div class="login-page">
    <div class="login-visual">
      <div class="visual-inner">
        <el-icon class="visual-icon"><FirstAidKit /></el-icon>
        <h1>仁和医疗</h1>
        <p>让每一次就诊，从一次顺畅的预约开始。</p>
        <ul>
          <li>在线查看科室与医生排班</li>
          <li>实名患者一键挂号，号源实时扣减</li>
          <li>JWT 登录鉴权，隐私字段自动脱敏</li>
        </ul>
      </div>
    </div>

    <div class="login-panel">
      <div class="panel-inner">
        <h2>{{ portal.title }}</h2>
        <p class="hint">{{ portal.hint }}</p>
        <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @keyup.enter="submit">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" size="large" clearable />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
          <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="submit">
            登 录
          </el-button>
        </el-form>

        <div class="portal-links">
          <span>其他入口：</span>
          <el-button v-if="portalRole !== 'PATIENT'" link type="primary" @click="$router.push('/login/patient')">患者入口</el-button>
          <el-button v-if="portalRole !== 'DOCTOR'" link type="primary" @click="$router.push('/login/doctor')">医生工作站</el-button>
          <el-button v-if="portalRole !== 'ADMIN'" link type="primary" @click="$router.push('/login/admin')">管理后台</el-button>
        </div>
        <div v-if="portalRole === 'PATIENT'" class="portal-links">
          <span>还没有账号？</span>
          <el-button link type="success" @click="$router.push('/register')">患者注册</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { saveSession } from '../utils/auth'
import { homePathByRole } from '../router'

const PORTALS = {
  PATIENT: { title: '患者入口', hint: '挂号、购药、支付药费，请从患者入口登录' },
  DOCTOR: { title: '医生工作站', hint: '接诊、开具诊断记录，请使用医生账号登录' },
  ADMIN: { title: '管理后台', hint: '全院数据管理与运营，请使用管理员账号登录' }
}

const router = useRouter()
const route = useRoute()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

// 由登录路由决定入口身份：/login/patient /login/doctor /login/admin
const portalRole = computed(() => {
  const m = String(route.path).match(/^\/login\/(patient|doctor|admin)$/)
  return m ? m[1].toUpperCase() : 'PATIENT'
})
const portal = computed(() => PORTALS[portalRole.value])

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const data = await login({ username: form.username, password: form.password })
      if (data.role !== portalRole.value) {
        const target = PORTALS[data.role]
        ElMessage.warning(`当前入口为${portal.value.title}，该账号属于「${target ? target.title : data.role}」，请从对应入口登录`)
        return
      }
      saveSession(data)
      ElMessage.success(`欢迎回来，${data.realName || data.username}`)
      router.push(route.query.redirect || homePathByRole(data.role))
    } catch (e) {
      // 错误提示已由 Axios 拦截器统一弹出
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
}
.login-visual {
  flex: 1.1;
  background: linear-gradient(135deg, #0b4f8a 0%, #1d7fd6 55%, #35b3a3 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
}
.visual-inner {
  max-width: 460px;
}
.visual-icon {
  font-size: 56px;
  opacity: 0.9;
}
.visual-inner h1 {
  font-size: 34px;
  letter-spacing: 4px;
  margin: 16px 0 10px;
}
.visual-inner p {
  font-size: 15px;
  opacity: 0.85;
  margin-bottom: 28px;
}
.visual-inner ul {
  list-style: none;
  padding: 0;
  line-height: 2.1;
  font-size: 14px;
}
.visual-inner li::before {
  content: '·';
  margin-right: 8px;
  opacity: 0.85;
}
.login-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  padding: 40px;
}
.panel-inner {
  width: 360px;
}
.panel-inner h2 {
  font-size: 24px;
  margin: 0 0 6px;
}
.hint {
  color: #8a97a8;
  font-size: 13px;
  margin-bottom: 24px;
}
.login-btn {
  width: 100%;
  margin-top: 6px;
  background: #1d7fd6;
  border-color: #1d7fd6;
}
.portal-links {
  margin-top: 16px;
  font-size: 13px;
  color: #8a97a8;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
