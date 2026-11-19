<template>
  <div class="page-card">
    <h2 class="page-title">挂号记录</h2>
    <p class="page-desc">医生与管理员可查询全院挂号记录，患者仅可查看本人记录；管理员可执行退号。</p>

    <el-form :inline="true" :model="query" class="filter-bar">
      <el-form-item label="就诊日期">
        <el-date-picker v-model="query.visitDate" type="date" value-format="YYYY-MM-DD" clearable style="width: 150px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
          <el-option label="待就诊" :value="0" />
          <el-option label="已完成" :value="1" />
          <el-option label="已取消" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="search">查询</el-button>
        <el-button icon="Refresh" @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="regNo" label="挂号编号" width="170" align="center" />
      <el-table-column prop="patientId" label="患者编号" width="100" align="center" />
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
      <el-table-column label="操作" width="150" align="center">
        <template #default="{ row }">
          <el-button
            v-if="canComplete(row)"
            type="success" link @click="complete(row)"
          >完成就诊</el-button>
          <el-button
            v-if="canCancel(row)"
            type="danger" link @click="cancel(row)"
          >退号</el-button>
          <span v-if="!canComplete(row) && !canCancel(row)" class="muted">—</span>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageRegistrations, cancelRegistration, completeRegistration } from '../api/registration'
import { pageDoctors } from '../api/doctor'
import { pageDepartments } from '../api/department'
import { getUser } from '../utils/auth'

const STATUS = { 0: '待就诊', 1: '已完成', 2: '已取消' }
const STATUS_TAG = { 0: 'primary', 1: 'success', 2: 'info' }

const user = getUser()
const isAdmin = user?.role === 'ADMIN'
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const doctors = ref([])
const departments = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, visitDate: '', status: null })

const STATUS_LIST = [0, 1, 2]
const statusType = (s) => STATUS_TAG[s] ?? 'info'
const statusText = (s) => STATUS[s] ?? s
const canCancel = computed(() => (row) => isAdmin && row.status === 0)
const canComplete = computed(() => (row) => (user?.role === 'DOCTOR' || isAdmin) && row.status === 0)

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
    const page = await pageRegistrations(query)
    rows.value = page.records || []
    total.value = page.total || 0
  } finally {
    loading.value = false
  }
}

function search() { query.pageNum = 1; load() }
function reset() { query.visitDate = ''; query.status = null; search() }

async function complete(row) {
  try {
    await ElMessageBox.confirm(`确认挂号 ${row.regNo} 已完成就诊吗？`, '完成就诊', { type: 'info' })
  } catch (e) {
    return
  }
  await completeRegistration(row.id)
  ElMessage.success('已完成就诊')
  load()
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
  // 患者角色后端会拒绝挂号记录查询，直接提示引导
  if (user?.role === 'PATIENT') {
    ElMessage.warning('挂号记录查询仅对医生与管理员开放')
    return
  }
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
.filter-bar { margin-bottom: 4px; }
.pager { margin-top: 18px; justify-content: flex-end; }
.muted { color: #9aa8ba; font-size: 12px; }
</style>
