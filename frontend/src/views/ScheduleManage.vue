<template>
  <div class="page-card">
    <h2 class="page-title">排班管理</h2>
    <p class="page-desc">管理员维护医生排班：出诊日期、时段、号源总数与挂号费。</p>

    <el-form :inline="true" :model="query" class="filter-bar">
      <el-form-item label="医生">
        <el-select v-model="query.doctorId" placeholder="全部" clearable filterable style="width: 180px">
          <el-option v-for="d in doctors" :key="d.id" :label="`${d.name}（${d.doctorNo}）`" :value="d.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="出诊日期">
        <el-date-picker v-model="query.date" type="date" value-format="YYYY-MM-DD" clearable style="width: 150px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="search">查询</el-button>
        <el-button icon="Refresh" @click="reset">重置</el-button>
        <el-button type="success" icon="Plus" @click="openCreate">新增排班</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column label="医生" width="140" align="center">
        <template #default="{ row }">{{ doctorName(row.doctorId) }}</template>
      </el-table-column>
      <el-table-column prop="scheduleDate" label="出诊日期" width="130" align="center" />
      <el-table-column prop="timeSlot" label="时段" width="100" align="center" />
      <el-table-column label="号源（已挂/总）" width="150" align="center">
        <template #default="{ row }">{{ row.bookedSlots }} / {{ row.totalSlots }}</template>
      </el-table-column>
      <el-table-column prop="regFee" label="挂号费（元）" width="120" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">{{ row.status === 1 ? '可预约' : '停诊' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template #default="{ row }">
          <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pager" background layout="total, prev, pager, next"
      :total="total" :page-size="query.pageSize" :current-page="query.pageNum"
      @current-change="(p) => { query.pageNum = p; load() }"
    />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑排班' : '新增排班'" width="520px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="医生" prop="doctorId">
          <el-select v-model="form.doctorId" filterable style="width: 100%">
            <el-option v-for="d in doctors" :key="d.id" :label="`${d.name}（${d.doctorNo}）`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="出诊日期" prop="scheduleDate">
          <el-date-picker v-model="form.scheduleDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="时段" prop="timeSlot">
          <el-radio-group v-model="form.timeSlot">
            <el-radio value="上午">上午</el-radio>
            <el-radio value="下午">下午</el-radio>
            <el-radio value="晚间">晚间</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="号源总数" prop="totalSlots">
          <el-input-number v-model="form.totalSlots" :min="1" :max="200" />
        </el-form-item>
        <el-form-item label="挂号费（元）" prop="regFee">
          <el-input-number v-model="form.regFee" :min="0" :precision="2" :step="5" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="可预约" inactive-text="停诊" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageSchedules, createSchedule, updateSchedule, deleteSchedule } from '../api/schedule'
import { pageDoctors } from '../api/doctor'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const doctors = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, doctorId: null, date: '' })
const dialogVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formRef = ref()
const form = reactive({
  doctorId: null, scheduleDate: '', timeSlot: '上午', totalSlots: 20, regFee: 10, status: 1
})

const rules = {
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  scheduleDate: [{ required: true, message: '请选择出诊日期', trigger: 'change' }],
  timeSlot: [{ required: true, message: '请选择时段', trigger: 'change' }],
  totalSlots: [{ required: true, message: '请填写号源总数', trigger: 'change' }]
}

function doctorName(id) {
  const d = doctors.value.find((item) => item.id === id)
  return d ? d.name : id
}

async function load() {
  loading.value = true
  try {
    const page = await pageSchedules(query)
    rows.value = page.records || []
    total.value = page.total || 0
  } finally {
    loading.value = false
  }
}

async function loadDoctors() {
  const page = await pageDoctors({ pageNum: 1, pageSize: 100 })
  doctors.value = page.records || []
}

function search() { query.pageNum = 1; load() }
function reset() { query.doctorId = null; query.date = ''; search() }

function openCreate() {
  editingId.value = null
  Object.assign(form, { doctorId: null, scheduleDate: '', timeSlot: '上午', totalSlots: 20, regFee: 10, status: 1 })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    doctorId: row.doctorId, scheduleDate: row.scheduleDate, timeSlot: row.timeSlot,
    totalSlots: row.totalSlots, regFee: Number(row.regFee), status: row.status
  })
  dialogVisible.value = true
}

async function save() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const payload = {
        doctorId: form.doctorId, scheduleDate: form.scheduleDate, timeSlot: form.timeSlot,
        totalSlots: form.totalSlots, regFee: form.regFee, status: form.status
      }
      if (editingId.value) {
        await updateSchedule(editingId.value, payload)
        ElMessage.success('排班已更新')
      } else {
        await createSchedule(payload)
        ElMessage.success('排班已创建')
      }
      dialogVisible.value = false
      load()
    } finally {
      saving.value = false
    }
  })
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除 ${doctorName(row.doctorId)} ${row.scheduleDate} ${row.timeSlot} 的排班吗？`,
      '删除确认', { type: 'warning' }
    )
  } catch (e) {
    return
  }
  await deleteSchedule(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  await loadDoctors()
  await load()
})
</script>

<style scoped>
.filter-bar { margin-bottom: 4px; }
.pager { margin-top: 18px; justify-content: flex-end; }
</style>
