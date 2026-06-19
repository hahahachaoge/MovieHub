<template>
  <div class="admin-page">
    <h2 class="page-title">👥 用户管理</h2>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索用户名..." clearable size="large" style="width:300px" @keyup.enter="search" />
      <el-button type="primary" size="large" @click="search" style="margin-left:8px">搜索</el-button>
    </div>
    <el-table :data="users" v-loading="loading" style="width:100%">
      <el-table-column label="ID" prop="id" width="80" align="center" />
      <el-table-column label="用户名" prop="username" min-width="120" />
      <el-table-column label="邮箱" prop="email" min-width="200" />
      <el-table-column label="手机" prop="phone" min-width="150" />
      <el-table-column label="角色" min-width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : row.role === 'VIP' ? 'warning' : 'info'" size="small">{{ row.role === 'ADMIN' ? '管理员' : row.role === 'VIP' ? 'VIP会员' : '普通用户' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" min-width="80" align="center">
        <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center">
        <template #default="{ row }">
          <span v-if="row.role === 'ADMIN'" style="color:var(--text-muted);font-size:13px">无法禁用</span>
          <el-button v-else :type="row.status === 1 ? 'warning' : 'success'" size="small" @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" prop="createTime" min-width="180" />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api'

const users = ref<any[]>([])
const loading = ref(false)
const keyword = ref('')

async function loadUsers() {
  loading.value = true
  try {
    const params: any = {}
    if (keyword.value) params.keyword = keyword.value
    const res = await request.get('/user/list', { params })
    if (res.data.code === 200) users.value = res.data.data
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function search() {
  loadUsers()
}

async function toggleStatus(row: any) {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await request.put('/user/set-status', { id: row.id, status: newStatus })
    ElMessage.success(`用户 ${row.username} 已${newStatus === 1 ? '启用' : '禁用'}`)
    row.status = newStatus
  } catch (e: any) { ElMessage.error('操作失败') }
}

onMounted(loadUsers)
</script>

<style scoped>
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 20px; color: var(--text-primary); }
.toolbar { margin-bottom: 16px; display: flex; align-items: center; }
</style>
