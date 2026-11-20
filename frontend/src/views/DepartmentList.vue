<template>
  <div>
    <div class="page-card">
      <h2 class="page-title">科室导航</h2>
      <p class="page-desc">{{ visitor ? '查看全院科室分布与楼层位置，登录后可在线预约挂号。' : '选择科室查看出诊医生，支持按名称与状态筛选。' }}</p>

      <el-form :inline="true" :model="query" class="filter-bar">
        <el-form-item label="科室名称">
          <el-input v-model="query.name" placeholder="如：内科" clearable style="width: 200px" @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="search">查询</el-button>
          <el-button icon="Refresh" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="rows" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="deptCode" label="科室编码" width="120" align="center" />
        <el-table-column prop="deptName" label="科室名称" min-width="140" />
        <el-table-column prop="location" label="位置" min-width="160" />
        <el-table-column prop="phone" label="联系电话" width="150" align="center" />
        <el-table-column prop="description" label="科室简介" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button type="primary" link icon="User" @click="viewDoctors(row)">查看医生</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        background
        layout="total, prev, pager, next, sizes"
        :total="total"
        :page-size="query.pageSize"
        :current-page="query.pageNum"
        :page-sizes="[5, 10, 20]"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { getUser } from '../utils/auth'

// 访客模式：未登录也可浏览科室导览
const visitor = !getUser()
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { pageDepartments } from '../api/department'

const router = useRouter()
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, name: '', status: null })

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

function search() {
  query.pageNum = 1
  load()
}

function reset() {
  query.name = ''
  query.status = null
  search()
}

function onPageChange(page) {
  query.pageNum = page
  load()
}

function onSizeChange(size) {
  query.pageSize = size
  query.pageNum = 1
  load()
}

function viewDoctors(row) {
  router.push(`/departments/${row.id}/doctors`)
}

onMounted(load)
</script>

<style scoped>
.filter-bar {
  margin-bottom: 4px;
}
.pager {
  margin-top: 18px;
  justify-content: flex-end;
}
</style>
