<template>
  <div class="page-card">
    <el-page-header content="医生列表" @back="$router.back()" class="back" />
    <h2 class="page-title">{{ departmentName || '本科室' }} · 出诊医生</h2>
    <p class="page-desc">点击医生卡片查看简介与排班，并进入挂号预约。</p>

    <div v-loading="loading" class="doctor-grid">
      <el-card v-for="doc in doctors" :key="doc.id" shadow="hover" class="doctor-card" @click="detail(doc)">
        <div class="card-head">
          <el-avatar :size="52" class="avatar">{{ doc.name?.slice(0, 1) }}</el-avatar>
          <div>
            <div class="doc-name">{{ doc.name }}</div>
            <div class="doc-title">{{ doc.title || '医师' }}</div>
          </div>
          <el-tag v-if="doc.status === 1" type="success" effect="plain" class="status-tag">在岗</el-tag>
          <el-tag v-else type="info" effect="plain" class="status-tag">停诊</el-tag>
        </div>
        <div class="doc-specialty">擅长：{{ doc.specialty || '暂无填写' }}</div>
        <div class="doc-foot">
          <span>工号 {{ doc.doctorNo }}</span>
          <el-button type="primary" link>查看详情与排班 →</el-button>
        </div>
      </el-card>
    </div>

    <el-empty v-if="!loading && doctors.length === 0" description="该科室暂无医生" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { pageDoctors } from '../api/doctor'
import { getDepartment } from '../api/department'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const doctors = ref([])
const departmentName = ref('')

async function load() {
  loading.value = true
  try {
    const deptId = route.params.departmentId
    const [dept, page] = await Promise.all([
      getDepartment(deptId).catch(() => null),
      pageDoctors({ pageNum: 1, pageSize: 20, departmentId: deptId })
    ])
    departmentName.value = dept ? dept.deptName : ''
    doctors.value = page.records || []
  } finally {
    loading.value = false
  }
}

function detail(doc) {
  router.push(`/doctors/${doc.id}`)
}

onMounted(load)
</script>

<style scoped>
.back {
  margin-bottom: 16px;
}
.doctor-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}
.doctor-card {
  cursor: pointer;
  border-radius: 10px;
}
.card-head {
  display: flex;
  align-items: center;
  gap: 12px;
}
.avatar {
  background: #e8f2fd;
  color: #1d7fd6;
  font-weight: 600;
}
.doc-name {
  font-size: 17px;
  font-weight: 600;
}
.doc-title {
  font-size: 12px;
  color: #8a97a8;
  margin-top: 2px;
}
.status-tag {
  margin-left: auto;
}
.doc-specialty {
  margin: 14px 0 12px;
  font-size: 13px;
  color: #40536b;
  min-height: 38px;
}
.doc-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #9aa8ba;
  border-top: 1px dashed #edf1f6;
  padding-top: 10px;
}
</style>
