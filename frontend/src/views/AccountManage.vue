<template>
  <div class="page-card">
    <h2 class="page-title">账号管理</h2>
    <p class="page-desc">医院内部账号由管理员统一开设：为医生开户将同时创建医生档案；医生/管理员不开放自助注册。</p>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="医生账号" name="DOCTOR">
        <div class="tab-bar">
          <el-button type="primary" icon="Plus" @click="openDoctor">为医生开户</el-button>
        </div>
        <el-table :data="doctorAccounts" v-loading="loading" stripe>
          <el-table-column prop="id" label="账号 ID" width="90" align="center" />
          <el-table-column prop="username" label="用户名" min-width="140" />
          <el-table-column prop="realName" label="姓名" width="120" align="center" />
          <el-table-column label="医生编号" width="110" align="center">
            <template #default="{ row }">{{ row.doctorId ? 'D' + String(row.doctorId).padStart(6, '0') : '—' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="管理员账号" name="ADMIN">
        <div class="tab-bar">
          <el-button type="primary" icon="Plus" @click="openAdmin">新增管理员</el-button>
        </div>
        <el-table :data="adminAccounts" v-loading="loading" stripe>
          <el-table-column prop="id" label="账号 ID" width="90" align="center" />
          <el-table-column prop="username" label="用户名" min-width="140" />
          <el-table-column prop="realName" label="姓名" width="120" align="center" />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 医生开户 -->
    <el-dialog v-model="doctorVisible" title="为医生开户（建档 + 账号）" width="560px" destroy-on-close>
      <el-form ref="doctorFormRef" :model="doctorForm" :rules="doctorRules" label-width="100px">
        <el-form-item label="登录用户名" prop="username">
          <el-input v-model="doctorForm.username" placeholder="建议使用姓名拼音，如 lihong" />
        </el-form-item>
        <el-form-item label="初始密码" prop="password">
          <el-input v-model="doctorForm.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="doctorForm.realName" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="doctorForm.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="所属科室" prop="departmentId">
          <el-select v-model="doctorForm.departmentId" filterable style="width: 100%">
            <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="doctorForm.title" placeholder="如：主任医师" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="doctorForm.phone" />
        </el-form-item>
        <el-form-item label="擅长领域">
          <el-input v-model="doctorForm.specialty" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="doctorVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitDoctor">开户</el-button>
      </template>
    </el-dialog>

    <!-- 管理员开户 -->
    <el-dialog v-model="adminVisible" title="新增管理员账号" width="420px" destroy-on-close>
      <el-form ref="adminFormRef" :model="adminForm" :rules="adminRules" label-width="100px">
        <el-form-item label="登录用户名" prop="username">
          <el-input v-model="adminForm.username" />
        </el-form-item>
        <el-form-item label="初始密码" prop="password">
          <el-input v-model="adminForm.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="adminForm.realName" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adminVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitAdmin">开户</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { pageDepartments } from '../api/department'

const activeTab = ref('DOCTOR')
const loading = ref(false)
const saving = ref(false)
const accounts = ref([])
const departments = ref([])

const doctorAccounts = computed(() => accounts.value.filter((a) => a.role === 'DOCTOR'))
const adminAccounts = computed(() => accounts.value.filter((a) => a.role === 'ADMIN'))

const doctorVisible = ref(false)
const doctorFormRef = ref()
const doctorForm = reactive({ username: '', password: '', realName: '', gender: '男', departmentId: null, title: '', phone: '', specialty: '' })
const doctorRules = {
  username: [{ required: true, message: '请填写用户名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少 6 位', trigger: 'blur' }],
  realName: [{ required: true, message: '请填写姓名', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择科室', trigger: 'change' }]
}

const adminVisible = ref(false)
const adminFormRef = ref()
const adminForm = reactive({ username: '', password: '', realName: '' })
const adminRules = {
  username: [{ required: true, message: '请填写用户名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少 6 位', trigger: 'blur' }],
  realName: [{ required: true, message: '请填写姓名', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    accounts.value = await request.get('/api/admin/accounts')
  } finally {
    loading.value = false
  }
}

function openDoctor() {
  doctorVisible.value = true
}

function openAdmin() {
  adminForm.username = ''
  adminForm.password = ''
  adminForm.realName = ''
  adminVisible.value = true
}

async function submitDoctor() {
  await doctorFormRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      await request.post('/api/admin/accounts/doctor', doctorForm)
      ElMessage.success(`医生账号 ${doctorForm.username} 已开通`)
      doctorVisible.value = false
      load()
    } finally {
      saving.value = false
    }
  })
}

async function submitAdmin() {
  await adminFormRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      await request.post('/api/admin/accounts/admin', adminForm)
      ElMessage.success(`管理员账号 ${adminForm.username} 已开通`)
      adminVisible.value = false
      load()
    } finally {
      saving.value = false
    }
  })
}

onMounted(async () => {
  load()
  const dep = await pageDepartments({ pageNum: 1, pageSize: 100 })
  departments.value = dep.records || []
})
</script>

<style scoped>
.tab-bar { margin-bottom: 14px; }
</style>
