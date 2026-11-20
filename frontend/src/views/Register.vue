<template>
  <div class="page-card">
    <h2 class="page-title">患者注册</h2>
    <p class="page-desc">注册即完成患者建档并开通就诊账号，注册成功后自动登录。</p>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="reg-form">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" placeholder="3-40 位登录名" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" type="password" show-password placeholder="6-40 位密码" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirm">
        <el-input v-model="form.confirm" type="password" show-password placeholder="再次输入密码" />
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
      <el-form-item label="身份证号" prop="idCard">
        <el-input v-model="form.idCard" maxlength="18" placeholder="将用于计算出生日期并校验真伪" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" maxlength="11" />
      </el-form-item>
      <el-form-item label="联系地址" prop="address">
        <el-input v-model="form.address" maxlength="200" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="submit">注册并登录</el-button>
        <el-button @click="$router.push('/login')">返回登录</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../api/auth'
import { saveSession } from '../utils/auth'
import { homePathByRole } from '../router'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({
  username: '', password: '', confirm: '', name: '',
  gender: '男', idCard: '', phone: '', address: ''
})

// 身份证号逐级校验：格式 → 出生日期 → 校验位，明确提示具体哪里不对
function validateIdCard(rule, value, callback) {
  if (!/^\d{17}[0-9Xx]$/.test(value)) {
    callback(new Error('身份证号应为 18 位（前 17 位数字 + 末位数字或 X）'))
    return
  }
  const y = Number(value.slice(6, 10))
  const m = Number(value.slice(10, 12))
  const d = Number(value.slice(12, 14))
  const now = new Date()
  if (y < 1900 || y > now.getFullYear()) {
    callback(new Error(`出生年份 ${y} 无效（应为 1900-${now.getFullYear()}）`))
    return
  }
  const birth = new Date(y, m - 1, d)
  if (birth.getFullYear() !== y || birth.getMonth() !== m - 1 || birth.getDate() !== d) {
    callback(new Error(`出生日期 ${y}-${m}-${d} 不存在，请检查号码`))
    return
  }
  if (birth > now) {
    callback(new Error('出生日期不能晚于今天，请检查号码'))
    return
  }
  const weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
  const codes = '10X98765432'
  let sum = 0
  for (let i = 0; i < 17; i++) sum += Number(value[i]) * weights[i]
  if (codes[sum % 11] !== value[17].toUpperCase()) {
    callback(new Error('身份证号校验位不正确，请核对是否输错'))
    return
  }
  callback()
}

const rules = {
  username: [{ required: true, message: '请填写用户名', trigger: 'blur' },
    { min: 3, max: 40, message: '用户名长度应为 3-40 位', trigger: 'blur' }],
  password: [{ required: true, message: '请填写密码', trigger: 'blur' },
    { min: 6, max: 40, message: '密码长度应为 6-40 位', trigger: 'blur' }],
  confirm: [{
    validator: (rule, value, callback) => {
      if (!value) callback(new Error('请再次输入密码'))
      else if (value !== form.password) callback(new Error('两次输入的密码不一致'))
      else callback()
    },
    trigger: 'blur'
  }],
  name: [{ required: true, message: '请填写姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  idCard: [{ required: true, message: '请填写身份证号', trigger: 'blur' },
    { validator: validateIdCard, trigger: 'blur' }],
  phone: [{ required: true, message: '请填写手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

async function submit() {
  await formRef.value.validate(async (valid, invalidFields) => {
    if (!valid) {
      // 点击注册时校验不通过：弹窗提示第一个错误字段，并滚动到该字段
      const firstField = Object.keys(invalidFields || {})[0]
      if (firstField) {
        const firstError = invalidFields[firstField][0]?.message || '请检查填写内容'
        ElMessage.warning(firstError)
        formRef.value.scrollToField(firstField)
      }
      return
    }
    loading.value = true
    try {
      const data = await register(form)
      saveSession(data)
      ElMessage.success(`注册成功，欢迎您，${data.realName}`)
      router.push(homePathByRole(data.role))
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.reg-form {
  max-width: 520px;
  margin: 0 auto;
  padding-top: 8px;
}
</style>
