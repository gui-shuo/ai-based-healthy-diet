<template>
  <view class="page-container" v-if="plan">
    <!-- Hero cover -->
    <view class="hero-wrap">
      <image class="hero-image" :src="plan.coverImage || '/static/default-food.jpg'" mode="aspectFill" />
      <view class="hero-overlay" />
      <view class="hero-info">
        <view class="badge-difficulty" :class="'diff-' + (plan.difficulty || 'MEDIUM').toLowerCase()">
          {{ difficultyMap[plan.difficulty] || '中等' }}
        </view>
      </view>
    </view>

    <!-- Title section -->
    <view class="section title-section">
      <text class="plan-title">{{ plan.title }}</text>
      <text class="plan-desc" v-if="plan.description">{{ plan.description }}</text>
      <view class="tags-row" v-if="plan.tags">
        <text v-for="t in plan.tags.split(',').slice(0, 5)" :key="t" class="tag-chip">{{ t.trim() }}</text>
      </view>
      <view class="meta-row">
        <text class="meta-item" v-if="plan.suitableCrowd">👥 {{ plan.suitableCrowd }}</text>
        <text class="meta-item">📅 {{ plan.durationDays }}天</text>
        <text class="meta-item" v-if="plan.avgRating > 0">⭐ {{ Number(plan.avgRating).toFixed(1) }} ({{ plan.ratingCount }})</text>
        <text class="meta-item">👥 {{ plan.followCount || 0 }}人跟随</text>
      </view>
    </view>

    <!-- Target nutrition -->
    <view class="section">
      <text class="section-title">目标营养</text>
      <view class="nutrition-grid">
        <view class="nut-card"><text class="nut-icon">🔥</text><text class="nut-value">{{ plan.targetCalories || 0 }}</text><text class="nut-label">热量(kcal)</text></view>
        <view class="nut-card"><text class="nut-icon">🥩</text><text class="nut-value">{{ plan.targetProtein || 0 }}g</text><text class="nut-label">蛋白质</text></view>
        <view class="nut-card"><text class="nut-icon">🧈</text><text class="nut-value">{{ plan.targetFat || 0 }}g</text><text class="nut-label">脂肪</text></view>
        <view class="nut-card"><text class="nut-icon">🌾</text><text class="nut-value">{{ plan.targetCarbs || 0 }}g</text><text class="nut-label">碳水</text></view>
      </view>
    </view>

    <!-- Follow progress (if following) -->
    <view class="section" v-if="isFollowing && progressData">
      <text class="section-title">📊 跟随进度</text>
      <view class="progress-section">
        <view class="progress-bar-lg">
          <view class="progress-fill-lg" :style="{ width: progressData.progress + '%' }" />
        </view>
        <text class="progress-label">已完成 {{ progressData.checkedDays }}/{{ progressData.totalDays }} 天 ({{ progressData.progress }}%)</text>
      </view>
    </view>

    <!-- Day selector -->
    <view class="section">
      <text class="section-title">每日计划</text>
      <scroll-view scroll-x class="day-scroll" :show-scrollbar="false">
        <view class="day-tabs">
          <view v-for="day in totalDays" :key="day" class="day-tab" :class="{ active: selectedDay === day }" @tap="selectedDay = day">
            <text class="day-text">Day {{ day }}</text>
            <view v-if="isFollowing && isDayChecked(day)" class="day-check">✓</view>
          </view>
        </view>
      </scroll-view>

      <!-- Daily nutrition bar -->
      <view class="daily-summary" v-if="dailyData">
        <view class="summary-row">
          <text class="summary-label">热量</text>
          <view class="bar-wrap"><view class="bar-fill" :style="{ width: caloriePercent + '%' }" /></view>
          <text class="summary-value">{{ dailyCalories }}/{{ plan.targetCalories || 0 }}kcal</text>
        </view>
      </view>
    </view>

    <!-- Meal sections -->
    <view v-for="meal in mealSections" :key="meal.type" class="section">
      <view class="meal-header">
        <text class="section-title">{{ meal.icon }} {{ meal.label }}</text>
        <!-- Checkin button per meal -->
        <view v-if="isFollowing" class="checkin-btn" :class="{ checked: isMealChecked(selectedDay, meal.type) }"
          @tap="toggleCheckin(selectedDay, meal.type)">
          <text class="checkin-text">{{ isMealChecked(selectedDay, meal.type) ? '✅ 已打卡' : '打卡' }}</text>
        </view>
      </view>
      <view v-for="(item, idx) in meal.items" :key="idx" class="meal-item">
        <view class="meal-item-header">
          <text class="meal-food-name">{{ item.foodName }}</text>
          <text class="meal-portion">{{ item.portion }}</text>
        </view>
        <view class="meal-nutrition-row">
          <text class="meal-nut">🔥 {{ item.calories || 0 }}kcal</text>
          <text class="meal-nut">🥩 {{ item.protein || 0 }}g</text>
          <text class="meal-nut">🧈 {{ item.fat || 0 }}g</text>
          <text class="meal-nut">🌾 {{ item.carbs || 0 }}g</text>
        </view>
        <view v-if="item.recipeId || item.recipeCorpusId" class="recipe-link" @tap="goRecipe(item.recipeId || item.recipeCorpusId)">
          <text class="recipe-link-text">查看食谱 →</text>
        </view>
      </view>
      <view v-if="meal.items.length === 0" class="meal-empty">
        <text class="meal-empty-text">暂无安排</text>
      </view>
    </view>

    <!-- Rating section -->
    <view class="section">
      <text class="section-title">⭐ 评价</text>
      <!-- My rating -->
      <view class="my-rating">
        <text class="rate-label">我的评分：</text>
        <view class="stars-row">
          <text v-for="s in 5" :key="s" class="star" :class="{ filled: myRating >= s }" @tap="setRating(s)">★</text>
        </view>
      </view>
      <view v-if="myRating > 0" class="review-input-wrap">
        <input class="review-input" placeholder="写一条评价（可选）" :value="myReview" @input="e => myReview = e.detail.value" />
        <view class="submit-review" @tap="submitRating">
          <text class="submit-text">提交</text>
        </view>
      </view>

      <!-- Ratings list -->
      <view v-for="r in ratings" :key="r.id" class="rating-item">
        <view class="rating-header">
          <text class="rating-user">{{ r.username || '用户' }}</text>
          <view class="rating-stars">
            <text v-for="s in 5" :key="s" class="star-sm" :class="{ filled: r.rating >= s }">★</text>
          </view>
        </view>
        <text v-if="r.review" class="rating-review">{{ r.review }}</text>
      </view>
    </view>

    <!-- Bottom spacer -->
    <view style="height: 160rpx;" />

    <!-- Fixed action bar -->
    <view class="action-bar">
      <view class="action-btn fav-btn" :class="{ favorited: isFavorited }" @tap="toggleFavorite">
        <text class="action-icon">{{ isFavorited ? '❤️' : '🤍' }}</text>
        <text class="action-label">{{ plan.favoriteCount || 0 }}</text>
      </view>
      <view class="action-btn follow-action" :class="{ following: isFollowing }" @tap="toggleFollow">
        <text class="action-icon">{{ isFollowing ? '📋' : '➕' }}</text>
        <text class="action-label">{{ isFollowing ? '取消跟随' : '开始跟随' }}</text>
      </view>
      <button class="action-btn share-btn" open-type="share">
        <text class="action-icon">📤</text>
        <text class="action-label">分享</text>
      </button>
    </view>
  </view>

  <view class="loading-page" v-else>
    <text class="loading-text">加载中...</text>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { mealPlanApi } from '@/services/api'

