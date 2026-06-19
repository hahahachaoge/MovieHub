<template>
  <div class="chart-container">
    <h3 class="chart-title">🎯 评分分布</h3>
    <div ref="chartRef" class="chart-box"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps<{
  categories: string[]
  data: number[]
}>()

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

function initChart() {
  if (!chartRef.value) return
  chart = echarts.init(chartRef.value, 'dark')
  updateChart()
}

function updateChart() {
  if (!chart) return
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: props.categories,
      axisLabel: { color: '#b3b3b3' }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#b3b3b3' },
      splitLine: { lineStyle: { color: '#2a2a4a' } }
    },
    series: [{
      type: 'bar',
      data: props.data,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#e50914' },
          { offset: 1, color: '#ff6b6b' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      barMaxWidth: 50
    }],
    grid: { left: 60, right: 20, top: 20, bottom: 30 }
  })
}

function handleResize() {
  chart?.resize()
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})

watch(() => props.data, updateChart)
</script>

<style scoped>
.chart-container {
  background: var(--bg-card);
  border-radius: var(--card-radius);
  padding: 20px;
  border: 1px solid var(--border-color);
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.chart-box {
  width: 100%;
  height: 350px;
}
</style>
