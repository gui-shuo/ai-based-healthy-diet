<template>
  <view class="page">
    <view class="nav-bar">
      <view class="nav-bar-inner">
        <view class="nav-back pressable" @tap="goBack">
          <NutriIcon name="arrow-left" :size="36" color="#1F2937" />
        </view>
        <text class="nav-title">订单管理</text>
        <view class="nav-placeholder" />
      </view>
    </view>

    <view class="scroll-body" :style="{ paddingTop: navHeight + 'px' }">
      <!-- Tab Switch -->
      <view class="tab-bar">
        <view class="tab-item pressable" :class="{ active: activeTab === 'meal' }" @tap="activeTab = 'meal'">
          <text>营养餐订单</text>
        </view>
        <view class="tab-item pressable" :class="{ active: activeTab === 'product' }" @tap="activeTab = 'product'">
          <text>产品订单</text>
        </view>
      </view>

      <!-- Status Filter -->
      <scroll-view scroll-x class="status-bar" :show-scrollbar="false">
        <view class="status-chips">
          <view v-for="s in currentStatuses" :key="s.value" class="status-chip pressable"
            :class="{ active: statusFilter === s.value }" @tap="filterByStatus(s.value)">
            {{ s.label }}
          </view>
        </view>
      </scroll-view>

      <!-- Order List -->
      <scroll-view scroll-y class="order-scroll" @scrolltolower="loadMore" :lower-threshold="100">
        <view v-if="loading && orders.length === 0" class="loading-wrap">
          <text class="loading-text">加载中...</text>
        </view>
        <view v-else-if="orders.length === 0" class="empty-wrap">
          <NutriIcon name="receipt" :size="64" color="#D1D5DB" />
          <text class="empty-text">暂无订单</text>
        </view>
        <view v-else>
          <view v-for="order in orders" :key="order.orderNo" class="card order-card">
            <view class="order-header">
              <text class="order-no">{{ order.orderNo }}</text>
              <text class="order-status" :class="'s-' + (order.status || '').toLowerCase()">
                {{ orderStatusLabel(order.status) }}
              </text>
            </view>
            <view class="order-body">
              <text class="order-item-name">
                {{ activeTab === 'meal' ? (order.mealItemName || order.itemName || '营养餐') : (order.productName || order.itemName || '产品') }}
              </text>
              <view class="order-detail-row">
                <text class="order-amount">¥{{ order.totalAmount || order.payAmount }}</text>
                <text class="order-qty">x{{ order.quantity || 1 }}</text>
                <text class="order-time">{{ formatTime(order.createdAt) }}</text>
              </view>
            </view>
            <!-- Actions -->
            <view class="order-actions" v-if="canOperate(order)">
              <template v-if="activeTab === 'meal'">
                <button v-if="order.status === 'PAID'" class="action-btn-sm confirm" @tap="handleMealAction(order, 'PREPARING')">开始制作</button>
                <button v-if="order.status === 'PREPARING'" class="action-btn-sm confirm" @tap="handleMealAction(order, 'READY')">待取餐</button>
                <button v-if="order.status === 'READY'" class="action-btn-sm confirm" @tap="handleMealAction(order, 'COMPLETED')">已取餐</button>
              </template>
              <template v-else>
                <button v-if="order.status === 'PAID'" class="action-btn-sm confirm" @tap="showShipSheet(order)">发货</button>
                <button v-if="order.status === 'SHIPPED'" class="action-btn-sm confirm" @tap="handleProductAction(order, 'DELIVERED')">已送达</button>
              </template>
            </view>
          </view>
        </view>
        <view v-if="loading && orders.length > 0" class="loading-more">
          <text>加载更多...</text>
        </view>
        <view class="safe-bottom" />
      </scroll-view>
    </view>

    <!-- Ship BottomSheet -->
    <BottomSheet v-model="showShipping" title="发货">
      <view class="form-group">
        <text class="form-label">快递公司</text>
        <input class="form-input" v-model="shipForm.shippingCompany" placeholder="如：顺丰、圆通" :adjust-position="true" />
      </view>
      <view class="form-group">
        <text class="form-label">快递单号</text>
        <input class="form-input" v-model="shipForm.trackingNo" placeholder="请输入快递单号" :adjust-position="true" />
      </view>
      <template #footer>
        <button class="btn-primary sheet-save-btn" :loading="shipping" @tap="handleShip">确认发货</button>
      </template>
    </BottomSheet>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, reactive, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { adminApi } from '@/services/api'
