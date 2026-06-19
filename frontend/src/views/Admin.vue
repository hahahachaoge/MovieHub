<template>
  <div class="admin-layout">
    <div class="admin-sidebar">
      <div class="sidebar-title">🎬 管理后台</div>
      <el-menu :default-active="activeMenu" class="sidebar-menu" @select="handleMenu">
        <el-menu-item index="dashboard">
          <el-icon><DataAnalysis /></el-icon>数据概览
        </el-menu-item>
        <el-menu-item index="movies">
          <el-icon><Film /></el-icon>电影管理
        </el-menu-item>
        <el-menu-item index="users">
          <el-icon><User /></el-icon>用户管理
        </el-menu-item>
      </el-menu>
    </div>
    <div class="admin-main">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const activeMenu = route.name as string || 'dashboard'

function handleMenu(index: string) {
  router.push(`/admin/${index}`)
}
</script>

<style scoped>
.admin-layout { display: flex; min-height: calc(100vh - var(--header-height)); background: var(--bg-dark); }
.admin-sidebar { width: 180px; background: var(--bg-darker); border-right: 1px solid var(--border-color); flex-shrink: 0; }
.sidebar-title { font-size: 16px; font-weight: 700; color: var(--primary-color); padding: 16px 20px; border-bottom: 1px solid var(--border-color); }
.sidebar-menu { background: transparent; border-right: none; --el-menu-text-color: var(--text-secondary); --el-menu-active-color: var(--primary-color); --el-menu-bg-color: transparent; --el-menu-hover-bg-color: rgba(229,9,20,0.05); }
:deep(.el-menu-item) { font-size: 14px !important; height: 44px !important; line-height: 44px !important; }
.admin-main { flex: 1; padding: 24px; overflow: auto; }
</style>
