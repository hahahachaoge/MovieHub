<template>
  <div class="chart-container">
    <h3 class="chart-title">🌍 地区分布</h3>
    <div ref="chartRef" class="chart-box"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps<{
  regions: string[]
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
  const colors = ['#e50914', '#ff6b6b', '#ffd700', '#2ecc71', '#3498db', '#9b59b6']

  chart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}部 ({d}%)'
    },
    series: [{
      type: 'pie',
      radius: ['30%', '70%'],
      center: ['50%', '50%'],
      roseType: 'area',
      itemStyle: {
        borderRadius: 6
      },
      label: {
        color: '#b3b3b3',
        formatter: '{b}\n{d}%'
      },
      data: props.regions.map((name, i) => ({
        name,
        value: props.data[i],
        itemStyle: { color: colors[i % colors.length] }
      }))
    }],
    legend: {
      bottom: 0,
      textStyle: { color: '#b3b3b3' }
    }
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
  height: 400px;
}
</style>
