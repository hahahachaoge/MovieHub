<template>
  <div class="home-page">
    <!-- 轮播图（指定5部） -->
    <MovieCarousel :movies="carouselMovies" />

    <div class="page-container">
      <!-- 热播推荐 -->
      <section class="movie-section">
        <div class="section-header">
          <h2 class="section-title">🔥 热播推荐</h2>
          <router-link to="/rankings" class="view-more">查看更多 <el-icon><ArrowRight /></el-icon></router-link>
        </div>
        <div class="movie-grid">
          <MovieCard v-for="movie in hotMovies.slice(0, 12)" :key="movie.id" :movie="movie" />
        </div>
      </section>

      <!-- 分类浏览 -->
      <section class="movie-section">
        <div class="section-header">
          <h2 class="section-title">📂 分类浏览</h2>
        </div>
        <div class="category-grid">
          <div
            v-for="cat in categories"
            :key="cat.id"
            class="category-card"
            @click="router.push(`/movies?categoryId=${cat.id}`)"
          >
            <span class="category-icon">{{ categoryIcons[cat.id] || '🎬' }}</span>
            <span class="category-name">{{ cat.name }}</span>
            <span class="category-arrow"><el-icon><ArrowRight /></el-icon></span>
          </div>
        </div>
      </section>

      <!-- AI推荐 -->
      <section v-if="userStore.isLoggedIn && aiMovies.length > 0" class="movie-section ai-section">
        <div class="section-header">
          <h2 class="section-title">🤖 AI 为你推荐</h2>
        </div>
        <div class="movie-grid">
          <MovieCard v-for="movie in aiMovies" :key="movie.id" :movie="movie" />
        </div>
      </section>

      <!-- 最新上线 -->
      <section class="movie-section">
        <div class="section-header">
          <h2 class="section-title">✨ 最新上线</h2>
          <router-link to="/movies" class="view-more">查看更多 <el-icon><ArrowRight /></el-icon></router-link>
        </div>
        <div class="movie-grid">
          <MovieCard v-for="movie in latestMovies" :key="movie.id" :movie="movie" />
        </div>
      </section>

      <!-- 加载骨架屏 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import MovieCard from '../components/MovieCard.vue'
import MovieCarousel from '../components/MovieCarousel.vue'
import { getHotMovies, getLatestMovies, getAllCategories, getCarouselMovies } from '../api/movie'
import { getAiRecommendations } from '../api/ai'
import { useUserStore } from '../stores/user'
import type { MovieVO, Category } from '../api/movie'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const hotMovies = ref<MovieVO[]>([])
const latestMovies = ref<MovieVO[]>([])
const categories = ref<Category[]>([])
const carouselMovies = ref<MovieVO[]>([])
const aiMovies = ref<MovieVO[]>([])

const categoryIcons: Record<number, string> = {
  1: '💥',
  2: '😂',
  3: '💕',
  4: '🚀',
  5: '🎨',
  6: '🔍'
}

onMounted(async () => {
  try {
    const [carouselRes, hotRes, latestRes, catRes] = await Promise.all([
      getCarouselMovies(),
      getHotMovies(12),
      getLatestMovies(6),
      getAllCategories()
    ])
    if (carouselRes.data.code === 200) carouselMovies.value = carouselRes.data.data
    if (hotRes.data.code === 200) hotMovies.value = hotRes.data.data
    if (latestRes.data.code === 200) latestMovies.value = latestRes.data.data
    if (catRes.data.code === 200) categories.value = catRes.data.data

    // 已登录用户加载AI推荐
    if (userStore.isLoggedIn) {
      try {
        const aiRes = await getAiRecommendations(userStore.userInfo?.id)
        if (aiRes.data.code === 200) aiMovies.value = aiRes.data.data
      } catch (e) { /* AI推荐失败不影响首页 */ }
    }
  } catch (e) {
    console.error('首页数据加载失败', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.home-page {
  padding-bottom: 40px;
}

.movie-section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.view-more {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text-secondary);
  font-size: 14px;
  transition: color 0.2s;
}

.view-more:hover {
  color: var(--primary-color);
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.category-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: var(--bg-card);
  border-radius: var(--card-radius);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid var(--border-color);
}

.category-card:hover {
  transform: translateY(-2px);
  border-color: var(--primary-color);
  background: rgba(229, 9, 20, 0.05);
}

.category-icon {
  font-size: 28px;
}

.category-name {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.category-arrow {
  color: var(--text-muted);
  opacity: 0;
  transition: opacity 0.2s;
}

.category-card:hover .category-arrow {
  opacity: 1;
}

@media (max-width: 768px) {
  .category-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 10px;
  }

  .category-card {
    padding: 12px;
  }

  .category-icon {
    font-size: 22px;
  }

  .category-name {
    font-size: 14px;
  }
}
</style>
