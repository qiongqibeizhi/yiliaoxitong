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
        <h2>账号登录</h2>
        <p class="hint">请使用患者 / 医生 / 管理员账号登录</p>
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

        <div class="demo-box">
          <span>演示账号</span>
          <el-button link type="primary" @click="fill('patient1')">患者</el-button>
          <el-button link type="primary" @click="fill('doctor1')">医生</el-button>
          <el-button link type="primary" @click="fill('admin')">管理员</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { saveSession } from '../utils/auth'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function fill(username) {
  form.username = username
  form.password = '123456'
}

async function submit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const data = await login({ username: form.username, password: form.password })
      saveSession(data)
      ElMessage.success(`欢迎回来，${data.realName || data.username}`)
      router.push(route.query.redirect || '/departments')
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
.demo-box {
  margin-top: 22px;
  font-size: 13px;
  color: #8a97a8;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
