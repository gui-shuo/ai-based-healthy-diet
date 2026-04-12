<template>
  <view class="page-container">
    <!-- Search bar -->
    <view class="search-bar">
      <view class="search-input-wrap">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          type="text"
          placeholder="搜索食谱..."
          :value="keyword"
          @input="onSearchInput"
          confirm-type="search"
          @confirm="onSearchConfirm"
        />
        <text v-if="keyword" class="search-clear" @tap="clearSearch">✕</text>
      </view>
    </view>

    <!-- Category tabs -->
    <scroll-view scroll-x class="category-bar" :show-scrollbar="false">
      <view class="category-tabs">
        <view
          v-for="cat in categories"
          :key="cat.value"
          class="category-tab"
          :class="{ active: activeCategory === cat.value }"
          @tap="switchCategory(cat.value)"
        >
          <text class="category-label">{{ cat.label }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- Filter row -->
    <view class="filter-row">
      <picker :range="dietTypes" range-key="label" @change="onDietChange">
        <view class="filter-btn">
          <text class="filter-text">{{ currentDietLabel }}</text>
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

    <!-- Recipe grid -->
    <view class="recipe-grid" v-if="recipes.length > 0">
      <view
        v-for="item in recipes"
        :key="item.id"
        class="recipe-card"
        @tap="goDetail(item.id)"
      >
        <view class="card-cover-wrap">
          <image
            class="card-cover"
            :src="item.coverImage"
            mode="aspectFill"
            lazy-load
          />
          <view class="badge-category">{{ item.category }}</view>
          <view class="badge-difficulty" :class="difficultyClass(item.difficulty)">
            {{ item.difficulty }}
          </view>
        </view>
        <view class="card-body">
          <text class="card-title">{{ item.title }}</text>
          <view class="card-meta">
            <text class="meta-item">⏱ {{ item.totalTime }}min</text>
            <text class="meta-sep">|</text>
            <text class="meta-item">🔥 {{ item.calories }}kcal</text>
          </view>
          <view class="card-rating">
            <text class="stars">{{ renderStars(item.ratingAvg) }}</text>
            <text class="rating-count">({{ item.ratingCount || 0 }})</text>
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
      <text class="empty-icon">🍽️</text>
      <text class="empty-text">暂无食谱</text>
    </view>

    <!-- Loading / no more -->
    <view class="list-status">
      <text v-if="loading" class="status-text">加载中...</text>
      <text v-else-if="noMore && recipes.length > 0" class="status-text">没有更多了</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow, onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'

const categories = [
  { value: '', label: '全部' },
  { value: '早餐', label: '早餐' },
  { value: '午餐', label: '午餐' },
  { value: '晚餐', label: '晚餐' },
  { value: '小食', label: '小食' },
  { value: '甜品', label: '甜品' },
  { value: '汤品', label: '汤品' }
]

const dietTypes = [
  { value: '', label: '全部' },
  { value: '低脂', label: '低脂' },
  { value: '高蛋白', label: '高蛋白' },
  { value: '素食', label: '素食' },
  { value: '纯素', label: '纯素' },
  { value: '生酮', label: '生酮' },
  { value: '地中海', label: '地中海' }
]

const sortOptions = [
  { value: 'newest', label: '最新' },
  { value: 'popular', label: '最热' },
  { value: 'rating', label: '好评' },
  { value: 'lowcal', label: '低卡' },
  { value: 'quick', label: '快手' }
]

const keyword = ref('')
const activeCategory = ref('')
const activeDiet = ref(0)
const activeSort = ref(0)
const recipes = ref<any[]>([])
const page = ref(1)
const pageSize = 12
const loading = ref(false)
const noMore = ref(false)

let searchTimer: ReturnType<typeof setTimeout> | null = null

const currentDietLabel = computed(() => dietTypes[activeDiet.value].label)
const currentSortLabel = computed(() => sortOptions[activeSort.value].label)

function difficultyClass(difficulty: string) {
  const map: Record<string, string> = {
    '简单': 'diff-easy',
    '中等': 'diff-medium',
    '困难': 'diff-hard'
  }
  return map[difficulty] || 'diff-easy'
}

function renderStars(avg: number) {
  const score = Math.round(avg || 0)
  return '★'.repeat(score) + '☆'.repeat(5 - score)
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

function switchCategory(val: string) {
  activeCategory.value = val
  refreshData()
}

function onDietChange(e: any) {
  activeDiet.value = Number(e.detail.value)
  refreshData()
}

function onSortChange(e: any) {
  activeSort.value = Number(e.detail.value)
  refreshData()
}

function refreshData() {
  page.value = 1
  noMore.value = false
  recipes.value = []
  loadRecipes()
}

async function loadRecipes() {
  if (loading.value || noMore.value) return
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: page.value,
      size: pageSize
    }
    if (keyword.value) params.keyword = keyword.value
    if (activeCategory.value) params.category = activeCategory.value
    const dietVal = dietTypes[activeDiet.value].value
    if (dietVal) params.dietType = dietVal
    params.sort = sortOptions[activeSort.value].value

    const res = await request({ url: '/recipes', data: params })
    const list = res.data?.content || res.data?.records || res.data?.list || res.data || []
    const arr = Array.isArray(list) ? list : []

    if (page.value === 1) {
      recipes.value = arr
    } else {
      recipes.value = [...recipes.value, ...arr]
    }
    noMore.value = arr.length < pageSize
  } catch (e: any) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function goDetail(id: number | string) {
  uni.navigateTo({ url: `/pages/recipes/detail?id=${id}` })
}

onShow(() => {
  uni.setNavigationBarTitle({ title: '食谱库' })
  if (recipes.value.length === 0) {
    loadRecipes()
  }
})

onReachBottom(() => {
  if (noMore.value || loading.value) return
  page.value++
  loadRecipes()
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

/* ===== Category Tabs ===== */
.category-bar {
  background: $card;
  border-bottom: 1rpx solid $border;
  white-space: nowrap;
}

.category-tabs {
  display: inline-flex;
  padding: 16rpx 24rpx;
  gap: 16rpx;
}

.category-tab {
  padding: 12rpx 28rpx;
  border-radius: $radius-full;
  background: $muted;
  flex-shrink: 0;
  transition: all 0.2s;
}

.category-tab.active {
  background: $accent;
}

.category-tab.active .category-label {
  color: #fff;
}

.category-label {
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

/* ===== Recipe Grid ===== */
.recipe-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
  padding: 20rpx 24rpx;
}

.recipe-card {
  background: $card;
  border-radius: $radius-xl;
  overflow: hidden;
  box-shadow: $shadow-sm;
  transition: box-shadow 0.2s;
}

.recipe-card:active {
  box-shadow: $shadow-md;
}

.card-cover-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
}

.card-cover {
  width: 100%;
  height: 100%;
}

.badge-category {
  position: absolute;
  top: 12rpx;
  left: 12rpx;
  padding: 4rpx 14rpx;
  border-radius: $radius-sm;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 20rpx;
}

.badge-difficulty {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  padding: 4rpx 14rpx;
  border-radius: $radius-sm;
  font-size: 20rpx;
  color: #fff;
}

.diff-easy {
  background: #22C55E;
}

.diff-medium {
  background: #F59E0B;
}

.diff-hard {
  background: #EF4444;
}

.card-body {
  padding: 16rpx;
}

.card-title {
  font-size: 26rpx;
  font-weight: 600;
  color: $foreground;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 10rpx;
}

.meta-item {
  font-size: 22rpx;
  color: $muted-foreground;
}

.meta-sep {
  font-size: 20rpx;
  color: $border;
}

.card-rating {
  display: flex;
  align-items: center;
  gap: 6rpx;
  margin-top: 8rpx;
}

.stars {
  font-size: 22rpx;
  color: #F59E0B;
}

.rating-count {
  font-size: 20rpx;
  color: $muted-foreground;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10rpx;
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
