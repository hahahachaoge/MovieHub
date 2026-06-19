<template>
  <div class="payment-result-page page-container">
    <div v-if="processing" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>
    <div v-else class="result-card">
      <el-icon :size="64" :color="isSuccess ? '#2ecc71' : '#e50914'">
        <SuccessFilled v-if="isSuccess" />
        <WarningFilled v-else />
      </el-icon>
      <h2>{{ isSuccess ? '支付成功！' : '支付处理中' }}</h2>
      <p>{{ isSuccess ? 'VIP会员已开通，立即畅享所有精彩影视' : '请稍后查看支付状态' }}</p>
      <el-button type="primary" size="large" @click="router.push('/')" class="result-btn">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { SuccessFilled, WarningFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import request from '../api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const processing = ref(true)
const isSuccess = ref(true)

onMounted(async () => {
  // 支付宝可能传 out_trade_no 或 orderNo，都接受
  const orderNo = (route.query.orderNo || route.query.out_trade_no) as string
  if (orderNo) {
    try {
      const res = await request.post(`/payment/mock/${orderNo}`)
      if (res.data.code === 200) {
        isSuccess.value = true
        const newToken = res.data.data?.token
        if (newToken) {
          localStorage.setItem('token', newToken)
          userStore.token = newToken
        }
      }
    } catch (e) {
      console.error('模拟支付失败', e)
    }
  }
  // 刷新用户信息
  try {
    await userStore.fetchUserInfo()
    if (userStore.isVip) {
      ElMessage.success('VIP会员已激活！')
    }
  } catch (e) {
    console.error('刷新用户信息失败', e)
  } finally {
    processing.value = false
  }
})
</script>

<style scoped>
.payment-result-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}

.result-card {
  text-align: center;
  background: var(--bg-card);
  border-radius: var(--card-radius);
  padding: 48px;
  border: 1px solid var(--border-color);
  max-width: 480px;
}

.result-card h2 {
  font-size: 24px;
  margin: 16px 0 8px;
  color: var(--text-primary);
}

.result-card p {
  color: var(--text-secondary);
  margin-bottom: 24px;
}

.result-btn {
  width: 200px;
}
</style>