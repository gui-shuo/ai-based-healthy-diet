<template>
  <view class="page">
    <!-- Custom Nav Bar -->
    <view class="nav-bar">
      <view class="nav-bar-inner">
        <view class="nav-back pressable" @tap="goBack">
          <NutriIcon name="arrow-left" :size="36" color="#1F2937" />
        </view>
        <text class="nav-title">商家管理</text>
        <view class="nav-placeholder" />
      </view>
    </view>

    <scroll-view scroll-y class="scroll-body" :style="{ paddingTop: navHeight + 'px' }">
      <!-- Stats Overview -->
      <view class="stats-grid">
        <view class="stat-card" style="background: linear-gradient(135deg, #10B981 0%, #059669 100%)">
          <text class="stat-value">{{ mealOrderStats.totalOrders || 0 }}</text>
          <text class="stat-label">营养餐订单</text>
          <NutriIcon name="receipt" :size="48" color="rgba(255,255,255,0.25)" />
        </view>
        <view class="stat-card" style="background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%)">
          <text class="stat-value">{{ productOrderStats.totalOrders || 0 }}</text>
          <text class="stat-label">产品订单</text>
          <NutriIcon name="shopping-bag" :size="48" color="rgba(255,255,255,0.25)" />
        </view>
        <view class="stat-card" style="background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%)">
          <text class="stat-value">¥{{ totalRevenue }}</text>
          <text class="stat-label">总营收</text>
          <NutriIcon name="credit-card" :size="48" color="rgba(255,255,255,0.25)" />
        </view>
        <view class="stat-card" style="background: linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%)">
          <text class="stat-value">{{ pendingCount }}</text>
          <text class="stat-label">待处理</text>
          <NutriIcon name="bell" :size="48" color="rgba(255,255,255,0.25)" />
        </view>
      </view>

      <!-- Quick Actions -->
      <view class="section-label">
        <NutriIcon name="sparkles" :size="28" color="#10B981" />
        <text>快捷操作</text>
      </view>
      <view class="quick-grid">
        <view class="quick-item pressable" @tap="navigateTo('/pages/merchant/meals')">
          <view class="quick-icon" style="background: rgba(16,185,129,0.1)">
            <NutriIcon name="utensils" :size="44" color="#10B981" />
          </view>
          <text class="quick-text">营养餐管理</text>
        </view>
        <view class="quick-item pressable" @tap="navigateTo('/pages/merchant/products')">
          <view class="quick-icon" style="background: rgba(59,130,246,0.1)">
            <NutriIcon name="shopping-bag" :size="44" color="#3B82F6" />
          </view>
          <text class="quick-text">产品管理</text>
        </view>
        <view class="quick-item pressable" @tap="navigateTo('/pages/merchant/orders')">
          <view class="quick-icon" style="background: rgba(139,92,246,0.1)">
            <NutriIcon name="receipt" :size="44" color="#8B5CF6" />
          </view>
          <text class="quick-text">订单管理</text>
        </view>
        <view class="quick-item pressable" @tap="showPickupSheet = true">
          <view class="quick-icon" style="background: rgba(245,158,11,0.1)">
            <NutriIcon name="scan" :size="44" color="#F59E0B" />
          </view>
          <text class="quick-text">核验取餐码</text>
        </view>
      </view>

      <!-- Pending Orders -->
      <view class="section-label">
        <NutriIcon name="hourglass" :size="28" color="#F59E0B" />
        <text>待处理订单</text>
        <text class="section-more pressable" @tap="navigateTo('/pages/merchant/orders')">查看全部</text>
      </view>
      <view v-if="pendingOrders.length === 0" class="card empty-card">
        <NutriIcon name="check-circle" :size="48" color="#10B981" />
        <text class="empty-text">暂无待处理订单</text>
      </view>
      <view v-else class="card">
        <view v-for="order in pendingOrders" :key="order.orderNo" class="order-row pressable">
          <view class="order-info">
            <text class="order-no">{{ order.orderNo }}</text>
            <text class="order-amount">¥{{ order.totalAmount || order.payAmount }}</text>
          </view>
          <view class="order-meta">
            <text class="order-status pending">{{ order.status === 'PAID' ? '待发货' : '待处理' }}</text>
            <text class="order-time">{{ formatTime(order.createdAt) }}</text>
          </view>
        </view>
      </view>

      <view class="safe-bottom" />
    </scroll-view>

    <!-- Pickup Code BottomSheet -->
    <BottomSheet v-model="showPickupSheet" title="核验取餐码">
      <view class="form-group">
        <text class="form-label">取餐码</text>
        <input class="form-input" v-model="pickupCode" placeholder="请输入取餐码" maxlength="8" :adjust-position="true" />
      </view>
      <template #footer>
        <button class="btn-primary sheet-save-btn" :loading="verifying" @tap="handleVerifyPickup">核验</button>
      </template>
    </BottomSheet>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { adminApi } from '@/services/api'
import NutriIcon from '@/components/NutriIcon.vue'
import BottomSheet from '@/components/BottomSheet.vue'

const navHeight = computed(() => {
  const sysInfo = uni.getSystemInfoSync()
  return (sysInfo.statusBarHeight || 44) + 44
})

const mealOrderStats = ref<any>({})
const productOrderStats = ref<any>({})
const pendingOrders = ref<any[]>([])
const showPickupSheet = ref(false)
const pickupCode = ref('')
const verifying = ref(false)

const totalRevenue = computed(() => {
  const m = Number(mealOrderStats.value.totalRevenue || 0)
  const p = Number(productOrderStats.value.totalRevenue || 0)
  return (m + p).toFixed(2)
})

