<template>
  <div class="movie-list-page page-container">
    <!-- 搜索框 -->
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索电影名称..."
        size="large"
        clearable
        :prefix-icon="Search"
        @keyup.enter="doSearch"
        @clear="clearSearch"
      >
        <template #append>
          <el-button @click="doSearch" :loading="loading">搜索</el-button>
        </template>
      </el-input>
      <div v-if="keyword && searched" class="search-info">
        搜索 "<strong>{{ keyword }}</strong>"，共找到 <strong>{{ total }}</strong> 部电影
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-section">
        <span class="filter-label">分类：</span>
        <div class="filter-tags">
          <el-tag
            :type="!currentCategoryId ? 'primary' : 'info'"
            class="filter-tag"
            @click="switchCategory(null)"
          >
            全部
          </el-tag>
          <el-tag
            v-for="cat in categories"
            :key="cat.id"
            :type="currentCategoryId === cat.id ? 'primary' : 'info'"
            class="filter-tag"
            @click="switchCategory(cat.id)"
          >
            {{ cat.name }}
          </el-tag>
        </div>
      </div>

      <div class="filter-section">
        <span class="filter-label">地区：</span>
        <el-select v-model="currentRegion" placeholder="全部地区" clearable @change="onRegionChange" class="region-select">
          <el-option
            v-for="region in regions"
            :key="region"
            :label="region"
            :value="region"
          />
        </el-select>
      </div>
    </div>

    <!-- 电影网格 -->
    <div v-if="!loading && movies.length > 0" class="movie-grid">
      <MovieCard v-for="movie in movies" :key="movie.id" :movie="movie" />
    </div>

    <!-- 空状态 -->
    <el-empty v-else-if="!loading" description="暂无相关电影" />

    <!-- 加载中 -->
    <div v-else class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 分页 -->
    <div v-if="total > size" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import MovieCard from '../components/MovieCard.vue'
import { filterMovies, getAllCategories, getAllRegions } from '../api/movie'
import type { MovieVO, Category } from '../api/movie'

const route = useRoute()

const loading = ref(true)
const movies = ref<MovieVO[]>([])
const categories = ref<Category[]>([])
const regions = ref<string[]>([])
const total = ref(0)
const currentPage = ref(1)
const size = ref(12)

const currentCategoryId = ref<number | null>(null)
const currentRegion = ref<string>('')
const keyword = ref('')
const searched = ref(false)

async function fetchMovies() {
  loading.value = true
  try {
    const params: any = { page: currentPage.value, size: size.value }
    if (currentCategoryId.value) params.categoryId = currentCategoryId.value
    if (currentRegion.value) params.region = currentRegion.value
    if (keyword.value) params.keyword = keyword.value

    const res = await filterMovies(currentCategoryId.value, currentRegion.value || undefined, currentPage.value, size.value, keyword.value || undefined)
    if (res.data.code === 200) {
      movies.value = res.data.data.records
      total.value = res.data.data.total
    }
  } catch (e) {
    console.error('加载失败', e)
  } finally {
    loading.value = false
  }
}

function doSearch() {
  if (!keyword.value.trim()) return
  searched.value = true
  currentPage.value = 1
  fetchMovies().then(() => {
    if (movies.value.length > 0) {
      const first = movies.value[0]
      if (first.categoryNames && first.categoryNames.length > 0) {
        const found = categories.value.find(c => c.name === first.categoryNames[0])
        if (found) currentCategoryId.value = found.id
      }
      if (first.region) currentRegion.value = first.region
    }
  })
}

function clearSearch() {
  keyword.value = ''
  searched.value = false
  currentPage.value = 1
  fetchMovies()
}

function switchCategory(id: number | null) {
  keyword.value = ''
  searched.value = false
  currentCategoryId.value = id
  currentPage.value = 1
  fetchMovies()
}

function onRegionChange(val: any) {
  keyword.value = ''
  searched.value = false
  currentRegion.value = val || ''
  currentPage.value = 1
  fetchMovies()
}

function onPageChange(page: number) {
  currentPage.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
  fetchMovies()
}

async function fetchFilterOptions() {
  try {
    const [catRes, regionRes] = await Promise.all([
      getAllCategories(),
      getAllRegions()
    ])
    if (catRes.data.code === 200) categories.value = catRes.data.data
    if (regionRes.data.code === 200) regions.value = regionRes.data.data
  } catch (e) {
    console.error('筛选选项加载失败', e)
  }
}

onMounted(async () => {
  const categoryId = route.query.categoryId
  const region = route.query.region as string
  const kw = route.query.keyword as string

  if (categoryId) currentCategoryId.value = Number(categoryId)
  if (region) currentRegion.value = region
  if (kw) { keyword.value = kw; searched.value = true }

  await fetchFilterOptions()
  await fetchMovies()
})
</script>

<style scoped>
.movie-list-page {
  padding-top: 20px;
  padding-bottom: 40px;
}

.search-bar {
  margin-bottom: 20px;
}

.search-info {
  color: var(--text-secondary);
  font-size: 14px;
  margin-top: 10px;
}

.search-info strong {
  color: var(--text-primary);
}

.filter-bar {
  background: var(--bg-card);
  border-radius: var(--card-radius);
  padding: 20px;
  margin-bottom: 24px;
  border: 1px solid var(--border-color);
}

.filter-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.filter-section:last-child {
  margin-bottom: 0;
}

.filter-label {
  color: var(--text-secondary);
  font-size: 14px;
  white-space: nowrap;
  min-width: 50px;
}

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-tag {
  cursor: pointer;
  padding: 0 12px;
  height: 28px;
  line-height: 28px;
  border-radius: 14px;
  transition: all 0.2s;
}

.region-select {
  width: 160px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
