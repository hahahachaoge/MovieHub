<template>
  <div class="ranking-table">
    <el-table :data="items" style="width: 100%" v-loading="loading">
      <el-table-column label="排名" width="80" align="center">
        <template #default="{ $index }">
          <span v-if="$index < 3" class="rank-medal">
            {{ ['🥇', '🥈', '🥉'][$index] }}
          </span>
          <span v-else class="rank-number">#{{ $index + 1 }}</span>
        </template>
      </el-table-column>

      <el-table-column label="电影" min-width="200">
        <template #default="{ row }">
          <div class="movie-cell">
            <el-image
              :src="row.coverUrl"
              class="rank-cover"
              fit="cover"
              lazy
            >
              <template #error>
                <div class="cover-placeholder"><el-icon><PictureFilled /></el-icon></div>
              </template>
            </el-image>
            <span class="movie-title" @click="goDetail(row.movieId)">{{ row.title }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="地区" prop="region" width="100" />
      <el-table-column label="评分" width="100" align="center">
        <template #default="{ row }">
          <span class="rating-star">
            <el-icon :size="14" color="#ffd700"><StarFilled /></el-icon>
            {{ row.rating }}
          </span>
        </template>
      </el-table-column>

      <el-table-column :label="type === 'rating' ? '评分人数' : '播放量'" min-width="110" align="center">
        <template #default="{ row }">
          <span class="play-count">{{ row.playCount || 0 }}</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { StarFilled, PictureFilled } from '@element-plus/icons-vue'
import type { RankingVO } from '../api/ranking'

defineProps<{
  items: RankingVO[]
  loading: boolean
  type: 'playCount' | 'rating'
}>()

const router = useRouter()
function goDetail(movieId: number) {
  router.push(`/movie/${movieId}`)
}
</script>

<style scoped>
.ranking-table {
  background: var(--bg-card);
  border-radius: var(--card-radius);
  overflow: hidden;
}

.rank-medal {
  font-size: 22px;
}

.rank-number {
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 14px;
}

.movie-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}

.rank-cover {
  width: 40px;
  height: 56px;
  border-radius: 4px;
  object-fit: cover;
  flex-shrink: 0;
}

.cover-placeholder {
  width: 40px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-darker);
  border-radius: 4px;
  color: var(--text-muted);
  flex-shrink: 0;
}

.movie-title {
  color: var(--text-primary);
  font-weight: 500;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.2s;
}

.movie-title:hover {
  color: var(--primary-color);
}

.rating-star {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #ffd700;
  font-weight: 600;
}

.play-count {
  color: var(--text-secondary);
  font-size: 13px;
}
</style>