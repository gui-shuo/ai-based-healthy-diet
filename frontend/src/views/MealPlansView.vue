<template>
  <div class="meal-plans-view">
    <!-- 顶部导航 -->
    <nav class="top-nav">
      <div class="nav-inner">
        <div class="nav-left">
          <el-button text @click="router.push('/')">
            <el-icon><ArrowLeft /></el-icon>
            返回首页
          </el-button>
          <h2 class="page-title">🥗 营养餐计划</h2>
        </div>
        <div class="nav-right">
          <el-button type="primary" round @click="openFavorites">
            <el-icon><Star /></el-icon>
            我的收藏
          </el-button>
        </div>
      </div>
    </nav>

    <main class="main-area">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索餐计划..."
          size="large"
          clearable
          @keydown.enter="handleSearch"
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <!-- 饮食目标 -->
        <div class="filter-group">
          <div class="filter-tabs">
            <div
              v-for="goal in dietGoalOptions"
              :key="goal.value"
              class="filter-tab"
              :class="{ active: selectedDietGoal === goal.value }"
              @click="selectDietGoal(goal.value)"
            >
              {{ goal.label }}
            </div>
          </div>
        </div>

        <!-- 计划类型 & 排序 -->
        <div class="filter-row">
          <div class="plan-type-toggle">
            <el-radio-group v-model="selectedPlanType" size="default" @change="handleFilterChange">
              <el-radio-button value="">全部</el-radio-button>
              <el-radio-button value="DAILY">每日计划</el-radio-button>
              <el-radio-button value="WEEKLY">每周计划</el-radio-button>
            </el-radio-group>
          </div>
          <div class="sort-toggle">
            <el-radio-group v-model="selectedSort" size="default" @change="handleFilterChange">
              <el-radio-button value="latest">最新</el-radio-button>
              <el-radio-button value="popular">最热</el-radio-button>
              <el-radio-button value="calories_asc">低卡</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </div>

      <!-- 餐计划网格 -->
      <el-skeleton :loading="loading" animated :rows="8">
        <el-empty v-if="!mealPlans.length" description="暂无符合条件的餐计划" />
        <div v-else class="plan-grid">
          <div
            v-for="plan in mealPlans"
            :key="plan.id"
            class="plan-card"
            @click="openDetail(plan.id)"
          >
            <div class="card-cover">
              <img
                :src="plan.coverImage || defaultCover"
                :alt="plan.title"
                loading="lazy"
              />
              <div class="cover-overlay" />
              <span class="badge-goal" :style="{ background: goalColorMap[plan.dietGoal] || '#10B981' }">
                {{ dietGoalMap[plan.dietGoal] || plan.dietGoal }}
              </span>
              <span class="badge-type">
                {{ planTypeMap[plan.planType] || plan.planType }}
              </span>
            </div>
            <div class="card-body">
              <h3 class="card-title">{{ plan.title }}</h3>
              <p class="card-desc">{{ plan.description }}</p>
              <div class="card-info">
                <span>📅 {{ plan.durationDays }}天</span>
                <span>🔥 {{ plan.targetCalories }} kcal/日</span>
                <span v-if="plan.suitableCrowd">👥 {{ plan.suitableCrowd }}</span>
              </div>
              <div class="card-stats">
                <span class="stat"><el-icon><Star /></el-icon> {{ plan.favoriteCount || 0 }}</span>
                <span class="stat"><el-icon><View /></el-icon> {{ plan.viewCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-skeleton>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next, jumper"
          background
          @current-change="handlePageChange"
        />
      </div>
    </main>

    <!-- 餐计划详情抽屉 -->
    <el-drawer
      v-model="detailVisible"
      direction="rtl"
      size="680px"
      :title="detailData?.title || '餐计划详情'"
      class="detail-drawer"
      destroy-on-close
    >
      <div v-if="detailLoading" class="drawer-loading">
        <el-skeleton :rows="12" animated />
      </div>
      <div v-else-if="detailData" class="detail-content">
        <!-- 封面 -->
        <div class="detail-cover">
          <img :src="detailData.coverImage || defaultCover" :alt="detailData.title" />
        </div>

        <!-- 标题区 -->
        <div class="detail-header">
          <h2 class="detail-title">{{ detailData.title }}</h2>
          <p class="detail-desc">{{ detailData.description }}</p>
          <div class="detail-meta">
            <el-tag :color="goalColorMap[detailData.dietGoal]" effect="dark" style="border: none">
              {{ dietGoalMap[detailData.dietGoal] }}
            </el-tag>
            <el-tag type="info" effect="plain">{{ planTypeMap[detailData.planType] }}</el-tag>
            <span v-if="detailData.suitableCrowd" class="crowd-label">👥 {{ detailData.suitableCrowd }}</span>
          </div>
        </div>

        <!-- 目标营养摘要 -->
        <div class="nutrition-summary">
          <div class="nutri-card calories">
            <div class="nutri-icon">🔥</div>
            <div class="nutri-value">{{ detailData.targetCalories }}</div>
            <div class="nutri-label">千卡/日</div>
          </div>
          <div class="nutri-card protein">
            <div class="nutri-icon">🥩</div>
            <div class="nutri-value">{{ detailData.targetProtein }}g</div>
            <div class="nutri-label">蛋白质</div>
          </div>
          <div class="nutri-card fat">
            <div class="nutri-icon">🥑</div>
            <div class="nutri-value">{{ detailData.targetFat }}g</div>
            <div class="nutri-label">脂肪</div>
          </div>
          <div class="nutri-card carbs">
            <div class="nutri-icon">🍚</div>
            <div class="nutri-value">{{ detailData.targetCarbs }}g</div>
            <div class="nutri-label">碳水</div>
          </div>
        </div>

        <!-- 每日标签页 -->
        <el-tabs v-if="detailData.days && detailData.days.length" v-model="activeDay" class="day-tabs">
          <el-tab-pane
            v-for="day in detailData.days"
            :key="day.dayNumber"
            :label="'Day ' + day.dayNumber"
            :name="String(day.dayNumber)"
          >
            <!-- 日营养摘要 -->
            <div class="day-nutrition-bar">
              <span class="day-nutri-item">🔥 {{ day.totalCalories }} kcal</span>
              <span class="day-nutri-item">🥩 {{ day.totalProtein }}g 蛋白质</span>
              <span class="day-nutri-item">🥑 {{ day.totalFat }}g 脂肪</span>
              <span class="day-nutri-item">🍚 {{ day.totalCarbs }}g 碳水</span>
            </div>

            <!-- 各餐段 -->
            <div
              v-for="mealType in ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACK']"
              :key="mealType"
              class="meal-section"
            >
              <template v-if="getMealItems(day, mealType).length">
                <h4 class="meal-type-title">
                  {{ mealTypeIcons[mealType] }} {{ mealTypeMap[mealType] }}
                </h4>
                <div class="meal-items">
                  <div
                    v-for="(item, idx) in getMealItems(day, mealType)"
                    :key="idx"
                    class="meal-item"
                  >
                    <div class="item-main">
                      <span class="item-name">{{ item.foodName }}</span>
                      <span class="item-portion">{{ item.portion }}</span>
                    </div>
                    <div class="item-nutrition">
                      <span>{{ item.calories }} kcal</span>
                      <span>蛋白质 {{ item.protein }}g</span>
                      <span>脂肪 {{ item.fat }}g</span>
                      <span>碳水 {{ item.carbs }}g</span>
                    </div>
                    <a
                      v-if="item.recipeId"
                      class="recipe-link"
                      @click.stop="goToRecipe(item.recipeId)"
                    >
                      查看食谱 →
                    </a>
                  </div>
                </div>
              </template>
            </div>
          </el-tab-pane>
        </el-tabs>
        <el-empty v-else description="暂无每日安排数据" :image-size="80" />

        <!-- 收藏按钮 -->
        <div class="detail-actions">
          <el-button
            :type="isFavorited ? 'danger' : 'default'"
            size="large"
            round
            :loading="favoriteLoading"
            @click="toggleFavorite"
          >
            <el-icon><component :is="isFavorited ? StarFilled : Star" /></el-icon>
            {{ isFavorited ? '已收藏' : '收藏' }}
          </el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 我的收藏抽屉 -->
    <el-drawer
      v-model="favoritesVisible"
      direction="rtl"
      size="560px"
      title="我的收藏"
      class="favorites-drawer"
      destroy-on-close
    >
      <div v-if="favoritesLoading" class="drawer-loading">
        <el-skeleton :rows="6" animated />
      </div>
      <template v-else>
        <el-empty v-if="!favoritePlans.length" description="还没有收藏任何餐计划" />
        <div v-else class="fav-list">
          <div
            v-for="plan in favoritePlans"
            :key="plan.id"
            class="fav-card"
            @click="openDetail(plan.id)"
          >
            <div class="fav-cover">
              <img :src="plan.coverImage || defaultCover" :alt="plan.title" />
            </div>
            <div class="fav-body">
              <h4 class="fav-title">{{ plan.title }}</h4>
              <p class="fav-desc">{{ plan.description }}</p>
              <div class="fav-meta">
                <el-tag size="small" :color="goalColorMap[plan.dietGoal]" effect="dark" style="border: none">
                  {{ dietGoalMap[plan.dietGoal] }}
                </el-tag>
                <span>🔥 {{ plan.targetCalories }} kcal/日</span>
              </div>
            </div>
          </div>
        </div>
        <div v-if="favoritesTotal > favoritesPageSize" class="pagination-wrap">
          <el-pagination
            v-model:current-page="favoritesPage"
            :page-size="favoritesPageSize"
            :total="favoritesTotal"
            layout="prev, pager, next"
            background
            small
            @current-change="loadFavorites"
          />
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage as message } from 'element-plus'
import { ArrowLeft, Search, Star, StarFilled, View } from '@element-plus/icons-vue'
import api from '@/services/api'

const router = useRouter()
const authStore = useAuthStore()

const defaultCover = 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=600&h=340&fit=crop'

// ========== Maps ==========
const dietGoalMap = {
  BALANCED: '均衡饮食',
  WEIGHT_LOSS: '减脂塑形',
  MUSCLE_GAIN: '增肌增重',
  WELLNESS: '养生调理',
  DIABETES_FRIENDLY: '糖尿病适用',
  PREGNANCY: '孕期营养',
  POSTPARTUM: '产后恢复',
  ELDERLY: '老年营养'
}

const goalColorMap = {
  BALANCED: '#10B981',
  WEIGHT_LOSS: '#F59E0B',
  MUSCLE_GAIN: '#EF4444',
  WELLNESS: '#8B5CF6',
  DIABETES_FRIENDLY: '#3B82F6',
  PREGNANCY: '#EC4899',
  POSTPARTUM: '#F97316',
  ELDERLY: '#6366F1'
}

const planTypeMap = { DAILY: '每日计划', WEEKLY: '每周计划' }
const mealTypeMap = { BREAKFAST: '早餐', LUNCH: '午餐', DINNER: '晚餐', SNACK: '加餐' }
const mealTypeIcons = { BREAKFAST: '🌅', LUNCH: '☀️', DINNER: '🌙', SNACK: '🍎' }

const dietGoalOptions = [
  { value: '', label: '全部' },
  ...Object.entries(dietGoalMap).map(([value, label]) => ({ value, label }))
]

// ========== List state ==========
const mealPlans = ref([])
const loading = ref(false)
const keyword = ref('')
const selectedDietGoal = ref('')
const selectedPlanType = ref('')
const selectedSort = ref('latest')
const currentPage = ref(1)
const pageSize = 12
const total = ref(0)

// ========== Detail state ==========
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)
const activeDay = ref('1')
const isFavorited = ref(false)
const favoriteLoading = ref(false)

