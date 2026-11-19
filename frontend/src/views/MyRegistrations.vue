<template>
  <div class="page-card">
    <h2 class="page-title">我的挂号</h2>
    <p class="page-desc">查看本人全部挂号记录；待就诊的记录可以退号，号源将自动释放。</p>

    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="regNo" label="挂号编号" width="180" align="center" />
      <el-table-column label="医生" width="110" align="center">
        <template #default="{ row }">{{ doctorName(row.doctorId) }}</template>
      </el-table-column>
      <el-table-column label="科室" width="110" align="center">
        <template #default="{ row }">{{ deptName(row.departmentId) }}</template>
      </el-table-column>
      <el-table-column prop="visitDate" label="就诊日期" width="120" align="center" />
      <el-table-column prop="timeSlot" label="时段" width="90" align="center" />
      <el-table-column prop="regFee" label="挂号费（元）" width="110" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" effect="plain">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="110" align="center">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="danger" link @click="cancel(row)">退号</el-button>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pager" background layout="total, prev, pager, next"
      :total="total" :page-size="query.pageSize" :current-page="query.pageNum"
      @current-change="(p) => { query.pageNum = p; load() }"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageRegistrations, cancelRegistration } from '../api/registration'
import { pageDoctors } from '../api/doctor'
import { pageDepartments } from '../api/department'
import { getUser } from '../utils/auth'

const STATUS = { 0: '待就诊', 1: '已完成', 2: '已取消' }
const STATUS_TAG = { 0: 'primary', 1: 'success', 2: 'info' }

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const doctors = ref([])
const departments = ref([])
const query = reactive({ pageNum: 1, pageSize: 10 })

const statusType = (s) => STATUS_TAG[s] ?? 'info'
const statusText = (s) => STATUS[s] ?? s

function doctorName(id) {
  const d = doctors.value.find((item) => item.id === id)
  return d ? d.name : id
}

function deptName(id) {
  const d = departments.value.find((item) => item.id === id)
  return d ? d.deptName : id
}

async function load() {
  loading.value = true
  try {
    // 后端按登录患者自动收窄为本人的挂号记录
    const page = await pageRegistrations(query)
    rows.value = page.records || []
    total.value = page.total || 0
  } finally {
    loading.value = false
  }
}

async function cancel(row) {
  try {
    await ElMessageBox.confirm(`确定取消挂号 ${row.regNo} 吗？号源将同步释放。`, '退号确认', { type: 'warning' })
  } catch (e) {
    return
  }
  await cancelRegistration(row.id)
  ElMessage.success('已退号')
  load()
}

onMounted(async () => {
  const [d, dep] = await Promise.all([
    pageDoctors({ pageNum: 1, pageSize: 100 }),
    pageDepartments({ pageNum: 1, pageSize: 50 })
  ])
  doctors.value = d.records || []
  departments.value = dep.records || []
  await load()
})
</script>

<style scoped>
.pager { margin-top: 18px; justify-content: flex-end; }
.muted { color: #9aa8ba; font-size: 12px; }
</style>
