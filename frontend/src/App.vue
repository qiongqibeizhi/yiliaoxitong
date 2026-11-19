<template>
  <div v-if="isLogin">
    <router-view />
  </div>
  <el-container v-else class="layout">
    <el-header class="topbar">
      <div class="brand" @click="$router.push('/departments')">
        <el-icon class="brand-icon"><FirstAidKit /></el-icon>
        <div>
          <div class="brand-name">仁和医疗</div>
          <div class="brand-sub">在线挂号平台</div>
        </div>
      </div>
      <el-menu mode="horizontal" :ellipsis="false" class="nav" router default-active="/departments">
        <el-menu-item index="/departments">科室导航</el-menu-item>
      </el-menu>
      <div class="user-area">
        <el-tag v-if="user" type="success" effect="plain" round>{{ roleText }}</el-tag>
        <span v-if="user" class="username">{{ user.realName || user.username }}</span>
        <el-button v-if="user" link type="primary" @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>

    <el-main class="main">
      <router-view />
    </el-main>

    <el-footer class="footer">仁和医疗 · Java Web 课程实验项目 · 数据仅用于教学演示</el-footer>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { clearSession, getUser, ROLE_TEXT } from './utils/auth'
import { logout } from './api/auth'

const route = useRoute()
const router = useRouter()

const isLogin = computed(() => route.path === '/login')
const user = computed(() => getUser())
const roleText = computed(() => (user.value ? ROLE_TEXT[user.value.role] || user.value.role : ''))

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定退出当前账号吗？', '退出登录', {
      confirmButtonText: '退出',
      cancelButtonText: '留在本页',
      type: 'warning'
    })
  } catch (e) {
    return
  }
  try {
    await logout()
  } catch (e) {
    // 令牌可能已过期，忽略接口错误，本地会话照常清理
  }
  clearSession()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style>
* {
  box-sizing: border-box;
}
body {
  margin: 0;
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
  background: #f4f7fb;
  color: #1f2d3d;
}
#app {
  min-height: 100vh;
}
.layout {
  min-height: 100vh;
}
.topbar {
  display: flex;
  align-items: center;
  gap: 24px;
  background: #ffffff;
  border-bottom: 1px solid #e6ebf2;
  box-shadow: 0 2px 8px rgba(15, 58, 102, 0.06);
  padding: 0 28px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}
.brand-icon {
  font-size: 28px;
  color: #1d7fd6;
}
.brand-name {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
}
.brand-sub {
  font-size: 12px;
  color: #7b8aa0;
}
.nav {
  flex: 1;
  border-bottom: none;
}
.user-area {
  display: flex;
  align-items: center;
  gap: 10px;
}
.username {
  font-size: 14px;
  color: #40536b;
}
.main {
  width: 1080px;
  max-width: 100%;
  margin: 0 auto;
  padding: 24px 20px 40px;
}
.footer {
  text-align: center;
  color: #9aa8ba;
  font-size: 12px;
  line-height: 48px;
}
.page-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(15, 58, 102, 0.06);
  padding: 24px;
}
.page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 4px;
}
.page-desc {
  color: #7b8aa0;
  font-size: 13px;
  margin: 0 0 20px;
}
</style>
