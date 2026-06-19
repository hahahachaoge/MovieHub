<template>
  <div class="payment-page page-container">
    <h1 class="section-title">👑 升级VIP会员</h1>
    <p class="page-subtitle">开通VIP，畅享所有精彩影视内容</p>

    <!-- 当前VIP信息 -->
    <div v-if="userStore.isVip" class="vip-status-card">
      <span class="vip-icon">👑</span>
      <div class="vip-status-info">
        <span class="vip-status-title">VIP会员</span>
        <span class="vip-status-expire">有效期至：{{ userStore.userInfo?.vipExpireTime?.slice(0, 10) || '永久' }}</span>
      </div>
    </div>

    <!-- 套餐列表 -->
    <div class="product-grid">
      <div
        v-for="product in products"
        :key="product.id"
        class="product-card"
        :class="{ selected: selectedProduct === product.id }"
      >
        <div v-if="product.badge" class="product-badge">{{ product.badge }}</div>
        <h3 class="product-name">{{ product.name }}</h3>
        <div class="product-price">
          <span class="price-symbol">¥</span>
          <span class="price-value">{{ product.price }}</span>
        </div>
        <ul class="product-benefits">
          <li v-for="benefit in product.benefits" :key="benefit">
            <span v-if="product.disabledBenefits?.includes(benefit)">
              <span class="benefit-x">✕</span>
            </span>
            <span v-else>
              <span class="benefit-check">✓</span>
            </span>
            {{ benefit }}
          </li>
        </ul>
        <el-button
          :type="selectedProduct === product.id ? 'warning' : 'default'"
          round
          class="product-btn"
          @click="selectedProduct = product.id"
        >
          {{ selectedProduct === product.id ? '已选择' : '选择方案' }}
        </el-button>
      </div>
    </div>

    <div class="payment-action">
      <el-button
        type="warning"
        size="large"
        :loading="paying"
        :disabled="!selectedProduct"
        class="pay-btn"
        @click="handlePay"
      >
        {{ paying ? '处理中...' : '立即开通' }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { getProducts, createOrder } from '../api/payment'

const router = useRouter()
const userStore = useUserStore()
const products = ref<any[]>([])
const selectedProduct = ref('')
const paying = ref(false)

onMounted(async () => {
  try {
    const res = await getProducts()
    if (res.data.code === 200) products.value = res.data.data
  } catch (e) {
    console.error('获取套餐失败', e)
  }
})

async function handlePay() {
  if (!selectedProduct.value) return
  paying.value = true
  try {
    const res = await createOrder(selectedProduct.value)
    if (res.data.code === 200) {
      const payUrl = res.data.data.form
      // 直接跳转到支付宝支付页面（GET方式，参数已由后端UTF-8编码）
      window.location.href = payUrl
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '支付创建失败')
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.payment-page {
  padding-top: 20px;
  padding-bottom: 60px;
  text-align: center;
}

.page-subtitle {
  color: var(--text-secondary);
  margin-bottom: 32px;
}

.vip-status-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: linear-gradient(135deg, rgba(212, 160, 23, 0.15), rgba(245, 215, 66, 0.05));
  border: 1px solid var(--vip-gold);
  border-radius: var(--card-radius);
  padding: 16px 20px;
  max-width: 380px;
  margin: 0 auto 32px;
}

.vip-icon {
  font-size: 28px;
}

.vip-status-info {
  display: flex;
  flex-direction: column;
  text-align: left;
}

.vip-status-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--vip-gold);
}

.vip-status-expire {
  font-size: 12px;
  color: var(--text-secondary);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  max-width: 900px;
  margin: 0 auto 32px;
}

.product-card {
  background: var(--bg-card);
  border: 2px solid var(--border-color);
  border-radius: var(--card-radius);
  padding: 28px 20px;
  transition: all 0.3s;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.product-card:hover {
  transform: translateY(-4px);
}

.product-card.selected {
  border-color: var(--vip-gold);
  box-shadow: 0 8px 24px rgba(212, 160, 23, 0.2);
}

.product-badge {
  position: absolute;
  top: -12px;
  background: linear-gradient(135deg, #d4a017, #f5d742);
  color: #1a1a2e;
  padding: 2px 16px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 700;
}

.product-name {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 16px;
  color: var(--text-primary);
}

.product-price {
  margin-bottom: 20px;
}

.price-symbol {
  font-size: 20px;
  vertical-align: top;
  color: var(--vip-gold);
}

.price-value {
  font-size: 48px;
  font-weight: 800;
  color: var(--vip-gold);
}

.product-benefits {
  list-style: none;
  padding: 0;
  margin-bottom: 20px;
  text-align: left;
}

.product-benefits li {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 14px;
  padding: 5px 0;
  line-height: 1;
}

.benefit-x {
  color: #ff4d4f;
  font-size: 14px;
  font-weight: 700;
  opacity: 0.6;
  display: inline-block;
  width: 16px;
  text-align: center;
}

.benefit-check {
  color: #2ecc71;
  font-size: 14px;
  font-weight: 700;
  display: inline-block;
  width: 16px;
  text-align: center;
}

.product-btn {
  width: 100%;
}

.payment-action {
  display: flex;
  justify-content: center;
}

.pay-btn {
  width: 300px;
  font-size: 18px;
  font-weight: 600;
  height: 50px;
}

@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: 1fr;
    max-width: 360px;
  }
}
</style>