const MEAL_TYPE_MAP: Record<string, { label: string; icon: string; order: number }> = {
  BREAKFAST: { label: '早餐', icon: '🌅', order: 0 },
  LUNCH: { label: '午餐', icon: '☀️', order: 1 },
  DINNER: { label: '晚餐', icon: '🌙', order: 2 },
  SNACK: { label: '加餐', icon: '🍎', order: 3 }
}

const difficultyMap: Record<string, string> = { EASY: '简单', MEDIUM: '中等', HARD: '困难' }

const props = defineProps({ id: String })

const plan = ref<any>(null)
const isFavorited = ref(false)
const isFollowing = ref(false)
const followData = ref<any>(null)
const progressData = ref<any>(null)
const selectedDay = ref(1)
const checkins = ref<any[]>([])
const ratings = ref<any[]>([])
const myRating = ref(0)
const myReview = ref('')

const totalDays = computed(() => plan.value?.durationDays || 1)

const dailyData = computed(() => {
  const days = plan.value?.days || []
  return days.find((d: any) => d.dayNumber === selectedDay.value) || days[selectedDay.value - 1] || null
})

const dailyItems = computed(() => dailyData.value?.items || dailyData.value?.meals || [])

const dailyCalories = computed(() =>
  dailyItems.value.reduce((sum: number, i: any) => sum + (i.calories || 0), 0)
)

