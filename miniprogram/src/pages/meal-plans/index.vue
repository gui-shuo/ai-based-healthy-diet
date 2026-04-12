<template>
  <view class="page-container">
    <!-- Search bar -->
    <view class="search-bar">
      <view class="search-input-wrap">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          type="text"
          placeholder="搜索餐计划..."
          :value="keyword"
          @input="onSearchInput"
          confirm-type="search"
          @confirm="onSearchConfirm"
        />
        <text v-if="keyword" class="search-clear" @tap="clearSearch">✕</text>
      </view>
    </view>

    <!-- Diet goal tabs -->
    <scroll-view scroll-x class="goal-bar" :show-scrollbar="false">
      <view class="goal-tabs">
        <view
          v-for="goal in goalTabs"
          :key="goal.value"
          class="goal-tab"
          :class="{ active: activeGoal === goal.value }"
          @tap="switchGoal(goal.value)"
        >
          <text class="goal-label">{{ goal.label }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- Filter row -->
    <view class="filter-row">
      <picker :range="planTypes" range-key="label" @change="onPlanTypeChange">
        <view class="filter-btn">
          <text class="filter-text">{{ currentPlanTypeLabel }}</text>
          <text class="filter-arrow">▾</text>
        </view>
      </picker>
      <picker :range="sortOptions" range-key="label" @change="onSortChange">
        <view class="filter-btn">
          <text class="filter-text">{{ currentSortLabel }}</text>
          <text class="filter-arrow">▾</text>
        </view>
      </picker>
    </view>

    <!-- Plan list -->
    <view class="plan-list" v-if="plans.length > 0">
      <view
        v-for="item in plans"
        :key="item.id"
        class="plan-card"
        @tap="goDetail(item.id)"
      >
        <view class="card-cover-wrap">
          <image
            class="card-cover"
            :src="item.coverImage"
            mode="aspectFill"
            lazy-load
          />
          <view class="cover-gradient" />
          <view
            class="badge-goal"
            :style="{ background: getGoalColor(item.dietGoal) }"
          >
            {{ getGoalLabel(item.dietGoal) }}
          </view>
          <view class="badge-type">{{ getPlanTypeLabel(item.planType) }}</view>
        </view>
        <view class="card-body">
          <text class="card-title">{{ item.title }}</text>
          <text class="card-desc">{{ item.description }}</text>
          <view class="card-info">
            <text class="info-item">📅 {{ item.durationDays }}天</text>
            <text class="info-sep">|</text>
            <text class="info-item">🔥 {{ item.targetCalories }}kcal/日</text>
            <text class="info-sep">|</text>
            <text class="info-item">👥 {{ item.suitableCrowd }}</text>
          </view>
          <view class="card-footer">
            <text class="footer-item">❤️ {{ item.favoriteCount || 0 }}</text>
            <text class="footer-item">👁 {{ item.viewCount || 0 }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Empty state -->
    <view class="empty-state" v-else-if="!loading">
      <text class="empty-icon">📋</text>
      <text class="empty-text">暂无餐计划</text>
    </view>

    <!-- Loading / no more -->
    <view class="list-status">
      <text v-if="loading" class="status-text">加载中...</text>
      <text v-else-if="noMore && plans.length > 0" class="status-text">没有更多了</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow, onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'

const DIET_GOAL_MAP: Record<string, { label: string; color: string }> = {
  BALANCED: { label: '均衡饮食', color: '#10B981' },
  WEIGHT_LOSS: { label: '减脂塑形', color: '#F59E0B' },
  MUSCLE_GAIN: { label: '增肌增重', color: '#3B82F6' },
  WELLNESS: { label: '养生调理', color: '#8B5CF6' },
  DIABETES_FRIENDLY: { label: '糖尿病适用', color: '#EF4444' },
  PREGNANCY: { label: '孕期营养', color: '#EC4899' },
  POSTPARTUM: { label: '产后恢复', color: '#F97316' },
  ELDERLY: { label: '老年营养', color: '#6366F1' }
}

const goalTabs = [
  { value: '', label: '全部' },
  { value: 'BALANCED', label: '均衡饮食' },
  { value: 'WEIGHT_LOSS', label: '减脂塑形' },
  { value: 'MUSCLE_GAIN', label: '增肌增重' },
  { value: 'WELLNESS', label: '养生调理' },
  { value: 'DIABETES_FRIENDLY', label: '糖尿病适用' },
  { value: 'PREGNANCY', label: '孕期营养' },
  { value: 'POSTPARTUM', label: '产后恢复' },
  { value: 'ELDERLY', label: '老年营养' }
]

const planTypes = [
  { value: '', label: '全部' },
  { value: 'DAILY', label: '每日' },
  { value: 'WEEKLY', label: '每周' }
]

const sortOptions = [
  { value: 'newest', label: '最新' },
  { value: 'popular', label: '最热' },
  { value: 'lowcal', label: '低卡' }
]

const keyword = ref('')
const activeGoal = ref('')
const activePlanType = ref(0)
const activeSort = ref(0)
const plans = ref<any[]>([])
const page = ref(1)
const pageSize = 10
const loading = ref(false)
const noMore = ref(false)

let searchTimer: ReturnType<typeof setTimeout> | null = null

const currentPlanTypeLabel = computed(() => planTypes[activePlanType.value].label)
const currentSortLabel = computed(() => sortOptions[activeSort.value].label)

function getGoalLabel(goal: string) {
  return DIET_GOAL_MAP[goal]?.label || goal
}

function getGoalColor(goal: string) {
  return DIET_GOAL_MAP[goal]?.color || '#10B981'
}

function getPlanTypeLabel(type: string) {
  const map: Record<string, string> = { DAILY: '每日', WEEKLY: '每周' }
  return map[type] || type
}

function onSearchInput(e: any) {
  keyword.value = e.detail.value
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    refreshData()
  }, 500)
}

