<template>
  <div class="portal">
    <!-- 顶部导航 -->
    <header class="portal-header">
      <div class="header-inner">
        <div class="brand">
          <el-icon class="brand-icon"><FirstAidKit /></el-icon>
          <div>
            <div class="brand-name">仁和医疗</div>
            <div class="brand-sub">在线挂号平台</div>
          </div>
        </div>
        <nav class="top-links">
          <a href="#services">服务介绍</a>
          <a href="#portals">登录入口</a>
          <el-button v-if="loggedIn" link type="primary" @click="$router.push(homePath)">进入系统</el-button>
        </nav>
      </div>
    </header>

    <!-- 首屏 -->
    <section class="hero">
      <h1>专业 · 便捷 · 可信赖的在线医疗服务</h1>
      <p>科室导航 / 医生排班 / 在线挂号 / 余额支付 / 诊断记录，一站式就医体验</p>
      <el-button type="primary" size="large" round @click="scrollTo('portals')">
        选择身份登录
      </el-button>
    </section>

    <!-- 服务介绍 -->
    <section id="services" class="services">
      <h2>服务介绍</h2>
      <div class="service-grid">
        <div class="service-card" v-for="s in services" :key="s.title">
          <el-icon class="service-icon"><component :is="s.icon" /></el-icon>
          <h3>{{ s.title }}</h3>
          <p>{{ s.desc }}</p>
        </div>
      </div>
    </section>

    <!-- 三身份入口 -->
    <section id="portals" class="portals">
      <h2>选择身份登录</h2>
      <p class="portal-tip">不同身份拥有独立工作台，请按您的角色选择对应入口</p>
      <div class="portal-grid">
        <div class="portal-card patient" @click="$router.push('/login/patient')">
          <el-icon class="pc-icon"><User /></el-icon>
          <h3>患者入口</h3>
          <p>在线挂号 · 购药商城 · 钱包充值 · 查看诊断记录</p>
          <span class="pc-link">进入患者登录 →</span>
        </div>
        <div class="portal-card doctor" @click="$router.push('/login/doctor')">
          <el-icon class="pc-icon"><UserFilled /></el-icon>
          <h3>医生工作站</h3>
          <p>接诊患者 · 开具诊断记录 · 管理本人患者</p>
          <span class="pc-link">进入医生登录 →</span>
        </div>
        <div class="portal-card admin" @click="$router.push('/login/admin')">
          <el-icon class="pc-icon"><Setting /></el-icon>
          <h3>管理后台</h3>
          <p>科室排班 · 医生档案 · 药品库存 · 全院数据</p>
          <span class="pc-link">进入管理员登录 →</span>
        </div>
      </div>
    </section>

    <footer class="portal-footer">仁和医疗 · Java Web 课程实验项目 · 数据仅用于教学演示</footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { getToken, getUser } from '../utils/auth'

const route = useRoute()
const loggedIn = computed(() => Boolean(getToken() && getUser()))
const homePath = computed(() => {
  const user = getUser()
  return user ? (user.role === 'PATIENT' ? '/departments' : '/registrations') : '/login/patient'
})

function scrollTo(id) {
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' })
}

const services = [
  { icon: 'Calendar', title: '在线挂号', desc: '按科室、医生、时段选号，每时段 10 个号源实时扣减，满员自动置灰' },
  { icon: 'Wallet', title: '余额支付', desc: '钱包自定义金额充值，购药与药费凭余额支付，支持微信 / 支付宝 / 银行卡渠道' },
  { icon: 'Document', title: '诊断记录', desc: '医生接诊后开具诊断记录，开药可选，患者在线查看并支付药费' },
  { icon: 'Shop', title: '购药商城', desc: '在售药品自助下单，库存实时扣减，订单状态全程可查' }
]
</script>

<style scoped>
.portal { min-height: 100vh; background: #f5f8fc; }
.portal-header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(15, 40, 80, 0.08);
  position: sticky; top: 0; z-index: 10;
}
.header-inner {
  max-width: 1080px; margin: 0 auto; padding: 14px 24px;
  display: flex; align-items: center; justify-content: space-between;
}
.brand { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.brand-icon { font-size: 30px; color: #1d7fd6; }
.brand-name { font-size: 18px; font-weight: 700; color: #16324f; }
.brand-sub { font-size: 12px; color: #8a97a8; }
.top-links { display: flex; align-items: center; gap: 18px; }
.top-links a { color: #40536b; text-decoration: none; font-size: 14px; }
.top-links a:hover { color: #1d7fd6; }

.hero {
  background: linear-gradient(135deg, #0b4f8a 0%, #1d7fd6 55%, #35b3a3 100%);
  color: #fff; text-align: center; padding: 90px 24px;
}
.hero h1 { font-size: 36px; letter-spacing: 2px; margin: 0 0 14px; }
.hero p { font-size: 15px; opacity: 0.9; margin-bottom: 30px; }

.services { max-width: 1080px; margin: 0 auto; padding: 60px 24px 20px; }
.services h2, .portals h2 { text-align: center; color: #16324f; font-size: 24px; margin-bottom: 8px; }
.service-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 18px; margin-top: 26px;
}
.service-card {
  background: #fff; border-radius: 12px; padding: 26px 20px; text-align: center;
  box-shadow: 0 2px 10px rgba(15, 40, 80, 0.06);
}
.service-icon { font-size: 34px; color: #1d7fd6; }
.service-card h3 { font-size: 16px; color: #16324f; margin: 12px 0 8px; }
.service-card p { font-size: 13px; color: #67788c; line-height: 1.7; margin: 0; }

.portals { max-width: 1080px; margin: 0 auto; padding: 50px 24px 70px; }
.portal-tip { text-align: center; color: #8a97a8; font-size: 14px; }
.portal-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 22px; margin-top: 30px; }
.portal-card {
  background: #fff; border-radius: 14px; padding: 34px 26px; cursor: pointer;
  box-shadow: 0 2px 10px rgba(15, 40, 80, 0.06);
  border-top: 4px solid #1d7fd6;
  transition: transform 0.2s, box-shadow 0.2s;
  text-align: center;
}
.portal-card:hover { transform: translateY(-4px); box-shadow: 0 8px 22px rgba(15, 40, 80, 0.12); }
.portal-card.doctor { border-top-color: #27b6a6; }
.portal-card.admin { border-top-color: #e6653a; }
.pc-icon { font-size: 40px; color: #1d7fd6; }
.portal-card.doctor .pc-icon { color: #27b6a6; }
.portal-card.admin .pc-icon { color: #e6653a; }
.portal-card h3 { font-size: 19px; color: #16324f; margin: 14px 0 8px; }
.portal-card p { font-size: 13px; color: #67788c; line-height: 1.7; min-height: 44px; }
.pc-link { color: #1d7fd6; font-size: 13px; font-weight: 600; }

.portal-footer { text-align: center; color: #9aa8ba; font-size: 13px; padding: 26px; background: #fff; }
</style>
