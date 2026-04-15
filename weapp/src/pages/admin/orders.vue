<template>
  <view class="page">
    <!-- 状态筛选 -->
    <scroll-view scroll-x class="tab-scroll">
      <view class="tab-item" :class="{ active: statusFilter === s.key }"
        v-for="s in statusTabs" :key="s.key" @click="statusFilter = s.key; loadList(true)">
        <text>{{ s.label }}</text>
      </view>
    </scroll-view>

    <!-- 搜索 -->
    <view class="search-bar">
      <input class="search-input" v-model="keyword" placeholder="搜索订单号..." @confirm="loadList(true)" />
    </view>

    <view v-if="list.length" class="order-list">
      <view class="order-card" v-for="order in list" :key="order.orderNo">
        <view class="order-header">
          <text class="order-no">{{ order.orderNo }}</text>
          <text class="order-status" :class="'s-' + order.status">{{ getStatusText(order.status) }}</text>
        </view>

        <view class="order-user" v-if="order.user">
          <text>买家: {{ order.user.nickname || order.user.username }}</text>
        </view>

        <view class="order-items" v-if="order.items?.length">
          <view class="oi" v-for="(item, idx) in order.items.slice(0, 2)" :key="idx">
            <text class="oi-name">{{ item.productName }}</text>
            <text class="oi-qty">x{{ item.quantity }}</text>
            <text class="oi-price">¥{{ item.price }}</text>
          </view>
          <text v-if="order.items.length > 2" class="oi-more">还有{{ order.items.length - 2 }}件商品...</text>
        </view>

        <view class="order-footer">
          <text class="order-total">合计: ¥{{ order.totalAmount }}</text>
          <text class="order-time">{{ formatTime(order.createdAt) }}</text>
        </view>

        <view class="order-actions" v-if="order.status === 'PAID'">
          <button class="act-btn ship" size="mini" @click="handleShip(order)">发货</button>
        </view>
      </view>
    </view>
    <view v-else-if="!loading" class="empty"><text>暂无订单</text></view>

    <view v-if="loading" class="loading-hint"><text>加载中...</text></view>
    <view v-else-if="noMore && list.length" class="loading-hint"><text>没有更多了</text></view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminOrderApi } from '../../services/api'
import { formatTime } from '../../utils'

const list = ref<any[]>([])
const keyword = ref('')
const statusFilter = ref('')
const page = ref(1)
const loading = ref(false)
const noMore = ref(false)

const statusTabs = [
  { key: '', label: '全部' },
  { key: 'PENDING_PAYMENT', label: '待付款' },
  { key: 'PAID', label: '待发货' },
  { key: 'SHIPPED', label: '已发货' },
  { key: 'COMPLETED', label: '已完成' },
  { key: 'CANCELLED', label: '已取消' }
]

const getStatusText = (s: string) => {
  const map: Record<string, string> = {
    PENDING_PAYMENT: '待付款', PAID: '待发货', SHIPPED: '已发货',
    COMPLETED: '已完成', CANCELLED: '已取消', REFUNDED: '已退款'
  }
  return map[s] || s
}

const loadList = async (reset = false) => {
  if (reset) { page.value = 1; noMore.value = false }
  loading.value = true
  try {
    const res = await adminOrderApi.getList({
      page: page.value, size: 10, status: statusFilter.value, orderNo: keyword.value
    })
    if (res.code === 200) {
      const items = res.data?.content || res.data?.records || res.data || []
      list.value = reset ? items : [...list.value, ...items]
      noMore.value = items.length < 10
    }
  } catch { uni.showToast({ title: '加载失败', icon: 'none' }) }
  finally { loading.value = false }
}

const handleShip = (order: any) => {
  uni.showModal({
    title: '确认发货',
    content: `确认发货订单 ${order.orderNo}？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await adminOrderApi.ship(order.orderNo, {})
          order.status = 'SHIPPED'
          uni.showToast({ title: '已发货', icon: 'success' })
        } catch { uni.showToast({ title: '操作失败', icon: 'none' }) }
      }
    }
  })
}

onMounted(() => loadList(true))
onReachBottom(() => { if (!noMore.value && !loading.value) { page.value++; loadList() } })
</script>

<style lang="scss" scoped>
.page { padding: 24rpx; background: #F5F7FA; min-height: 100vh; }
.tab-scroll { white-space: nowrap; margin-bottom: 16rpx; }
.tab-item {
  display: inline-block; padding: 14rpx 32rpx; margin-right: 16rpx; border-radius: 32rpx;
  background: #fff; font-size: 26rpx; color: #64748B; border: 1rpx solid #E2E8F0;
  &.active { background: #10B981; color: #fff; border-color: #10B981; }
}
.search-bar { margin-bottom: 20rpx; }
.search-input {
  height: 72rpx; background: #fff; border-radius: 12rpx; padding: 0 24rpx;
  font-size: 28rpx; border: 1rpx solid #E2E8F0; width: 100%; box-sizing: border-box;
}
.order-list { display: flex; flex-direction: column; gap: 20rpx; }
.order-card {
  background: #fff; border-radius: 16rpx; padding: 24rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.order-header { display: flex; justify-content: space-between; align-items: center; }
.order-no { font-size: 26rpx; color: #334155; font-weight: 500; }
.order-status {
  font-size: 24rpx; padding: 4rpx 16rpx; border-radius: 8rpx;
  &.s-PAID { background: #DBEAFE; color: #2563EB; }
  &.s-SHIPPED { background: #FEF3C7; color: #D97706; }
  &.s-COMPLETED { background: #D1FAE5; color: #059669; }
  &.s-PENDING_PAYMENT { background: #FEE2E2; color: #DC2626; }
  &.s-CANCELLED, &.s-REFUNDED { background: #F1F5F9; color: #64748B; }
}
.order-user { font-size: 24rpx; color: #94A3B8; margin-top: 12rpx; }
.order-items { margin-top: 16rpx; }
.oi {
  display: flex; align-items: center; gap: 12rpx; padding: 8rpx 0;
  .oi-name { flex: 1; font-size: 26rpx; color: #334155; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .oi-qty { font-size: 24rpx; color: #94A3B8; }
  .oi-price { font-size: 26rpx; color: #EF4444; }
}
.oi-more { font-size: 24rpx; color: #94A3B8; }
.order-footer {
  display: flex; justify-content: space-between; align-items: center;
  margin-top: 16rpx; padding-top: 16rpx; border-top: 1rpx solid #F1F5F9;
}
.order-total { font-size: 30rpx; color: #EF4444; font-weight: 600; }
.order-time { font-size: 24rpx; color: #94A3B8; }
.order-actions { margin-top: 16rpx; display: flex; justify-content: flex-end; gap: 16rpx; }
.act-btn {
  border-radius: 8rpx !important; font-size: 26rpx !important;
  &.ship { background: #10B981 !important; color: #fff !important; }
}
.empty { text-align: center; padding: 120rpx 0; color: #94A3B8; font-size: 28rpx; }
.loading-hint { text-align: center; padding: 32rpx; color: #94A3B8; font-size: 26rpx; }
</style>
