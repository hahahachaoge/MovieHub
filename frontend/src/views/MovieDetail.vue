<template>
  <div class="movie-detail-page">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <template v-else-if="movie">
      <!-- 顶部信息区 -->
      <div class="detail-hero" :style="{ backgroundImage: `url(${movie.coverUrl})` }">
        <div class="hero-overlay">
          <div class="hero-content page-container">
            <div class="hero-poster">
              <el-image :src="movie.coverUrl" fit="cover" class="poster-img">
                <template #error>
                  <div class="poster-error"><el-icon :size="48"><PictureFilled /></el-icon></div>
                </template>
              </el-image>
            </div>
            <div class="hero-info">
              <h1 class="movie-title">{{ movie.title }}</h1>
              <p v-if="movie.originalTitle" class="movie-original-title">{{ movie.originalTitle }}</p>

              <div class="movie-meta">
                <span class="meta-rating">
                  <el-icon :size="18" color="#ffd700"><StarFilled /></el-icon>
                  <span class="rating-value">{{ movie.rating }}</span>
                  <span class="rating-count">({{ movie.ratingCount }}人评分)</span>
                </span>
              </div>

              <div class="movie-info-tags">
                <span class="info-tag">{{ movie.region }}</span>
                <span class="info-divider">|</span>
                <span class="info-tag">{{ movie.releaseDate?.slice(0, 4) }}</span>
                <span class="info-divider">|</span>
                <span class="info-tag">{{ movie.duration }} 分钟</span>
                <span class="info-divider">|</span>
                <span class="info-tag">{{ movie.language }}</span>
              </div>

              <div class="category-tags">
                <el-tag v-for="cat in movie.categoryNames" :key="cat" class="cat-tag">{{ cat }}</el-tag>
              </div>

              <p class="movie-desc">{{ movie.description }}</p>

              <!-- 播放按钮 -->
              <div class="play-section">
                <el-button
                  v-if="!isVipRestricted"
                  type="primary"
                  size="large"
                  class="play-btn"
                  @click="handlePlay"
                >
                  <el-icon><VideoPlay /></el-icon>
                  立即播放
                </el-button>
                <el-button
                  v-else
                  type="warning"
                  size="large"
                  class="vip-play-btn"
                  @click="showVipDialog = true"
                >
                  <el-icon><Lock /></el-icon>
                  VIP专享
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="detail-body page-container">
        <!-- 主创信息 -->
        <div class="detail-section">
          <h2 class="section-title">🎬 主创团队</h2>
          <div class="crew-list">
            <div v-for="director in movie.directors" :key="director.id" class="crew-item" @click="goCrew(director.id)">
              <el-avatar :size="56" :src="director.avatar" class="crew-avatar"> {{ director.name[0] }} </el-avatar>
              <span class="crew-name">{{ director.name }}</span>
              <span class="crew-role">导演</span>
            </div>
            <div v-for="actor in movie.actors" :key="actor.id" class="crew-item" @click="goCrew(actor.id)">
              <el-avatar :size="56" :src="actor.avatar" class="crew-avatar"> {{ actor.name[0] }} </el-avatar>
              <span class="crew-name">{{ actor.name }}</span>
              <span class="crew-role" v-if="actor.characterName">饰 {{ actor.characterName }}</span>
            </div>
          </div>
        </div>

        <!-- 相关推荐 -->
        <div v-if="relatedMovies.length > 0" class="detail-section">
          <h2 class="section-title">🎯 相关推荐</h2>
          <div class="movie-grid">
            <MovieCard v-for="m in relatedMovies" :key="m.id" :movie="m" />
          </div>
        </div>

        <!-- 评论区域 -->
        <div class="detail-section">
          <h2 class="section-title">💬 用户评论</h2>
          <div v-if="userStore.isLoggedIn" class="review-form">
            <el-rate v-model="newRating" :max="5" show-text />
            <el-input v-model="newComment" type="textarea" :rows="3" placeholder="写下你的评论..." class="comment-input" />
            <el-button type="primary" @click="submitReview" :loading="reviewSubmitting">发表评论</el-button>
          </div>
          <div v-else class="review-login-tip">
            <el-button text @click="router.push('/login')">登录后即可评论</el-button>
          </div>
          <div v-if="reviews.length > 0" class="review-list">
            <div v-for="review in reviews" :key="review.id" class="review-item">
              <div class="review-header">
                <strong>{{ review.username || '匿名用户' }}</strong>
                <el-rate :model-value="review.rating" disabled :max="5" size="small" />
              </div>
              <p class="review-content">{{ review.content }}</p>
              <span class="review-time">{{ review.createTime?.slice(0, 10) }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无评论" :image-size="80" />
        </div>
      </div>
    </template>

    <el-empty v-else description="电影不存在" />

    <!-- VIP充值弹窗 -->
    <el-dialog v-model="showVipDialog" title="开通VIP会员" width="420px" class="vip-dialog">
      <div class="vip-dialog-content">
        <el-icon :size="48" color="#d4a017"><Lock /></el-icon>
        <h3>该影片为VIP专享</h3>
        <p>开通VIP会员即可观看所有VIP影片</p>
        <el-button type="warning" size="large" @click="goPayment" class="vip-go-btn">
          去开通VIP
        </el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { StarFilled, VideoPlay, Lock, PictureFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { getMovieDetail, getRelatedMovies } from '../api/movie'
import MovieCard from '../components/MovieCard.vue'
import request from '../api'
import type { MovieVO } from '../api/movie'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const movie = ref<MovieVO | null>(null)
const showVipDialog = ref(false)
const reviews = ref<any[]>([])
const newRating = ref(0)
const newComment = ref('')
const reviewSubmitting = ref(false)
const relatedMovies = ref<MovieVO[]>([])

const isVipRestricted = computed(() => {
  if (!movie.value?.isVip) return false
  return !userStore.isVip
})

async function loadMovie() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const res = await getMovieDetail(id)
    if (res.data.code === 200) {
      movie.value = res.data.data
      loadReviews()
      loadRelated()
    }
  } catch (e) {
    console.error('加载电影详情失败', e)
  } finally {
    loading.value = false
  }
}

