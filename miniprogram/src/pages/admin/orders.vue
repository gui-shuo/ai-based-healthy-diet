<template>
  <view class="page">
    <!-- Header -->
    <view class="top-bar">
      <text class="top-title">订单管理</text>
    </view>

    <!-- Tabs -->
    <view class="tabs">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentTab === tab.value }"
        @tap="switchTab(tab.value)"
      >
        <text class="tab-text">{{ tab.label }}</text>
      </view>
    </view>

    <!-- Pickup Code Verification -->
    <view class="verify-section">
      <view class="verify-card">
        <text class="verify-title">🎫 取餐码核验</text>
        <view class="verify-row">
          <input
            class="verify-input"
            v-model="pickupCodeInput"
            type="number"
            placeholder="输入6位取餐码"
            maxlength="6"
            @confirm="verifyPickupCode"
          />
          <button class="verify-btn" :loading="verifying" @tap="verifyPickupCode">核验</button>
        </view>
        <view v-if="verifyResult" class="verify-result" :class="verifyResult.success ? 'result-success' : 'result-error'">
          <text>{{ verifyResult.message }}</text>
        </view>
      </view>
    </view>

    <!-- Loading -->
    <view v-if="loading" class="loading-wrap">
      <text class="loading-text">加载中...</text>
    </view>

    <!-- Empty -->
    <view v-else-if="!orders.length" class="empty-wrap">
      <text class="empty-icon">📦</text>
      <text class="empty-text">暂无订单</text>
    </view>

    <!-- Orders List -->
    <view v-else class="list">
      <view v-for="order in orders" :key="order.id" class="order-card">
        <view class="order-header" @tap="toggleExpand(order.id)">
          <view class="order-no-row">
            <text class="order-no">{{ order.orderNo }}</text>
            <view class="order-status" :class="getStatusClass(order.status)">
              {{ getStatusLabel(order.status) }}
            </view>
          </view>
          <text class="order-time">{{ formatTime(order.createdAt) }}</text>
        </view>

        <!-- Expanded Detail -->
        <view v-if="expandedId === order.id" class="order-detail">
          <view class="detail-row" v-if="order.username || order.userNickname">
            <text class="detail-label">用户</text>
            <text class="detail-value">{{ order.userNickname || order.username || '-' }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">商品</text>
            <text class="detail-value">{{ getItemsSummary(order) }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">总价</text>
            <text class="detail-value price">¥{{ order.totalAmount || order.totalPrice || '0.00' }}</text>
          </view>
          <view v-if="order.address || order.shippingAddress" class="detail-row">
            <text class="detail-label">地址</text>
            <text class="detail-value">{{ order.address || order.shippingAddress }}</text>
          </view>
          <view v-if="order.remark" class="detail-row">
            <text class="detail-label">备注</text>
            <text class="detail-value">{{ order.remark }}</text>
          </view>
          <view v-if="order.pickupCode" class="detail-row">
            <text class="detail-label">取餐码</text>
            <text class="detail-value pickup-code-display">{{ order.pickupCode }}</text>
          </view>
          <view v-if="order.fulfillmentType" class="detail-row">
            <text class="detail-label">方式</text>
            <text class="detail-value">{{ order.fulfillmentType === 'PICKUP' ? '自取' : '配送' }}</text>
          </view>

          <!-- Actions -->
          <view class="order-actions" v-if="canAction(order.status)">
            <view
              v-if="order.status === 'PAID' || order.status === 'PENDING_SHIP'"
              class="act-btn"
              @tap="updateStatus(order, 'SHIPPED')"
            >确认发货</view>
            <view
              v-if="order.status === 'SHIPPED'"
              class="act-btn"
              @tap="updateStatus(order, 'COMPLETED')"
            >确认完成</view>
            <view
              v-if="order.status === 'PENDING_PAY' || order.status === 'PENDING'"
              class="act-btn"
              @tap="updateStatus(order, 'CONFIRMED')"
            >确认订单</view>
          </view>
        </view>

        <!-- Collapsed summary -->
        <view v-else class="order-summary" @tap="toggleExpand(order.id)">
          <text class="summary-text">{{ getItemsSummary(order) }}</text>
          <text class="summary-price">¥{{ order.totalAmount || order.totalPrice || '0.00' }}</text>
        </view>
      </view>
    </view>

    <view class="safe-bottom" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

import { adminApi } from '@/services/api'

const tabs = [
  { label: '全部', value: '' },
  { label: '待付款', value: 'PENDING_PAY' },
  { label: '待发货', value: 'PAID' },
  { label: '已发货', value: 'SHIPPED' },
  { label: '已完成', value: 'COMPLETED' }
]

const currentTab = ref('')
const loading = ref(false)
const orders = ref<any[]>([])
const expandedId = ref<number | null>(null)
const pickupCodeInput = ref('')
const verifying = ref(false)
const verifyResult = ref<{ success: boolean; message: string } | null>(null)

const statusMap: Record<string, string> = {
  PENDING: '待确认',
  PENDING_PAY: '待付款',
  PAID: '待发货',
  PENDING_SHIP: '待发货',
  CONFIRMED: '已确认',
  SHIPPED: '已发货',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  REFUNDED: '已退款'
}

const statusClassMap: Record<string, string> = {
  PENDING: 'pending',
  PENDING_PAY: 'pending',
  PAID: 'paid',
  PENDING_SHIP: 'paid',
  CONFIRMED: 'paid',
  SHIPPED: 'shipped',
  COMPLETED: 'completed',
  CANCELLED: 'cancelled',
  REFUNDED: 'cancelled'
}

function checkPermission() {
  if (!userStore.isAdmin) {
    uni.showToast({ title: '无管理员权限', icon: 'none' })
    setTimeout(() => uni.navigateBack(), 300)
    return false
  }
  return true
}

function getStatusLabel(status: string) {
  return statusMap[status] || status
}

function getStatusClass(status: string) {
  return statusClassMap[status] || 'pending'
}

function canAction(status: string) {
  return ['PENDING', 'PENDING_PAY', 'PAID', 'PENDING_SHIP', 'SHIPPED'].includes(status)
}

function getItemsSummary(order: any) {
  if (order.itemsSummary) return order.itemsSummary
  if (Array.isArray(order.items) && order.items.length) {
    const names = order.items.map((i: any) => i.name || i.productName).filter(Boolean)
    if (names.length > 2) return names.slice(0, 2).join('、') + ` 等${names.length}件`
    return names.join('、') || '订单商品'
  }
  return '订单商品'
}

function formatTime(time: string) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

function toggleExpand(id: number) {
  expandedId.value = expandedId.value === id ? null : id
}

function switchTab(value: string) {
  currentTab.value = value
  loadOrders()
}

async function loadOrders() {
  loading.value = true
  try {
    const params: any = {}
    if (currentTab.value) params.status = currentTab.value
    const res = await adminApi.getMealOrders(params)
    if (res.code === 200) {
      const data = res.data as any
      orders.value = data?.content || data?.records || data?.list || (Array.isArray(data) ? data : [])
    }
  } catch {} finally {
    loading.value = false
  }
}

async function updateStatus(order: any, newStatus: string) {
  uni.showModal({
    title: '确认操作',
    content: `确定将订单状态更改为「${statusMap[newStatus] || newStatus}」？`,
    success: async ({ confirm }) => {
      if (!confirm) return
      try {
        const res = await adminApi.updateMealOrderStatus(order.orderNo, { status: newStatus })
        if (res.code === 200) {
          uni.showToast({ title: '操作成功', icon: 'success' })
          loadOrders()
        } else {
          uni.showToast({ title: (res as any).message || '操作失败', icon: 'none' })
        }
      } catch (e: any) {
        uni.showToast({ title: e?.message || '操作失败', icon: 'none' })
      }
    }
  })
}

async function verifyPickupCode() {
  if (!pickupCodeInput.value || pickupCodeInput.value.length !== 6) {
    uni.showToast({ title: '请输入6位取餐码', icon: 'none' })
    return
  }
  verifying.value = true
  verifyResult.value = null
  try {
    const res = await adminApi.verifyPickupCode(pickupCodeInput.value)
    if (res.code === 200) {
      const order = res.data as any
      verifyResult.value = {
        success: true,
        message: `✅ 核验成功！订单 ${order.orderNo}，${getItemsSummary(order)}，¥${order.totalAmount}`
      }
      pickupCodeInput.value = ''
      loadOrders()
    } else {
      verifyResult.value = { success: false, message: `❌ ${(res as any).message || '核验失败'}` }
    }
  } catch (e: any) {
    verifyResult.value = { success: false, message: `❌ ${e?.message || '核验失败'}` }
  } finally {
    verifying.value = false
  }
}

onShow(() => {
  if (checkPermission()) loadOrders()
})
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: $background;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  background: $card;
  border-bottom: 1rpx solid $border;
}

.top-title {
  font-size: 34rpx;
  font-weight: 700;
  color: $foreground;
}

.tabs {
  display: flex;
  background: $card;
  padding: 0 16rpx;
  border-bottom: 1rpx solid $border;
  position: sticky;
  top: 0;
  z-index: 10;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 24rpx 8rpx;
  position: relative;

  &.active .tab-text {
    color: $accent;
    font-weight: 600;
  }

  &.active::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 40rpx;
    height: 4rpx;
    border-radius: 2rpx;
    background: $accent;
  }
}

