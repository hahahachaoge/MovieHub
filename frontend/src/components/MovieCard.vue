<template>
  <div class="movie-card" @click="goDetail">
    <div class="card-poster">
      <el-image
        :src="movie.coverUrl"
        :alt="movie.title"
        fit="cover"
        class="poster-img"
        lazy
      >
        <template #error>
          <div class="poster-error">
            <el-icon :size="32"><PictureFilled /></el-icon>
          </div>
        </template>
      </el-image>

      <!-- VIP角标 -->
      <div v-if="movie.isVip" class="vip-badge">VIP</div>

      <!-- 评分 -->
      <div class="rating-badge">
        <el-icon :size="14" color="#ffd700"><StarFilled /></el-icon>
        <span>{{ movie.rating }}</span>
      </div>
    </div>

    <div class="card-info">
      <h3 class="card-title" :title="movie.title">{{ movie.title }}</h3>
      <div class="card-meta">
        <span class="meta-item">{{ movie.region }}</span>
        <span class="meta-divider">·</span>
        <span class="meta-item">{{ movie.duration }}分钟</span>
      </div>
      <div class="card-tags" v-if="movie.categoryNames?.length">
        <el-tag
          v-for="cat in movie.categoryNames.slice(0, 2)"
          :key="cat"
          size="small"
          class="category-tag"
        >
          {{ cat }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { StarFilled, PictureFilled } from '@element-plus/icons-vue'
import type { MovieVO } from '../api/movie'

const props = defineProps<{
  movie: MovieVO
}>()

const router = useRouter()

function goDetail() {
  router.push(`/movie/${props.movie.id}`)
}
</script>

<style scoped>
.movie-card {
  cursor: pointer;
  border-radius: var(--card-radius);
  overflow: hidden;
  background: var(--bg-card);
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.movie-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.3);
  border-color: var(--border-color);
}

.card-poster {
  position: relative;
  aspect-ratio: 2/3;
  overflow: hidden;
  background: var(--bg-darker);
}

.poster-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.movie-card:hover .poster-img {
  transform: scale(1.05);
}

.poster-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-darker);
  color: var(--text-muted);
}

.vip-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: linear-gradient(135deg, #d4a017, #f5d742);
  color: #1a1a2e;
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 4px;
  letter-spacing: 1px;
}

.rating-badge {
  position: absolute;
  bottom: 8px;
  left: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
  background: rgba(0, 0, 0, 0.7);
  color: #ffd700;
  font-size: 13px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
}

.card-info {
  padding: 12px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.card-meta {
  display: flex;
  align-items: center;
  color: var(--text-muted);
  font-size: 12px;
  margin-bottom: 6px;
}

.meta-divider {
  margin: 0 4px;
}

.card-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.category-tag {
  --el-tag-bg-color: rgba(229, 9, 20, 0.1);
  --el-tag-border-color: transparent;
  --el-tag-text-color: var(--primary-color);
  font-size: 11px;
  height: 22px;
  padding: 0 6px;
}
</style>
