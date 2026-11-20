<template>
  <div class="page-card">
    <h2 class="page-title">我的钱包</h2>
    <p class="page-desc">余额用于支付处方药费与购药订单；沙箱充值不产生真实扣款。</p>

    <div class="wallet-box">
      <div class="balance-label">当前余额</div>
      <div class="balance">¥ {{ Number(balance).toFixed(2) }}</div>
      <el-button type="primary" size="large" @click="openRecharge">充值</el-button>
    </div>

    <el-dialog v-model="visible" title="收银台 · 余额充值" width="420px" destroy-on-close>
      <el-alert type="info" :closable="false" class="tip"
        title="沙箱环境不产生真实扣款，统一付款密码为 123456" />
      <el-form label-width="90px">
        <el-form-item label="充值金额">
          <el-input-number v-model="amount" :min="0.01" :max="50000" :precision="2" :step="100" style="width: 200px" />
        </el-form-item>
        <el-form-item label="付款密码">
          <el-input
            v-model="payPassword" type="password" maxlength="6" show-password
            placeholder="输入 6 位数字付款密码" @keyup.enter="submit"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="submit">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getBalance, recharge } from '../api/wallet'

const balance = ref(0)
const visible = ref(false)
const amount = ref(100)
const payPassword = ref('')
const loading = ref(false)

async function load() {
  balance.value = await getBalance()
}

function openRecharge() {
  payPassword.value = ''
  visible.value = true
}

async function submit() {
  if (!/^\d{6}$/.test(payPassword.value)) {
    ElMessage.warning('请输入 6 位数字付款密码')
    return
  }
  loading.value = true
  try {
    balance.value = await recharge({ amount: amount.value, payPassword: payPassword.value })
    ElMessage.success(`充值成功，当前余额 ¥ ${Number(balance.value).toFixed(2)}`)
    visible.value = false
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.wallet-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  background: linear-gradient(135deg, #1d7fd6, #27b6a6);
  color: #fff;
  border-radius: 14px;
  padding: 36px;
}
.balance-label { font-size: 14px; opacity: 0.9; }
.balance {
  font-size: 44px;
  font-weight: 700;
  letter-spacing: 1px;
}
.tip { margin-bottom: 14px; }
</style>