function onSearchConfirm() {
  if (searchTimer) clearTimeout(searchTimer)
  refreshData()
}

function clearSearch() {
  keyword.value = ''
  refreshData()
}

function switchGoal(val: string) {
  activeGoal.value = val
  refreshData()
}

function onPlanTypeChange(e: any) {
  activePlanType.value = Number(e.detail.value)
  refreshData()
}

function onSortChange(e: any) {
  activeSort.value = Number(e.detail.value)
  refreshData()
}

function refreshData() {
  page.value = 1
  noMore.value = false
  plans.value = []
  loadPlans()
}

async function loadPlans() {
  if (loading.value || noMore.value) return
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: page.value,
      size: pageSize
    }
    if (keyword.value) params.keyword = keyword.value
    if (activeGoal.value) params.dietGoal = activeGoal.value
    const typeVal = planTypes[activePlanType.value].value
    if (typeVal) params.planType = typeVal
    params.sort = sortOptions[activeSort.value].value

    const res = await request({ url: '/meal-plans', data: params })
    const list = res.data?.content || res.data?.records || res.data?.list || res.data || []
    const arr = Array.isArray(list) ? list : []

    if (page.value === 1) {
      plans.value = arr
    } else {
      plans.value = [...plans.value, ...arr]
    }
    noMore.value = arr.length < pageSize
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function goDetail(id: number | string) {
  uni.navigateTo({ url: `/pages/meal-plans/detail?id=${id}` })
}

onShow(() => {
  if (plans.value.length === 0) {
    loadPlans()
  }
})

onReachBottom(() => {
  if (noMore.value || loading.value) return
  page.value++
  loadPlans()
})
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: $background;
  padding-bottom: 40rpx;
}

/* ===== Search Bar ===== */
.search-bar {
  padding: 20rpx 24rpx;
  background: $card;
}

.search-input-wrap {
  display: flex;
  align-items: center;
  background: $muted;
  border-radius: $radius-full;
  padding: 0 24rpx;
  height: 72rpx;
}

.search-icon {
  font-size: 28rpx;
  margin-right: 12rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: $foreground;
}

.search-clear {
  font-size: 28rpx;
  color: $muted-foreground;
  padding: 8rpx;
}

/* ===== Goal Tabs ===== */
.goal-bar {
  background: $card;
  border-bottom: 1rpx solid $border;
  white-space: nowrap;
}

.goal-tabs {
  display: inline-flex;
  padding: 16rpx 24rpx;
  gap: 16rpx;
}

.goal-tab {
  padding: 12rpx 28rpx;
  border-radius: $radius-full;
  background: $muted;
  flex-shrink: 0;
  transition: all 0.2s;
}

.goal-tab.active {
  background: $accent;
}

.goal-tab.active .goal-label {
  color: #fff;
}

.goal-label {
  font-size: 26rpx;
  color: $foreground;
  white-space: nowrap;
}

/* ===== Filter Row ===== */
.filter-row {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 24rpx;
  background: $card;
  border-bottom: 1rpx solid $border;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 10rpx 20rpx;
  border-radius: $radius-sm;
  border: 1rpx solid $border;
  background: $card;
}

.filter-text {
  font-size: 24rpx;
  color: $foreground;
}

.filter-arrow {
  font-size: 20rpx;
  color: $muted-foreground;
}

/* ===== Plan List ===== */
.plan-list {
  padding: 20rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.plan-card {
  background: $card;
  border-radius: $radius-xl;
  overflow: hidden;
  box-shadow: $shadow-sm;
  transition: box-shadow 0.2s;
}

.plan-card:active {
  box-shadow: $shadow-md;
}

.card-cover-wrap {
  position: relative;
  width: 100%;
  height: 300rpx;
}

.card-cover {
  width: 100%;
  height: 100%;
}

.cover-gradient {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120rpx;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.45), transparent);
}

.badge-goal {
  position: absolute;
  top: 16rpx;
  left: 16rpx;
  padding: 6rpx 18rpx;
  border-radius: $radius-sm;
  color: #fff;
  font-size: 22rpx;
  font-weight: 500;
}

.badge-type {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  padding: 6rpx 18rpx;
  border-radius: $radius-sm;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 22rpx;
}

.card-body {
  padding: 20rpx 24rpx 24rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: $foreground;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-desc {
  font-size: 24rpx;
  color: $muted-foreground;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 10rpx;
  line-height: 1.5;
}

.card-info {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 16rpx;
}

.info-item {
  font-size: 22rpx;
  color: $muted-foreground;
}

.info-sep {
  font-size: 20rpx;
  color: $border;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid $border;
}

.footer-item {
  font-size: 22rpx;
  color: $muted-foreground;
}

/* ===== Empty State ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: $muted-foreground;
}

/* ===== List Status ===== */
.list-status {
  text-align: center;
  padding: 30rpx 0;
}

.status-text {
  font-size: 24rpx;
  color: $muted-foreground;
}
</style>
