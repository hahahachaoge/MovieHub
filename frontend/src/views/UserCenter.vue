<template>
  <div class="user-center-page page-container">
    <h1 class="section-title">👤 个人中心</h1>

    <div class="user-grid">
      <!-- 用户信息卡片 -->
      <div class="user-card">
        <div class="user-card-header">
          <div class="avatar-wrapper">
            <el-avatar :size="72" :src="userStore.userInfo?.avatar" class="user-avatar">
            {{ userStore.userInfo?.username?.[0]?.toUpperCase() }}
          </el-avatar>
            <div class="avatar-edit-btn" @click="avatarDialogVisible = true">
              <el-icon><Edit /></el-icon>
            </div>
          </div>
          <div class="user-meta">
            <h2>{{ userStore.userInfo?.username }}</h2>
            <el-tag v-if="userStore.isAdmin" type="danger" size="large">管理员</el-tag>
            <el-tag v-else-if="userStore.isVip" type="warning">VIP会员</el-tag>
            <el-tag v-else type="info">普通用户</el-tag>
          </div>
        </div>
        <div class="user-details">
          <div class="detail-row"><span class="label">邮箱</span><span>{{ userStore.userInfo?.email || '未设置' }}</span></div>
          <div class="detail-row"><span class="label">手机</span><span>{{ userStore.userInfo?.phone || '未设置' }}</span></div>
          <div class="detail-row" v-if="userStore.isVip"><span class="label">VIP有效期</span><span class="vip-date">{{ userStore.userInfo?.vipExpireTime?.slice(0, 10) || '永久' }}</span></div>
          <div class="detail-row"><span class="label">注册时间</span><span>{{ userStore.userInfo?.createTime?.slice(0, 10) }}</span></div>
        </div>
        <div class="user-actions">
          <el-button type="warning" @click="router.push('/payment')" v-if="!userStore.isVip">开通VIP</el-button>
          <el-button @click="handleLogout">退出登录</el-button>
        </div>
      </div>

      <!-- 播放历史 -->

    <div class="history-card">
        <h3 class="history-title">📋 播放历史</h3>
        <div v-if="history.length > 0" class="history-list">
          <template v-for="(item, index) in (showAll ? history : history.slice(0, 3))" :key="item.id">
            <div class="history-item" @click="router.push(`/movie/${item.movieId}`)">
              <el-image :src="item.coverUrl" class="history-cover" fit="cover" lazy>
                <template #error><div class="cover-placeholder"><el-icon><PictureFilled /></el-icon></div></template>
              </el-image>
              <div class="history-info">
                <span class="history-movie-title">{{ item.movieTitle }}</span>
                <span class="history-date">{{ item.watchDate }}</span>
              </div>
              <el-icon><ArrowRight /></el-icon>
            </div>
          </template>
          <div v-if="history.length > 3" style="text-align:center;margin-top:8px">
            <el-button text size="small" @click="showAll = !showAll">{{ showAll ? '收起' : '展开全部 (' + history.length + '部)' }}</el-button>
          </div>
        </div>
        <el-empty v-if="history.length === 0" description="暂无播放记录" :image-size="60" />
      </div>
    </div>

    <!-- AI推荐 -->
    <div v-if="userStore.isLoggedIn && aiMovies.length > 0" class="ai-section" style="margin-top:32px">
      <h2 class="section-title">🤖 AI 为你推荐</h2>
      <div class="movie-grid" style="display:grid;grid-template-columns:repeat(auto-fill,minmax(200px,1fr));gap:20px">
        <MovieCard v-for="movie in aiMovies" :key="movie.id" :movie="movie" />
      </div>
    </div>
  <!-- 换头像弹窗 -->
  <el-dialog v-model="avatarDialogVisible" title="更换头像" width="420px">
    <div style="text-align:center;padding:20px">
      <el-avatar :size="120" :src="previewUrl" style="margin-bottom:16px;background:var(--bg-darker)">
        {{ userStore.userInfo?.username?.[0]?.toUpperCase() }}
      </el-avatar>
      <input ref="fileInputRef" type="file" accept="image/*" style="display:none" @change="onFileChange" />
      <el-button type="primary" size="large" @click="fileInputRef?.click()" style="margin-bottom:12px">
        <el-icon><Upload /></el-icon> 选择图片
      </el-button>
      <p style="color:var(--text-muted);font-size:13px;margin-bottom:16px">支持 JPG、PNG、GIF 格式</p>
      <template v-if="selectedFile">
        <el-button type="success" @click="saveAvatar" :loading="saving" size="large">确认上传</el-button>
      </template>
    </div>
  </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, PictureFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import MovieCard from '../components/MovieCard.vue'
