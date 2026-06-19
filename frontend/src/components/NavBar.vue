<template>
  <header class="navbar" :class="{ 'navbar-scrolled': isScrolled }">
    <div class="navbar-inner">
      <div class="logo" @click="goHome">
        <span class="logo-icon">🎬</span>
        <span class="logo-text">MovieHub</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        mode="horizontal"
        :ellipsis="false"
        class="nav-menu"
        @select="handleMenuSelect"
      >
        <el-menu-item index="home">
          <el-icon><HomeFilled /></el-icon>
          首页
        </el-menu-item>

        <el-sub-menu index="category" popper-class="category-popper" :popper-style="{ background: '#000', '--el-menu-bg-color': '#000' }">
          <template #title>
            <el-icon><Menu /></el-icon>
            电影分类
          </template>
          <el-menu-item
            v-for="cat in categories"
            :key="cat.id"
            :index="`category-${cat.id}`"
          >
            {{ cat.name }}
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item index="rankings">
          <el-icon><TrendCharts /></el-icon>
          排行榜
        </el-menu-item>

        <el-menu-item index="crew">
          <el-icon><Search /></el-icon>
          主创查询
        </el-menu-item>

        <el-menu-item index="statistics">
          <el-icon><DataAnalysis /></el-icon>
          数据统计
        </el-menu-item>
        <el-menu-item index="graph">
          <el-icon><Connection /></el-icon>
          关系图谱
        </el-menu-item>
      </el-menu>

      <div class="navbar-right">
        <template v-if="userStore.isLoggedIn">
          <el-dropdown trigger="click" @command="handleUserCommand">
            <span class="user-info">
              <el-avatar
                :size="32"
                :src="userStore.userInfo?.avatar"
                class="user-avatar"
              >
                {{ userStore.userInfo?.username?.[0]?.toUpperCase() }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.username }}</span>
              <el-tag
                v-if="userStore.isAdmin"
                size="small"
                type="danger"
                class="admin-tag"
              >
                管理员
              </el-tag>
              <el-tag
                v-else-if="userStore.isVip"
                size="small"
                type="warning"
                class="vip-tag"
              >
                VIP
              </el-tag>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown-menu">
                <div class="dropdown-user-header">
                  <el-avatar :size="36" :src="userStore.userInfo?.avatar" class="dropdown-avatar">
                    {{ userStore.userInfo?.username?.[0]?.toUpperCase() }}
                  </el-avatar>
                  <div class="dropdown-user-info">
                    <span class="dropdown-username">{{ userStore.userInfo?.username }}</span>
                    <span class="dropdown-role">{{ userStore.isAdmin ? '管理员' : userStore.isVip ? 'VIP会员' : '普通用户' }}</span>
                  </div>
                </div>
                <el-dropdown-item command="profile" class="dropdown-item-custom">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="admin" class="dropdown-item-custom" v-if="userStore.isAdmin">
                  <el-icon><Setting /></el-icon>管理后台
                </el-dropdown-item>
                <el-dropdown-item command="payment" class="dropdown-item-custom">
                  <el-icon><Coin /></el-icon>VIP充值
                </el-dropdown-item>
                <el-dropdown-item divided command="logout" class="dropdown-item-custom">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" size="small" @click="router.push('/login')">
            登录
          </el-button>
          <el-button size="small" @click="router.push('/register')">
            注册
          </el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import request from '../api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isScrolled = ref(false)
const categories = ref<Array<{ id: number; name: string }>>([])

const activeMenu = computed(() => {
  const name = route.name as string
  if (name === 'Home') return 'home'
  if (name === 'MovieList') return 'category'
  if (name === 'Rankings') return 'rankings'
  if (name === 'CrewSearch') return 'crew'
  if (name === 'Statistics') return 'statistics'
  if (name === 'Graph') return 'graph'
  return ''
})

function handleMenuSelect(index: string) {
  if (index === 'home') router.push('/')
  else if (index === 'rankings') router.push('/rankings')
  else if (index === 'crew') router.push('/crew')
  else if (index === 'statistics') router.push('/statistics')
  else if (index === 'graph') router.push('/graph')
  else if (index.startsWith('category-')) {
    const categoryId = index.split('-')[1]
    router.push(`/movies?categoryId=${categoryId}`)
  }
}

