<template>
  <div class="statistics-page page-container">
    <h1 class="section-title">📈 数据统计</h1>
    <p class="page-desc">电影评分分布与地区分布可视化分析</p>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="4" animated />
    </div>

    <template v-else>
      <div class="charts-grid">
        <MovieRatingChart :categories="ratingData.categories" :data="ratingData.data" />
        <MovieRegionChart :regions="regionData.regions" :data="regionData.data" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import MovieRatingChart from '../components/Charts/MovieRatingChart.vue'
import MovieRegionChart from '../components/Charts/MovieRegionChart.vue'
import request from '../api'

const loading = ref(true)
const ratingData = ref({ categories: [] as string[], data: [] as number[] })
const regionData = ref({ regions: [] as string[], data: [] as number[] })

onMounted(async () => {
  try {
    const [ratingRes, regionRes] = await Promise.all([
      request.get('/report/rating-distribution'),
      request.get('/report/region-distribution')
    ])
    if (ratingRes.data.code === 200) ratingData.value = ratingRes.data.data
    if (regionRes.data.code === 200) regionData.value = regionRes.data.data
  } catch (e) {
    console.error('统计数据加载失败', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.statistics-page {
  padding-top: 20px;
  padding-bottom: 40px;
}

.page-desc {
  color: var(--text-secondary);
  margin-bottom: 24px;
}

.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}
</style>
