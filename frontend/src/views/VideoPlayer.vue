<template>
  <div class="video-player-page">
    <div class="video-header">
      <el-button text @click="router.back()" class="back-btn">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <span class="video-title" v-if="movie">{{ movie.title }}</span>
    </div>

    <div class="video-wrapper" v-if="trailerUrl">
    <div class="video-container" @mousemove="onMouseMove" @mouseleave="onMouseLeave">
      <video
        ref="videoRef"
        :src="trailerUrl"
        class="video-element"
        @click="togglePlay"
        @timeupdate="onTimeUpdate"
        @loadedmetadata="onLoaded"
        @ended="onEnded" @play="onPlay"
      ></video>

      <!-- 播放中遮罩 -->
      <div v-if="!playing && !ended" class="play-overlay" @click="togglePlay">
        <div class="play-btn-big">
          <el-icon :size="64" color="#fff"><VideoPlay /></el-icon>
        </div>
      </div>

      <!-- 结束遮罩 -->
      <div v-if="ended" class="play-overlay" @click="togglePlay">
        <div class="ended-box">
          <el-icon :size="48" color="#fff"><VideoPlay /></el-icon>
          <p>重新播放</p>
        </div>
      </div>

      <!-- 控制栏 -->
      <div class="controls" :class="{ 'controls-hidden': !showControls }">
        <div class="progress-bar" @click="seekVideo" @mousedown="startDragging">
          <div class="progress-track">
            <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
            <div class="progress-thumb" :style="{ left: progressPercent + '%' }"></div>
          </div>
        </div>

        <div class="controls-bottom">
          <div class="controls-left">
            <el-button text class="control-btn" @click="togglePlay">
              <el-icon :size="22"><VideoPause v-if="playing" /><VideoPlay v-else /></el-icon>
            </el-button>
            <span class="time-display">{{ currentTimeDisplay }} / {{ durationDisplay }}</span>
          </div>

          <div class="controls-right">
            <div class="volume-control">
              <el-button text class="control-btn" @click="toggleMute">
                <el-icon :size="20">
                  <MuteNotification v-if="muted" />
                  <Notification v-else />
                </el-icon>
              </el-button>
              <el-slider v-model="volume" :max="1" :step="0.01" class="volume-slider" @input="setVolume" />
            </div>
            <el-button text class="control-btn" @click="toggleFullscreen">
              <el-icon :size="20"><FullScreen /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>
    </div>

    <!-- 相关推荐 -->
    <div v-if="relatedMovies.length > 0" class="page-container" style="padding: 32px 20px;">
      <h2 class="section-title">🎯 相关推荐</h2>
      <div class="movie-grid" style="display:grid;grid-template-columns:repeat(auto-fill,minmax(200px,1fr));gap:20px;margin-top:16px">
        <MovieCard v-for="m in relatedMovies" :key="m.id" :movie="m" />
      </div>
    </div>

    <div v-else-if="!loading" class="no-video">
      <el-empty description="暂无视频资源" />
    </div>
    <div v-else class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, VideoPlay, VideoPause, Notification, MuteNotification, FullScreen } from '@element-plus/icons-vue'
import MovieCard from "../components/MovieCard.vue"
import request from '../api'
import { getMovieDetail, getRelatedMovies } from '../api/movie'
import type { MovieVO } from '../api/movie'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const movie = ref<MovieVO | null>(null)
const trailerUrl = ref('')
const relatedMovies = ref<MovieVO[]>([])
const videoRef = ref<HTMLVideoElement | null>(null)

const playing = ref(false)
const ended = ref(false)
const showControls = ref(true)
const muted = ref(false)
const volume = ref(1)
const currentTime = ref(0)
const duration = ref(0)
const progressPercent = ref(0)
let controlsTimer: any = null
let hideTimer: any = null

const currentTimeDisplay = computed(() => formatTime(currentTime.value))
const durationDisplay = computed(() => formatTime(duration.value))

function formatTime(s: number) {
  const m = Math.floor(s / 60)
  const sec = Math.floor(s % 60)
  return m + ':' + String(sec).padStart(2, '0')
}

