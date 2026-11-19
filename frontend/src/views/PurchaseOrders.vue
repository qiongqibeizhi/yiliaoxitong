<template>
  <div class="page-card">
    <h2 class="page-title">购药订单</h2>
    <p class="page-desc">全院患者单独购药订单，管理员可按订单编号查询。</p>

    <el-form :inline="true" class="filter-bar">
      <el-form-item label="患者编号">
        <el-input v-model="query.patientId" placeholder="患者 ID" clearable style="width: 140px" @keyup.enter="search" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="search">查询</el-button>
        <el-button icon="Refresh" @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="orderNo" label="订单编号" width="200" align="center" />
      <el-table-column prop="patientId" label="患者编号" width="100" align="center" />
      <el-table-column prop="drugName" label="药品" min-width="140" />
      <el-table-column prop="spec" label="规格" width="120" align="center" />
      <el-table-column prop="quantity" label="数量" width="70" align="center" />
      <el-table-column prop="price" label="单价（元）" width="100" align="center" />
      <el-table-column prop="amount" label="总额（元）" width="100" align="center" />
      <el-table-column prop="createTime" label="下单时间" width="170" align="center" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">
            {{ row.status === 1 ? '已完成' : '已退款' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pager" background layout="total, prev, pager, next"
      :total="total" :page-size="query.pageSize" :current-page="query.pageNum"
      @current-change="(p) => { query.pageNum = p; load() }"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { pagePurchases } from '../api/purchase'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, patientId: '' })

async function load() {
  loading.value = true
  try {
    const params = { ...query }
    if (!params.patientId) delete params.patientId
    const page = await pagePurchases(params)
    rows.value = page.records || []
    total.value = page.total || 0
  } finally {
    loading.value = false
  }
}

function search() { query.pageNum = 1; load() }
function reset() { query.patientId = ''; search() }

onMounted(load)
</script>

<style scoped>
.filter-bar { margin-bottom: 4px; }
.pager { margin-top: 18px; justify-content: flex-end; }
</style>
