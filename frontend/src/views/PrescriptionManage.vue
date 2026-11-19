<template>
  <div class="page-card">
    <h2 class="page-title">处方管理</h2>
    <p class="page-desc">医生与管理员可查询处方明细、按挂号记录开具新处方。</p>

    <el-form :inline="true" :model="query" class="filter-bar">
      <el-form-item label="挂号编号">
        <el-input v-model="query.registrationId" placeholder="挂号记录 ID" clearable style="width: 160px" @keyup.enter="search" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="search">查询</el-button>
        <el-button icon="Refresh" @click="reset">重置</el-button>
        <el-button v-if="!isPatient" type="success" icon="Plus" @click="openCreate">开具新处方</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="prescNo" label="处方编号" width="200" align="center" />
      <el-table-column prop="registrationId" label="挂号记录 ID" width="110" align="center" />
      <el-table-column prop="patientId" label="患者编号" width="100" align="center" />
      <el-table-column label="医生" width="110" align="center">
        <template #default="{ row }">{{ doctorName(row.doctorId) }}</template>
      </el-table-column>
      <el-table-column prop="diagnosis" label="诊断" min-width="160" show-overflow-tooltip />
      <el-table-column prop="prescTime" label="开方时间" width="170" align="center" />
      <el-table-column label="操作" width="110" align="center">
        <template #default="{ row }">
          <el-button type="primary" link @click="showDetail(row)">明细</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pager" background layout="total, prev, pager, next"
      :total="total" :page-size="query.pageSize" :current-page="query.pageNum"
      @current-change="(p) => { query.pageNum = p; load() }"
    />

    <!-- 处方明细弹窗 -->
    <el-dialog v-model="detailVisible" title="处方明细" width="640px">
      <el-descriptions :column="2" border size="small" class="detail-desc">
        <el-descriptions-item label="处方编号">{{ detail.prescNo }}</el-descriptions-item>
        <el-descriptions-item label="开方时间">{{ detail.prescTime }}</el-descriptions-item>
        <el-descriptions-item label="诊断" :span="2">{{ detail.diagnosis }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="items" size="small" border class="detail-table">
        <el-table-column prop="drugName" label="药品" min-width="140" />
        <el-table-column prop="spec" label="规格" width="120" align="center" />
        <el-table-column prop="quantity" label="数量" width="70" align="center" />
        <el-table-column prop="unit" label="单位" width="70" align="center" />
        <el-table-column prop="dosage" label="用法" min-width="120" />
        <el-table-column prop="price" label="单价（元）" width="100" align="center" />
      </el-table>
    </el-dialog>

    <!-- 开方弹窗 -->
    <el-dialog v-model="createVisible" title="开具新处方" width="680px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="挂号记录 ID" prop="registrationId">
          <el-input-number v-model="form.registrationId" :min="1" style="width: 220px" />
        </el-form-item>
        <el-form-item label="诊断" prop="diagnosis">
          <el-input v-model="form.diagnosis" placeholder="如：急性上呼吸道感染" />
        </el-form-item>
      </el-form>

      <div class="items-head">
        <span class="card-title">处方药品</span>
        <el-button type="primary" size="small" icon="Plus" @click="addItem">添加药品</el-button>
      </div>
      <el-table :data="form.items" size="small" border>
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
        <el-table-column label="数量" width="120">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" :max="999" size="small" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="用法" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.dosage" placeholder="如：每日3次，每次1片" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="70" align="center">
          <template #default="{ $index }">
            <el-button type="danger" link size="small" @click="form.items.splice($index, 1)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">提交处方</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { pagePrescriptions, getPrescription, getPrescriptionItems, createPrescription } from '../api/prescription'
import { pageDoctors } from '../api/doctor'
import { pageDrugs } from '../api/drug'
import { getUser } from '../utils/auth'

const user = getUser()
const isPatient = user?.role === 'PATIENT'
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const doctors = ref([])
const drugs = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, registrationId: '' })
const detailVisible = ref(false)
const detail = ref({})
const items = ref([])
const createVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({ registrationId: null, diagnosis: '', items: [] })

const rules = {
  registrationId: [{ required: true, message: '请填写挂号记录 ID', trigger: 'change' }],
  diagnosis: [{ required: true, message: '请填写诊断', trigger: 'blur' }]
}

function doctorName(id) {
  const d = doctors.value.find((item) => item.id === id)
  return d ? d.name : id
}

async function load() {
  loading.value = true
  try {
    const params = { ...query }
    if (!params.registrationId) delete params.registrationId
    const page = await pagePrescriptions(params)
    rows.value = page.records || []
    total.value = page.total || 0
  } finally {
    loading.value = false
  }
}

function search() { query.pageNum = 1; load() }
function reset() { query.registrationId = ''; search() }

async function showDetail(row) {
  detail.value = await getPrescription(row.id)
  items.value = await getPrescriptionItems(row.id)
  detailVisible.value = true
}

function openCreate() {
  form.registrationId = null
  form.diagnosis = ''
  form.items = [{ drugId: null, quantity: 1, dosage: '' }]
  createVisible.value = true
}

function addItem() {
  form.items.push({ drugId: null, quantity: 1, dosage: '' })
}

async function save() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    const validItems = form.items.filter((item) => item.drugId)
    if (validItems.length === 0) {
      ElMessage.warning('请至少选择一种药品')
      return
    }
    saving.value = true
    try {
      const result = await createPrescription({
        registrationId: form.registrationId,
        diagnosis: form.diagnosis,
        items: validItems
      })
      ElMessage.success(`处方 ${result.prescNo} 已开具`)
      createVisible.value = false
      load()
    } finally {
      saving.value = false
    }
  })
}

onMounted(async () => {
  if (user?.role === 'PATIENT') {
    ElMessage.warning('处方管理仅对医生与管理员开放')
    return
  }
  const [d, dr] = await Promise.all([
    pageDoctors({ pageNum: 1, pageSize: 100 }),
    pageDrugs({ pageNum: 1, pageSize: 100 })
  ])
  doctors.value = d.records || []
  drugs.value = dr.records || []
  await load()
})
</script>

<style scoped>
.filter-bar { margin-bottom: 4px; }
.pager { margin-top: 18px; justify-content: flex-end; }
.items-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 6px 0 10px;
}
.card-title { font-weight: 600; font-size: 14px; }
.detail-desc { margin-bottom: 12px; }
.detail-table { margin-bottom: 4px; }
</style>