const pendingCount = computed(() => {
  const m = Number(mealOrderStats.value.pendingCount || 0)
  const p = Number(productOrderStats.value.pendingCount || 0)
  return m + p
})

function goBack() {
  uni.navigateBack()
}

function navigateTo(url: string) {
  uni.navigateTo({ url })
}

function formatTime(t: string) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

async function loadStats() {
  try {
    const [mealRes, productRes] = await Promise.all([
      adminApi.getMealOrderStats(),
      adminApi.getProductOrderStats()
    ])
    if (mealRes.code === 200) mealOrderStats.value = mealRes.data || {}
    if (productRes.code === 200) productOrderStats.value = productRes.data || {}
  } catch {}
}

async function loadPendingOrders() {
  try {
    const [mealRes, productRes] = await Promise.all([
      adminApi.getMealOrders({ status: 'PAID', page: 0, size: 5 }),
      adminApi.getProductOrders({ status: 'PAID', page: 0, size: 5 })
    ])
    const meals = mealRes.code === 200 ? (mealRes.data?.content || []) : []
    const products = productRes.code === 200 ? (productRes.data?.content || []) : []
    pendingOrders.value = [...meals, ...products]
      .sort((a: any, b: any) => (b.createdAt || '').localeCompare(a.createdAt || ''))
      .slice(0, 10)
  } catch {}
}

async function handleVerifyPickup() {
  if (!pickupCode.value.trim()) {
    uni.showToast({ title: '请输入取餐码', icon: 'none' })
    return
  }
  verifying.value = true
  try {
    const res = await adminApi.verifyPickupCode(pickupCode.value.trim())
    if (res.code === 200) {
      uni.showToast({ title: '核验成功', icon: 'success' })
      showPickupSheet.value = false
      pickupCode.value = ''
      loadPendingOrders()
    } else {
      uni.showToast({ title: res.message || '核验失败', icon: 'none' })
    }
  } catch {
    uni.showToast({ title: '核验失败', icon: 'none' })
  } finally {
    verifying.value = false
  }
}

onShow(() => {
  loadStats()
  loadPendingOrders()
})
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: #F5F7FA; }
.nav-bar {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  background: #fff;
  .nav-bar-inner {
    display: flex; align-items: center; justify-content: space-between;
    height: 44px; padding: 0 24rpx;
    margin-top: var(--status-bar-height, 44px);
  }
  .nav-back { padding: 12rpx; }
  .nav-title { font-size: 34rpx; font-weight: 600; color: #1F2937; }
  .nav-placeholder { width: 60rpx; }
}
.scroll-body { padding: 24rpx 24rpx 0; }
.stats-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 20rpx; margin-bottom: 32rpx;
}
.stat-card {
  position: relative; border-radius: 20rpx; padding: 28rpx 24rpx; overflow: hidden;
  .stat-value { font-size: 40rpx; font-weight: 700; color: #fff; display: block; }
  .stat-label { font-size: 24rpx; color: rgba(255,255,255,0.8); display: block; margin-top: 8rpx; }
  :deep(.nutri-icon) { position: absolute; right: 16rpx; bottom: 16rpx; }
}
.section-label {
  display: flex; align-items: center; gap: 8rpx; margin: 28rpx 0 16rpx; font-size: 28rpx; font-weight: 600; color: #374151;
  .section-more { margin-left: auto; font-size: 24rpx; font-weight: 400; color: #10B981; }
}
.quick-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 16rpx; margin-bottom: 8rpx;
}
.quick-item {
  display: flex; flex-direction: column; align-items: center; gap: 12rpx;
  background: #fff; border-radius: 16rpx; padding: 24rpx 8rpx;
  .quick-icon { width: 80rpx; height: 80rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
  .quick-text { font-size: 22rpx; color: #4B5563; }
}
.card {
  background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx;
}
.empty-card {
  display: flex; flex-direction: column; align-items: center; gap: 16rpx; padding: 48rpx;
  .empty-text { font-size: 26rpx; color: #9CA3AF; }
}
.order-row {
  padding: 20rpx 0; border-bottom: 1px solid #F3F4F6;
  &:last-child { border-bottom: none; }
}
.order-info {
  display: flex; justify-content: space-between; align-items: center;
  .order-no { font-size: 26rpx; color: #374151; font-weight: 500; }
  .order-amount { font-size: 28rpx; color: #EF4444; font-weight: 600; }
}
.order-meta {
  display: flex; justify-content: space-between; margin-top: 8rpx;
  .order-status { font-size: 22rpx; padding: 4rpx 12rpx; border-radius: 8rpx;
    &.pending { background: #FEF3C7; color: #D97706; }
  }
  .order-time { font-size: 22rpx; color: #9CA3AF; }
}
.form-group { margin-bottom: 24rpx; }
.form-label { font-size: 26rpx; color: #374151; font-weight: 500; display: block; margin-bottom: 12rpx; }
.form-input {
  width: 100%; height: 80rpx; background: #F9FAFB; border: 1px solid #E5E7EB;
  border-radius: 12rpx; padding: 0 24rpx; font-size: 28rpx; box-sizing: border-box;
}
.btn-primary {
  width: 100%; height: 88rpx; background: #10B981; color: #fff;
  border-radius: 16rpx; font-size: 30rpx; font-weight: 600;
  display: flex; align-items: center; justify-content: center; border: none;
}
.sheet-save-btn { margin: 0; }
.pressable { &:active { opacity: 0.7; } }
.safe-bottom { height: 60rpx; }
</style>
