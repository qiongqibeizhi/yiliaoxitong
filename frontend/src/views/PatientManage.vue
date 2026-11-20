<template>
  <div class="page-card">
    <h2 class="page-title">患者档案管理</h2>
    <p class="page-desc">医生可查看患者基本信息，管理员可新增、编辑、删除患者档案；患者隐私信息严格保密。</p>

    <el-form :inline="true" :model="query" class="filter-bar">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="姓名 / 档案号 / 身份证" clearable style="width: 220px" @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="性别">
        <el-select v-model="query.gender" placeholder="全部" clearable style="width: 110px">
          <el-option label="男" value="男" />
          <el-option label="女" value="女" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="search">查询</el-button>
        <el-button icon="Refresh" @click="reset">重置</el-button>
        <el-button v-if="isAdmin" type="success" icon="Plus" @click="openCreate">新增患者</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="patientNo" label="档案号" width="130" align="center" />
      <el-table-column prop="name" label="姓名" width="100" align="center" />
      <el-table-column prop="gender" label="性别" width="70" align="center" />
      <el-table-column prop="birthDate" label="出生日期" width="120" align="center" />
      <el-table-column prop="idCard" label="身份证号" min-width="180" />
      <el-table-column prop="phone" label="手机号" width="140" align="center" />
      <el-table-column prop="address" label="住址" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="150" align="center">
        <template #default="{ row }">
          <template v-if="isAdmin">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="remove(row)">删除</el-button>
          </template>
          <span v-else class="muted">只读</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pager" background layout="total, prev, pager, next, sizes"
      :total="total" :page-size="query.pageSize" :current-page="query.pageNum" :page-sizes="[5, 10, 20]"
      @current-change="(p) => { query.pageNum = p; load() }"
      @size-change="(s) => { query.pageSize = s; query.pageNum = 1; load() }"
    />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑患者' : '新增患者'" width="560px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期" prop="birthDate">
          <el-date-picker v-model="form.birthDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" maxlength="18" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" />
        </el-form-item>
        <el-form-item label="住址">
          <el-input v-model="form.address" type="textarea" :rows="2" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pagePatients, createPatient, updatePatient, deletePatient } from '../api/patient'
import { getUser } from '../utils/auth'

const isAdmin = computed(() => getUser()?.role === 'ADMIN')
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', gender: '' })
const dialogVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formRef = ref()
const form = reactive({ name: '', gender: '男', birthDate: '', idCard: '', phone: '', address: '' })

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[0-9Xx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

async function load() {
  loading.value = true
  try {
    const page = await pagePatients(query)
    rows.value = page.records || []
    total.value = page.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNum = 1
  load()
}

function reset() {
  query.keyword = ''
  query.gender = ''
  search()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { name: '', gender: '男', birthDate: '', idCard: '', phone: '', address: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name, gender: row.gender, birthDate: row.birthDate,
    idCard: row.idCard, phone: row.phone, address: row.address
  })
  dialogVisible.value = true
}

async function save() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (editingId.value) {
        await updatePatient(editingId.value, { ...form })
        ElMessage.success('患者信息已更新')
      } else {
        await createPatient({ ...form })
        ElMessage.success('患者已创建')
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
    await ElMessageBox.confirm(`确定删除患者“${row.name}”的档案吗？`, '删除确认', { type: 'warning' })
  } catch (e) {
    return
  }
  await deletePatient(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.filter-bar { margin-bottom: 4px; }
.pager { margin-top: 18px; justify-content: flex-end; }
.muted { color: #9aa8ba; font-size: 12px; }
</style>
