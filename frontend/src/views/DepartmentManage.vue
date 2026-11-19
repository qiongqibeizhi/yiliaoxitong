<template>
  <div class="page-card">
    <h2 class="page-title">科室管理</h2>
    <p class="page-desc">管理员维护科室编码、名称、位置与状态。</p>

    <el-form :inline="true" :model="query" class="filter-bar">
      <el-form-item label="科室名称">
        <el-input v-model="query.name" placeholder="如：内科" clearable style="width: 200px" @keyup.enter="search" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="search">查询</el-button>
        <el-button icon="Refresh" @click="reset">重置</el-button>
        <el-button type="success" icon="Plus" @click="openCreate">新增科室</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="deptCode" label="编码" width="110" align="center" />
      <el-table-column prop="deptName" label="名称" width="140" />
      <el-table-column prop="location" label="位置" min-width="150" />
      <el-table-column prop="phone" label="电话" width="140" align="center" />
      <el-table-column prop="description" label="简介" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑科室' : '新增科室'" width="520px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="科室编码" prop="deptCode">
          <el-input v-model="form.deptCode" maxlength="20" />
        </el-form-item>
        <el-form-item label="科室名称" prop="deptName">
          <el-input v-model="form.deptName" maxlength="50" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
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
import { pageDepartments, createDepartment, updateDepartment, deleteDepartment } from '../api/department'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, name: '' })
const dialogVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formRef = ref()
const form = reactive({ deptCode: '', deptName: '', location: '', phone: '', description: '', status: 1 })

const rules = {
  deptCode: [{ required: true, message: '请输入科室编码', trigger: 'blur' }],
  deptName: [{ required: true, message: '请输入科室名称', trigger: 'blur' }],
  phone: [{ pattern: /^[0-9-]{7,20}$/, message: '电话格式不正确', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const page = await pageDepartments(query)
    rows.value = page.records || []
    total.value = page.total || 0
  } finally {
    loading.value = false
  }
}

function search() { query.pageNum = 1; load() }
function reset() { query.name = ''; search() }

function openCreate() {
  editingId.value = null
  Object.assign(form, { deptCode: '', deptName: '', location: '', phone: '', description: '', status: 1 })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    deptCode: row.deptCode, deptName: row.deptName, location: row.location,
    phone: row.phone, description: row.description, status: row.status
  })
  dialogVisible.value = true
}

async function save() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (editingId.value) {
        await updateDepartment(editingId.value, { ...form })
        ElMessage.success('科室已更新')
      } else {
        await createDepartment({ ...form })
        ElMessage.success('科室已创建')
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
    await ElMessageBox.confirm(`确定删除科室“${row.deptName}”吗？`, '删除确认', { type: 'warning' })
  } catch (e) {
    return
  }
  await deleteDepartment(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.filter-bar { margin-bottom: 4px; }
.pager { margin-top: 18px; justify-content: flex-end; }
</style>