import { getAiRecommendations } from '../api/ai'
import request from '../api'
import type { MovieVO } from '../api/movie'

const router = useRouter()
const userStore = useUserStore()
const history = ref<any[]>([])
const showAll = ref(false)
const avatarDialogVisible = ref(false)
const previewUrl = ref("")
const selectedFile = ref<File | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)
const aiMovies = ref<MovieVO[]>([])

async function loadHistory() {
  try {
    const res = await request.get('/movie/history')
    if (res.data.code === 200) history.value = res.data.data.records
  } catch (e) {
    console.error('加载播放历史失败', e)
  }
}

function handleLogout() {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    loadHistory()
    loadAiRecommendations()
  }
})

const saving = ref(false)

function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files && input.files[0]) {
    selectedFile.value = input.files[0]
    previewUrl.value = URL.createObjectURL(input.files[0])
  }
}

async function saveAvatar() {
  if (!selectedFile.value) {
    ElMessage.warning("请先选择图片")
    return
  }
  saving.value = true
  try {
    const formData = new FormData()
    formData.append("file", selectedFile.value)
    const res = await request.post("/upload/avatar", formData, {
      headers: { "Content-Type": "multipart/form-data" }
    })
    if (res.data.code === 200) {
      await userStore.updateUserInfo({ avatar: res.data.data })
      ElMessage.success("头像已更新")
      avatarDialogVisible.value = false
      selectedFile.value = null
      previewUrl.value = ""
    } else {
      ElMessage.error(res.data.message || "上传失败")
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || "上传失败")
  } finally {
    saving.value = false
  }
}

async function loadAiRecommendations() {
  try {
    const res = await getAiRecommendations(userStore.userInfo?.id)
    if (res.data.code === 200) aiMovies.value = res.data.data
  } catch (e) {}
}
</script>


<style scoped>
.user-center-page { padding-top:20px; padding-bottom:40px; }
.user-grid { display:grid; grid-template-columns:380px 1fr; gap:24px; }
.user-card { background:var(--bg-card); border-radius:var(--card-radius); padding:24px; border:1px solid var(--border-color); }
.avatar-wrapper { position:relative; display:inline-flex; }
.avatar-edit-btn { position:absolute; bottom:0; right:0; width:24px; height:24px; border-radius:50%; background:var(--primary-color); color:#fff; display:flex; align-items:center; justify-content:center; cursor:pointer; border:2px solid var(--bg-card); transition:transform 0.2s; }
.avatar-edit-btn:hover { transform:scale(1.1); }
.user-card-header { display:flex; align-items:center; gap:16px; margin-bottom:24px; padding-bottom:20px; border-bottom:1px solid var(--border-color); }
.user-avatar { background:var(--bg-darker); font-size:28px; font-weight:700; }
.user-meta h2 { font-size:20px; margin-bottom:4px; }
.detail-row { display:flex; justify-content:space-between; padding:10px 0; color:var(--text-secondary); font-size:14px; border-bottom:1px solid rgba(255,255,255,0.03); }
.detail-row .label { color:var(--text-muted); }
.vip-date { color:var(--vip-gold); font-weight:600; }
.user-actions { display:flex; gap:8px; margin-top:20px; }
.history-card { background:var(--bg-card); border-radius:var(--card-radius); padding:24px; border:1px solid var(--border-color); }
.history-title { font-size:18px; margin-bottom:16px; }
.history-list { display:flex; flex-direction:column; gap:8px; }
.history-item { display:flex; align-items:center; gap:12px; padding:8px; border-radius:8px; cursor:pointer; transition:background 0.2s; }
.history-item:hover { background:rgba(255,255,255,0.03); }
.history-cover { width:50px; height:70px; border-radius:4px; object-fit:cover; }
.cover-placeholder { width:50px; height:70px; display:flex; align-items:center; justify-content:center; background:var(--bg-darker); border-radius:4px; color:var(--text-muted); }
.history-info { flex:1; display:flex; flex-direction:column; gap:4px; }
.history-movie-title { font-weight:600; color:var(--text-primary); }
.history-date { font-size:12px; color:var(--text-muted); }
@media (max-width:768px) { .user-grid { grid-template-columns:1fr; } }
</style>
