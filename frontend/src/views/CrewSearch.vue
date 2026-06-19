<template>
  <div class="crew-search-page page-container">
    <!-- Tab 切换 -->
    <div class="search-tabs">
      <el-radio-group v-model="searchMode" size="large">
        <el-radio-button label="crew">🎭 主创查询</el-radio-button>
        <el-radio-button label="ai">🤖 AI智能搜索</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 主创查询模式 -->
    <template v-if="searchMode === 'crew'">
      <p class="page-desc">按演员或导演名字搜索，查看其参与的电影作品</p>

      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="输入演员或导演姓名..."
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
      </div>

      <div v-if="searched" class="search-result">
        <div v-if="crewList.length > 0" class="crew-grid">
          <div
            v-for="person in crewList"
            :key="person.id"
            class="crew-card"
            @click="goCrewDetail(person.id)"
          >
            <el-avatar :size="60" :src="person.avatar" class="crew-avatar">
              {{ person.name[0] }}
            </el-avatar>
            <div class="crew-info">
              <div class="crew-name">{{ person.name }}</div>
              <el-tag :type="person.displayRole === 'DIRECTOR' ? 'success' : person.displayRole === 'BOTH' ? 'warning' : 'primary'" size="small">
                {{ person.displayRole === 'BOTH' ? '导演&演员' : person.displayRole === 'DIRECTOR' ? '导演' : '演员' }}
              </el-tag>
            </div>
            <el-icon class="crew-arrow"><ArrowRight /></el-icon>
          </div>
        </div>
        <el-empty v-else description="未找到相关主创，请尝试其他关键词" />
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
      <div v-else class="empty-hint">
        <el-empty description="输入姓名搜索主创人员" />
      </div>
    </template>

    <!-- AI智能搜索模式 -->
    <template v-if="searchMode === 'ai'">
      <p class="page-desc">用自然语言搜索电影，AI 帮您找到最合适的影片</p>

      <div class="search-bar">
        <el-input
          v-model="aiQuery"
          placeholder="例如：推荐一部国产科幻片..."
          size="large"
          clearable
          :prefix-icon="Search"
          @keyup.enter="doAiSearch"
        >
          <template #append>
            <el-button type="danger" @click="doAiSearch" :loading="aiLoading">AI搜索</el-button>
          </template>
        </el-input>
      </div>

      <div v-if="aiSearched" class="ai-result">
        <div v-if="aiNote" class="ai-note">
          <el-alert :title="aiNote" type="success" :closable="false" show-icon />
        </div>
        <div v-if="aiMovies.length > 0" class="movie-grid">
          <MovieCard v-for="movie in aiMovies" :key="movie.id" :movie="movie" />
        </div>
      </div>
      <div v-if="!aiSearched" class="empty-hint">
        <el-empty description="输入关键词，AI 帮你智能搜索电影" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, ArrowRight } from '@element-plus/icons-vue'
import MovieCard from '../components/MovieCard.vue'
import { searchCrew } from '../api/crew'
import { aiSearch } from '../api/ai'
import type { CrewVO } from '../api/crew'
import type { MovieVO } from '../api/movie'

const router = useRouter()
const searchMode = ref('crew')

// 主创搜索
const keyword = ref('')
const searched = ref(false)
const loading = ref(false)
const crewList = ref<CrewVO[]>([])
const total = ref(0)
const size = ref(10)
const currentPage = ref(1)

// AI搜索
const aiQuery = ref('')
const aiSearched = ref(false)
const aiLoading = ref(false)
const aiMovies = ref<MovieVO[]>([])
const aiNote = ref('')

function goCrewDetail(crewId: number) {
  router.push(`/crew/${crewId}`)
}

async function doSearch() {
  if (!keyword.value.trim()) return
  loading.value = true
  searched.value = true
  try {
    const res = await searchCrew(keyword.value.trim(), currentPage.value, size.value)
    if (res.data.code === 200) {
      crewList.value = res.data.data.records
      total.value = res.data.data.total
    }
  } catch (e) {
    console.error('搜索失败', e)
  } finally {
    loading.value = false
  }
}

function clearSearch() {
  searched.value = false
  crewList.value = []
}

function onPageChange(page: number) {
  currentPage.value = page
  doSearch()
}

async function doAiSearch() {
  if (!aiQuery.value.trim()) return
  aiLoading.value = true
  aiSearched.value = true
  aiMovies.value = []
  aiNote.value = ''
  try {
    const res = await aiSearch(aiQuery.value.trim())
    if (res.data.code === 200 && res.data.data) {
      aiMovies.value = res.data.data.records || []
      aiNote.value = res.data.data.aiNote || ''
      console.log('AI搜索结果:', aiMovies.value.length, '部电影')
    }
  } catch (e: any) {
    console.error('AI搜索失败', e)
    if (e.response?.data?.data?.records) {
      aiMovies.value = e.response.data.data.records
    }
  } finally {
    aiLoading.value = false
  }
}

</script>

<style scoped>
.crew-search-page {
  padding-top: 20px;
  padding-bottom: 40px;
}

.search-tabs {
  text-align: center;
  margin-bottom: 24px;
}

.page-desc {
  color: var(--text-secondary);
  margin-bottom: 24px;
  text-align: center;
}

.search-bar {
  max-width: 600px;
  margin: 0 auto 32px;
}

:deep(.el-input-group__append) {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

:deep(.el-input-group__append .el-button) {
  color: white;
}

.ai-result {
  margin-top: 20px;
}

.ai-note {
  margin-bottom: 24px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.search-result {
  max-width: 600px;
  margin: 0 auto;
}

.crew-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.crew-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--bg-card);
  border-radius: var(--card-radius);
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid var(--border-color);
}

.crew-card:hover {
  border-color: var(--primary-color);
  background: rgba(229, 9, 20, 0.05);
}

.crew-avatar {
  flex-shrink: 0;
  background: var(--bg-darker);
}

.crew-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.crew-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.crew-arrow {
  color: var(--text-muted);
  opacity: 0;
  transition: opacity 0.2s;
}

.crew-card:hover .crew-arrow {
  opacity: 1;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.empty-hint {
  margin-top: 60px;
}
</style>
