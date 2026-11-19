<template>
  <div class="page-card">
    <h2 class="page-title">医生管理</h2>
    <p class="page-desc">管理员维护医生档案，支持按姓名、科室、职称筛选。</p>

    <el-form :inline="true" :model="query" class="filter-bar">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="姓名 / 工号" clearable style="width: 180px" @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="科室">
        <el-select v-model="query.departmentId" placeholder="全部" clearable style="width: 150px">
          <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="search">查询</el-button>
        <el-button icon="Refresh" @click="reset">重置</el-button>
        <el-button type="success" icon="Plus" @click="openCreate">新增医生</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="doctorNo" label="工号" width="110" align="center" />
      <el-table-column prop="name" label="姓名" width="100" align="center" />
      <el-table-column prop="gender" label="性别" width="70" align="center" />
      <el-table-column prop="title" label="职称" width="110" align="center" />
      <el-table-column label="科室" width="110" align="center">
        <template #default="{ row }">{{ deptName(row.departmentId) }}</template>
      </el-table-column>
      <el-table-column prop="specialty" label="擅长" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">{{ row.status === 1 ? '在岗' : '停诊' }}</el-tag>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑医生' : '新增医生'" width="580px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="工号" prop="doctorNo">
          <el-input v-model="form.doctorNo" maxlength="20" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="职称">
          <el-select v-model="form.title" style="width: 100%">
            <el-option v-for="t in titles" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="科室" prop="departmentId">
          <el-select v-model="form.departmentId" style="width: 100%">
            <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" />
        </el-form-item>
        <el-form-item label="擅长">
          <el-input v-model="form.specialty" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.introduction" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="在岗" inactive-text="停诊" />
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
import { pageDoctors, createDoctor, updateDoctor, deleteDoctor } from '../api/doctor'
import { pageDepartments } from '../api/department'

const titles = ['主任医师', '副主任医师', '主治医师', '住院医师']
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const departments = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', departmentId: null })
const dialogVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formRef = ref()
const form = reactive({
  doctorNo: '', name: '', gender: '男', title: '主治医师', departmentId: null,
  phone: '', specialty: '', introduction: '', status: 1
})

const rules = {
  doctorNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  departmentId: [{ required: true, message: '请选择科室', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

function deptName(id) {
  const d = departments.value.find((item) => item.id === id)
  return d ? d.deptName : id
}

async function load() {
  loading.value = true
  try {
    const page = await pageDoctors(query)
    rows.value = page.records || []
    total.value = page.total || 0
  } finally {
    loading.value = false
  }
}

async function loadDepartments() {
  const page = await pageDepartments({ pageNum: 1, pageSize: 50 })
  departments.value = page.records || []
}

function search() { query.pageNum = 1; load() }
function reset() { query.keyword = ''; query.departmentId = null; search() }

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    doctorNo: '', name: '', gender: '男', title: '主治医师', departmentId: null,
    phone: '', specialty: '', introduction: '', status: 1
  })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    doctorNo: row.doctorNo, name: row.name, gender: row.gender, title: row.title,
    departmentId: row.departmentId, phone: row.phone, specialty: row.specialty,
    introduction: row.introduction, status: row.status
  })
  dialogVisible.value = true
}

async function save() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (editingId.value) {
        await updateDoctor(editingId.value, { ...form })
        ElMessage.success('医生信息已更新')
      } else {
        await createDoctor({ ...form })
        ElMessage.success('医生已创建')
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
    await ElMessageBox.confirm(`确定删除医生“${row.name}”吗？`, '删除确认', { type: 'warning' })
  } catch (e) {
    return
  }
  await deleteDoctor(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  await loadDepartments()
  await load()
})
</script>

<style scoped>
.filter-bar { margin-bottom: 4px; }
.pager { margin-top: 18px; justify-content: flex-end; }
</style>