onMounted(async () => {
  try {
    const id = Number(route.params.id)
    const res = await getMovieDetail(id)
    if (res.data.code === 200) {
      movie.value = res.data.data
      trailerUrl.value = `/api/movie/public/video/${id}`
      loadRelated()
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  if (controlsTimer) clearTimeout(controlsTimer)
  if (hideTimer) clearTimeout(hideTimer)
})

function togglePlay() {
  const video = videoRef.value
  if (!video) return
  if (video.paused || video.ended) {
    video.play()
    playing.value = true
    ended.value = false
  } else {
    video.pause()
    playing.value = false
  }
  showControlsNow()
}

function onTimeUpdate() {
  const video = videoRef.value
  if (!video) return
  currentTime.value = video.currentTime
  duration.value = video.duration || 0
  progressPercent.value = (video.currentTime / (video.duration || 1)) * 100
}

function onLoaded() {
  const video = videoRef.value
  if (video) duration.value = video.duration
}

async function loadRelated() {
  try {
    const res = await getRelatedMovies(Number(route.params.id))
    if (res.data.code === 200) relatedMovies.value = res.data.data
  } catch(e) {}
}

async function onPlay() {
  try {
    const token = localStorage.getItem("token")
    if (token) await request.post(`/movie/play/${route.params.id}`)
  } catch(e) {}
}

function onEnded() {
  playing.value = false
  ended.value = true
  showControlsNow()
}

function seekVideo(e: MouseEvent) {
  const bar = e.currentTarget as HTMLElement
  const rect = bar.getBoundingClientRect()
  const percent = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width))
  const video = videoRef.value
  if (video) {
    video.currentTime = percent * video.duration
  }
}

function startDragging(e: MouseEvent) {
  seekVideo(e)
}

function setVolume(val: number) {
  const video = videoRef.value
  if (video) {
    video.volume = val
    video.muted = val === 0
    muted.value = val === 0
  }
}

function toggleMute() {
  const video = videoRef.value
  if (video) {
    video.muted = !video.muted
    muted.value = video.muted
  }
}

function toggleFullscreen() {
  const container = document.querySelector('.video-container') as HTMLElement
  if (!container) return
  if (document.fullscreenElement) {
    document.exitFullscreen()
  } else {
    container.requestFullscreen()
  }
}

function onMouseMove() {
  showControlsNow()
}

function onMouseLeave() {
  if (playing.value) {
    scheduleHide()
  }
}

function showControlsNow() {
  showControls.value = true
  if (hideTimer) clearTimeout(hideTimer)
  if (playing.value) {
    hideTimer = setTimeout(() => {
      showControls.value = false
    }, 3000)
  }
}

function scheduleHide() {
  if (hideTimer) clearTimeout(hideTimer)
  hideTimer = setTimeout(() => {
    if (playing.value) showControls.value = false
  }, 3000)
}
</script>

<style scoped>
.video-player-page {
  min-height: 100vh;
  background: var(--bg-darker);
  display: flex;
  flex-direction: column;
}

.video-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 24px;
  background: rgba(0,0,0,0.5);
  z-index: 10;
}

.back-btn {
  color: var(--text-primary) !important;
  font-size: 15px;
}

.video-title {
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
}

.video-wrapper {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.video-container {
  position: relative;
  background: #000;
  cursor: pointer;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-element {
  width: 100%;
  height: 100%;
  outline: none;
  object-fit: contain;
}

.play-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0,0,0,0.3);
  z-index: 2;
}

.play-btn-big {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: rgba(229,9,20,0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
}

.play-btn-big:hover { transform: scale(1.1); }

.ended-box {
  text-align: center;
  color: #fff;
  cursor: pointer;
}

.ended-box p { margin-top: 8px; font-size: 14px; }

.controls {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.85));
  padding: 30px 20px 14px;
  transition: opacity 0.3s;
  z-index: 3;
}

.controls-hidden { opacity: 0; pointer-events: none; }

.progress-bar {
  padding: 8px 0;
  cursor: pointer;
}

.progress-track {
  height: 4px;
  background: rgba(255,255,255,0.2);
  border-radius: 2px;
  position: relative;
  transition: height 0.15s;
}

.progress-bar:hover .progress-track {
  height: 6px;
}

.progress-fill {
  height: 100%;
  background: var(--primary-color);
  border-radius: 2px;
  transition: width 0.1s;
}

.progress-thumb {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--primary-color);
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  opacity: 0;
  transition: opacity 0.15s;
}

.progress-bar:hover .progress-thumb {
  opacity: 1;
}

.controls-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 6px;
}

.controls-left, .controls-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.control-btn {
  color: #fff !important;
}

.time-display {
  color: #fff;
  font-size: 13px;
  font-family: monospace;
}

.volume-control {
  display: flex;
  align-items: center;
  gap: 4px;
}

.volume-slider {
  width: 80px;
  --el-slider-rail-bg-color: rgba(255,255,255,0.2);
  --el-slider-runway-bg-color: rgba(255,255,255,0.2);
  --el-slider-button-size: 14px;
}

.no-video, .loading-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
