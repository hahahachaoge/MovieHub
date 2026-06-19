<template>
  <div class="movie-carousel">
    <el-carousel
      height="420px"
      :interval="4000"
      arrow="hover"
      indicator-position="none"
    >
      <el-carousel-item v-for="movie in movies" :key="movie.id">
        <div class="carousel-slide" :style="{ backgroundImage: `url(${movie.coverUrl})` }">
          <div class="carousel-overlay">
            <div class="carousel-content">
              <div class="carousel-tags">
                <el-tag v-if="movie.isVip" type="warning" size="small">VIP</el-tag>
                <el-tag
                  v-for="cat in movie.categoryNames?.slice(0, 2)"
                  :key="cat"
                  size="small"
                  class="carousel-tag"
                >
                  {{ cat }}
                </el-tag>
              </div>
              <h2 class="carousel-title">{{ movie.title }}</h2>
              <div class="carousel-meta">
                <span class="carousel-rating">
                  <el-icon :size="16" color="#ffd700"><StarFilled /></el-icon>
                  {{ movie.rating }}
                </span>
                <span class="meta-sep">|</span>
                <span>{{ movie.region }}</span>
                <span class="meta-sep">|</span>
                <span>{{ movie.duration }} 分钟</span>
              </div>
              <p class="carousel-desc">{{ movie.description?.slice(0, 80) }}...</p>
              <el-button type="primary" round class="carousel-btn" @click="goDetail(movie.id)">
                立即观看
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { StarFilled, ArrowRight } from '@element-plus/icons-vue'
import type { MovieVO } from '../api/movie'

defineProps<{
  movies: MovieVO[]
}>()

const router = useRouter()
function goDetail(id: number) {
  router.push(`/movie/${id}`)
}
</script>

<style scoped>
.movie-carousel {
  border-radius: var(--card-radius);
  overflow: hidden;
  margin-bottom: 32px;
}

.carousel-slide {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
}

.carousel-overlay {
  width: 100%;
  height: 100%;
  background: linear-gradient(to right, rgba(15, 15, 26, 0.95) 0%, rgba(15, 15, 26, 0.6) 50%, transparent 100%);
  display: flex;
  align-items: center;
}

.carousel-content {
  max-width: 550px;
  padding-left: calc((100vw - 1400px) / 2 + 20px);
}

.carousel-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 12px;
}

.carousel-tag {
  --el-tag-bg-color: rgba(255, 255, 255, 0.1);
  --el-tag-border-color: transparent;
  --el-tag-text-color: var(--text-primary);
}

.carousel-title {
  font-size: 36px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 12px;
  line-height: 1.2;
}

.carousel-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 16px;
}

.carousel-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #ffd700;
  font-weight: 600;
}

.meta-sep {
  color: var(--text-muted);
}

.carousel-desc {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 20px;
}

.carousel-btn {
  font-weight: 600;
}
</style>