import NutriIcon from '@/components/NutriIcon.vue'
import BottomSheet from '@/components/BottomSheet.vue'

const navHeight = computed(() => {
  const sysInfo = uni.getSystemInfoSync()
  return (sysInfo.statusBarHeight || 44) + 44
})

const activeTab = ref('meal')
const statusFilter = ref('')
const orders = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(0)
const hasMore = ref(true)

const showShipping = ref(false)
const shipping = ref(false)
const currentOrderNo = ref('')
const shipForm = reactive({ shippingCompany: '', trackingNo: '' })

const mealStatuses = [
  { value: '', label: '全部' },
  { value: 'PAID', label: '已支付' },
  { value: 'PREPARING', label: '制作中' },
  { value: 'READY', label: '待取餐' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' }
]

const productStatuses = [
  { value: '', label: '全部' },
  { value: 'PAID', label: '已支付' },
  { value: 'SHIPPED', label: '已发货' },
  { value: 'DELIVERED', label: '已送达' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' }
]

const currentStatuses = computed(() => activeTab.value === 'meal' ? mealStatuses : productStatuses)

function goBack() { uni.navigateBack() }

function orderStatusLabel(s: string) {
  const all = [...mealStatuses, ...productStatuses]
  return all.find(x => x.value === s)?.label || s
}

function canOperate(order: any) {
  if (activeTab.value === 'meal') {
    return ['PAID', 'PREPARING', 'READY'].includes(order.status)
  }
  return ['PAID', 'SHIPPED'].includes(order.status)
}

function formatTime(t: string) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

async function loadOrders(reset = false) {
  if (loading.value) return
  if (reset) { currentPage.value = 0; hasMore.value = true; orders.value = [] }
  if (!hasMore.value) return
  loading.value = true
  try {
    const params: any = { page: currentPage.value, size: 15 }
    if (statusFilter.value) params.status = statusFilter.value
    const res = activeTab.value === 'meal'
      ? await adminApi.getMealOrders(params)
      : await adminApi.getProductOrders(params)
    if (res.code === 200) {
      const page = res.data
      const list = page?.content || []
      if (reset) orders.value = list
      else orders.value = [...orders.value, ...list]
      hasMore.value = !page?.last
      currentPage.value++
    }
  } catch {} finally { loading.value = false }
}

function filterByStatus(s: string) {
  statusFilter.value = s
  loadOrders(true)
}

function loadMore() { loadOrders() }

async function handleMealAction(order: any, status: string) {
  try {
    const res = await adminApi.updateMealOrderStatus(order.orderNo, status)
    if (res.code === 200) {
      order.status = status
      uni.showToast({ title: '操作成功', icon: 'success' })
    }
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

function showShipSheet(order: any) {
  currentOrderNo.value = order.orderNo
  shipForm.shippingCompany = ''
  shipForm.trackingNo = ''
  showShipping.value = true
}

async function handleShip() {
  if (!shipForm.trackingNo.trim()) {
    uni.showToast({ title: '请输入快递单号', icon: 'none' })
    return
  }
  shipping.value = true
  try {
    const res = await adminApi.shipProductOrder(currentOrderNo.value, {
      shippingCompany: shipForm.shippingCompany,
      trackingNo: shipForm.trackingNo
    })
    if (res.code === 200) {
      uni.showToast({ title: '发货成功', icon: 'success' })
      showShipping.value = false
      loadOrders(true)
    }
  } catch {
    uni.showToast({ title: '发货失败', icon: 'none' })
  } finally { shipping.value = false }
}

async function handleProductAction(order: any, status: string) {
  try {
    const res = await adminApi.updateProductOrderStatus(order.orderNo, status)
    if (res.code === 200) {
      order.status = status
      uni.showToast({ title: '操作成功', icon: 'success' })
    }
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

watch(activeTab, () => {
  statusFilter.value = ''
  loadOrders(true)
})

onShow(() => { loadOrders(true) })
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: #F5F7FA; display: flex; flex-direction: column; }
.nav-bar {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100; background: #fff;
  .nav-bar-inner {
    display: flex; align-items: center; justify-content: space-between;
    height: 44px; padding: 0 24rpx; margin-top: var(--status-bar-height, 44px);
  }
  .nav-back { padding: 12rpx; }
  .nav-title { font-size: 34rpx; font-weight: 600; color: #1F2937; }
  .nav-placeholder { width: 60rpx; }
}
.scroll-body { flex: 1; display: flex; flex-direction: column; }
.tab-bar {
  display: flex; background: #fff; padding: 0 24rpx; border-bottom: 1px solid #F3F4F6;
  .tab-item {
    flex: 1; text-align: center; padding: 24rpx 0; font-size: 28rpx; color: #6B7280;
    position: relative;
    &.active {
      color: #10B981; font-weight: 600;
      &::after {
        content: ''; position: absolute; bottom: 0; left: 30%; right: 30%;
        height: 4rpx; background: #10B981; border-radius: 2rpx;
      }
    }
  }
}
.status-bar { padding: 16rpx 24rpx; white-space: nowrap; }
.status-chips { display: inline-flex; gap: 12rpx; }
.status-chip {
  font-size: 24rpx; padding: 8rpx 20rpx; border-radius: 20rpx;
  background: #fff; color: #6B7280; flex-shrink: 0;
  &.active { background: #10B981; color: #fff; }
}
.order-scroll { flex: 1; padding: 0 24rpx; }
.loading-wrap, .empty-wrap {
  display: flex; flex-direction: column; align-items: center; padding: 120rpx 0; gap: 16rpx;
  .loading-text, .empty-text { font-size: 26rpx; color: #9CA3AF; }
}
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.order-card {
  .order-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx;
    .order-no { font-size: 24rpx; color: #6B7280; }
    .order-status {
      font-size: 22rpx; padding: 4rpx 12rpx; border-radius: 8rpx;
      &.s-paid { background: #FEF3C7; color: #D97706; }
      &.s-preparing { background: rgba(59,130,246,0.1); color: #3B82F6; }
      &.s-ready { background: rgba(16,185,129,0.1); color: #10B981; }
      &.s-shipped { background: rgba(59,130,246,0.1); color: #3B82F6; }
      &.s-delivered { background: rgba(16,185,129,0.1); color: #10B981; }
      &.s-completed { background: #F3F4F6; color: #6B7280; }
      &.s-cancelled { background: rgba(239,68,68,0.08); color: #EF4444; }
    }
  }
  .order-body {
    .order-item-name { font-size: 28rpx; font-weight: 500; color: #1F2937; display: block; margin-bottom: 8rpx; }
    .order-detail-row { display: flex; align-items: center; gap: 16rpx; }
    .order-amount { font-size: 28rpx; color: #EF4444; font-weight: 600; }
    .order-qty { font-size: 24rpx; color: #9CA3AF; }
    .order-time { font-size: 22rpx; color: #9CA3AF; margin-left: auto; }
  }
  .order-actions {
    display: flex; justify-content: flex-end; gap: 16rpx; margin-top: 16rpx; padding-top: 16rpx;
    border-top: 1px solid #F3F4F6;
  }
}
.action-btn-sm {
  font-size: 24rpx; padding: 8rpx 24rpx; border-radius: 8rpx; border: none;
  &.confirm { background: #10B981; color: #fff; }
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
.loading-more { text-align: center; padding: 24rpx; font-size: 24rpx; color: #9CA3AF; }
.pressable { &:active { opacity: 0.7; } }
.safe-bottom { height: 60rpx; }
</style>
