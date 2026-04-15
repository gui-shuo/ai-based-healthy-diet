<template>
  <view class="admin-page">
    <view class="admin-header">
      <text class="admin-title">管理后台</text>
      <text class="admin-subtitle">数据概览与快捷管理</text>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-grid">
      <view class="stat-card" v-for="item in statCards" :key="item.label">
        <text class="stat-icon">{{ item.icon }}</text>
        <text class="stat-value">{{ item.value }}</text>
        <text class="stat-label">{{ item.label }}</text>
      </view>
    </view>

    <!-- 快捷管理入口 -->
    <view class="section">
      <text class="section-title">快捷管理</text>
      <view class="menu-grid">
        <view class="menu-item" v-for="item in menuItems" :key="item.path" @click="navigateTo(item.path)">
          <view class="menu-icon-wrap" :style="{ background: item.bg }">
            <text class="menu-icon">{{ item.icon }}</text>
          </view>
          <text class="menu-label">{{ item.label }}</text>
          <text class="menu-badge" v-if="item.badge">{{ item.badge }}</text>
        </view>
      </view>
    </view>

    <!-- 最近订单 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">最近订单</text>
        <text class="section-more" @click="navigateTo('/pages/admin/orders')">查看全部 ›</text>
      </view>
      <view v-if="recentOrders.length" class="order-list">
        <view class="order-item" v-for="order in recentOrders" :key="order.orderNo">
          <view class="order-top">
            <text class="order-no">{{ order.orderNo }}</text>
            <text class="order-status" :class="'status-' + order.status">{{ statusText(order.status) }}</text>
          </view>
          <view class="order-bottom">
            <text class="order-amount">¥{{ order.totalAmount }}</text>
            <text class="order-time">{{ formatTime(order.createdAt) }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty-hint">
        <text>暂无订单数据</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { adminDashboardApi, adminOrderApi } from '../../services/api'
import { formatTime } from '../../utils'

const stats = ref<any>({})
const recentOrders = ref<any[]>([])
const loading = ref(true)

const statCards = computed(() => [
  { icon: '👥', value: stats.value.totalUsers || 0, label: '总用户' },
  { icon: '📦', value: stats.value.totalOrders || 0, label: '总订单' },
  { icon: '🍽️', value: stats.value.totalMealPlans || 0, label: '营养餐' },
  { icon: '🛍️', value: stats.value.totalProducts || 0, label: '商品数' }
])

const menuItems = [
  { icon: '🍱', label: '营养餐管理', path: '/pages/admin/meals', bg: 'linear-gradient(135deg, #10B981, #34D399)' },
  { icon: '🏪', label: '产品管理', path: '/pages/admin/products', bg: 'linear-gradient(135deg, #3B82F6, #60A5FA)' },
  { icon: '📋', label: '订单管理', path: '/pages/admin/orders', bg: 'linear-gradient(135deg, #F59E0B, #FBBF24)' },
  { icon: '💬', label: '社区管理', path: '/pages/admin/community', bg: 'linear-gradient(135deg, #8B5CF6, #A78BFA)' },
  { icon: '📢', label: '公告管理', path: '/pages/admin/announcements', bg: 'linear-gradient(135deg, #EF4444, #F87171)' },
  { icon: '📝', label: '反馈管理', path: '/pages/admin/feedback', bg: 'linear-gradient(135deg, #06B6D4, #22D3EE)' }
]

const statusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING_PAYMENT: '待付款', PAID: '已付款', SHIPPED: '已发货',
    COMPLETED: '已完成', CANCELLED: '已取消', REFUNDED: '已退款'
  }
  return map[status] || status
}

const navigateTo = (path: string) => {
  uni.navigateTo({ url: path })
}

const loadData = async () => {
  loading.value = true
  try {
    const [statsRes, ordersRes] = await Promise.all([
      adminDashboardApi.getStats(),
      adminOrderApi.getList({ page: 1, size: 5 })
    ])
    if (statsRes.code === 200) stats.value = statsRes.data || {}
    if (ordersRes.code === 200) {
      recentOrders.value = (ordersRes.data?.content || ordersRes.data?.records || []).slice(0, 5)
    }
  } catch (e) {
    console.error('加载管理数据失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.admin-page { padding: 0 0 40rpx; background: #F5F7FA; min-height: 100vh; }
.admin-header {
  background: linear-gradient(135deg, #10B981, #059669);
  padding: 60rpx 32rpx 40rpx;
  .admin-title { font-size: 40rpx; font-weight: 700; color: #fff; display: block; }
  .admin-subtitle { font-size: 26rpx; color: rgba(255,255,255,0.8); margin-top: 8rpx; display: block; }
}
.stats-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 16rpx;
  padding: 0 24rpx; margin-top: -30rpx; position: relative; z-index: 1;
}
.stat-card {
  background: #fff; border-radius: 16rpx; padding: 20rpx 12rpx; text-align: center;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
  .stat-icon { font-size: 36rpx; display: block; }
  .stat-value { font-size: 36rpx; font-weight: 700; color: #0F172A; display: block; margin: 4rpx 0; }
  .stat-label { font-size: 22rpx; color: #94A3B8; display: block; }
}
.section { padding: 32rpx 24rpx 0; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.section-title { font-size: 32rpx; font-weight: 600; color: #0F172A; }
.section-more { font-size: 26rpx; color: #10B981; }
.menu-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 24rpx; }
.menu-item {
  display: flex; flex-direction: column; align-items: center; position: relative;
  .menu-icon-wrap {
    width: 96rpx; height: 96rpx; border-radius: 24rpx; display: flex;
    align-items: center; justify-content: center;
  }
  .menu-icon { font-size: 40rpx; }
  .menu-label { font-size: 26rpx; color: #334155; margin-top: 12rpx; }
  .menu-badge {
    position: absolute; top: -8rpx; right: 16rpx; background: #EF4444; color: #fff;
    font-size: 20rpx; padding: 2rpx 12rpx; border-radius: 20rpx; min-width: 32rpx; text-align: center;
  }
}
.order-list { background: #fff; border-radius: 16rpx; overflow: hidden; }
.order-item {
  padding: 24rpx; border-bottom: 1rpx solid #F1F5F9;
  &:last-child { border-bottom: none; }
}
.order-top { display: flex; justify-content: space-between; align-items: center; }
.order-no { font-size: 26rpx; color: #334155; font-weight: 500; }
.order-status { font-size: 24rpx; padding: 4rpx 16rpx; border-radius: 8rpx; }
.status-PAID { background: #DBEAFE; color: #2563EB; }
.status-SHIPPED { background: #FEF3C7; color: #D97706; }
.status-COMPLETED { background: #D1FAE5; color: #059669; }
.status-PENDING_PAYMENT { background: #FEE2E2; color: #DC2626; }
.status-CANCELLED { background: #F1F5F9; color: #64748B; }
.order-bottom { display: flex; justify-content: space-between; margin-top: 12rpx; }
.order-amount { font-size: 28rpx; color: #EF4444; font-weight: 600; }
.order-time { font-size: 24rpx; color: #94A3B8; }
.empty-hint { text-align: center; padding: 60rpx 0; color: #94A3B8; font-size: 28rpx; }
</style>