function safePercent(actual: number, target: number) {
  if (!target) return 0
  return Math.min(Math.round((actual / target) * 100), 100)
}

const caloriePercent = computed(() => safePercent(dailyCalories.value, plan.value?.targetCalories))

const mealSections = computed(() => {
  const items = dailyItems.value
  return ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACK'].map(type => {
    const meta = MEAL_TYPE_MAP[type]
    return {
      type,
      icon: meta.icon,
      label: meta.label,
      items: items.filter((i: any) => i.mealType === type)
    }
  })
})

// Check-in helpers
function isMealChecked(day: number, mealType: string) {
  return checkins.value.some(c => c.dayNumber === day && c.mealType === mealType)
}

function isDayChecked(day: number) {
  return checkins.value.some(c => c.dayNumber === day)
}

async function toggleCheckin(day: number, mealType: string) {
  if (!followData.value) return
  try {
    if (isMealChecked(day, mealType)) {
      await mealPlanApi.uncheckin(followData.value.id, day, mealType)
      checkins.value = checkins.value.filter(c => !(c.dayNumber === day && c.mealType === mealType))
      uni.showToast({ title: '已取消打卡', icon: 'none' })
    } else {
      const res = await mealPlanApi.checkin({ followId: followData.value.id, dayNumber: day, mealType })
      const data = res.data?.data || res.data
      checkins.value.push(data)
      uni.showToast({ title: '打卡成功 ✅', icon: 'none' })
    }
    loadProgress()
  } catch (e: any) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

async function fetchPlan() {
  try {
    const res = await mealPlanApi.getDetail(props.id as string)
    const data = res.data?.data || res.data
    plan.value = data.mealPlan || data
    isFavorited.value = data.isFavorited || false
    isFollowing.value = data.isFollowing || false
    followData.value = data.followStatus || null

    if (data.userRating) {
      myRating.value = data.userRating.rating
      myReview.value = data.userRating.review || ''
    }

    if (isFollowing.value) {
      loadProgress()
    }
    loadRatings()
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

async function loadProgress() {
  if (!props.id) return
  try {
    const res = await mealPlanApi.getProgress(props.id)
    const data = res.data?.data || res.data
    progressData.value = data
    checkins.value = data?.checkins || []
  } catch {}
}

async function loadRatings() {
  if (!props.id) return
  try {
    const res = await mealPlanApi.getRatings(props.id, { page: 1, size: 10 })
    const data = res.data?.data || res.data
    ratings.value = data?.content || data || []
  } catch {}
}

async function toggleFavorite() {
  try {
    const res = await mealPlanApi.toggleFavorite(props.id as string)
    const data = res.data?.data || res.data
    isFavorited.value = data.favorited
    if (plan.value) {
      plan.value.favoriteCount += isFavorited.value ? 1 : -1
    }
    uni.showToast({ title: isFavorited.value ? '已收藏' : '已取消收藏', icon: 'none' })
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

async function toggleFollow() {
  try {
    if (isFollowing.value) {
      await mealPlanApi.unfollow(props.id as string)
      isFollowing.value = false
      followData.value = null
      progressData.value = null
      checkins.value = []
      uni.showToast({ title: '已取消跟随', icon: 'none' })
    } else {
      const res = await mealPlanApi.follow(props.id as string)
      const data = res.data?.data || res.data
      isFollowing.value = true
      followData.value = data
      loadProgress()
      uni.showToast({ title: '已开始跟随 🎯', icon: 'none' })
    }
    if (plan.value) {
      plan.value.followCount += isFollowing.value ? 1 : -1
    }
  } catch {
    uni.showToast({ title: '操作失败，请先登录', icon: 'none' })
  }
}

function setRating(star: number) {
  myRating.value = star
}

async function submitRating() {
  if (!myRating.value) return
  try {
    await mealPlanApi.rate(props.id as string, { rating: myRating.value, review: myReview.value || undefined })
    uni.showToast({ title: '评价成功', icon: 'none' })
    loadRatings()
  } catch {
    uni.showToast({ title: '评价失败', icon: 'none' })
  }
}

function goRecipe(recipeId: number | string) {
  uni.navigateTo({ url: '/pages/recipes/detail?id=' + recipeId })
}

onLoad(() => {
  if (props.id) {
    fetchPlan()
  }
})
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: $background;
}

.hero-wrap {
  position: relative;
  width: 100%;
  height: 400rpx;
}

.hero-image {
  width: 100%;
  height: 100%;
}

.hero-overlay {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  height: 200rpx;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.5), transparent);
}

.hero-info {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
}

.badge-difficulty {
  padding: 8rpx 20rpx;
  border-radius: $radius-sm;
  color: #fff;
  font-size: 24rpx;
}

.diff-easy { background: #22C55E; }
.diff-medium { background: #F59E0B; }
.diff-hard { background: #EF4444; }

.section {
  background: $card;
  margin: 16rpx 24rpx;
  border-radius: $radius-lg;
  padding: 24rpx;
  box-shadow: $shadow-sm;
}

.title-section {
  margin-top: -40rpx;
  position: relative;
  z-index: 1;
}

.plan-title {
  font-size: 36rpx;
  font-weight: 700;
  color: $foreground;
  display: block;
}

.plan-desc {
  font-size: 26rpx;
  color: $muted-foreground;
  margin-top: 12rpx;
  line-height: 1.6;
  display: block;
}

.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 16rpx;
}

.tag-chip {
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: $radius-full;
  background: rgba(16, 185, 129, 0.1);
  color: $accent;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 16rpx;
}

.meta-item {
  font-size: 24rpx;
  color: $muted-foreground;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: $foreground;
  display: block;
  margin-bottom: 16rpx;
}

.nutrition-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12rpx;
}

.nut-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 8rpx;
  background: $muted;
  border-radius: $radius-md;
}

.nut-icon { font-size: 36rpx; }
.nut-value { font-size: 28rpx; font-weight: 600; color: $foreground; margin-top: 4rpx; }
.nut-label { font-size: 20rpx; color: $muted-foreground; margin-top: 4rpx; }

/* Progress */
.progress-section {
  margin-top: 8rpx;
}

.progress-bar-lg {
  height: 16rpx;
  background: $muted;
  border-radius: $radius-full;
  overflow: hidden;
}

.progress-fill-lg {
  height: 100%;
  background: $gradient-accent;
  border-radius: $radius-full;
}

.progress-label {
  font-size: 24rpx;
  color: $muted-foreground;
  margin-top: 8rpx;
  display: block;
}

/* Day tabs */
.day-scroll { margin-bottom: 16rpx; }

.day-tabs {
  display: inline-flex;
  gap: 12rpx;
}

.day-tab {
  padding: 12rpx 24rpx;
  border-radius: $radius-full;
  background: $muted;
  flex-shrink: 0;
  position: relative;
}

.day-tab.active {
  background: $accent;
}

.day-tab.active .day-text {
  color: #fff;
}

.day-text {
  font-size: 24rpx;
  color: $foreground;
}

.day-check {
  position: absolute;
  top: -6rpx;
  right: -6rpx;
  width: 28rpx;
  height: 28rpx;
  background: #22C55E;
  border-radius: 50%;
  color: #fff;
  font-size: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Daily summary */
.daily-summary { margin-top: 12rpx; }

.summary-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.summary-label { font-size: 22rpx; color: $muted-foreground; width: 60rpx; }

.bar-wrap {
  flex: 1;
  height: 10rpx;
  background: $muted;
  border-radius: $radius-full;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: $accent;
  border-radius: $radius-full;
}

.summary-value { font-size: 22rpx; color: $foreground; white-space: nowrap; }

/* Meal sections */
.meal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.checkin-btn {
  padding: 8rpx 20rpx;
  border-radius: $radius-full;
  background: $muted;
}

.checkin-btn.checked {
  background: rgba(16, 185, 129, 0.1);
}

.checkin-text {
  font-size: 22rpx;
  color: $accent;
}

.meal-item {
  background: $muted;
  border-radius: $radius-md;
  padding: 16rpx;
  margin-bottom: 12rpx;
}

.meal-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.meal-food-name { font-size: 28rpx; font-weight: 500; color: $foreground; }
.meal-portion { font-size: 24rpx; color: $muted-foreground; }

.meal-nutrition-row {
  display: flex;
  gap: 16rpx;
  margin-top: 8rpx;
}

.meal-nut { font-size: 22rpx; color: $muted-foreground; }

.recipe-link {
  margin-top: 8rpx;
}

.recipe-link-text {
  font-size: 24rpx;
  color: $accent;
}

.meal-empty { padding: 20rpx 0; }
.meal-empty-text { font-size: 24rpx; color: $muted-foreground; }

/* Rating */
.my-rating {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.rate-label { font-size: 26rpx; color: $foreground; }

.stars-row { display: flex; gap: 8rpx; }

.star {
  font-size: 40rpx;
  color: #CBD5E1;
}

.star.filled { color: #F59E0B; }

.review-input-wrap {
  display: flex;
  gap: 12rpx;
  margin-top: 16rpx;
}

.review-input {
  flex: 1;
  background: $muted;
  border-radius: $radius-md;
  padding: 12rpx 16rpx;
  font-size: 26rpx;
}

.submit-review {
  padding: 12rpx 24rpx;
  background: $accent;
  border-radius: $radius-md;
}

.submit-text { color: #fff; font-size: 26rpx; }

.rating-item {
  margin-top: 16rpx;
  padding: 16rpx;
  background: $muted;
  border-radius: $radius-md;
}

.rating-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rating-user { font-size: 26rpx; font-weight: 500; color: $foreground; }
.rating-stars { display: flex; gap: 4rpx; }
.star-sm { font-size: 24rpx; color: #CBD5E1; }
.star-sm.filled { color: #F59E0B; }
.rating-review { font-size: 24rpx; color: $muted-foreground; margin-top: 8rpx; display: block; }

/* Action bar */
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 16rpx 24rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  background: $card;
  border-top: 1rpx solid $border;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  padding: 8rpx 20rpx;
  background: none;
  border: none;
  line-height: normal;
}

.action-btn::after { display: none; }

.action-icon { font-size: 36rpx; }
.action-label { font-size: 22rpx; color: $muted-foreground; }

.fav-btn.favorited .action-label { color: #EF4444; }
.follow-action.following .action-label { color: $accent; }

.loading-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}

.loading-text { font-size: 28rpx; color: $muted-foreground; }
</style>