// ========== Favorites state ==========
const favoritesVisible = ref(false)
const favoritesLoading = ref(false)
const favoritePlans = ref([])
const favoritesPage = ref(1)
const favoritesPageSize = 12
const favoritesTotal = ref(0)

// ========== Fetch meal plans ==========
async function fetchMealPlans() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize,
      sort: selectedSort.value
    }
    if (selectedDietGoal.value) params.dietGoal = selectedDietGoal.value
    if (selectedPlanType.value) params.planType = selectedPlanType.value
    if (keyword.value.trim()) params.keyword = keyword.value.trim()

    const res = await api.get('/meal-plans', { params })
    const page = res.data?.data
    mealPlans.value = page?.content || []
    total.value = page?.totalElements ?? page?.total ?? 0
  } catch (e) {
    message.error('加载餐计划失败，请稍后重试')
    console.error(e)
  } finally {
    loading.value = false
  }
}

// ========== Filter handlers ==========
function selectDietGoal(val) {
  selectedDietGoal.value = val
  currentPage.value = 1
  fetchMealPlans()
}

function handleFilterChange() {
  currentPage.value = 1
  fetchMealPlans()
}

function handleSearch() {
  currentPage.value = 1
  fetchMealPlans()
}

function handlePageChange(page) {
  currentPage.value = page
  fetchMealPlans()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// ========== Detail ==========
async function openDetail(id) {
  detailVisible.value = true
  detailLoading.value = true
  activeDay.value = '1'
  try {
    const res = await api.get(`/meal-plans/${id}`)
    const data = res.data?.data
    detailData.value = data?.mealPlan || data
    isFavorited.value = data?.isFavorited ?? false
  } catch (e) {
    message.error('加载详情失败')
    console.error(e)
  } finally {
    detailLoading.value = false
  }
}

function getMealItems(day, mealType) {
  if (!day?.items) return []
  return day.items.filter(item => item.mealType === mealType)
}

function goToRecipe(recipeId) {
  router.push(`/recipes/${recipeId}`)
}

// ========== Favorite ==========
async function toggleFavorite() {
  if (!authStore.isLoggedIn) {
    message.warning('请先登录')
    return
  }
  if (!detailData.value) return

  favoriteLoading.value = true
  try {
    const res = await api.post(`/meal-plans/${detailData.value.id}/favorite`)
    const data = res.data?.data
    isFavorited.value = data?.favorited ?? !isFavorited.value
    message.success(isFavorited.value ? '已收藏' : '已取消收藏')

    // Update count in list
    const plan = mealPlans.value.find(p => p.id === detailData.value.id)
    if (plan) {
      plan.favoriteCount = (plan.favoriteCount || 0) + (isFavorited.value ? 1 : -1)
    }
  } catch (e) {
    message.error('操作失败，请重试')
    console.error(e)
  } finally {
    favoriteLoading.value = false
  }
}

// ========== Favorites drawer ==========
function openFavorites() {
  if (!authStore.isLoggedIn) {
    message.warning('请先登录')
    return
  }
  favoritesVisible.value = true
  favoritesPage.value = 1
  loadFavorites()
}

async function loadFavorites() {
  favoritesLoading.value = true
  try {
    const res = await api.get('/meal-plans/my-favorites', {
      params: { page: favoritesPage.value, size: favoritesPageSize }
    })
    const page = res.data?.data
    favoritePlans.value = page?.content || []
    favoritesTotal.value = page?.totalElements ?? page?.total ?? 0
  } catch (e) {
    message.error('加载收藏失败')
    console.error(e)
  } finally {
    favoritesLoading.value = false
  }
}

// ========== Init ==========
onMounted(() => {
  fetchMealPlans()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.meal-plans-view {
  min-height: 100vh;
  background: $background;
  font-family: $font-body;
  color: $foreground;
}

// ========== Top nav ==========
.top-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid $border;
  box-shadow: $shadow-sm;
}

.nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-title {
  font-family: $font-display;
  font-size: 22px;
  margin: 0;
  background: $gradient-accent;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

// ========== Main area ==========
.main-area {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

// ========== Search bar ==========
.search-bar {
  margin-bottom: 20px;
}

.search-input {
  :deep(.el-input__wrapper) {
    border-radius: $radius-lg;
    box-shadow: $shadow-sm;
    border: 1px solid $border;
    padding: 4px 12px;

    &:hover,
    &:focus-within {
      border-color: $accent;
      box-shadow: $shadow-accent;
    }
  }

  :deep(.el-input-group__append) {
    border-radius: 0 $radius-lg $radius-lg 0;
    background: $gradient-accent;
    color: #fff;
    border: none;

    .el-button {
      color: #fff;
    }
  }
}

// ========== Filter bar ==========
.filter-bar {
  margin-bottom: 24px;
}

.filter-group {
  margin-bottom: 12px;
}

.filter-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-tab {
  padding: 6px 16px;
  border-radius: $radius-full;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.25s ease;
  background: $card;
  border: 1px solid $border;
  color: $text-secondary;
  white-space: nowrap;

  &:hover {
    border-color: $accent;
    color: $accent;
  }

  &.active {
    background: $gradient-accent;
    color: #fff;
    border-color: transparent;
    box-shadow: $shadow-accent;
  }
}

.filter-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;

  :deep(.el-radio-button__inner) {
    border-radius: $radius-md !important;
    border: 1px solid $border !important;
    box-shadow: none !important;
    font-size: 13px;
  }

  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    background: $accent;
    border-color: $accent !important;
    color: #fff;
    box-shadow: $shadow-accent !important;
  }

  :deep(.el-radio-group) {
    display: flex;
    gap: 6px;
  }

  :deep(.el-radio-button:first-child .el-radio-button__inner),
  :deep(.el-radio-button:last-child .el-radio-button__inner) {
    border-radius: $radius-md !important;
  }
}

// ========== Plan grid ==========
.plan-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.plan-card {
  background: $card;
  border-radius: $radius-xl;
  overflow: hidden;
  border: 1px solid $border;
  box-shadow: $shadow-sm;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 10px 15px rgba(0, 0, 0, 0.1);
    border-color: $accent-secondary;

    .card-cover img {
      transform: scale(1.05);
    }
  }
}

.card-cover {
  position: relative;
  aspect-ratio: 16 / 9;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.4s ease;
  }
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.45) 0%, transparent 50%);
  pointer-events: none;
}