.tab-text {
  font-size: 26rpx;
  color: $muted-foreground;
}

.verify-section {
  padding: 20rpx 24rpx 0;
}

.verify-card {
  background: $card;
  border-radius: $radius-xl;
  padding: 24rpx;
  border: 1rpx solid rgba(16, 185, 129, 0.2);
  box-shadow: $shadow-sm;
}

.verify-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $foreground;
  display: block;
  margin-bottom: 16rpx;
}

.verify-row {
  display: flex;
  gap: 16rpx;
  align-items: center;
}

.verify-input {
  flex: 1;
  height: 80rpx;
  background: $muted;
  border: 1rpx solid $border;
  border-radius: $radius-lg;
  padding: 0 24rpx;
  font-size: 32rpx;
  font-family: 'JetBrains Mono', monospace;
  letter-spacing: 8rpx;
  text-align: center;
  color: $foreground;
}

.verify-btn {
  width: 160rpx;
  height: 80rpx;
  line-height: 80rpx;
  background: $accent;
  color: #fff;
  font-size: 28rpx;
  border-radius: $radius-lg;
  text-align: center;
  border: none;
  font-weight: 600;
}

.verify-result {
  margin-top: 16rpx;
  padding: 16rpx;
  border-radius: $radius-lg;
  font-size: 26rpx;
}

