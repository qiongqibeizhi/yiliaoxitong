<template>
  <div class="page-card">
    <h2 class="page-title">药品库</h2>
    <p class="page-desc">全院药品目录，所有角色可查询；库存与价格由后端维护。</p>

    <el-form :inline="true" :model="query" class="filter-bar">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="药品编码 / 名称" clearable style="width: 220px" @keyup.enter="search" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="search">查询</el-button>
        <el-button icon="Refresh" @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="drugCode" label="药品编码" width="120" align="center" />
      <el-table-column prop="drugName" label="药品名称" min-width="150" />
      <el-table-column prop="spec" label="规格" width="130" align="center" />
      <el-table-column prop="unit" label="单位" width="80" align="center" />
      <el-table-column prop="price" label="单价（元）" width="110" align="center" />
      <el-table-column label="库存" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="row.stock > 0 ? 'success' : 'danger'" effect="plain">
            {{ row.stock > 0 ? `余 ${row.stock}` : '缺货' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">{{ row.status === 1 ? '在售' : '停售' }}</el-tag>
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
import { pageDrugs } from '../api/drug'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

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

function search() { query.pageNum = 1; load() }
function reset() { query.keyword = ''; search() }

onMounted(load)
</script>

<style scoped>
.filter-bar { margin-bottom: 4px; }
.pager { margin-top: 18px; justify-content: flex-end; }
</style>