async function loadReviews() {
  try {
    const res = await request.get(`/review/movie/${route.params.id}`)
    if (res.data.code === 200) {
      reviews.value = res.data.data
    }
  } catch (e) {
    console.error('加载评论失败', e)
  }
}

async function loadRelated() {
  try {
    const res = await getRelatedMovies(Number(route.params.id))
    if (res.data.code === 200) relatedMovies.value = res.data.data
  } catch (e) { console.error("加载相关推荐失败", e) }
}

function handlePlay() {
  if (!userStore.isLoggedIn) {
    router.push(`/login?redirect=${route.fullPath}`)
    return
  }
  router.push(`/play/${movie.value!.id}`)
}

async function submitReview() {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  reviewSubmitting.value = true
  try {
    const res = await request.post('/review/submit', {
      movieId: movie.value!.id,
      rating: newRating.value || null,
      content: newComment.value.trim()
    })
    if (res.data.code === 200) {
      ElMessage.success('评论成功')
      newComment.value = ''
      newRating.value = 0
      loadReviews()
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '评论失败')
  } finally {
    reviewSubmitting.value = false
  }
}

function goCrew(crewId: number) {
  router.push(`/crew/${crewId}`)
}

function goPayment() {
  showVipDialog.value = false
  router.push('/payment')
}

onMounted(loadMovie)

// 监听路由参数变化，当从相关推荐点击时重新加载
watch(() => route.params.id, () => {
  loadMovie()
})
</script>

<style scoped>
.detail-hero {
  min-height: 500px;
  background-size: cover;
  background-position: center;
  position: relative;
}

.hero-overlay {
  min-height: 500px;
  background: linear-gradient(to right, rgba(15, 15, 26, 0.95) 0%, rgba(15, 15, 26, 0.7) 100%);
  display: flex;
  align-items: center;
}

.hero-content {
  display: flex;
  gap: 40px;
  align-items: flex-start;
  padding-top: 40px;
  padding-bottom: 40px;
}

.hero-poster {
  flex-shrink: 0;
  width: 300px;
  border-radius: var(--card-radius);
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
}

.poster-img {
  width: 100%;
  aspect-ratio: 2/3;
}

.poster-error {
  width: 100%;
  aspect-ratio: 2/3;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-darker);
  color: var(--text-muted);
}

.hero-info {
  flex: 1;
}

.movie-title {
  font-size: 32px;
  font-weight: 800;
  margin-bottom: 4px;
}

.movie-original-title {
  color: var(--text-muted);
  font-size: 16px;
  margin-bottom: 12px;
}

.movie-meta {
  margin-bottom: 12px;
}

.meta-rating {
  display: flex;
  align-items: center;
  gap: 6px;
}

.rating-value {
  font-size: 24px;
  font-weight: 700;
  color: #ffd700;
}

.rating-count {
  color: var(--text-muted);
  font-size: 14px;
}

.movie-info-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  margin-bottom: 12px;
  font-size: 14px;
}

.info-divider {
  color: var(--text-muted);
}

.category-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 16px;
}

.cat-tag {
  --el-tag-bg-color: rgba(229, 9, 20, 0.1);
  --el-tag-border-color: transparent;
  --el-tag-text-color: var(--primary-color);
}

.movie-desc {
  color: var(--text-secondary);
  line-height: 1.8;
  font-size: 14px;
  margin-bottom: 24px;
}

.play-section {
  display: flex;
  gap: 12px;
}

.play-btn {
  font-size: 16px;
  font-weight: 600;
  padding: 12px 32px;
  border-radius: 8px;
}

.vip-play-btn {
  font-size: 16px;
  font-weight: 600;
  padding: 12px 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #d4a017, #f5d742);
  color: #1a1a2e;
  border: none;
}

.detail-body {
  padding-top: 32px;
  padding-bottom: 40px;
}

.detail-section {
  margin-bottom: 40px;
}

.crew-list {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.crew-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 12px;
  border-radius: var(--card-radius);
  transition: background 0.2s;
  min-width: 100px;
}

.crew-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.crew-avatar {
  background: var(--bg-darker);
}

.crew-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.crew-role {
  font-size: 12px;
  color: var(--text-muted);
}

.review-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
  padding: 20px;
  background: var(--bg-card);
  border-radius: var(--card-radius);
}

.comment-input {
  --el-input-bg-color: var(--bg-darker);
  --el-input-border-color: var(--border-color);
  --el-input-text-color: var(--text-primary);
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  padding: 16px;
  background: var(--bg-card);
  border-radius: var(--card-radius);
  border: 1px solid var(--border-color);
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.review-content {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.6;
}

.review-time {
  color: var(--text-muted);
  font-size: 12px;
}

.vip-dialog-content {
  text-align: center;
  padding: 20px;
}

.vip-dialog-content h3 {
  font-size: 20px;
  margin: 12px 0;
  color: var(--text-primary);
}

.vip-dialog-content p {
  color: var(--text-secondary);
  margin-bottom: 20px;
}

.vip-go-btn {
  width: 200px;
}

@media (max-width: 768px) {
  .hero-content {
    flex-direction: column;
    align-items: center;
  }

  .hero-poster {
    width: 200px;
  }

  .movie-title {
    font-size: 24px;
  }
}
</style>