.result-success {
  background: rgba(16, 185, 129, 0.08);
  color: $accent;
}

.result-error {
  background: rgba(239, 68, 68, 0.08);
  color: $uni-error;
}

.pickup-code-display {
  font-family: 'JetBrains Mono', monospace;
  font-size: 32rpx;
  font-weight: 700;
  color: $accent;
  letter-spacing: 8rpx;
}

.loading-wrap, .empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;
  gap: 16rpx;
}

.loading-text, .empty-text {
  font-size: 28rpx;
  color: $muted-foreground;
}

.empty-icon {
  font-size: 80rpx;
}

.list {
  padding: 24rpx 32rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.order-card {
  background: $card;
  border-radius: $radius-xl;
  padding: 28rpx;
  box-shadow: $shadow-sm;
}

.order-header {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.order-no-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.order-no {
  font-size: 26rpx;
  font-weight: 600;
  color: $foreground;
  font-family: 'Inter', monospace;
}

.order-status {
  font-size: 22rpx;
  padding: 4rpx 14rpx;
  border-radius: $radius-full;

  &.pending {
    background: rgba(245, 158, 11, 0.1);
    color: #F59E0B;
  }

  &.paid {
    background: rgba(59, 130, 246, 0.1);
    color: #3B82F6;
  }

  &.shipped {
    background: rgba(16, 185, 129, 0.1);
    color: $accent;
  }

  &.completed {
    background: rgba(34, 197, 94, 0.1);
    color: #22C55E;
  }

  &.cancelled {
    background: rgba(100, 116, 139, 0.1);
    color: $muted-foreground;
  }
}

.order-time {
  font-size: 24rpx;
  color: $muted-foreground;
}

.order-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid $border;
}

.summary-text {
  font-size: 26rpx;
  color: $muted-foreground;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.summary-price {
  font-size: 28rpx;
  font-weight: 600;
  color: $accent;
  flex-shrink: 0;
  margin-left: 16rpx;
}

.order-detail {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid $border;
}

.detail-row {
  display: flex;
  padding: 10rpx 0;
  gap: 16rpx;
}

.detail-label {
  font-size: 26rpx;
  color: $muted-foreground;
  width: 80rpx;
  flex-shrink: 0;
}

.detail-value {
  font-size: 26rpx;
  color: $foreground;
  flex: 1;

  &.price {
    color: $accent;
    font-weight: 600;
  }
}

.order-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid $border;
}

.act-btn {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  font-size: 26rpx;
  font-weight: 600;
  color: #fff;
  background: $gradient-accent;
  border-radius: $radius-md;

  &:active {
    opacity: 0.85;
  }
}

.safe-bottom {
  height: env(safe-area-inset-bottom);
  padding-bottom: 40rpx;
}
</style>
