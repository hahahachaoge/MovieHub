<template>
  <div class="crew-detail-page page-container">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <template v-else-if="crew">
      <div class="crew-header">
        <el-avatar :size="100" :src="crew.avatar" class="crew-avatar-large">
          {{ crew.name[0] }}
        </el-avatar>
        <div class="crew-header-info">
          <h1 class="crew-name-white">{{ crew.name }}</h1>
          <div class="role-tags">
            <el-tag v-if="hasDirectorMovies" type="success" style="margin-right:8px">导演</el-tag>
            <el-tag v-if="hasActorMovies" type="primary" style="margin-right:8px">演员</el-tag>
          </div>
          <p class="crew-bio">{{ crew.bio }}</p>
        </div>
      </div>

      <!-- 作品展示 -->
      <div class="section-header" style="margin-top: 32px;">
        <h2 class="section-title">🎬 影视作品</h2>
      </div>

      <!-- 切换按钮 -->
      <div class="tab-buttons">
        <button
          v-if="hasDirectorMovies"
          :class="['tab-btn', { active: activeTab === 'director' }]"
          @click="activeTab = 'director'"
        >
          🎬 导演作品（{{ directorMovies.length }}）
        </button>
        <button
          v-if="hasActorMovies"
          :class="['tab-btn', { active: activeTab === 'actor' }]"
          @click="activeTab = 'actor'"
        >
          🎭 参演电影（{{ actorMovies.length }}）
        </button>
      </div>

      <!-- 内容区 -->
      <div class="tab-content">
        <div v-if="activeTab === 'director' && hasDirectorMovies">
          <div v-if="directorMovies.length > 0" class="movie-grid">
            <MovieCard v-for="m in directorMovies" :key="m.id" :movie="m" />
          </div>
          <el-empty v-else description="暂无导演作品" :image-size="80" />
        </div>
        <div v-if="activeTab === 'actor' && hasActorMovies">
          <div v-if="actorMovies.length > 0" class="movie-grid">
            <MovieCard v-for="m in actorMovies" :key="m.id" :movie="m" />
          </div>
          <el-empty v-else description="暂无参演电影" :image-size="80" />
        </div>
      </div>
    </template>

    <el-empty v-else description="主创不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import MovieCard from '../components/MovieCard.vue'
import { getCrewDetail, getMoviesByRole } from '../api/crew'
import type { MovieVO } from '../api/movie'

const route = useRoute()

const loading = ref(true)
const crew = ref<any>(null)
const activeTab = ref('director')
const directorMovies = ref<MovieVO[]>([])
const actorMovies = ref<MovieVO[]>([])

const hasDirectorMovies = computed(() => directorMovies.value.length > 0)
const hasActorMovies = computed(() => actorMovies.value.length > 0)

onMounted(async () => {
  try {
    const id = Number(route.params.id)
    const [crewRes, moviesRes] = await Promise.all([
      getCrewDetail(id),
      getMoviesByRole(id)
    ])
    if (crewRes.data.code === 200) crew.value = crewRes.data.data
    if (moviesRes.data.code === 200) {
      directorMovies.value = moviesRes.data.data.directorMovies || []
      actorMovies.value = moviesRes.data.data.actorMovies || []
    }
    if (actorMovies.value.length > 0 && directorMovies.value.length === 0) {
      activeTab.value = 'actor'
    }
  } catch (e) {
    console.error('加载主创信息失败', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.crew-detail-page { padding-top: 20px; padding-bottom: 40px; }

.crew-header {
  display: flex; gap: 32px; align-items: flex-start;
  padding-bottom: 32px; border-bottom: 1px solid var(--border-color);
}

.crew-avatar-large { flex-shrink: 0; background: var(--bg-darker); }
.crew-header-info { flex: 1; }

.crew-name-white {
  font-size: 28px; font-weight: 700;
  color: var(--text-primary); margin: 0 0 8px 0;
}

.role-tags { margin-bottom: 4px; }

.crew-bio {
  color: var(--text-secondary); line-height: 1.6; margin-top: 12px;
}

.section-header { display: flex; justify-content: space-between; align-items: center; }

/* 切换按钮 */
.tab-buttons {
  display: flex;
  gap: 12px;
  margin: 16px 0 20px;
}

.tab-btn {
  padding: 10px 24px;
  font-size: 16px;
  font-weight: 600;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn:hover {
  border-color: var(--primary-color);
  color: var(--text-primary);
}

.tab-btn.active {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: #fff;
}

.tab-content {
  min-height: 100px;
}
</style>