function handleUserCommand(command: string) {
  switch (command) {
    case 'profile': router.push('/user'); break
    case 'admin': router.push('/admin'); break
    case 'payment': router.push('/payment'); break
    case 'logout': userStore.logout(); router.push('/'); break
  }
}

function goHome() { router.push('/') }

function handleScroll() { isScrolled.value = window.scrollY > 50 }

async function fetchCategories() {
  try {
    const res = await request.get('/movie/public/categories')
    if (res.data.code === 200) categories.value = res.data.data
  } catch (e) { console.error('获取分类失败', e) }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  if (categories.value.length === 0) fetchCategories()
})

onUnmounted(() => { window.removeEventListener('scroll', handleScroll) })
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: var(--header-height);
  background: rgba(15, 15, 26, 0.95);
  backdrop-filter: blur(10px);
  z-index: 1000;
  transition: all 0.3s ease;
  border-bottom: 1px solid transparent;
}

.navbar-scrolled {
  border-bottom-color: var(--border-color);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.3);
}

.navbar-inner {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  margin-right: 24px;
  flex-shrink: 0;
}

.logo-icon { font-size: 28px; }

.logo-text {
  font-size: 22px;
  font-weight: 800;
  color: var(--primary-color);
  letter-spacing: -0.5px;
}

.nav-menu {
  flex: 1;
  background: transparent;
  border-bottom: none;
  --el-menu-hover-bg-color: transparent;
  --el-menu-active-color: var(--primary-color);
  --el-menu-text-color: var(--text-secondary);
  --el-menu-hover-text-color: var(--text-primary);
  --el-menu-bg-color: transparent;
}

.nav-menu .el-menu-item,
.nav-menu .el-sub-menu__title {
  color: var(--text-secondary) !important;
  font-weight: 500;
  background: transparent !important;
}

.nav-menu .el-menu-item:hover {
  color: var(--text-primary) !important;
  background: transparent !important;
}

/* 电影分类悬停保持原色不变白 */
.nav-menu .el-sub-menu__title:hover {
  color: var(--text-secondary) !important;
  background: transparent !important;
}

.nav-menu .el-menu-item.is-active {
  color: var(--primary-color) !important;
  border-bottom-color: var(--primary-color) !important;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.05);
}

.username {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
}

.vip-tag {
  --el-tag-bg-color: rgba(212, 160, 23, 0.2);
  --el-tag-border-color: var(--vip-gold);
  --el-tag-text-color: var(--vip-gold);
}

.admin-tag {
  --el-tag-bg-color: rgba(229, 9, 20, 0.2);
  --el-tag-border-color: var(--primary-color);
  --el-tag-text-color: var(--primary-color);
}

.user-avatar {
  background: var(--border-color);
}
</style>

<style>
/* === 电影分类下拉菜单 === */
.category-popper {
  border: none !important;
  border-radius: 0 0 6px 6px !important;
  padding: 4px 0 !important;
  min-width: 80px !important;
  margin-top: 0 !important;
}

.category-popper,
.category-popper .el-menu--popup {
  background: #000 !important;
}

.category-popper .el-menu-item {
  color: #fff !important;
  height: 34px !important;
  line-height: 34px !important;
  font-size: 13px !important;
  padding: 0 14px !important;
  border-bottom: none !important;
}

.category-popper .el-menu-item:hover {
  color: var(--primary-color) !important;
  background: #000 !important;
}

/* === 用户下拉菜单 === */
.user-dropdown-menu {
  background: var(--bg-darker) !important;
  border: none !important;
  border-radius: 6px !important;
  padding: 4px 0 !important;
  min-width: 130px !important;
}

.dropdown-user-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 4px;
}

.dropdown-avatar {
  background: var(--bg-darker);
  flex-shrink: 0;
}

.dropdown-user-info {
  display: flex;
  flex-direction: column;
}

.dropdown-username {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 600;
}

.dropdown-role {
  color: var(--text-muted);
  font-size: 12px;
}

.dropdown-item-custom {
  color: var(--text-secondary) !important;
  font-size: 13px !important;
  display: flex !important;
  align-items: center !important;
  gap: 6px !important;
}

.dropdown-item-custom:hover {
  background: rgba(229, 9, 20, 0.1) !important;
  color: var(--primary-color) !important;
}

.el-dropdown-menu__item--divided {
  border-top-color: var(--border-color) !important;
  margin-top: 4px !important;
  padding-top: 4px !important;
}
</style>