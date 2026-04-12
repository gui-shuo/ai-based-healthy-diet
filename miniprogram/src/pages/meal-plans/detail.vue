<template>
  <view class="page-container" v-if="plan">
    <!-- Hero cover -->
    <view class="hero-wrap">
      <image class="hero-image" :src="plan.coverImage" mode="aspectFill" />
    </view>

    <!-- Title section -->
    <view class="section title-section">
      <text class="plan-title">{{ plan.title }}</text>
      <text class="plan-desc" v-if="plan.description">{{ plan.description }}</text>
      <view class="crowd-row" v-if="plan.suitableCrowd">
        <text class="crowd-label">适用人群:</text>
        <text class="crowd-value">{{ plan.suitableCrowd }}</text>
      </view>
    </view>

    <!-- Target nutrition summary -->
    <view class="section">
      <text class="section-title">目标营养</text>
      <view class="nutrition-grid">
        <view class="nut-card">
          <text class="nut-icon">🔥</text>
          <text class="nut-value">{{ plan.targetCalories || 0 }}</text>
          <text class="nut-label">热量(kcal)</text>
        </view>
        <view class="nut-card">
          <text class="nut-icon">🥩</text>
          <text class="nut-value">{{ plan.targetProtein || 0 }}g</text>
          <text class="nut-label">蛋白质</text>
        </view>
        <view class="nut-card">
          <text class="nut-icon">🧈</text>
          <text class="nut-value">{{ plan.targetFat || 0 }}g</text>
          <text class="nut-label">脂肪</text>
        </view>
        <view class="nut-card">
          <text class="nut-icon">🌾</text>
          <text class="nut-value">{{ plan.targetCarbs || 0 }}g</text>
          <text class="nut-label">碳水</text>
        </view>
      </view>
    </view>

    <!-- Day selector -->
    <view class="section">
      <text class="section-title">每日计划</text>
      <scroll-view scroll-x class="day-scroll" :show-scrollbar="false">
        <view class="day-tabs">
          <view
            v-for="day in totalDays"
            :key="day"
            class="day-tab"
            :class="{ active: selectedDay === day }"
            @tap="selectedDay = day"
          >
            <text class="day-text">Day {{ day }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- Daily nutrition summary bar -->
      <view class="daily-summary" v-if="dailyData">
        <view class="summary-row">
          <text class="summary-label">热量</text>
          <view class="bar-wrap">
            <view class="bar-fill" :style="{ width: caloriePercent + '%' }" />
          </view>
          <text class="summary-value">{{ dailyCalories }}/{{ plan.targetCalories || 0 }}kcal</text>
        </view>
        <view class="summary-row">
          <text class="summary-label">蛋白</text>
          <view class="bar-wrap">
            <view class="bar-fill bar-protein" :style="{ width: proteinPercent + '%' }" />
          </view>
          <text class="summary-value">{{ dailyProtein }}/{{ plan.targetProtein || 0 }}g</text>
        </view>
        <view class="summary-row">
          <text class="summary-label">脂肪</text>
          <view class="bar-wrap">
            <view class="bar-fill bar-fat" :style="{ width: fatPercent + '%' }" />
          </view>
          <text class="summary-value">{{ dailyFat }}/{{ plan.targetFat || 0 }}g</text>
        </view>
        <view class="summary-row">
          <text class="summary-label">碳水</text>
          <view class="bar-wrap">
            <view class="bar-fill bar-carbs" :style="{ width: carbsPercent + '%' }" />
          </view>
          <text class="summary-value">{{ dailyCarbs }}/{{ plan.targetCarbs || 0 }}g</text>
        </view>
      </view>
    </view>

    <!-- Meal sections for selected day -->
    <view
      v-for="meal in mealSections"
      :key="meal.type"
      class="section"
    >
      <text class="section-title">{{ meal.icon }} {{ meal.label }}</text>
      <view
        v-for="(item, idx) in meal.items"
        :key="idx"
        class="meal-item"
      >
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
        <view
          v-if="item.recipeId"
          class="recipe-link"
          @tap="goRecipe(item.recipeId)"
        >
          <text class="recipe-link-text">查看食谱 →</text>
        </view>
      </view>
      <view v-if="meal.items.length === 0" class="meal-empty">
        <text class="meal-empty-text">暂无安排</text>
      </view>
    </view>

    <!-- Bottom spacer -->
    <view class="bottom-spacer" />

    <!-- Fixed action bar -->
    <view class="action-bar">
      <view
        class="action-btn fav-btn"
        :class="{ favorited: isFavorited }"
        @tap="toggleFavorite"
      >
        <text class="action-icon">{{ isFavorited ? '❤️' : '🤍' }}</text>
        <text class="action-label">{{ plan.favoriteCount || 0 }}</text>
      </view>
      <button class="action-btn share-btn" open-type="share">
        <text class="action-icon">📤</text>
        <text class="action-label">分享</text>
      </button>
    </view>
  </view>

  <!-- Loading state -->
  <view class="loading-page" v-else>
    <text class="loading-text">加载中...</text>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { request } from '@/utils/request'

const MEAL_TYPE_MAP: Record<string, { label: string; icon: string; order: number }> = {
  BREAKFAST: { label: '早餐', icon: '🌅', order: 0 },
  LUNCH: { label: '午餐', icon: '☀️', order: 1 },
  DINNER: { label: '晚餐', icon: '🌙', order: 2 },
  SNACK: { label: '加餐', icon: '🍎', order: 3 }
}

const props = defineProps({ id: String })

const plan = ref<any>(null)
const isFavorited = ref(false)
const selectedDay = ref(1)

const totalDays = computed(() => plan.value?.durationDays || 1)

const dailyData = computed(() => {
  const days = plan.value?.dailyPlans || plan.value?.days || []
  return days.find((d: any) => d.dayNumber === selectedDay.value) || days[selectedDay.value - 1] || null
})

const dailyItems = computed(() => dailyData.value?.items || dailyData.value?.meals || [])

const dailyCalories = computed(() =>
  dailyItems.value.reduce((sum: number, i: any) => sum + (i.calories || 0), 0)
)
const dailyProtein = computed(() =>
  dailyItems.value.reduce((sum: number, i: any) => sum + (i.protein || 0), 0)
)
const dailyFat = computed(() =>
  dailyItems.value.reduce((sum: number, i: any) => sum + (i.fat || 0), 0)
)
const dailyCarbs = computed(() =>
  dailyItems.value.reduce((sum: number, i: any) => sum + (i.carbs || 0), 0)
)

function safePercent(actual: number, target: number) {
  if (!target) return 0
  return Math.min(Math.round((actual / target) * 100), 100)
}

const caloriePercent = computed(() => safePercent(dailyCalories.value, plan.value?.targetCalories))
const proteinPercent = computed(() => safePercent(dailyProtein.value, plan.value?.targetProtein))
const fatPercent = computed(() => safePercent(dailyFat.value, plan.value?.targetFat))
const carbsPercent = computed(() => safePercent(dailyCarbs.value, plan.value?.targetCarbs))

const mealSections = computed(() => {
  const items = dailyItems.value
  const types = ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACK']
  return types.map(type => {
    const meta = MEAL_TYPE_MAP[type]
    return {
      type,
      icon: meta.icon,
      label: meta.label,
      items: items.filter((i: any) => i.mealType === type)
    }
  })
})

async function fetchPlan() {
  try {
    const res = await request({ url: `/meal-plans/${props.id}` })
    plan.value = res.data
    isFavorited.value = res.data?.isFavorited || false
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

async function toggleFavorite() {
  try {
    await request({ url: `/meal-plans/${props.id}/favorite`, method: 'POST' })
    isFavorited.value = !isFavorited.value
    if (plan.value) {
      plan.value.favoriteCount += isFavorited.value ? 1 : -1
    }
    uni.showToast({
      title: isFavorited.value ? '已收藏' : '已取消收藏',
      icon: 'none'
    })
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

function goRecipe(recipeId: number | string) {
  uni.navigateTo({ url: `/pages/recipes/detail?id=${recipeId}` })
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

.loading-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: $background;
}

.loading-text {
  font-size: 28rpx;
  color: $muted-foreground;
}

/* ===== Hero ===== */
.hero-wrap {
  width: 100%;
  height: 400rpx;
}

.hero-image {
  width: 100%;
  height: 100%;
}

/* ===== Section ===== */
.section {
  background: $card;
  margin: 20rpx 24rpx;
  padding: 28rpx;
  border-radius: $radius-xl;
  box-shadow: $shadow-sm;
}

.section-title {
  font-size: 30rpx;
  font-weight: 700;
  color: $foreground;
  margin-bottom: 20rpx;
  display: block;
}

/* ===== Title Section ===== */
.title-section {
  margin-top: -40rpx;
  position: relative;
  z-index: 2;
}

.plan-title {
  font-size: 36rpx;
  font-weight: 700;
  color: $foreground;
  display: block;
  line-height: 1.4;
}

.plan-desc {
  font-size: 26rpx;
  color: $muted-foreground;
  margin-top: 12rpx;
  display: block;
  line-height: 1.6;
}

.crowd-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 16rpx;
  padding: 12rpx 16rpx;
  background: $muted;
  border-radius: $radius-md;
}

.crowd-label {
  font-size: 24rpx;
  color: $muted-foreground;
  flex-shrink: 0;
}

.crowd-value {
  font-size: 24rpx;
  color: $foreground;
  font-weight: 500;
}

/* ===== Nutrition Grid ===== */
.nutrition-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr;
  gap: 16rpx;
}

.nut-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  padding: 20rpx 8rpx;
  background: $muted;
  border-radius: $radius-md;
}

.nut-icon {
  font-size: 32rpx;
}

.nut-value {
  font-size: 26rpx;
  font-weight: 600;
  color: $foreground;
}

.nut-label {
  font-size: 20rpx;
  color: $muted-foreground;
}

/* ===== Day Selector ===== */
.day-scroll {
  white-space: nowrap;
  margin-bottom: 20rpx;
}

.day-tabs {
  display: inline-flex;
  gap: 12rpx;
}

.day-tab {
  padding: 10rpx 24rpx;
  border-radius: $radius-full;
  background: $muted;
  flex-shrink: 0;
  transition: all 0.2s;
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
  white-space: nowrap;
}

/* ===== Daily Summary Bars ===== */
.daily-summary {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.summary-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.summary-label {
  font-size: 22rpx;
  color: $muted-foreground;
  width: 60rpx;
  flex-shrink: 0;
}

.bar-wrap {
  flex: 1;
  height: 14rpx;
  background: $muted;
  border-radius: $radius-full;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: $accent;
  border-radius: $radius-full;
  transition: width 0.3s;
}

.bar-protein {
  background: #3B82F6;
}

.bar-fat {
  background: #F59E0B;
}

.bar-carbs {
  background: #8B5CF6;
}

.summary-value {
  font-size: 20rpx;
  color: $muted-foreground;
  width: 160rpx;
  text-align: right;
  flex-shrink: 0;
}

/* ===== Meal Items ===== */
.meal-item {
  padding: 16rpx 0;
  border-bottom: 1rpx solid $border;
}

.meal-item:last-child {
  border-bottom: none;
}

.meal-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.meal-food-name {
  font-size: 28rpx;
  font-weight: 500;
  color: $foreground;
}

.meal-portion {
  font-size: 24rpx;
  color: $muted-foreground;
}

.meal-nutrition-row {
  display: flex;
  gap: 16rpx;
  margin-top: 10rpx;
  flex-wrap: wrap;
}

.meal-nut {
  font-size: 22rpx;
  color: $muted-foreground;
}

.recipe-link {
  margin-top: 10rpx;
}

.recipe-link-text {
  font-size: 24rpx;
  color: $accent;
  font-weight: 500;
}

.meal-empty {
  padding: 24rpx 0;
  text-align: center;
}

.meal-empty-text {
  font-size: 24rpx;
  color: $muted-foreground;
}

/* ===== Bottom Spacer & Action Bar ===== */
.bottom-spacer {
  height: 140rpx;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-around;
  height: 110rpx;
  padding-bottom: env(safe-area-inset-bottom);
  background: $card;
  border-top: 1rpx solid $border;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  padding: 0;
  margin: 0;
  background: none;
  border: none;
  line-height: 1.4;
}

.action-btn::after {
  display: none;
}

.action-icon {
  font-size: 36rpx;
}

.action-label {
  font-size: 22rpx;
  color: $muted-foreground;
}

.fav-btn.favorited .action-label {
  color: #EF4444;
}
</style>
