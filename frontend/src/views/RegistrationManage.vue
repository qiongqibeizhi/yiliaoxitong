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
      <el-table-column label="操作" width="170" align="center">
        <template #default="{ row }">
          <el-button
            v-if="isDoctor && row.status === 0"
            type="primary" link @click="openVisit(row)"
          >就诊</el-button>
          <el-button
            v-if="isAdmin && row.status === 0"
            type="success" link @click="complete(row)"
          >完成就诊</el-button>
          <el-button
            v-if="canCancel(row)"
            type="danger" link @click="cancel(row)"
          >退号</el-button>
          <span v-if="row.status !== 0" class="muted">—</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pager" background layout="total, prev, pager, next"
      :total="total" :page-size="query.pageSize" :current-page="query.pageNum"
      @current-change="(p) => { query.pageNum = p; load() }"
    />

    <!-- 就诊弹窗：诊断必填，开药可选 -->
    <el-dialog v-model="visitVisible" title="接诊 · 诊断记录" width="680px" destroy-on-close>
      <el-descriptions :column="2" border size="small" class="visit-desc">
        <el-descriptions-item label="挂号编号">{{ visitRow?.regNo }}</el-descriptions-item>
        <el-descriptions-item label="患者编号">{{ visitRow?.patientId }}</el-descriptions-item>
        <el-descriptions-item label="就诊日期">{{ visitRow?.visitDate }} {{ visitRow?.timeSlot }}</el-descriptions-item>
        <el-descriptions-item label="主诉">{{ visitRow?.symptom || '—' }}</el-descriptions-item>
      </el-descriptions>
      <el-form ref="visitFormRef" :model="visitForm" label-width="90px" style="margin-top: 14px">
        <el-form-item label="诊断" prop="diagnosis" :rules="[{ required: true, message: '请填写诊断结果', trigger: 'blur' }]">
          <el-input v-model="visitForm.diagnosis" placeholder="如：急性上呼吸道感染" />
        </el-form-item>
      </el-form>
      <div class="items-head">
        <span class="card-title">开具药品（可选，不开药可直接完成就诊）</span>
        <el-button type="primary" size="small" icon="Plus" @click="addVisitItem">添加药品</el-button>
      </div>
      <el-table :data="visitForm.items" size="small" border>
        <el-table-column label="药品" min-width="180">
          <template #default="{ row }">
            <el-select v-model="row.drugId" filterable placeholder="搜索药品" style="width: 100%">
              <el-option
                v-for="d in drugs" :key="d.id"
                :label="`${d.drugName}（${d.spec}）库存 ${d.stock}`"
                :value="d.id"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="110">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" :max="999" size="small" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="用法" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.dosage" placeholder="如：每日3次，每次1片" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ $index }">
            <el-button type="danger" link @click="visitForm.items.splice($index, 1)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="visitVisible = false">取消</el-button>
        <el-button :loading="saving" @click="submitVisit(false)">保存诊断记录</el-button>
        <el-button type="primary" :loading="saving" @click="submitVisit(true)">保存并完成就诊</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageRegistrations, cancelRegistration, completeRegistration } from '../api/registration'
import { pageDoctors } from '../api/doctor'
import { pageDrugs } from '../api/drug'
import { createPrescription } from '../api/prescription'
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
const isDoctor = user?.role === 'DOCTOR'
const visitVisible = ref(false)
const visitRow = ref(null)
const visitFormRef = ref()
const saving = ref(false)
const drugs = ref([])
const visitForm = reactive({ diagnosis: '', items: [] })
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

function openVisit(row) {
  visitRow.value = row
  visitForm.diagnosis = ''
  visitForm.items = []
  visitVisible.value = true
}

function addVisitItem() {
  visitForm.items.push({ drugId: null, quantity: 1, dosage: '' })
}

async function submitVisit(andComplete) {
  await visitFormRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const items = visitForm.items
        .filter((it) => it.drugId)
        .map((it) => ({ drugId: it.drugId, quantity: it.quantity, dosage: it.dosage }))
      await createPrescription({
        registrationId: visitRow.value.id,
        diagnosis: visitForm.diagnosis,
        items
      })
      if (andComplete) {
        await completeRegistration(visitRow.value.id)
        ElMessage.success('诊断记录已保存，就诊已完成')
      } else {
        ElMessage.success('诊断记录已保存，可稍后完成就诊')
      }
      visitVisible.value = false
      load()
    } finally {
      saving.value = false
    }
  })
}

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
  const [d, dep, dr] = await Promise.all([
    pageDoctors({ pageNum: 1, pageSize: 100 }),
    pageDepartments({ pageNum: 1, pageSize: 50 }),
    isDoctor ? pageDrugs({ pageNum: 1, pageSize: 100 }) : Promise.resolve({ records: [] })
  ])
  doctors.value = d.records || []
  departments.value = dep.records || []
  drugs.value = dr.records || []
  await load()
})
</script>

<style scoped>
.filter-bar { margin-bottom: 4px; }
.pager { margin-top: 18px; justify-content: flex-end; }
.muted { color: #9aa8ba; font-size: 12px; }
</style>
