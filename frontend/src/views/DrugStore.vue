<template>
  <div class="page-card">
    <h2 class="page-title">购药商城</h2>
    <p class="page-desc">无需挂号即可直接购买在售药品，下单即结算并扣减库存。</p>

    <el-form :inline="true" class="filter-bar">
      <el-form-item label="药品">
        <el-input v-model="query.keyword" placeholder="药品编码 / 名称" clearable style="width: 220px" @keyup.enter="search" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="search">查询</el-button>
        <el-button icon="Refresh" @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="drugCode" label="编码" width="100" align="center" />
      <el-table-column prop="drugName" label="药品名称" min-width="150" />
      <el-table-column prop="spec" label="规格" width="130" align="center" />
      <el-table-column prop="unit" label="单位" width="70" align="center" />
      <el-table-column prop="price" label="单价（元）" width="100" align="center" />
      <el-table-column label="库存" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="row.stock > 0 ? 'success' : 'danger'" effect="plain">
            {{ row.stock > 0 ? `余 ${row.stock}` : '缺货' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 1 && row.stock > 0"
            type="primary" size="small" @click="openBuy(row)"
          >购买</el-button>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pager" background layout="total, prev, pager, next"
      :total="total" :page-size="query.pageSize" :current-page="query.pageNum"
      @current-change="(p) => { query.pageNum = p; load() }"
    />

    <h3 class="section-title">我的购药订单</h3>
    <el-table :data="orders" v-loading="ordersLoading" stripe size="small">
      <el-table-column prop="orderNo" label="订单编号" width="200" align="center" />
      <el-table-column prop="drugName" label="药品" min-width="140" />
      <el-table-column prop="spec" label="规格" width="120" align="center" />
      <el-table-column prop="quantity" label="数量" width="70" align="center" />
      <el-table-column prop="price" label="单价（元）" width="100" align="center" />
      <el-table-column prop="amount" label="总额（元）" width="100" align="center" />
      <el-table-column prop="createTime" label="下单时间" width="170" align="center" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="orderTag(row.status)" effect="plain">{{ orderText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="warning" size="small" link @click="openPay(row)">去支付</el-button>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      class="pager" background layout="total, prev, pager, next"
      :total="ordersTotal" :page-size="orderQuery.pageSize" :current-page="orderQuery.pageNum"
      @current-change="(p) => { orderQuery.pageNum = p; loadOrders() }"
    />

    <el-dialog v-model="payVisible" title="沙箱收银台 · 虚拟支付" width="400px" destroy-on-close>
      <el-alert type="info" :closable="false" class="pay-tip"
        title="沙箱环境不产生真实扣款，统一付款密码为 123456" />
      <el-descriptions :column="1" border size="small" class="buy-desc">
        <el-descriptions-item label="钱包余额">
          <span class="balance-chip">¥ {{ Number(balance).toFixed(2) }}</span>
          <el-button v-if="Number(balance) < Number(payOrder.amount)" link type="danger" @click="$router.push('/wallet')">余额不足，去充值</el-button>
        </el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ payOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="应付金额">
          <span class="amount">¥ {{ Number(payOrder.amount).toFixed(2) }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <el-form label-width="90px">
        <el-form-item label="支付渠道">
          <el-radio-group v-model="payChannel">
            <el-radio-button value="WECHAT">微信支付</el-radio-button>
            <el-radio-button value="ALIPAY">支付宝</el-radio-button>
            <el-radio-button value="BANK">银行卡</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="payChannel === 'BANK'" label="银行卡号">
          <el-input v-model="bankCardNo" maxlength="19" placeholder="输入 13-19 位银行卡号" />
        </el-form-item>
        <el-form-item label="付款密码">
          <el-input
            v-model="payPassword" type="password" maxlength="6" show-password
            placeholder="输入 6 位数字付款密码" @keyup.enter="confirmPay"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payVisible = false">稍后支付</el-button>
        <el-button type="primary" :loading="paying" @click="confirmPay">确认支付</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="buyVisible" title="确认购买" width="420px">
      <el-descriptions :column="1" border size="small" class="buy-desc">
        <el-descriptions-item label="药品">{{ buyDrug.drugName }}</el-descriptions-item>
        <el-descriptions-item label="规格">{{ buyDrug.spec }}</el-descriptions-item>
        <el-descriptions-item label="单价">{{ buyDrug.price }} 元 / {{ buyDrug.unit }}</el-descriptions-item>
        <el-descriptions-item label="库存">余 {{ buyDrug.stock }} {{ buyDrug.unit }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="90px">
        <el-form-item label="购买数量">
          <el-input-number v-model="quantity" :min="1" :max="buyDrug.stock" />
        </el-form-item>
        <el-form-item label="应付金额">
          <span class="amount">¥ {{ (Number(buyDrug.price) * quantity).toFixed(2) }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="buyVisible = false">取消</el-button>
        <el-button type="primary" :loading="buying" @click="confirmBuy">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { pageDrugs } from '../api/drug'
import { purchase, pagePurchases, payPurchase } from '../api/purchase'
import { getBalance } from '../api/wallet'
import { useRouter } from 'vue-router'
const router = useRouter()

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 8, keyword: '' })

const orders = ref([])
const ordersTotal = ref(0)
const ordersLoading = ref(false)
const orderQuery = reactive({ pageNum: 1, pageSize: 8 })

const buyVisible = ref(false)
const buyDrug = ref({})
const quantity = ref(1)
const buying = ref(false)
const balance = ref(0)
const payVisible = ref(false)
const payOrder = ref({})
const payPassword = ref('')
const payChannel = ref('WECHAT')
const bankCardNo = ref('')
const paying = ref(false)

const ORDER = { 0: '待支付', 1: '已完成', 2: '已退款' }
const ORDER_TAG = { 0: 'warning', 1: 'success', 2: 'info' }
const orderText = (s) => ORDER[s] ?? s
const orderTag = (s) => ORDER_TAG[s] ?? 'info'
const CHANNEL = { WECHAT: '微信支付', ALIPAY: '支付宝', BANK: '银行卡' }
const channelText = (c) => CHANNEL[c] ?? c

async function load() {
  loading.value = true
  try {
    const page = await pageDrugs(query)
    rows.value = page.records || []
    total.value = page.total || 0
  } finally {
    loading.value = false
  }
}

async function loadOrders() {
  ordersLoading.value = true
  try {
    // 后端按登录患者自动收窄为本人订单
    const page = await pagePurchases(orderQuery)
    orders.value = page.records || []
    ordersTotal.value = page.total || 0
  } finally {
    ordersLoading.value = false
  }
}

function search() { query.pageNum = 1; load() }
function reset() { query.keyword = ''; search() }

function openBuy(row) {
  buyDrug.value = row
  quantity.value = 1
  buyVisible.value = true
}

async function confirmBuy() {
  buying.value = true
  try {
    const order = await purchase({ drugId: buyDrug.value.id, quantity: quantity.value })
    buyVisible.value = false
    ElMessage.success(`下单成功，请完成支付（订单号 ${order.orderNo}）`)
    load()
    loadOrders()
    // 下单后直接弹出沙箱收银台
    openPay(order)
  } finally {
    buying.value = false
  }
}

async function openPay(row) {
  payOrder.value = row
  payPassword.value = ''
  payChannel.value = 'WECHAT'
  bankCardNo.value = ''
  balance.value = await getBalance()
  payVisible.value = true
}

async function confirmPay() {
  if (!/^\d{6}$/.test(payPassword.value)) {
    ElMessage.warning('请输入 6 位数字付款密码')
    return
  }
  if (payChannel.value === 'BANK' && !/^\d{13,19}$/.test(bankCardNo.value)) {
    ElMessage.warning('请输入 13-19 位数字银行卡号')
    return
  }
  paying.value = true
  try {
    const order = await payPurchase(payOrder.value.id, payPassword.value, payChannel.value, bankCardNo.value)
    ElMessage.success(`支付成功（${channelText(payChannel.value)}），实付 ¥ ${Number(order.amount).toFixed(2)}`)
    payVisible.value = false
    loadOrders()
    balance.value = await getBalance()
  } finally {
    paying.value = false
  }
}

onMounted(() => {
  load()
  loadOrders()
})
</script>

<style scoped>
.filter-bar { margin-bottom: 4px; }
.pager { margin-top: 14px; justify-content: flex-end; }
.section-title {
  margin: 28px 0 10px;
  font-size: 16px;
}
.muted { color: #9aa8ba; font-size: 12px; }
.amount {
  font-size: 18px;
  font-weight: 600;
  color: #e6653a;
}
.buy-desc { margin-bottom: 14px; }
.pay-tip { margin-bottom: 14px; }
</style>
