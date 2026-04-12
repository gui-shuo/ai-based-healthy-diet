<template>
  <view class="page-container">
    <!-- Tab bar -->
    <view class="tab-bar">
      <view class="tab-item" :class="{ active: activeTab === 'discover' }" @tap="activeTab = 'discover'">
        <text class="tab-text">发现</text>
      </view>
      <view class="tab-item" :class="{ active: activeTab === 'following' }" @tap="switchToFollowing">
        <text class="tab-text">我的跟随</text>
        <view v-if="followCount > 0" class="tab-badge">{{ followCount }}</view>
      </view>
    </view>

    <!-- ===== Discover Tab ===== -->
    <template v-if="activeTab === 'discover'">
      <!-- Search -->
      <view class="search-bar">
        <view class="search-input-wrap">
          <text class="search-icon">🔍</text>
          <input class="search-input" type="text" placeholder="搜索营养餐..." :value="keyword"
            @input="e => keyword = e.detail.value" confirm-type="search" @confirm="refreshData" />
          <text v-if="keyword" class="search-clear" @tap="keyword = ''; refreshData()">✕</text>
        </view>
      </view>

      <!-- Tags -->
      <scroll-view scroll-x class="tag-bar" :show-scrollbar="false" v-if="tags.length > 0">
        <view class="tag-list">
          <view v-for="tag in tags" :key="tag" class="tag-chip" :class="{ active: activeTag === tag }"
            @tap="toggleTag(tag)">
            <text class="tag-text">{{ tag }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- Sort -->
      <view class="sort-bar">
        <view v-for="s in sortOptions" :key="s.value" class="sort-item"
          :class="{ active: selectedSort === s.value }" @tap="selectedSort = s.value; refreshData()">
          <text class="sort-text">{{ s.label }}</text>
        </view>
      </view>

      <!-- Plan list -->
      <view class="plan-list" v-if="plans.length > 0">
        <view v-for="item in plans" :key="item.id" class="plan-card" @tap="goDetail(item.id)">
          <view class="card-cover-wrap">
            <image class="card-cover" :src="item.coverImage || '/static/default-food.jpg'" mode="aspectFill" lazy-load />
            <view class="cover-gradient" />
            <view class="badge-difficulty" :class="'diff-' + (item.difficulty || 'MEDIUM').toLowerCase()">
              {{ difficultyMap[item.difficulty] || '中等' }}
            </view>
          </view>
          <view class="card-body">
            <text class="card-title">{{ item.title }}</text>
            <text class="card-desc">{{ item.description }}</text>
            <view class="card-tags" v-if="item.tags">
              <text v-for="t in item.tags.split(',').slice(0, 3)" :key="t" class="mini-tag">{{ t.trim() }}</text>
            </view>
            <view class="card-info">
              <text class="info-item">📅 {{ item.durationDays }}天</text>
              <text class="info-sep">·</text>
              <text class="info-item">🔥 {{ item.targetCalories }}kcal</text>
            </view>
            <view class="card-footer">
              <view class="footer-left">
                <text v-if="item.avgRating > 0" class="footer-item">⭐ {{ Number(item.avgRating).toFixed(1) }}</text>
                <text class="footer-item">👥 {{ item.followCount || 0 }}人跟随</text>
              </view>
              <text class="footer-item">👁 {{ item.viewCount || 0 }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Empty -->
      <view class="empty-state" v-else-if="!loading">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无营养餐</text>
      </view>

      <!-- Loading -->
      <view class="list-status">
        <text v-if="loading" class="status-text">加载中...</text>
        <text v-else-if="noMore && plans.length > 0" class="status-text">— 没有更多了 —</text>
      </view>
    </template>

    <!-- ===== Following Tab ===== -->
    <template v-if="activeTab === 'following'">
      <view class="follow-list" v-if="follows.length > 0">
        <view v-for="f in follows" :key="f.id" class="follow-card" @tap="goDetail(f.mealPlanId)">
          <view class="follow-header">
            <text class="follow-title">{{ f.mealPlan?.title || '营养餐' }}</text>
            <view class="follow-status" :class="'st-' + (f.status || '').toLowerCase()">
              <text class="st-text">{{ statusMap[f.status] || f.status }}</text>
            </view>
          </view>
          <view class="follow-progress">
            <view class="progress-bar">
              <view class="progress-fill" :style="{ width: getProgress(f) + '%' }" />
            </view>
            <text class="progress-text">Day {{ f.currentDay || 1 }} / {{ f.mealPlan?.durationDays || '?' }}</text>
          </view>
          <view class="follow-meta">
            <text class="meta-item">开始于 {{ formatDate(f.startDate) }}</text>
          </view>
        </view>
      </view>

      <view class="empty-state" v-else-if="!followLoading">
        <text class="empty-icon">📝</text>
        <text class="empty-text">还没有跟随任何营养餐计划</text>
        <view class="empty-btn" @tap="activeTab = 'discover'">
          <text class="empty-btn-text">去发现 →</text>
        </view>
      </view>

      <view class="list-status" v-if="followLoading">
        <text class="status-text">加载中...</text>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow, onReachBottom } from '@dcloudio/uni-app'
import { mealPlanApi } from '@/services/api'

const activeTab = ref('discover')

// ===== Discover =====
const keyword = ref('')
const activeTag = ref('')
const selectedSort = ref('latest')
const plans = ref<any[]>([])
const page = ref(1)
const pageSize = 10
const loading = ref(false)
const noMore = ref(false)
const tags = ref<string[]>([])

const sortOptions = [
  { label: '最新', value: 'latest' },
  { label: '最热', value: 'popular' },
  { label: '评分', value: 'rating' },
  { label: '跟随', value: 'followers' },
  { label: '低卡', value: 'calories_asc' }
]

const difficultyMap: Record<string, string> = { EASY: '简单', MEDIUM: '中等', HARD: '困难' }
const statusMap: Record<string, string> = { ACTIVE: '进行中', COMPLETED: '已完成', ABANDONED: '已放弃' }

function toggleTag(tag: string) {
  activeTag.value = activeTag.value === tag ? '' : tag
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
    let res: any
    if (activeTag.value) {
      res = await mealPlanApi.searchByTag(activeTag.value, { page: page.value, size: pageSize })
    } else {
      const params: Record<string, any> = { page: page.value, size: pageSize, sort: selectedSort.value }
      if (keyword.value) params.keyword = keyword.value
      res = await mealPlanApi.getList(params)
    }
    const data = res.data?.data || res.data
    const list = data?.content || data?.records || data?.list || data || []
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

async function loadTags() {
  try {
    const res = await mealPlanApi.getTags()
    const data = res.data?.data || res.data
    tags.value = Array.isArray(data) ? data : []
  } catch {}
}

// ===== Following =====
const follows = ref<any[]>([])
const followLoading = ref(false)
const followCount = computed(() => follows.value.filter(f => f.status === 'ACTIVE').length)

async function loadFollows() {
  followLoading.value = true
  try {
    const res = await mealPlanApi.getMyFollows()
    const data = res.data?.data || res.data
    follows.value = Array.isArray(data) ? data : []
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    followLoading.value = false
  }
}

function switchToFollowing() {
  activeTab.value = 'following'
  loadFollows()
}

function getProgress(f: any) {
  const total = f.mealPlan?.durationDays || 1
  const current = f.currentDay || 1
  return Math.min(Math.round((current / total) * 100), 100)
}

function formatDate(d: string) {
  if (!d) return ''
  return d.substring(0, 10)
}

function goDetail(id: number | string) {
  uni.navigateTo({ url: '/pages/meal-plans/detail?id=' + id })
}

onShow(() => {
  if (plans.value.length === 0) {
    loadPlans()
    loadTags()
  }
})

onReachBottom(() => {
  if (activeTab.value !== 'discover') return
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

.tab-bar {
  display: flex;
  background: $card;
  border-bottom: 1rpx solid $border;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}

.tab-item.active {
  border-bottom: 4rpx solid $accent;
}

.tab-text {
  font-size: 30rpx;
  color: $muted-foreground;
}

.tab-item.active .tab-text {
  color: $accent;
  font-weight: 600;
}

.tab-badge {
  background: $uni-error;
  color: #fff;
  font-size: 20rpx;
  min-width: 32rpx;
  height: 32rpx;
  line-height: 32rpx;
  border-radius: $radius-full;
  text-align: center;
  padding: 0 8rpx;
}

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

.search-icon { font-size: 28rpx; margin-right: 12rpx; }

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

.tag-bar {
  background: $card;
  border-bottom: 1rpx solid $border;
  white-space: nowrap;
}

.tag-list {
  display: inline-flex;
  padding: 16rpx 24rpx;
  gap: 12rpx;
}

.tag-chip {
  padding: 8rpx 24rpx;
  border-radius: $radius-full;
  background: $muted;
  flex-shrink: 0;
}

.tag-chip.active {
  background: $accent;
}

.tag-chip.active .tag-text {
  color: #fff;
}

.tag-text {
  font-size: 24rpx;
  color: $foreground;
  white-space: nowrap;
}

.sort-bar {
  display: flex;
  background: $card;
  border-bottom: 1rpx solid $border;
  padding: 0 24rpx;
}

.sort-item {
  padding: 16rpx 20rpx;
}

.sort-item.active .sort-text {
  color: $accent;
  font-weight: 600;
}

.sort-text {
  font-size: 26rpx;
  color: $muted-foreground;
}

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
  bottom: 0; left: 0; right: 0;
  height: 120rpx;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.4), transparent);
}

.badge-difficulty {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  padding: 6rpx 18rpx;
  border-radius: $radius-sm;
  color: #fff;
  font-size: 22rpx;
}

.diff-easy { background: #22C55E; }
.diff-medium { background: #F59E0B; }
.diff-hard { background: #EF4444; }

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
  margin-top: 8rpx;
  line-height: 1.5;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 12rpx;
}

.mini-tag {
  font-size: 20rpx;
  padding: 4rpx 14rpx;
  border-radius: $radius-full;
  background: rgba(16, 185, 129, 0.1);
  color: $accent;
}

.card-info {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 12rpx;
}

.info-item { font-size: 22rpx; color: $muted-foreground; }
.info-sep { font-size: 20rpx; color: $border; }

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12rpx;
  padding-top: 12rpx;
  border-top: 1rpx solid $border;
}

.footer-left { display: flex; gap: 16rpx; }
.footer-item { font-size: 22rpx; color: $muted-foreground; }

.follow-list {
  padding: 20rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.follow-card {
  background: $card;
  border-radius: $radius-lg;
  padding: 24rpx;
  box-shadow: $shadow-sm;
}

.follow-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.follow-title {
  font-size: 30rpx;
  font-weight: 600;
  color: $foreground;
}

.follow-status {
  padding: 6rpx 16rpx;
  border-radius: $radius-full;
}

.st-active { background: rgba(16, 185, 129, 0.1); }
.st-active .st-text { color: $accent; }
.st-completed { background: rgba(59, 130, 246, 0.1); }
.st-completed .st-text { color: #3B82F6; }
.st-abandoned { background: rgba(239, 68, 68, 0.1); }
.st-abandoned .st-text { color: #EF4444; }

.st-text { font-size: 22rpx; }

.follow-progress {
  margin-top: 16rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.progress-bar {
  flex: 1;
  height: 12rpx;
  background: $muted;
  border-radius: $radius-full;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: $gradient-accent;
  border-radius: $radius-full;
}

.progress-text {
  font-size: 24rpx;
  color: $muted-foreground;
  white-space: nowrap;
}

.follow-meta { margin-top: 12rpx; }
.meta-item { font-size: 22rpx; color: $muted-foreground; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 200rpx 0;
}

.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: $muted-foreground; }

.empty-btn {
  margin-top: 24rpx;
  padding: 16rpx 40rpx;
  background: $gradient-accent;
  border-radius: $radius-full;
}

.empty-btn-text { color: #fff; font-size: 28rpx; }

.list-status {
  text-align: center;
  padding: 30rpx 0;
}

.status-text { font-size: 24rpx; color: $muted-foreground; }
</style>
