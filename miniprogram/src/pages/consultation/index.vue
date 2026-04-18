<template>
  <view class="consultation-page">
    <!-- Disclaimer -->
    <view class="disclaimer-bar" v-if="showDisclaimer">
      <text class="disclaimer-text">
        <NutriIcon name="heart" :size="22" color="#10B981" /> 咨询服务仅供参考，不构成医疗诊断。如有疾病请及时就医。
      </text>
      <text class="dismiss-btn" @tap="showDisclaimer = false">✕</text>
    </view>

    <!-- Tab Bar -->
    <view class="tab-bar">
      <view
        class="tab-item"
        :class="{ active: activeTab === 'find' }"
        @tap="activeTab = 'find'"
      >找营养师</view>
      <view
        class="tab-item"
        :class="{ active: activeTab === 'mine' }"
        @tap="switchToMine"
      >我的咨询</view>
    </view>

    <!-- ===== Tab: 找营养师 ===== -->
    <scroll-view
      v-show="activeTab === 'find'"
      scroll-y
      class="tab-content"
      refresher-enabled
      :refresher-triggered="refreshingFind"
      @refresherrefresh="onRefreshFind"
    >
      <!-- Active Consultations Banner -->
      <view class="active-banner" v-if="activeOrders.length > 0">
        <view class="active-banner-label">
          <NutriIcon name="message" :size="24" color="#10B981" />
          <text class="abl-text">进行中的咨询</text>
        </view>
        <view
          class="active-banner-card"
          v-for="item in activeOrders"
          :key="item.orderNo"
          @tap="goChat(item.orderNo)"
        >
          <image class="abc-avatar" :src="defaultAvatar(item.nutritionistAvatar || item.avatar)" mode="aspectFill" />
          <view class="abc-info">
            <text class="abc-name">{{ item.nutritionistName }}</text>
            <text class="abc-desc">{{ item.description || '营养咨询进行中' }}</text>
          </view>
          <view class="abc-btn">继续咨询</view>
        </view>
      </view>

      <!-- Status Filter Sub-tabs -->
      <view class="filter-bar">
        <view
          v-for="f in statusFilters"
          :key="f.value"
          class="filter-chip"
          :class="{ active: statusFilter === f.value }"
          @tap="statusFilter = f.value"
        >{{ f.label }}</view>
      </view>

      <!-- Nutritionist Grid (2-col) -->
      <view class="nutri-grid" v-if="filteredNutritionists.length > 0">
        <view
          class="nutri-card"
          v-for="n in filteredNutritionists"
          :key="n.id"
          @tap="showDetail(n)"
        >
          <view class="nc-header">
            <view class="nc-avatar-wrap">
              <image class="nc-avatar" :src="defaultAvatar(n.avatar)" mode="aspectFill" />
              <view class="nc-status-dot" :class="statusDotClass(n.status)"></view>
            </view>
            <view class="nc-online-badge" :class="statusDotClass(n.status)">
              {{ statusTextMap[n.status] || '离线' }}
            </view>
          </view>

          <text class="nc-name">{{ n.name }}</text>
          <text class="nc-title">{{ n.title }}</text>

          <text class="nc-bio" v-if="n.introduction">{{ n.introduction }}</text>

          <view class="nc-tags" v-if="n.specialties && n.specialties.length">
            <text class="nc-tag" v-for="(s, i) in (n.specialties || []).slice(0, 3)" :key="i">{{ s }}</text>
          </view>

          <view class="nc-stats">
            <view class="nc-stat">
              <text class="nc-stat-val">{{ n.experienceYears || 0 }}年</text>
              <text class="nc-stat-lbl">经验</text>
            </view>
            <view class="nc-stat-div"></view>
            <view class="nc-stat">
              <text class="nc-stat-val">
                <NutriIcon name="star" :size="20" color="#F59E0B" />
                {{ formatRating(n.rating) }}
              </text>
              <text class="nc-stat-lbl">评分</text>
            </view>
            <view class="nc-stat-div"></view>
            <view class="nc-stat">
              <text class="nc-stat-val">{{ n.consultationCount || 0 }}</text>
              <text class="nc-stat-lbl">咨询</text>
            </view>
          </view>

          <view class="nc-footer">
            <text class="nc-price">¥{{ formatFee(n.consultationFee) }}<text class="nc-price-unit">/次</text></text>
            <view
              class="nc-book-btn"
              :class="{ disabled: n.status === 'OFFLINE' }"
              @tap.stop="openBooking(n)"
            >{{ n.status === 'OFFLINE' ? '离线' : '预约' }}</view>
          </view>
        </view>
      </view>

      <view class="empty-state" v-if="!listLoading && filteredNutritionists.length === 0">
        <NutriIcon name="users" :size="64" color="#D1D5DB" />
        <text class="empty-text">{{ statusFilter === 'ALL' ? '暂无营养师' : '当前没有' + currentFilterLabel + '的营养师' }}</text>
      </view>

      <view class="loading-bar" v-if="listLoading">
        <text class="loading-text">加载中...</text>
      </view>
    </scroll-view>

    <!-- ===== Tab: 我的咨询 ===== -->
    <scroll-view
      v-show="activeTab === 'mine'"
      scroll-y
      class="tab-content"
      refresher-enabled
      :refresher-triggered="refreshingMine"
      @refresherrefresh="onRefreshMine"
    >
      <!-- Active consultations -->
      <view v-if="activeOrders.length > 0" class="mine-section">
        <text class="mine-section-title">进行中</text>
        <view class="active-order-card" v-for="item in activeOrders" :key="item.orderNo">
          <view class="aoc-top">
            <image class="aoc-avatar" :src="defaultAvatar(item.nutritionistAvatar || item.avatar)" mode="aspectFill" />
            <view class="aoc-info">
              <text class="aoc-name">{{ item.nutritionistName }}</text>
              <view class="aoc-badge">咨询中</view>
            </view>
          </view>
          <text class="aoc-desc" v-if="item.description">{{ item.description }}</text>
          <view class="aoc-chat-btn" @tap="goChat(item.orderNo)">
            <NutriIcon name="message" :size="22" color="#FFFFFF" /> 继续咨询
          </view>
        </view>
      </view>

      <!-- Order history -->
      <view class="mine-section">
        <text class="mine-section-title">咨询记录</text>

        <view v-if="orders.length > 0" class="order-list">
          <view class="order-card" v-for="order in orders" :key="order.orderNo">
            <view class="oc-top">
              <text class="oc-no">{{ order.orderNo }}</text>
              <view class="oc-status" :class="orderStatusClass(order)">
                {{ orderStatusText(order) }}
              </view>
            </view>
            <view class="oc-row">
              <text class="oc-label">营养师</text>
              <text class="oc-value">{{ order.nutritionistName }}</text>
            </view>
            <view class="oc-row">
              <text class="oc-label">金额</text>
              <text class="oc-value oc-amount">¥{{ (order.amount || 0).toFixed(2) }}</text>
            </view>
            <view class="oc-row">
              <text class="oc-label">时间</text>
              <text class="oc-value">{{ formatTime(order.createdAt) }}</text>
            </view>
            <view class="oc-row" v-if="order.description">
              <text class="oc-label">描述</text>
              <text class="oc-value oc-desc-val">{{ order.description }}</text>
            </view>

            <view class="oc-actions" v-if="hasActions(order)">
              <view v-if="order.paymentStatus === 'PENDING'" class="oc-act-btn pay" @tap="handlePay(order)">去支付</view>
              <view v-if="order.status === 'IN_PROGRESS'" class="oc-act-btn chat" @tap="goChat(order.orderNo)">
                <NutriIcon name="message" :size="20" color="#FFFFFF" /> 聊天
              </view>
              <view v-if="canRefund(order)" class="oc-act-btn refund" @tap="handleRefund(order)">退款</view>
            </view>
          </view>
        </view>

        <view class="empty-state" v-if="!ordersLoading && orders.length === 0">
          <NutriIcon name="clipboard" :size="64" color="#D1D5DB" />
          <text class="empty-text">暂无咨询记录</text>
        </view>

        <view class="loading-bar" v-if="ordersLoading">
          <text class="loading-text">加载中...</text>
        </view>
      </view>
    </scroll-view>

    <!-- ===== Nutritionist Detail Modal ===== -->
    <view class="modal-mask" v-if="showDetailModal" @tap="showDetailModal = false">
      <view class="modal-sheet" @tap.stop>
        <view class="modal-handle"></view>
        <view class="modal-hdr">
          <text class="modal-hdr-title">营养师详情</text>
          <text class="modal-hdr-close" @tap="showDetailModal = false">✕</text>
        </view>

        <scroll-view class="modal-scroll" scroll-y v-if="detailNutritionist">
          <view class="dt-profile">
            <image class="dt-avatar" :src="defaultAvatar(detailNutritionist.avatar)" mode="aspectFill" />
            <view class="dt-meta">
              <view class="dt-name-row">
                <text class="dt-name">{{ detailNutritionist.name }}</text>
                <view class="dt-status-badge" :class="statusDotClass(detailNutritionist.status)">
                  {{ statusTextMap[detailNutritionist.status] || '离线' }}
                </view>
              </view>
              <text class="dt-title">{{ detailNutritionist.title }}</text>
              <view class="dt-rating-row">
                <NutriIcon name="star" :size="20" color="#F59E0B" /> {{ formatRating(detailNutritionist.rating) }}
                <text class="dt-sep">·</text>
                <text class="dt-exp">{{ detailNutritionist.experienceYears || 0 }}年经验</text>
                <text class="dt-sep">·</text>
                <text class="dt-count">{{ detailNutritionist.consultationCount || 0 }}次咨询</text>
              </view>
            </view>
          </view>

          <view class="dt-block" v-if="detailNutritionist.introduction">
            <text class="dt-block-label">简介</text>
            <text class="dt-block-text">{{ detailNutritionist.introduction }}</text>
          </view>

          <view class="dt-block" v-if="detailNutritionist.specialties && detailNutritionist.specialties.length">
            <text class="dt-block-label">擅长方向</text>
            <view class="dt-tags">
              <text class="nc-tag" v-for="(s, i) in detailNutritionist.specialties" :key="i">{{ s }}</text>
            </view>
          </view>

          <view class="dt-price-row">
            <text class="dt-price-label">咨询费用</text>
            <text class="dt-price-val">¥{{ formatFee(detailNutritionist.consultationFee) }}/次</text>
          </view>
        </scroll-view>

        <view class="modal-foot">
          <view
            class="modal-foot-btn"
            :class="{ disabled: detailNutritionist?.status === 'OFFLINE' }"
            @tap="openBookingFromDetail"
          >{{ detailNutritionist?.status === 'OFFLINE' ? '当前离线，无法预约' : '立即预约咨询' }}</view>
        </view>
      </view>
    </view>

    <!-- ===== Booking Modal ===== -->
    <view class="modal-mask" v-if="showBookingModal" @tap="showBookingModal = false">
      <view class="modal-sheet" @tap.stop>
        <view class="modal-handle"></view>
        <view class="modal-hdr">
          <text class="modal-hdr-title">预约咨询</text>
          <text class="modal-hdr-close" @tap="showBookingModal = false">✕</text>
        </view>

        <view class="bk-body" v-if="bookingNutritionist">
          <view class="bk-nutri">
            <image class="bk-avatar" :src="defaultAvatar(bookingNutritionist.avatar)" mode="aspectFill" />
            <view class="bk-nutri-info">
              <text class="bk-nutri-name">{{ bookingNutritionist.name }}</text>
              <text class="bk-nutri-title">{{ bookingNutritionist.title }}</text>
            </view>
            <text class="bk-fee">¥{{ formatFee(bookingNutritionist.consultationFee) }}</text>
          </view>

          <view class="bk-field">
            <text class="bk-field-label">咨询描述</text>
            <textarea
              class="bk-textarea"
              v-model="bookingDesc"
              placeholder="请简要描述您的营养需求或饮食问题..."
              :maxlength="500"
              placeholder-style="color: #94A3B8"
            />
            <text class="bk-char-count">{{ bookingDesc.length }}/500</text>
          </view>

          <view class="bk-notice">
            <text class="bk-notice-text">
              <NutriIcon name="info" :size="20" color="#6366F1" /> 模拟支付：点击确认后将自动完成支付流程
            </text>
          </view>
        </view>

        <view class="modal-foot bk-foot">
          <view class="bk-cancel" @tap="showBookingModal = false">取消</view>
          <view
            class="bk-confirm"
            :class="{ loading: createLoading }"
            @tap="handleCreateOrder"
          >{{ createLoading ? '处理中...' : '确认预约并支付' }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { consultationApi } from '@/services/api'
import { checkLogin, formatTime, defaultAvatar } from '@/utils/common'
import NutriIcon from '@/components/NutriIcon.vue'

// ====== Tabs & UI ======
const activeTab = ref('find')
const showDisclaimer = ref(true)

// ====== Nutritionists ======
const listLoading = ref(false)
const allNutritionists = ref<any[]>([])
const statusFilter = ref('ALL')
const refreshingFind = ref(false)

const statusFilters = [
  { label: '全部', value: 'ALL' },
  { label: '在线', value: 'ONLINE' },
  { label: '忙碌', value: 'BUSY' },
  { label: '离线', value: 'OFFLINE' }
]

const statusTextMap: Record<string, string> = {
  ONLINE: '在线',
  BUSY: '忙碌',
  OFFLINE: '离线'
}

const currentFilterLabel = computed(() =>
  statusFilters.find(f => f.value === statusFilter.value)?.label || ''
)

const filteredNutritionists = computed(() => {
  if (statusFilter.value === 'ALL') return allNutritionists.value
  return allNutritionists.value.filter(n => n.status === statusFilter.value)
})

// ====== Active Orders ======
const activeOrders = ref<any[]>([])

// ====== Order History ======
const orders = ref<any[]>([])
const ordersLoading = ref(false)
const refreshingMine = ref(false)

// ====== Detail Modal ======
const showDetailModal = ref(false)
const detailNutritionist = ref<any>(null)

// ====== Booking Modal ======
const showBookingModal = ref(false)
const bookingNutritionist = ref<any>(null)
const bookingDesc = ref('')
const createLoading = ref(false)

// ====== Data Loading ======

function extractList(data: any): any[] {
  return data?.content || data?.records || data?.list || (Array.isArray(data) ? data : [])
}

async function loadNutritionists() {
  listLoading.value = true
  try {
    const res = await consultationApi.getNutritionists()
    allNutritionists.value = extractList(res.data)
  } catch {
    uni.showToast({ title: '加载营养师失败', icon: 'none' })
  } finally {
    listLoading.value = false
  }
}

async function loadActiveOrders() {
  try {
    const res = await consultationApi.getActiveOrders()
    activeOrders.value = extractList(res.data)
  } catch {
    // silent
  }
}

async function loadOrders() {
  ordersLoading.value = true
  try {
    const res = await consultationApi.getOrders({ page: 0, size: 30 })
    orders.value = extractList(res.data)
  } catch {
    uni.showToast({ title: '加载订单失败', icon: 'none' })
  } finally {
    ordersLoading.value = false
  }
}

// ====== Pull to Refresh ======

async function onRefreshFind() {
  refreshingFind.value = true
  await Promise.all([loadNutritionists(), loadActiveOrders()])
  refreshingFind.value = false
}

async function onRefreshMine() {
  refreshingMine.value = true
  await Promise.all([loadActiveOrders(), loadOrders()])
  refreshingMine.value = false
}

// ====== Tab Switch ======

function switchToMine() {
  if (!checkLogin()) return
  activeTab.value = 'mine'
  loadActiveOrders()
  loadOrders()
}

// ====== Detail Modal ======

function showDetail(n: any) {
  detailNutritionist.value = n
  showDetailModal.value = true
}

// ====== Booking ======

function openBooking(n: any) {
  if (n.status === 'OFFLINE') return
  if (!checkLogin()) return
  bookingNutritionist.value = n
  bookingDesc.value = ''
  showDetailModal.value = false
  showBookingModal.value = true
}

function openBookingFromDetail() {
  if (!detailNutritionist.value || detailNutritionist.value.status === 'OFFLINE') return
  openBooking(detailNutritionist.value)
}

async function handleCreateOrder() {
  if (createLoading.value) return
  if (!checkLogin() || !bookingNutritionist.value) return

  createLoading.value = true
  try {
    const res = await consultationApi.createOrder({
      nutritionistId: bookingNutritionist.value.id,
      description: bookingDesc.value
    })
    const orderNo = res.data?.orderNo || res.data

    await consultationApi.simulatePay(orderNo)

    showBookingModal.value = false
    uni.showToast({ title: '🎉 预约成功！', icon: 'none' })

    await Promise.all([loadActiveOrders(), loadOrders()])
    activeTab.value = 'mine'
  } catch (e: any) {
    uni.showToast({ title: e?.message || '预约失败，请重试', icon: 'none' })
  } finally {
    createLoading.value = false
  }
}

// ====== Order Actions ======

async function handlePay(order: any) {
  try {
    uni.showLoading({ title: '支付中...' })
    await consultationApi.simulatePay(order.orderNo)
    uni.showToast({ title: '支付成功', icon: 'success' })
    loadOrders()
    loadActiveOrders()
  } catch {
    uni.showToast({ title: '支付失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

async function handleRefund(order: any) {
  uni.showModal({
    title: '确认退款',
    content: '确定要申请退款吗？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        uni.showLoading({ title: '退款中...' })
        await consultationApi.simulateRefund(order.orderNo)
        uni.showToast({ title: '退款成功', icon: 'success' })
        loadOrders()
        loadActiveOrders()
      } catch {
        uni.showToast({ title: '退款失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    }
  })
}

function goChat(orderNo: string) {
  uni.navigateTo({ url: `/pages/consultation/chat?orderNo=${orderNo}` })
}

// ====== Helpers ======

function statusDotClass(status: string): string {
  return { ONLINE: 'online', BUSY: 'busy', OFFLINE: 'offline' }[status] || 'offline'
}

function formatRating(r: number | undefined): string {
  return (r || 5).toFixed(1)
}

function formatFee(fee: number | undefined): string {
  const v = fee || 0
  return v % 1 === 0 ? String(v) : v.toFixed(2)
}

function orderStatusClass(order: any): string {
  if (order.paymentStatus === 'REFUNDED') return 'refunded'
  if (order.paymentStatus === 'PENDING') return 'pending'
  const map: Record<string, string> = {
    IN_PROGRESS: 'active', WAITING: 'pending', COMPLETED: 'completed', CANCELLED: 'cancelled'
  }
  return map[order.status] || 'pending'
}

function orderStatusText(order: any): string {
  if (order.paymentStatus === 'REFUNDED') return '已退款'
  if (order.paymentStatus === 'PENDING') return '待支付'
  if (order.paymentStatus === 'CANCELLED') return '已取消'
  const map: Record<string, string> = {
    IN_PROGRESS: '咨询中', WAITING: '等待中', COMPLETED: '已完成', CANCELLED: '已取消'
  }
  return map[order.status] || order.status
}

function hasActions(order: any): boolean {
  return order.paymentStatus === 'PENDING' ||
    order.status === 'IN_PROGRESS' ||
    canRefund(order)
}

function canRefund(order: any): boolean {
  return order.paymentStatus === 'PAID' && order.status !== 'COMPLETED'
}

// ====== Lifecycle ======

onLoad((options: any) => {
  if (options?.tab === 'orders' || options?.tab === 'mine') {
    activeTab.value = 'mine'
  }
})

onShow(() => {
  loadNutritionists()
  loadActiveOrders()
  if (activeTab.value === 'mine') loadOrders()
})
</script>

<style lang="scss" scoped>
// ====== Page ======
.consultation-page {
  min-height: 100vh;
  background: $background;
  display: flex;
  flex-direction: column;
}

// ====== Disclaimer ======
.disclaimer-bar {
  display: flex;
  align-items: center;
  background: $muted;
  border: 1rpx solid $border;
  border-radius: $radius-lg;
  padding: 14rpx 48rpx 14rpx 20rpx;
  margin: 16rpx 20rpx 0;
  position: relative;
}

.disclaimer-text {
  font-size: 22rpx;
  color: $foreground;
  line-height: 1.5;
  flex: 1;
}

.dismiss-btn {
  position: absolute;
  right: 12rpx;
  top: 50%;
  transform: translateY(-50%);
  font-size: 28rpx;
  color: $muted-foreground;
  padding: 8rpx;
}

// ====== Tab Bar ======
.tab-bar {
  display: flex;
  background: $card;
  position: sticky;
  top: 0;
  z-index: 10;
  border-bottom: 1rpx solid $border;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 28rpx 0;
  font-size: 28rpx;
  color: $muted-foreground;
  position: relative;

  &.active {
    color: $accent;
    font-weight: 600;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 60rpx;
      height: 6rpx;
      background: $accent;
      border-radius: 3rpx;
    }
  }
}

// ====== Tab Content ======
.tab-content {
  flex: 1;
  height: calc(100vh - 100rpx);
}

// ====== Status Filter ======
.filter-bar {
  display: flex;
  gap: 16rpx;
  padding: 20rpx 24rpx;
  background: $card;
}

.filter-chip {
  padding: 10rpx 28rpx;
  border-radius: $radius-full;
  font-size: 24rpx;
  color: $foreground;
  background: $card;
  border: 1rpx solid $border;
  transition: all 0.2s;

  &.active {
    background: $accent;
    color: $accent-foreground;
    border-color: $accent;
    box-shadow: $shadow-accent;
  }
}

// ====== Active Consultations Banner ======
.active-banner {
  padding: 20rpx 24rpx 0;
}

.active-banner-label {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 16rpx;
}

.abl-icon { font-size: 28rpx; }

.abl-text {
  font-size: 26rpx;
  font-weight: 600;
  color: $foreground;
}

.active-banner-card {
  display: flex;
  align-items: center;
  background: $card;
  border: 1rpx solid rgba(16, 185, 129, 0.2);
  border-radius: $radius-xl;
  padding: 20rpx;
  margin-bottom: 12rpx;
  box-shadow: $shadow-accent;
}

.abc-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: $radius-full;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.abc-info {
  flex: 1;
  min-width: 0;
}

.abc-name {
  font-size: 28rpx;
  font-weight: 600;
  color: $foreground;
  display: block;
}

.abc-desc {
  font-size: 22rpx;
  color: $muted-foreground;
  display: block;
  margin-top: 4rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.abc-btn {
  flex-shrink: 0;
  padding: 12rpx 24rpx;
  border-radius: $radius-full;
  font-size: 24rpx;
  font-weight: 600;
  background: $gradient-accent;
  color: $accent-foreground;
  box-shadow: $shadow-accent;
}

// ====== Nutritionist Grid (2 columns) ======
.nutri-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  padding: 16rpx 24rpx;
}

.nutri-card {
  background: $card;
  border: 1rpx solid $border;
  border-radius: $radius-xl;
  padding: 24rpx;
  box-shadow: $shadow-sm;
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.2s;

  &:active {
    box-shadow: $shadow-md;
  }
}

.nc-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
}

.nc-avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.nc-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: $radius-full;
}

.nc-status-dot {
  position: absolute;
  bottom: 2rpx;
  right: 2rpx;
  width: 20rpx;
  height: 20rpx;
  border-radius: $radius-full;
  border: 3rpx solid $card;

  &.online { background: $uni-success; }
  &.busy { background: $uni-warning; }
  &.offline { background: $muted-foreground; }
}

.nc-online-badge {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: $radius-full;

  &.online { background: rgba(34, 197, 94, 0.1); color: $uni-success; }
  &.busy { background: rgba(245, 158, 11, 0.1); color: $uni-warning; }
  &.offline { background: $muted; color: $muted-foreground; }
}

.nc-name {
  font-size: 28rpx;
  font-weight: 600;
  color: $foreground;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nc-title {
  font-size: 22rpx;
  color: $accent;
  display: block;
  margin-top: 4rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nc-bio {
  font-size: 22rpx;
  color: $muted-foreground;
  line-height: 1.5;
  margin-top: 12rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.nc-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 12rpx;
}

.nc-tag {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: $radius-full;
  background: rgba(16, 185, 129, 0.06);
  color: $accent;
  border: 1rpx solid rgba(16, 185, 129, 0.15);
}

.nc-stats {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16rpx;
  padding: 12rpx 0;
  border-top: 1rpx solid $border;
  border-bottom: 1rpx solid $border;
}

.nc-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.nc-stat-val {
  font-size: 22rpx;
  font-weight: 600;
  color: $foreground;
}

.nc-stat-lbl {
  font-size: 18rpx;
  color: $muted-foreground;
  margin-top: 2rpx;
}

.nc-stat-div {
  width: 1rpx;
  height: 32rpx;
  background: $border;
}

.nc-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
}

.nc-price {
  font-size: 30rpx;
  font-weight: 700;
  color: $accent;
}

.nc-price-unit {
  font-size: 20rpx;
  font-weight: 400;
  color: $muted-foreground;
}

.nc-book-btn {
  padding: 10rpx 24rpx;
  border-radius: $radius-full;
  font-size: 22rpx;
  font-weight: 600;
  background: $accent;
  color: $accent-foreground;
  box-shadow: $shadow-accent;
  transition: transform 0.15s;

  &:active { transform: scale(0.96); }
  &.disabled {
    background: $muted;
    color: $muted-foreground;
    box-shadow: none;
  }
}

// ====== My Consultations Tab ======
.mine-section {
  padding: 24rpx;
}

.mine-section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: $foreground;
  display: block;
  margin-bottom: 20rpx;
}

// Active order card
.active-order-card {
  background: $card;
  border: 1rpx solid rgba(16, 185, 129, 0.2);
  border-radius: $radius-xl;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: $shadow-accent;
}

.aoc-top {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.aoc-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: $radius-full;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.aoc-info { flex: 1; }

.aoc-name {
  font-size: 28rpx;
  font-weight: 600;
  color: $foreground;
  display: block;
}

.aoc-badge {
  display: inline-block;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: $radius-full;
  background: rgba(16, 185, 129, 0.1);
  color: $uni-success;
  margin-top: 6rpx;
}

.aoc-desc {
  font-size: 24rpx;
  color: $muted-foreground;
  display: block;
  margin-bottom: 16rpx;
}

.aoc-chat-btn {
  text-align: center;
  padding: 18rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: $accent-foreground;
  background: $gradient-accent;
  border-radius: $radius-lg;
  box-shadow: $shadow-accent;
  transition: transform 0.15s;

  &:active { transform: scale(0.98); }
}

// Order list
.order-list {
  // no extra padding – parent already has it
}

.order-card {
  background: $card;
  border: 1rpx solid $border;
  border-radius: $radius-xl;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: $shadow-sm;
}

.oc-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.oc-no {
  font-size: 22rpx;
  color: $muted-foreground;
  font-family: 'JetBrains Mono', monospace;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 12rpx;
}

.oc-status {
  flex-shrink: 0;
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: $radius-full;

  &.pending { background: rgba(245, 158, 11, 0.1); color: $uni-warning; }
  &.active { background: rgba(16, 185, 129, 0.1); color: $uni-success; }
  &.completed { background: $muted; color: $muted-foreground; }
  &.cancelled { background: rgba(239, 68, 68, 0.1); color: $uni-error; }
  &.refunded { background: rgba(239, 68, 68, 0.1); color: $uni-error; }
}

.oc-row {
  display: flex;
  font-size: 26rpx;
  margin-bottom: 8rpx;
}

.oc-label {
  color: $muted-foreground;
  flex-shrink: 0;
  width: 100rpx;
}

.oc-value {
  color: $foreground;
  flex: 1;
  min-width: 0;
}

.oc-amount {
  color: $accent;
  font-weight: 600;
}

.oc-desc-val {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.oc-actions {
  display: flex;
  gap: 16rpx;
  padding-top: 16rpx;
  margin-top: 8rpx;
  border-top: 1rpx solid $border;
}

.oc-act-btn {
  flex: 1;
  text-align: center;
  padding: 14rpx 0;
  border-radius: $radius-lg;
  font-size: 26rpx;
  font-weight: 600;
  transition: transform 0.15s;

  &:active { transform: scale(0.97); }

  &.pay {
    background: $gradient-accent;
    color: $accent-foreground;
    box-shadow: $shadow-accent;
  }

  &.chat {
    background: $accent;
    color: $accent-foreground;
    box-shadow: $shadow-accent;
  }

  &.refund {
    background: rgba(239, 68, 68, 0.08);
    color: $uni-error;
    border: 1rpx solid rgba(239, 68, 68, 0.2);
  }
}

// ====== Modal Shared ======
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.modal-sheet {
  width: 100%;
  max-height: 85vh;
  background: $card;
  border-radius: $radius-2xl $radius-2xl 0 0;
  display: flex;
  flex-direction: column;
}

.modal-handle {
  width: 60rpx;
  height: 8rpx;
  background: $border;
  border-radius: 4rpx;
  margin: 16rpx auto 0;
}

.modal-hdr {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 28rpx;
  border-bottom: 1rpx solid $border;
}

.modal-hdr-title {
  font-size: 32rpx;
  font-weight: 600;
  color: $foreground;
}

.modal-hdr-close {
  font-size: 36rpx;
  color: $muted-foreground;
  padding: 8rpx;
}

.modal-scroll {
  flex: 1;
  padding: 24rpx 28rpx;
  max-height: 55vh;
}

.modal-foot {
  padding: 20rpx 28rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid $border;
}

.modal-foot-btn {
  text-align: center;
  padding: 24rpx;
  background: $gradient-accent;
  color: $accent-foreground;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: $radius-xl;
  box-shadow: $shadow-accent;
  transition: transform 0.15s;

  &:active { transform: scale(0.98); }
  &.disabled {
    background: $muted;
    color: $muted-foreground;
    box-shadow: none;
  }
}

// ====== Detail Modal ======
.dt-profile {
  display: flex;
  align-items: center;
  margin-bottom: 28rpx;
}

.dt-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: $radius-full;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.dt-meta { flex: 1; }

.dt-name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
}

.dt-name {
  font-size: 32rpx;
  font-weight: 600;
  color: $foreground;
}

.dt-status-badge {
  font-size: 22rpx;
  padding: 4rpx 16rpx;
  border-radius: $radius-full;

  &.online { background: rgba(34, 197, 94, 0.1); color: $uni-success; }
  &.busy { background: rgba(245, 158, 11, 0.1); color: $uni-warning; }
  &.offline { background: $muted; color: $muted-foreground; }
}

.dt-title {
  font-size: 26rpx;
  color: $accent;
  display: block;
  margin-top: 6rpx;
}

.dt-rating-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 10rpx;
  flex-wrap: wrap;
}

.dt-stars {
  font-size: 24rpx;
  color: $uni-warning;
  font-weight: 600;
}

.dt-sep {
  color: $muted-foreground;
  font-size: 22rpx;
}

.dt-exp, .dt-count {
  font-size: 22rpx;
  color: $muted-foreground;
}

.dt-block {
  margin-bottom: 24rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid $border;
}

.dt-block-label {
  font-size: 26rpx;
  font-weight: 600;
  color: $foreground;
  display: block;
  margin-bottom: 12rpx;
}

.dt-block-text {
  font-size: 26rpx;
  color: $muted-foreground;
  line-height: 1.7;
}

.dt-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.dt-price-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8rpx;
}

.dt-price-label {
  font-size: 26rpx;
  color: $muted-foreground;
}

.dt-price-val {
  font-size: 36rpx;
  font-weight: 700;
  color: $accent;
}

// ====== Booking Modal ======
.bk-body {
  padding: 24rpx 28rpx;
}

.bk-nutri {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: $muted;
  border: 1rpx solid $border;
  border-radius: $radius-lg;
  margin-bottom: 24rpx;
}

.bk-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: $radius-full;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.bk-nutri-info { flex: 1; }

.bk-nutri-name {
  font-size: 28rpx;
  font-weight: 600;
  color: $foreground;
  display: block;
}

.bk-nutri-title {
  font-size: 22rpx;
  color: $muted-foreground;
  display: block;
  margin-top: 4rpx;
}

.bk-fee {
  font-size: 36rpx;
  font-weight: 700;
  color: $accent;
  flex-shrink: 0;
}

.bk-field {
  margin-bottom: 20rpx;
}

.bk-field-label {
  font-size: 26rpx;
  font-weight: 600;
  color: $foreground;
  display: block;
  margin-bottom: 12rpx;
}

.bk-textarea {
  width: 100%;
  min-height: 200rpx;
  background: $muted;
  border: 2rpx solid $border;
  border-radius: $radius-lg;
  padding: 20rpx;
  font-size: 26rpx;
  color: $foreground;
  line-height: 1.6;
  box-sizing: border-box;
}

.bk-char-count {
  display: block;
  text-align: right;
  font-size: 22rpx;
  color: $muted-foreground;
  margin-top: 8rpx;
}

.bk-notice {
  background: rgba(16, 185, 129, 0.06);
  border: 1rpx solid rgba(16, 185, 129, 0.15);
  border-radius: $radius-lg;
  padding: 16rpx 20rpx;
}

.bk-notice-text {
  font-size: 24rpx;
  color: $accent;
  line-height: 1.5;
}

.bk-foot {
  display: flex;
  gap: 16rpx;
}

.bk-cancel {
  flex: 1;
  text-align: center;
  padding: 22rpx;
  font-size: 28rpx;
  color: $foreground;
  background: $card;
  border: 2rpx solid $border;
  border-radius: $radius-xl;
  transition: transform 0.15s;

  &:active { transform: scale(0.97); }
}

.bk-confirm {
  flex: 2;
  text-align: center;
  padding: 22rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: $accent-foreground;
  background: $gradient-accent;
  border-radius: $radius-xl;
  box-shadow: $shadow-accent;
  transition: transform 0.15s;

  &:active { transform: scale(0.97); }
  &.loading { opacity: 0.7; }
}

// ====== Common ======
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 0;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: $muted-foreground;
}

.loading-bar {
  text-align: center;
  padding: 60rpx;
}

.loading-text {
  font-size: 28rpx;
  color: $muted-foreground;
}
</style>
