<template>
  <div class="admin-dashboard">
    <h2 class="page-title">📊 数据概览</h2>

    <!-- 核心数字卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon film-icon"><el-icon :size="32"><Film /></el-icon></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.movieCount }}</span>
          <span class="stat-label">电影总数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon vip-icon"><el-icon :size="32"><StarFilled /></el-icon></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.vipMovieCount }}</span>
          <span class="stat-label">VIP影片</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon user-icon"><el-icon :size="32"><User /></el-icon></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.userCount }}</span>
          <span class="stat-label">注册用户</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon vip-user-icon"><el-icon :size="32"><Medal /></el-icon></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.vipUserCount }}</span>
          <span class="stat-label">VIP会员</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon comment-icon"><el-icon :size="32"><ChatDotSquare /></el-icon></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.reviewCount }}</span>
          <span class="stat-label">用户评论</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon play-icon"><el-icon :size="32"><VideoPlay /></el-icon></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.todayPlays }}</span>
          <span class="stat-label">今日播放</span>
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="charts-row">
      <div class="chart-card">
        <h3 class="chart-title">📊 每日播放量趋势</h3>
        <div ref="trendChartRef" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <h3 class="chart-title">🥧 用户构成</h3>
        <div ref="userChartRef" class="chart-box"></div>
      </div>
    </div>
    <div class="charts-row">
      <div class="chart-card full-width">
        <h3 class="chart-title">🏆 播放量 TOP10</h3>
        <div ref="topChartRef" class="chart-box"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import request from '../../api'

const today = ref('')
const stats = ref({
  movieCount: 0, vipMovieCount: 0,
  userCount: 0, vipUserCount: 0,
  reviewCount: 0,
  todayPlays: 0, totalPlays: 0,
  newUsersThisMonth: 0
})

const trendChartRef = ref<HTMLElement>()
const userChartRef = ref<HTMLElement>()
const topChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let userChart: echarts.ECharts | null = null
let topChart: echarts.ECharts | null = null

function initCharts() {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value, 'dark')
  }
  if (userChartRef.value) {
    userChart = echarts.init(userChartRef.value, 'dark')
  }
  if (topChartRef.value) {
    topChart = echarts.init(topChartRef.value, 'dark')
  }
}

function renderCharts() {
  // 每日播放趋势（最近7天）
  if (trendChart) {
    const labels = trendDays.value.map(d => d.slice(5))
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: labels, axisLabel: { color: '#b3b3b3' } },
      yAxis: { type: 'value', axisLabel: { color: '#b3b3b3' }, splitLine: { lineStyle: { color: '#2a2a4a' } } },
      series: [{
        type: 'bar', data: trendData.value,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#e50914' }, { offset: 1, color: '#ff6b6b' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        barMaxWidth: 40
      }],
      grid: { left: 50, right: 20, top: 20, bottom: 30 }
    })
  }

  // 用户构成饼图
  if (userChart) {
    const vip = stats.value.vipUserCount
    const normal = stats.value.userCount - vip
    userChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
      series: [{
        type: 'pie', radius: ['40%', '70%'],
        center: ['50%', '50%'],
        label: { color: '#b3b3b3', formatter: '{b}\n{c}人' },
        data: [
          { value: normal, name: '普通用户', itemStyle: { color: '#3498db' } },
          { value: vip, name: 'VIP会员', itemStyle: { color: '#d4a017' } }
        ],
        emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.5)' } }
      }]
    })
  }

  // TOP10播放量条形图
  if (topChart && topMovies.value.length > 0) {
    topChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'value', axisLabel: { color: '#b3b3b3' }, splitLine: { lineStyle: { color: '#2a2a4a' } } },
      yAxis: {
        type: 'category', data: topMovies.value.map(m => m.title).reverse(),
        axisLabel: { color: '#b3b3b3', fontSize: 11 }
      },
      series: [{
        type: 'bar',
        data: (() => {
          const colors = ['#e50914','#ff6b6b','#d4a017','#3498db','#2ecc71','#9b59b6','#e67e22','#1abc9c','#e74c3c','#f39c12']
          const copy = [...topMovies.value].reverse()
          return copy.map((m, i) => ({ value: m.playCount, itemStyle: { color: colors[i] } }))
        })(),
        barMaxWidth: 30
      }],
      grid: { left: 120, right: 30, top: 10, bottom: 20 }
    })
  }
}

const trendDays = ref<string[]>([])
const trendData = ref<number[]>([])
const topMovies = ref<Array<{title: string, playCount: number}>>([])

async function loadChartData() {
  try {
    // 每日播放趋势（最近7天）
    const trendRes = await request.get('/report/daily-play-trend')
    if (trendRes.data.code === 200) {
      trendDays.value = trendRes.data.data.days
      trendData.value = trendRes.data.data.data
    }

    // TOP10播放量
    const topRes = await request.get('/ranking/all')
    if (topRes.data.code === 200) {
      topMovies.value = topRes.data.data.slice(0, 10)
    }
  } catch (e) {
    console.error('加载图表数据失败', e)
  }
  renderCharts()
}

function handleResize() {
  trendChart?.resize()
  userChart?.resize()
  topChart?.resize()
}

onMounted(async () => {
  today.value = new Date().toLocaleDateString('zh-CN')
  try {
    const res = await request.get('/report/dashboard')
    if (res.data.code === 200) stats.value = res.data.data
  } catch (e) { console.error('加载数据概览失败', e) }

  initCharts()
  await loadChartData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  userChart?.dispose()
  topChart?.dispose()
})
</script>

<style scoped>
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 24px; color: var(--text-primary); }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--card-radius);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: transform 0.2s;
}
.stat-card:hover { transform: translateY(-2px); }

.stat-icon {
  width: 52px; height: 52px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.film-icon { background: rgba(229,9,20,0.15); color: var(--primary-color); }
.vip-icon { background: rgba(212,160,23,0.15); color: #d4a017; }
.user-icon { background: rgba(46,204,113,0.15); color: #2ecc71; }
.vip-user-icon { background: rgba(155,89,182,0.15); color: #9b59b6; }
.comment-icon { background: rgba(52,152,219,0.15); color: #3498db; }
.play-icon { background: rgba(230,126,34,0.15); color: #e67e22; }

.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 28px; font-weight: 800; color: var(--text-primary); }
.stat-label { font-size: 13px; color: var(--text-muted); }

.charts-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.chart-card {
  flex: 1;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--card-radius);
  padding: 20px;
}
.chart-card.full-width { flex: 0 0 100%; }

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.chart-box {
  width: 100%;
  height: 280px;
}
</style>
