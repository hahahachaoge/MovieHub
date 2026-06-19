<template>
  <div class="admin-page">
    <h2 class="page-title">🎬 电影管理</h2>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索电影名称..." clearable size="large" style="width:300px" @keyup.enter="search" />
      <el-button type="primary" size="large" @click="search" style="margin-left:8px">搜索</el-button>
    </div>
    <el-table :data="movies" v-loading="loading" style="width:100%">
      <el-table-column label="ID" prop="id" width="70" />
      <el-table-column label="标题" min-width="220">
        <template #default="{ row }">{{ row.title }}</template>
      </el-table-column>
      <el-table-column label="地区" prop="region" width="120" />
      <el-table-column label="评分" width="80" align="center">
        <template #default="{ row }">{{ row.rating }}</template>
      </el-table-column>
      <el-table-column label="播放量" width="120" align="center">
        <template #default="{ row }">{{ row.playCount }}</template>
      </el-table-column>
      <el-table-column label="VIP" width="80" align="center">
        <template #default="{ row }"><el-tag :type="row.isVip ? 'warning' : 'info'" size="small">{{ row.isVip ? '是' : '否' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '上架' : '下架' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center">
        <template #default="{ row }">
          <el-button :type="row.status === 1 ? 'danger' : 'success'" size="small" @click="toggleStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrap">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="prev,pager,next" background @current-change="loadMovies" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api'

const movies = ref<any[]>([])
const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(20)
const total = ref(0)

async function loadMovies() {
  loading.value = true
  try {
    let res
    if (keyword.value) {
      res = await request.get('/movie/public/all', { params: { keyword: keyword.value, page: page.value, size: size.value } })
    } else {
      res = await request.get('/movie/public/all', { params: { page: page.value, size: size.value } })
    }
    if (res.data.code === 200) {
      movies.value = res.data.data.records || res.data.data
      total.value = res.data.data.total || 0
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function search() {
  page.value = 1
  loadMovies()
}

async function toggleStatus(row: any) {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await request.put('/movie/public/update-status', { id: row.id, status: newStatus })
    ElMessage.success(`已将《${row.title}》${newStatus === 1 ? '上架' : '下架'}`)
    row.status = newStatus
  } catch (e: any) { ElMessage.error('操作失败') }
}

onMounted(loadMovies)
</script>

<style scoped>
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 20px; color: var(--text-primary); }
.toolbar { margin-bottom: 16px; display: flex; align-items: center; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: center; }
</style>
