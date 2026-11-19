<template>
  <div>
    <div class="page-card doctor-panel">
      <el-page-header content="医生详情" @back="$router.back()" class="back" />
      <div class="doctor-head">
        <el-avatar :size="72" class="avatar">{{ doctor.name?.slice(0, 1) }}</el-avatar>
        <div class="doctor-meta">
          <div class="name-line">
            <span class="doc-name">{{ doctor.name }}</span>
            <el-tag type="primary" effect="plain">{{ doctor.title || '医师' }}</el-tag>
            <el-tag v-if="doctor.status === 1" type="success" effect="plain">在岗</el-tag>
          </div>
          <div class="muted">工号 {{ doctor.doctorNo }} · {{ departmentName }}</div>
          <div class="specialty">擅长：{{ doctor.specialty || '暂无填写' }}</div>
        </div>
      </div>
      <el-divider />
      <el-descriptions :column="2" border>
        <el-descriptions-item label="性别">{{ doctor.gender }}</el-descriptions-item>
        <el-descriptions-item label="所属科室">{{ departmentName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ doctor.phone || '—' }}</el-descriptions-item>
        <el-descriptions-item label="出诊状态">{{ doctor.status === 1 ? '正常出诊' : '暂停出诊' }}</el-descriptions-item>
        <el-descriptions-item label="医生简介" :span="2">
          {{ doctor.introduction || '暂无简介' }}
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="page-card schedule-panel">
      <h3 class="section-title">近期排班</h3>
      <p class="page-desc">剩余号源由后端根据已挂数量实时计算，挂号成功后立即扣减。</p>
      <el-table :data="schedules" v-loading="loading" stripe>
        <el-table-column prop="scheduleDate" label="出诊日期" width="130" align="center" />
        <el-table-column prop="timeSlot" label="时段" width="120" align="center" />
        <el-table-column label="号源" width="180" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="percent(row)"
              :stroke-width="10"
              :color="remain(row) > 0 ? '#1d7fd6' : '#c0c4cc'"
            />
            <div class="slots-text">剩余 {{ remain(row) }} / 共 {{ row.totalSlots }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="regFee" label="挂号费（元）" width="130" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">
              {{ row.status === 1 ? '可预约' : '已停诊' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              :disabled="row.status !== 1 || remain(row) <= 0"
              @click="book(row)"
            >
              预约挂号
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && schedules.length === 0" description="暂无排班" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getDoctor } from '../api/doctor'
import { pageSchedules } from '../api/schedule'
import { getDepartment } from '../api/department'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const doctor = ref({})
const departmentName = ref('')
const schedules = ref([])

function remain(row) {
  return Math.max((row.totalSlots || 0) - (row.bookedSlots || 0), 0)
}

function percent(row) {
  if (!row.totalSlots) return 100
  return Math.round(((row.bookedSlots || 0) / row.totalSlots) * 100)
}

async function load() {
  loading.value = true
  try {
    const doc = await getDoctor(route.params.id)
    doctor.value = doc
    const [dept, page] = await Promise.all([
      getDepartment(doc.departmentId).catch(() => null),
      pageSchedules({ pageNum: 1, pageSize: 50, doctorId: doc.id })
    ])
    departmentName.value = dept ? dept.deptName : ''
    schedules.value = page.records || []
  } finally {
    loading.value = false
  }
}

function book(row) {
  router.push({
    path: '/booking',
    query: { scheduleId: row.id, doctorId: doctor.value.id }
  })
}

onMounted(load)
</script>

<style scoped>
.doctor-panel {
  margin-bottom: 20px;
}
.back {
  margin-bottom: 18px;
}
.doctor-head {
  display: flex;
  gap: 18px;
  align-items: center;
}
.avatar {
  background: #e8f2fd;
  color: #1d7fd6;
  font-size: 26px;
  font-weight: 600;
}
.name-line {
  display: flex;
  align-items: center;
  gap: 10px;
}
.doc-name {
  font-size: 24px;
  font-weight: 600;
}
.muted {
  color: #8a97a8;
  font-size: 13px;
  margin: 6px 0;
}
.specialty {
  font-size: 13px;
  color: #40536b;
}
.section-title {
  margin: 0 0 4px;
  font-size: 17px;
}
.slots-text {
  font-size: 12px;
  color: #8a97a8;
  margin-top: 4px;
}
</style>
