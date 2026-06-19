<template>
  <div class="rankings-page page-container">
    <div class="page-header">
      <h1 class="section-title">📊 电影排行榜</h1>
      <el-button type="primary" size="small" @click="exportExcel" :loading="exporting">
        <el-icon><Download /></el-icon>
        导出Excel
      </el-button>
    </div>

    <el-tabs v-model="activeTab" class="ranking-tabs" @tab-change="onTabChange">
      <el-tab-pane label="本周排行" name="weekly">
        <RankingTable :items="weeklyData" :loading="loading" type="playCount" />
      </el-tab-pane>
      <el-tab-pane label="本月排行" name="monthly">
        <RankingTable :items="monthlyData" :loading="loading" type="playCount" />
      </el-tab-pane>
      <el-tab-pane label="全部排行" name="all">
        <RankingTable :items="allData" :loading="loading" type="playCount" />
      </el-tab-pane>
      <el-tab-pane label="好评排行" name="topRated">
        <RankingTable :items="topRatedData" :loading="loading" type="rating" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import RankingTable from '../components/RankingTable.vue'
import { getWeeklyRanking, getMonthlyRanking, getAllTimeRanking, getTopRated } from '../api/ranking'
import type { RankingVO } from '../api/ranking'

const activeTab = ref('weekly')
const loading = ref(true)
const exporting = ref(false)

async function exportExcel() {
  exporting.value = true
  try {
    const token = localStorage.getItem('token')
    window.open(`http://localhost:8088/api/report/export/${activeTab.value}?token=${token}`, '_blank')
  } catch (e) {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

const weeklyData = ref<RankingVO[]>([])
const monthlyData = ref<RankingVO[]>([])
const allData = ref<RankingVO[]>([])
const topRatedData = ref<RankingVO[]>([])

async function onTabChange() {
  loading.value = true
  try {
    let res: any
    switch (activeTab.value) {
      case 'weekly':
        res = await getWeeklyRanking()
        if (res.data.code === 200) weeklyData.value = res.data.data
        break
      case 'monthly':
        res = await getMonthlyRanking()
        if (res.data.code === 200) monthlyData.value = res.data.data
        break
      case 'all':
        res = await getAllTimeRanking()
        if (res.data.code === 200) allData.value = res.data.data
        break
      case 'topRated':
        res = await getTopRated()
        if (res.data.code === 200) topRatedData.value = res.data.data
        break
    }
  } catch (e) {
    console.error('排行榜加载失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getWeeklyRanking()
    if (res.data.code === 200) weeklyData.value = res.data.data
  } catch (e) {
    console.error('加载失败', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.rankings-page {
  padding-top: 20px;
  padding-bottom: 40px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.ranking-tabs {
  --el-tabs-header-text-color: var(--text-secondary);
  --el-tabs-active-text-color: var(--primary-color);
}

:deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 600;
  padding: 0 20px;
}

:deep(.el-tabs__item:hover) {
  color: var(--text-primary);
}

:deep(.el-tabs__active-bar) {
  background-color: var(--primary-color);
}

:deep(.el-tabs__nav-wrap::after) {
  background-color: var(--border-color);
}
</style>