.badge-goal {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 10px;
  border-radius: $radius-full;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  backdrop-filter: blur(4px);
}

.badge-type {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 10px;
  border-radius: $radius-full;
  font-size: 11px;
  font-weight: 600;
  color: $foreground;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
}

.card-body {
  padding: 16px;
}

.card-title {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 700;
  color: $foreground;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-desc {
  margin: 0 0 10px;
  font-size: 13px;
  color: $text-secondary;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-info {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 12px;
  color: $text-secondary;
  margin-bottom: 10px;

  span {
    white-space: nowrap;
  }
}

.card-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: $text-disabled;

  .stat {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

// ========== Pagination ==========
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;

  :deep(.el-pagination.is-background .el-pager li.is-active) {
    background: $accent;
  }
}

// ========== Detail drawer ==========
.detail-drawer {
  :deep(.el-drawer__header) {
    margin-bottom: 0;
    padding: 16px 24px;
    border-bottom: 1px solid $border;
  }

  :deep(.el-drawer__body) {
    padding: 0;
  }
}

.drawer-loading {
  padding: 24px;
}

.detail-content {
  padding-bottom: 32px;
}

.detail-cover {
  width: 100%;
  aspect-ratio: 16 / 9;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.detail-header {
  padding: 20px 24px 0;
}

.detail-title {
  font-family: $font-display;
  font-size: 24px;
  margin: 0 0 8px;
  color: $foreground;
}

.detail-desc {
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.6;
  margin: 0 0 12px;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;

  .crowd-label {
    font-size: 13px;
    color: $text-secondary;
  }
}

// ========== Nutrition summary ==========
.nutrition-summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 20px 24px;
}

.nutri-card {
  text-align: center;
  padding: 14px 8px;
  border-radius: $radius-lg;
  transition: transform 0.2s ease;

  &:hover {
    transform: translateY(-2px);
  }

  &.calories {
    background: rgba(239, 68, 68, 0.08);
  }
  &.protein {
    background: rgba(59, 130, 246, 0.08);
  }
  &.fat {
    background: rgba(245, 158, 11, 0.08);
  }
  &.carbs {
    background: rgba(16, 185, 129, 0.08);
  }
}

.nutri-icon {
  font-size: 22px;
  margin-bottom: 4px;
}

.nutri-value {
  font-size: 20px;
  font-weight: 700;
  color: $foreground;
}

.nutri-label {
  font-size: 12px;
  color: $text-secondary;
  margin-top: 2px;
}

// ========== Day tabs ==========
.day-tabs {
  padding: 0 24px;

  :deep(.el-tabs__item) {
    font-weight: 600;
    font-size: 14px;

    &.is-active {
      color: $accent;
    }
  }

  :deep(.el-tabs__active-bar) {
    background: $accent;
  }
}

.day-nutrition-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 12px 16px;
  background: $muted;
  border-radius: $radius-md;
  margin-bottom: 16px;
}

.day-nutri-item {
  font-size: 13px;
  color: $text-secondary;
  font-weight: 500;
}

// ========== Meal sections ==========
.meal-section {
  margin-bottom: 20px;
}

.meal-type-title {
  font-size: 15px;
  font-weight: 700;
  color: $foreground;
  margin: 0 0 10px;
  padding-bottom: 6px;
  border-bottom: 2px solid $border;
}

.meal-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meal-item {
  background: $card;
  border: 1px solid $border;
  border-radius: $radius-md;
  padding: 12px 14px;
  transition: border-color 0.2s ease;

  &:hover {
    border-color: $accent-secondary;
  }
}

.item-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.item-name {
  font-weight: 600;
  font-size: 14px;
  color: $foreground;
}

.item-portion {
  font-size: 12px;
  color: $text-secondary;
  background: $muted;
  padding: 2px 8px;
  border-radius: $radius-full;
}

.item-nutrition {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: $text-secondary;
}

.recipe-link {
  display: inline-block;
  margin-top: 6px;
  font-size: 12px;
  color: $accent;
  cursor: pointer;
  font-weight: 600;
  transition: color 0.2s ease;

  &:hover {
    color: darken(#10B981, 10%);
    text-decoration: underline;
  }
}

// ========== Detail actions ==========
.detail-actions {
  padding: 24px;
  display: flex;
  justify-content: center;

  .el-button {
    min-width: 160px;
    font-weight: 600;
    font-size: 15px;
  }
}

// ========== Favorites drawer ==========
.favorites-drawer {
  :deep(.el-drawer__header) {
    margin-bottom: 0;
    padding: 16px 24px;
    border-bottom: 1px solid $border;
  }

  :deep(.el-drawer__body) {
    padding: 16px;
  }
}

.fav-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.fav-card {
  display: flex;
  gap: 12px;
  background: $card;
  border: 1px solid $border;
  border-radius: $radius-lg;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 15px rgba(0, 0, 0, 0.1);
    border-color: $accent-secondary;
  }
}

.fav-cover {
  width: 120px;
  min-height: 90px;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.fav-body {
  flex: 1;
  padding: 10px 12px 10px 0;
  min-width: 0;
}

.fav-title {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 700;
  color: $foreground;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fav-desc {
  margin: 0 0 6px;
  font-size: 12px;
  color: $text-secondary;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.fav-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: $text-secondary;
}

// ========== Responsive ==========
@media (max-width: $breakpoint-tablet) {
  .plan-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .nutrition-summary {
    grid-template-columns: repeat(2, 1fr);
  }

  .detail-drawer {
    :deep(.el-drawer) {
      width: 90% !important;
    }
  }

  .nav-inner {
    padding: 10px 16px;
  }

  .main-area {
    padding: 16px;
  }
}

@media (max-width: $breakpoint-mobile) {
  .plan-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .page-title {
    font-size: 18px;
  }

  .filter-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .nutrition-summary {
    grid-template-columns: repeat(2, 1fr);
    padding: 16px;
  }

  .item-nutrition {
    flex-wrap: wrap;
    gap: 6px;
  }

  .day-nutrition-bar {
    gap: 8px;
  }

  .detail-drawer,
  .favorites-drawer {
    :deep(.el-drawer) {
      width: 100% !important;
    }
  }
}
</style>
