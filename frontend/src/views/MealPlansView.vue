<template>
  <div class="meal-plans-view">
    <!-- Header -->
    <header class="page-header">
      <div class="header-inner">
        <h1 class="page-title">🍽️ 营养餐计划</h1>
        <p class="page-subtitle">科学搭配，为你定制专属营养方案</p>
      </div>
    </header>

    <!-- Main Tabs -->
    <el-tabs v-model="activeTab" class="main-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="🍽️ 发现" name="discover" />
      <el-tab-pane label="📋 我的跟随" name="follows" />
      <el-tab-pane label="⭐ 我的收藏" name="favorites" />
    </el-tabs>

    <!-- ==================== DISCOVER TAB ==================== -->
    <div v-show="activeTab === 'discover'" class="tab-content">
      <!-- Tag Cloud -->
      <section v-if="tags.length" class="tag-cloud-section">
        <h3 class="section-label">🔥 热门标签</h3>
        <div class="tag-cloud">
          <span
            v-for="tag in tags"
            :key="tag"
            class="tag-chip"
            :class="{ active: selectedTag === tag }"
            @click="toggleTag(tag)"
          >{{ tag }}</span>
        </div>
      </section>

      <!-- Recommended Carousel -->
      <section v-if="recommendedPlans.length" class="recommended-section">
        <h3 class="section-label">✨ 为你推荐</h3>
        <div class="recommended-scroll">
          <div
            v-for="plan in recommendedPlans"
            :key="'rec-' + plan.id"
            class="rec-card"
            @click="openDetail(plan.id)"
          >
            <img :src="plan.coverImage || defaultCover" :alt="plan.title" class="rec-cover" />
            <div class="rec-info">
              <span class="goal-badge" :style="{ background: goalColorMap[plan.dietGoal] || '#999' }">
                {{ dietGoalMap[plan.dietGoal] || plan.dietGoal }}
              </span>
              <h4 class="rec-title">{{ plan.title }}</h4>
              <div class="rec-meta">
                <span>{{ plan.targetCalories }}kcal</span>
                <span>{{ plan.durationDays }}天</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Search & Filters -->
      <section class="filters-section">
        <div class="search-row">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索营养餐计划..."
            clearable
            :prefix-icon="SearchIcon"
            class="search-input"
            @keyup.enter="loadPlans(1)"
            @clear="loadPlans(1)"
          />
          <el-button type="primary" :icon="SearchIcon" @click="loadPlans(1)">搜索</el-button>
        </div>

        <div class="filter-row">
          <div class="filter-group">
            <span class="filter-label">目标：</span>
            <el-radio-group v-model="filters.dietGoal" size="small" @change="loadPlans(1)">
              <el-radio-button value="">全部</el-radio-button>
              <el-radio-button v-for="(label, key) in dietGoalMap" :key="key" :value="key">
                {{ label }}
              </el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <div class="filter-row">
          <div class="filter-group">
            <span class="filter-label">类型：</span>
            <el-radio-group v-model="filters.planType" size="small" @change="loadPlans(1)">
              <el-radio-button value="">全部</el-radio-button>
              <el-radio-button value="DAILY">单日</el-radio-button>
              <el-radio-button value="WEEKLY">周计划</el-radio-button>
              <el-radio-button value="MONTHLY">月计划</el-radio-button>
            </el-radio-group>
          </div>

          <div class="filter-group">
            <span class="filter-label">排序：</span>
            <el-radio-group v-model="filters.sort" size="small" @change="loadPlans(1)">
              <el-radio-button value="latest">最新</el-radio-button>
              <el-radio-button value="popular">最热</el-radio-button>
              <el-radio-button value="rating">评分</el-radio-button>
              <el-radio-button value="followers">跟随</el-radio-button>
              <el-radio-button value="calories_asc">低卡优先</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </section>

      <!-- Plan Grid -->
      <section class="plans-grid-section">
        <el-skeleton :loading="loadingPlans" animated :count="6">
          <template #template>
            <div class="skeleton-card">
              <el-skeleton-item variant="image" style="height: 180px" />
              <el-skeleton-item variant="h3" style="margin: 12px 0 8px" />
              <el-skeleton-item variant="text" />
              <el-skeleton-item variant="text" style="width: 60%" />
            </div>
          </template>
        </el-skeleton>

        <div v-if="!loadingPlans && plans.length" class="plans-grid">
          <div
            v-for="plan in plans"
            :key="plan.id"
            class="plan-card"
            @click="openDetail(plan.id)"
          >
            <div class="card-cover">
              <img :src="plan.coverImage || defaultCover" :alt="plan.title" />
              <span class="goal-badge floating" :style="{ background: goalColorMap[plan.dietGoal] || '#999' }">
                {{ dietGoalMap[plan.dietGoal] || plan.dietGoal }}
              </span>
              <span
                class="difficulty-badge floating"
                :style="{ background: difficultyColors[plan.difficulty] || '#999' }"
              >
                {{ difficultyMap[plan.difficulty] || plan.difficulty }}
              </span>
            </div>
            <div class="card-body">
              <h3 class="card-title">{{ plan.title }}</h3>
              <p class="card-desc">{{ plan.description }}</p>
              <div class="card-stats">
                <span class="stat">📅 {{ plan.durationDays }}天</span>
                <span class="stat">🔥 {{ plan.targetCalories }}kcal</span>
              </div>
              <div class="card-footer">
                <div class="rating-info">
                  <el-rate
                    :model-value="plan.avgRating || 0"
                    disabled
                    show-score
                    :score-template="(plan.avgRating || 0).toFixed(1)"
                    size="small"
                  />
                  <span class="count">({{ plan.ratingCount || 0 }})</span>
                </div>
                <div class="follow-info">
                  <el-icon><UserFilled /></el-icon>
                  <span>{{ plan.followCount || 0 }}</span>
                  <el-icon style="margin-left: 8px"><View /></el-icon>
                  <span>{{ plan.viewCount || 0 }}</span>
                </div>
              </div>
              <div v-if="plan.tags" class="card-tags">
                <el-tag
                  v-for="t in parseTags(plan.tags)"
                  :key="t"
                  size="small"
                  type="info"
                  effect="plain"
                  round
                >{{ t }}</el-tag>
              </div>
            </div>
          </div>
        </div>

        <el-empty v-if="!loadingPlans && !plans.length" description="暂无营养餐计划" />

        <div v-if="totalPlans > filters.size" class="pagination-wrap">
          <el-pagination
            v-model:current-page="filters.page"
            :page-size="filters.size"
            :total="totalPlans"
            layout="prev, pager, next"
            @current-change="loadPlans"
          />
        </div>
      </section>
    </div>

    <!-- ==================== MY FOLLOWS TAB ==================== -->
    <div v-show="activeTab === 'follows'" class="tab-content">
      <el-skeleton :loading="loadingFollows" animated :count="3">
        <template #template>
          <div class="skeleton-card" style="height: 120px">
            <el-skeleton-item variant="h3" />
            <el-skeleton-item variant="text" style="width: 80%" />
          </div>
        </template>
      </el-skeleton>

      <div v-if="!loadingFollows && myFollows.length" class="follows-list">
        <div v-for="follow in myFollows" :key="follow.id" class="follow-card">
          <div class="follow-header">
            <div class="follow-title-row">
              <h3>{{ follow.mealPlan?.title || '营养餐计划' }}</h3>
              <span
                v-if="follow.mealPlan?.dietGoal"
                class="goal-badge small"
                :style="{ background: goalColorMap[follow.mealPlan.dietGoal] || '#999' }"
              >
                {{ dietGoalMap[follow.mealPlan.dietGoal] || follow.mealPlan.dietGoal }}
              </span>
            </div>
            <p class="follow-status-text">
              第 {{ follow.currentDay || 1 }} / {{ follow.mealPlan?.durationDays || '?' }} 天
            </p>
          </div>
          <div class="follow-progress">
            <el-progress
              :percentage="calcFollowProgress(follow)"
              :stroke-width="12"
              :color="accentColor"
              striped
              striped-flow
            />
          </div>
          <div class="follow-actions">
            <el-button type="primary" size="small" @click="openDetail(follow.mealPlan?.id)">
              继续打卡
            </el-button>
            <el-button size="small" type="danger" plain @click="handleUnfollow(follow.mealPlan?.id)">
              放弃跟随
            </el-button>
          </div>
        </div>
      </div>

      <el-empty v-if="!loadingFollows && !myFollows.length" description="还没有跟随任何计划">
        <el-button type="primary" @click="activeTab = 'discover'">去发现营养餐</el-button>
      </el-empty>
    </div>

    <!-- ==================== MY FAVORITES TAB ==================== -->
    <div v-show="activeTab === 'favorites'" class="tab-content">
      <el-skeleton :loading="loadingFavorites" animated :count="4">
        <template #template>
          <div class="skeleton-card">
            <el-skeleton-item variant="image" style="height: 160px" />
            <el-skeleton-item variant="h3" style="margin-top: 12px" />
          </div>
        </template>
      </el-skeleton>

      <div v-if="!loadingFavorites && myFavorites.length" class="plans-grid">
        <div
          v-for="plan in myFavorites"
          :key="'fav-' + plan.id"
          class="plan-card"
          @click="openDetail(plan.id)"
        >
          <div class="card-cover">
            <img :src="plan.coverImage || defaultCover" :alt="plan.title" />
            <span class="goal-badge floating" :style="{ background: goalColorMap[plan.dietGoal] || '#999' }">
              {{ dietGoalMap[plan.dietGoal] || plan.dietGoal }}
            </span>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ plan.title }}</h3>
            <p class="card-desc">{{ plan.description }}</p>
            <div class="card-stats">
              <span class="stat">📅 {{ plan.durationDays }}天</span>
              <span class="stat">🔥 {{ plan.targetCalories }}kcal</span>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="!loadingFavorites && !myFavorites.length" description="还没有收藏任何计划">
        <el-button type="primary" @click="activeTab = 'discover'">去发现营养餐</el-button>
      </el-empty>
    </div>

    <!-- ==================== PLAN DETAIL DRAWER ==================== -->
    <el-drawer
      v-model="drawerVisible"
      :size="drawerWidth"
      direction="rtl"
      :destroy-on-close="true"
      class="detail-drawer"
    >
      <template #header>
        <span class="drawer-title">{{ detail?.title || '餐计划详情' }}</span>
      </template>

      <div v-if="loadingDetail" class="drawer-loading">
        <el-skeleton animated :count="4">
          <template #template>
            <el-skeleton-item variant="image" style="height: 200px; margin-bottom: 16px" />
            <el-skeleton-item variant="h3" />
            <el-skeleton-item variant="text" />
            <el-skeleton-item variant="text" style="width: 60%" />
          </template>
        </el-skeleton>
      </div>

      <div v-else-if="detail" class="detail-content">
        <!-- Cover -->
        <img :src="detail.coverImage || defaultCover" alt="" class="detail-cover" />

        <!-- Title & Badges -->
        <h2 class="detail-title">{{ detail.title }}</h2>
        <div class="detail-badges">
          <span class="goal-badge" :style="{ background: goalColorMap[detail.dietGoal] || '#999' }">
            {{ dietGoalMap[detail.dietGoal] || detail.dietGoal }}
          </span>
          <span class="type-badge">{{ planTypeMap[detail.planType] || detail.planType }}</span>
          <span class="difficulty-badge" :style="{ background: difficultyColors[detail.difficulty] || '#999' }">
            {{ difficultyMap[detail.difficulty] || detail.difficulty }}
          </span>
        </div>
        <p class="detail-desc">{{ detail.description }}</p>
        <p v-if="detail.suitableCrowd" class="detail-crowd">
          <el-icon><User /></el-icon> 适宜人群：{{ detail.suitableCrowd }}
        </p>
        <div v-if="detail.tags" class="detail-tags">
          <el-tag
            v-for="t in parseTags(detail.tags)"
            :key="t"
            size="small"
            round
            effect="plain"
          >{{ t }}</el-tag>
        </div>

        <!-- Nutrition Summary -->
        <div class="nutrition-cards">
          <div class="nutri-card calories">
            <span class="nutri-value">{{ detail.targetCalories }}</span>
            <span class="nutri-label">千卡</span>
          </div>
          <div class="nutri-card protein">
            <span class="nutri-value">{{ detail.targetProtein }}g</span>
            <span class="nutri-label">蛋白质</span>
          </div>
          <div class="nutri-card fat">
            <span class="nutri-value">{{ detail.targetFat }}g</span>
            <span class="nutri-label">脂肪</span>
          </div>
          <div class="nutri-card carbs">
            <span class="nutri-value">{{ detail.targetCarbs }}g</span>
            <span class="nutri-label">碳水</span>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="detail-actions">
          <el-button
            :type="detailIsFollowing ? 'danger' : 'primary'"
            :loading="actionLoading"
            @click="toggleFollow"
          >
            {{ detailIsFollowing ? '取消跟随' : '开始跟随' }}
          </el-button>
          <el-button
            :icon="detailIsFavorited ? StarFilled : Star"
            :class="{ favorited: detailIsFavorited }"
            :loading="actionLoading"
            @click="toggleFavorite"
          >
            {{ detailIsFavorited ? '已收藏' : '收藏' }}
          </el-button>
        </div>

        <!-- Follow Progress (if following) -->
        <div v-if="detailIsFollowing && followProgress" class="follow-progress-section">
          <h4>📊 跟随进度</h4>
          <el-progress
            :percentage="followProgress.progress || 0"
            :stroke-width="14"
            :color="accentColor"
            striped
            striped-flow
          />
          <p class="progress-text">
            已打卡 {{ followProgress.checkedDays || 0 }} / {{ followProgress.totalDays || 0 }} 天，
            共 {{ followProgress.totalCheckins || 0 }} 次
          </p>
          <div v-if="followProgress.progress >= 100" class="completion-banner">
            🎉 恭喜你！已完成全部计划！
          </div>
        </div>

        <!-- Daily Tabs -->
        <div v-if="detail.days && detail.days.length" class="days-section">
          <h4>📅 每日安排</h4>
          <el-tabs v-model="activeDayTab" type="card" class="day-tabs">
            <el-tab-pane
              v-for="day in sortedDays"
              :key="day.dayNumber"
              :label="'第' + day.dayNumber + '天'"
              :name="String(day.dayNumber)"
            >
              <!-- Day Nutrition Bar -->
              <div class="day-nutrition-bar">
                <span>🔥 {{ day.totalCalories }}kcal</span>
                <span>蛋白 {{ day.totalProtein }}g</span>
                <span>脂肪 {{ day.totalFat }}g</span>
                <span>碳水 {{ day.totalCarbs }}g</span>
              </div>

              <!-- Meal Sections -->
              <div v-for="mealType in mealTypes" :key="mealType" class="meal-section">
                <div class="meal-header">
                  <span class="meal-icon">{{ mealTypeIcons[mealType] }}</span>
                  <span class="meal-type-label">{{ mealTypeMap[mealType] }}</span>

                  <!-- Check-in checkbox -->
                  <label
                    v-if="detailIsFollowing && detailFollowId"
                    class="checkin-toggle"
                    @click.stop
                  >
                    <input
                      type="checkbox"
                      :checked="isCheckedIn(day.dayNumber, mealType)"
                      @change="handleCheckin(day.dayNumber, mealType, $event)"
                    />
                    <span class="checkin-label">{{ isCheckedIn(day.dayNumber, mealType) ? '已打卡' : '打卡' }}</span>
                  </label>
                </div>

                <div
                  v-for="(item, idx) in getMealItems(day, mealType)"
                  :key="item.foodName + '-' + idx"
                  class="meal-item"
                >
                  <div class="item-main">
                    <span class="item-name">{{ item.foodName }}</span>
                    <span v-if="item.portion" class="item-portion">{{ item.portion }}</span>
                  </div>
                  <p v-if="item.description" class="item-desc">{{ item.description }}</p>
                  <div class="item-macros">
                    <span>{{ item.calories }}kcal</span>
                    <span>蛋白{{ item.protein }}g</span>
                    <span>脂肪{{ item.fat }}g</span>
                    <span>碳水{{ item.carbs }}g</span>
                  </div>
                  <a
                    v-if="item.recipeId || item.recipeCorpusId"
                    class="recipe-link"
                    @click.stop="goToRecipe(item)"
                  >查看食谱 →</a>
                </div>

                <p v-if="!getMealItems(day, mealType).length" class="no-items">暂无安排</p>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- Rating Section -->
        <div class="rating-section">
          <h4>⭐ 评分与评价</h4>
          <div v-if="authStore.isLoggedIn" class="my-rating">
            <p class="rating-prompt">你的评分：</p>
            <el-rate v-model="userRatingValue" :colors="['#F59E0B', '#F59E0B', '#F59E0B']" />
            <el-input
              v-model="userReviewText"
              type="textarea"
              :rows="2"
              placeholder="写一句评价（可选）"
              maxlength="200"
              show-word-limit
              style="margin-top: 8px"
            />
            <el-button
              type="primary"
              size="small"
              style="margin-top: 8px"
              :loading="submittingRating"
              :disabled="!userRatingValue"
              @click="submitRating"
            >提交评价</el-button>
          </div>

          <!-- Ratings List -->
          <div v-if="ratings.length" class="ratings-list">
            <div v-for="r in ratings" :key="r.id" class="rating-item">
              <div class="rating-item-header">
                <span class="rater-name">{{ r.user?.nickname || r.user?.username || '用户' }}</span>
                <el-rate :model-value="r.rating" disabled size="small" />
              </div>
              <p v-if="r.review" class="rating-review">{{ r.review }}</p>
              <span class="rating-date">{{ formatDate(r.createdAt) }}</span>
            </div>
          </div>
          <el-empty v-else-if="!loadingRatings" description="暂无评价" :image-size="60" />

          <div v-if="totalRatings > ratingsPage.size" class="pagination-wrap small">
            <el-pagination
              v-model:current-page="ratingsPage.page"
              :page-size="ratingsPage.size"
              :total="totalRatings"
              layout="prev, pager, next"
              small
              @current-change="loadRatings"
            />
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import message from '@/utils/message'
import api from '@/services/api'
import {
  Search as SearchIcon,
  Star,
  StarFilled,
  UserFilled,
  View,
  User,
} from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const defaultCover = 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=600&h=340&fit=crop'
const accentColor = '#10B981'

// ========== Maps ==========
const dietGoalMap = {
  BALANCED: '均衡饮食',
  WEIGHT_LOSS: '减脂塑形',
  MUSCLE_GAIN: '增肌增重',
  WELLNESS: '养生调理',
  DIABETES_FRIENDLY: '控糖饮食',
  PREGNANCY: '孕期营养',
  POSTPARTUM: '产后恢复',
  ELDERLY: '老年养护',
}
const goalColorMap = {
  BALANCED: '#10B981',
  WEIGHT_LOSS: '#F59E0B',
  MUSCLE_GAIN: '#EF4444',
  WELLNESS: '#8B5CF6',
  DIABETES_FRIENDLY: '#3B82F6',
  PREGNANCY: '#EC4899',
  POSTPARTUM: '#F97316',
  ELDERLY: '#6366F1',
}
const difficultyMap = { EASY: '简单', MEDIUM: '适中', HARD: '挑战' }
const difficultyColors = { EASY: '#10B981', MEDIUM: '#F59E0B', HARD: '#EF4444' }
const planTypeMap = { DAILY: '单日计划', WEEKLY: '周计划', MONTHLY: '月计划' }
const mealTypeMap = { BREAKFAST: '早餐', LUNCH: '午餐', DINNER: '晚餐', SNACK: '加餐' }
const mealTypeIcons = { BREAKFAST: '🌅', LUNCH: '☀️', DINNER: '🌙', SNACK: '🍪' }
const mealTypes = ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACK']

// ========== Discover State ==========
const activeTab = ref('discover')
const tags = ref([])
const selectedTag = ref('')
const recommendedPlans = ref([])
const plans = ref([])
const totalPlans = ref(0)
const loadingPlans = ref(false)

const filters = reactive({
  page: 1,
  size: 12,
  keyword: '',
  dietGoal: '',
  planType: '',
  sort: 'latest',
})

// ========== Follows State ==========
const myFollows = ref([])
const loadingFollows = ref(false)

// ========== Favorites State ==========
const myFavorites = ref([])
const loadingFavorites = ref(false)

// ========== Detail State ==========
const drawerVisible = ref(false)
const loadingDetail = ref(false)
const detail = ref(null)
const detailIsFavorited = ref(false)
const detailIsFollowing = ref(false)
const detailFollowId = ref(null)
const actionLoading = ref(false)
const activeDayTab = ref('1')

// Follow progress
const followProgress = ref(null)
const checkinSet = ref(new Set())

// Ratings
const ratings = ref([])
const totalRatings = ref(0)
const loadingRatings = ref(false)
const ratingsPage = reactive({ page: 1, size: 10 })
const userRatingValue = ref(0)
const userReviewText = ref('')
const submittingRating = ref(false)

// Responsive drawer width
const drawerWidth = computed(() => {
  if (typeof window !== 'undefined' && window.innerWidth < 768) return '100%'
  return '780px'
})

const sortedDays = computed(() => {
  if (!detail.value?.days) return []
  return [...detail.value.days].sort((a, b) => a.dayNumber - b.dayNumber)
})

// ========== Helpers ==========
function parseTags(tagStr) {
  if (!tagStr) return []
  return tagStr.split(',').map(t => t.trim()).filter(Boolean)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
}

function getMealItems(day, mealType) {
  if (!day?.items) return []
  return day.items.filter(i => i.mealType === mealType)
}

function calcFollowProgress(follow) {
  const total = follow.mealPlan?.durationDays || 1
  const current = follow.currentDay || 0
  return Math.min(Math.round((current / total) * 100), 100)
}

function goToRecipe(item) {
  if (item.recipeId) {
    router.push('/recipes?id=' + item.recipeId)
  } else if (item.recipeCorpusId) {
    router.push('/recipes?corpusId=' + item.recipeCorpusId)
  }
}

// ========== API: Tags & Recommended ==========
async function loadTags() {
  try {
    const res = await api.get('/meal-plans/tags')
    if (res.data?.code === 200) {
      tags.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载标签失败', e)
  }
}

async function loadRecommended() {
  try {
    const res = await api.get('/meal-plans/recommendations')
    if (res.data?.code === 200) {
      recommendedPlans.value = res.data.data || []
    }
    if (!recommendedPlans.value.length) {
      const res2 = await api.get('/meal-plans/featured')
      if (res2.data?.code === 200) {
        recommendedPlans.value = res2.data.data || []
      }
    }
  } catch (e) {
    console.error('加载推荐失败', e)
  }
}

// ========== API: Plan List ==========
async function loadPlans(page) {
  if (page) filters.page = page
  loadingPlans.value = true
  try {
    const params = {
      page: filters.page,
      size: filters.size,
      sort: filters.sort,
    }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.dietGoal) params.dietGoal = filters.dietGoal
    if (filters.planType) params.planType = filters.planType

    let res
    if (selectedTag.value) {
      res = await api.get('/meal-plans/by-tag', { params: { tag: selectedTag.value, ...params } })
    } else {
      res = await api.get('/meal-plans', { params })
    }

    if (res.data?.code === 200) {
      const data = res.data.data
      if (Array.isArray(data)) {
        plans.value = data
        totalPlans.value = data.length
      } else {
        plans.value = data?.content || data?.records || []
        totalPlans.value = data?.totalElements || data?.total || plans.value.length
      }
    }
  } catch (e) {
    console.error('加载计划列表失败', e)
    message.error('加载营养餐计划失败')
  } finally {
    loadingPlans.value = false
  }
}

function toggleTag(tag) {
  selectedTag.value = selectedTag.value === tag ? '' : tag
  loadPlans(1)
}

// ========== API: Follows ==========
async function loadMyFollows() {
  if (!authStore.isLoggedIn) return
  loadingFollows.value = true
  try {
    const res = await api.get('/meal-plans/my-follows')
    if (res.data?.code === 200) {
      myFollows.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载跟随列表失败', e)
    message.error('加载跟随列表失败')
  } finally {
    loadingFollows.value = false
  }
}

// ========== API: Favorites ==========
async function loadMyFavorites() {
  if (!authStore.isLoggedIn) return
  loadingFavorites.value = true
  try {
    const res = await api.get('/meal-plans/my-favorites')
    if (res.data?.code === 200) {
      myFavorites.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载收藏列表失败', e)
    message.error('加载收藏列表失败')
  } finally {
    loadingFavorites.value = false
  }
}

// ========== API: Detail ==========
async function openDetail(planId) {
  if (!planId) return
  drawerVisible.value = true
  loadingDetail.value = true
  detail.value = null
  followProgress.value = null
  checkinSet.value = new Set()
  userRatingValue.value = 0
  userReviewText.value = ''
  ratings.value = []
  totalRatings.value = 0
  ratingsPage.page = 1
  activeDayTab.value = '1'

  try {
    const res = await api.get('/meal-plans/' + planId)
    if (res.data?.code === 200) {
      const d = res.data.data
      detail.value = d.mealPlan || d
      detailIsFavorited.value = !!d.isFavorited
      detailIsFollowing.value = !!d.isFollowing
      detailFollowId.value = d.followStatus?.id || null
      if (d.userRating) {
        userRatingValue.value = d.userRating.rating || 0
        userReviewText.value = d.userRating.review || ''
      }
    }
    await loadRatings()
    if (detailIsFollowing.value) {
      await loadFollowProgress(planId)
    }
  } catch (e) {
    console.error('加载计划详情失败', e)
    message.error('加载详情失败')
  } finally {
    loadingDetail.value = false
  }
}

async function loadFollowProgress(planId) {
  try {
    const res = await api.get('/meal-plans/' + planId + '/progress')
    if (res.data?.code === 200) {
      followProgress.value = res.data.data
      const newSet = new Set()
      const checkins = res.data.data?.checkins || []
      checkins.forEach(c => {
        newSet.add(c.dayNumber + '-' + c.mealType)
      })
      checkinSet.value = newSet
    }
  } catch (e) {
    console.error('加载进度失败', e)
  }
}

function isCheckedIn(dayNumber, mealType) {
  return checkinSet.value.has(dayNumber + '-' + mealType)
}

async function handleCheckin(dayNumber, mealType, event) {
  const checked = event.target.checked
  try {
    if (checked) {
      await api.post('/meal-plans/checkin', {
        followId: detailFollowId.value,
        dayNumber,
        mealType,
      })
      checkinSet.value.add(dayNumber + '-' + mealType)
      message.success('打卡成功 ✅')
    } else {
      await api.delete('/meal-plans/checkin', {
        params: {
          followId: detailFollowId.value,
          dayNumber,
          mealType,
        },
      })
      checkinSet.value.delete(dayNumber + '-' + mealType)
      message.info('已取消打卡')
    }
    if (detail.value?.id) {
      await loadFollowProgress(detail.value.id)
    }
  } catch (e) {
    console.error('打卡操作失败', e)
    message.error('打卡操作失败')
    event.target.checked = !checked
  }
}

// ========== API: Follow/Unfollow ==========
async function toggleFollow() {
  if (!authStore.isLoggedIn) {
    message.warning('请先登录')
    return
  }
  if (!detail.value?.id) return
  actionLoading.value = true
  try {
    if (detailIsFollowing.value) {
      await api.delete('/meal-plans/' + detail.value.id + '/follow')
      detailIsFollowing.value = false
      detailFollowId.value = null
      followProgress.value = null
      checkinSet.value = new Set()
      message.success('已取消跟随')
    } else {
      const res = await api.post('/meal-plans/' + detail.value.id + '/follow')
      detailIsFollowing.value = true
      if (res.data?.code === 200 && res.data.data) {
        detailFollowId.value = res.data.data.id || res.data.data
      }
      message.success('开始跟随计划！加油💪')
      await loadFollowProgress(detail.value.id)
    }
    loadMyFollows()
  } catch (e) {
    console.error('跟随操作失败', e)
    message.error(e.response?.data?.message || '操作失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleUnfollow(planId) {
  if (!planId) return
  try {
    await api.delete('/meal-plans/' + planId + '/follow')
    message.success('已取消跟随')
    loadMyFollows()
  } catch (e) {
    console.error('取消跟随失败', e)
    message.error('取消跟随失败')
  }
}

// ========== API: Favorite ==========
async function toggleFavorite() {
  if (!authStore.isLoggedIn) {
    message.warning('请先登录')
    return
  }
  if (!detail.value?.id) return
  actionLoading.value = true
  try {
    await api.post('/meal-plans/' + detail.value.id + '/favorite')
    detailIsFavorited.value = !detailIsFavorited.value
    message.success(detailIsFavorited.value ? '已收藏 ⭐' : '已取消收藏')
    loadMyFavorites()
  } catch (e) {
    console.error('收藏操作失败', e)
    message.error('收藏操作失败')
  } finally {
    actionLoading.value = false
  }
}

// ========== API: Ratings ==========
async function loadRatings(page) {
  if (page) ratingsPage.page = page
  if (!detail.value?.id) return
  loadingRatings.value = true
  try {
    const res = await api.get('/meal-plans/' + detail.value.id + '/ratings', {
      params: { page: ratingsPage.page, size: ratingsPage.size },
    })
    if (res.data?.code === 200) {
      const data = res.data.data
      if (Array.isArray(data)) {
        ratings.value = data
        totalRatings.value = data.length
      } else {
        ratings.value = data?.content || data?.records || []
        totalRatings.value = data?.totalElements || data?.total || ratings.value.length
      }
    }
  } catch (e) {
    console.error('加载评价失败', e)
  } finally {
    loadingRatings.value = false
  }
}

async function submitRating() {
  if (!detail.value?.id || !userRatingValue.value) return
  submittingRating.value = true
  try {
    await api.post('/meal-plans/' + detail.value.id + '/rate', {
      rating: userRatingValue.value,
      review: userReviewText.value || '',
    })
    message.success('评价已提交')
    await loadRatings(1)
  } catch (e) {
    console.error('提交评价失败', e)
    message.error(e.response?.data?.message || '提交失败')
  } finally {
    submittingRating.value = false
  }
}

// ========== Tab Change ==========
function handleTabChange(tab) {
  if (tab === 'follows') loadMyFollows()
  else if (tab === 'favorites') loadMyFavorites()
}

// ========== Init ==========
onMounted(() => {
  loadTags()
  loadRecommended()
  loadPlans(1)
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.meal-plans-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: $spacing-md;
  font-family: $font-body;
}

// ===== Header =====
.page-header {
  text-align: center;
  padding: $spacing-xl 0 $spacing-md;
}
.page-title {
  font-family: $font-display;
  font-size: 2rem;
  color: $foreground;
  margin: 0;
}
.page-subtitle {
  color: $text-secondary;
  margin-top: $spacing-xs;
  font-size: 0.95rem;
}

// ===== Main Tabs =====
.main-tabs {
  margin-bottom: $spacing-lg;
  :deep(.el-tabs__item) {
    font-size: 1rem;
    font-weight: 600;
  }
  :deep(.el-tabs__active-bar) {
    background: $gradient-accent;
  }
}

.tab-content {
  min-height: 400px;
}

// ===== Tag Cloud =====
.section-label {
  font-size: 1.05rem;
  font-weight: 700;
  color: $foreground;
  margin-bottom: $spacing-sm;
}

.tag-cloud-section {
  margin-bottom: $spacing-lg;
}
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}
.tag-chip {
  display: inline-block;
  padding: 4px 14px;
  border-radius: $radius-full;
  border: 1px solid $border;
  background: $card;
  color: $text-secondary;
  font-size: 0.82rem;
  cursor: pointer;
  transition: all 0.2s;
  &:hover {
    border-color: $accent;
    color: $accent;
  }
  &.active {
    background: $accent;
    color: #fff;
    border-color: $accent;
  }
}

// ===== Recommended =====
.recommended-section {
  margin-bottom: $spacing-xl;
}
.recommended-scroll {
  display: flex;
  gap: $spacing-md;
  overflow-x: auto;
  padding-bottom: $spacing-sm;
  scrollbar-width: thin;
  &::-webkit-scrollbar {
    height: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: $border;
    border-radius: 2px;
  }
}
.rec-card {
  flex: 0 0 220px;
  border-radius: $radius-lg;
  overflow: hidden;
  background: $card;
  box-shadow: $shadow-sm;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  &:hover {
    transform: translateY(-3px);
    box-shadow: $shadow-md;
  }
}
.rec-cover {
  width: 100%;
  height: 130px;
  object-fit: cover;
}
.rec-info {
  padding: $spacing-sm $spacing-md;
}
.rec-title {
  font-size: 0.92rem;
  font-weight: 600;
  color: $foreground;
  margin: 4px 0 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rec-meta {
  display: flex;
  gap: $spacing-sm;
  color: $text-secondary;
  font-size: 0.78rem;
}

// ===== Badges =====
.goal-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: $radius-full;
  color: #fff;
  font-size: 0.72rem;
  font-weight: 600;
  letter-spacing: 0.3px;
  &.floating {
    position: absolute;
    top: 10px;
    left: 10px;
  }
  &.small {
    font-size: 0.68rem;
    padding: 1px 8px;
  }
}
.difficulty-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: $radius-full;
  color: #fff;
  font-size: 0.72rem;
  font-weight: 600;
  &.floating {
    position: absolute;
    top: 10px;
    right: 10px;
  }
}
.type-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: $radius-full;
  background: $muted;
  color: $text-secondary;
  font-size: 0.72rem;
  font-weight: 600;
}

// ===== Filters =====
.filters-section {
  margin-bottom: $spacing-lg;
}
.search-row {
  display: flex;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
}
.search-input {
  flex: 1;
  max-width: 400px;
}
.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
  align-items: center;
  margin-bottom: $spacing-sm;
}
.filter-group {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  flex-wrap: wrap;
}
.filter-label {
  font-size: 0.85rem;
  color: $text-secondary;
  font-weight: 500;
  white-space: nowrap;
}

// ===== Plan Grid =====
.plans-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: $spacing-lg;
}
.skeleton-card {
  border-radius: $radius-lg;
  overflow: hidden;
  background: $card;
  padding: $spacing-md;
}
.plan-card {
  border-radius: $radius-lg;
  overflow: hidden;
  background: $card;
  box-shadow: $shadow-sm;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  &:hover {
    transform: translateY(-4px);
    box-shadow: $shadow-md;
  }
}
.card-cover {
  position: relative;
  height: 180px;
  overflow: hidden;
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}
.card-body {
  padding: $spacing-md;
}
.card-title {
  font-size: 1rem;
  font-weight: 700;
  color: $foreground;
  margin: 0 0 6px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-desc {
  font-size: 0.82rem;
  color: $text-secondary;
  margin: 0 0 $spacing-sm;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}
.card-stats {
  display: flex;
  gap: $spacing-md;
  font-size: 0.8rem;
  color: $text-secondary;
  margin-bottom: $spacing-sm;
  .stat {
    display: flex;
    align-items: center;
    gap: 2px;
  }
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-xs;
}
.rating-info {
  display: flex;
  align-items: center;
  gap: 4px;
  .count {
    font-size: 0.72rem;
    color: $text-secondary;
  }
  :deep(.el-rate) {
    --el-rate-icon-size: 14px;
  }
  :deep(.el-rate__text) {
    font-size: 0.75rem;
  }
}
.follow-info {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 0.78rem;
  color: $text-secondary;
}
.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: $spacing-xs;
  .el-tag {
    font-size: 0.68rem;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: $spacing-lg 0;
  &.small {
    padding: $spacing-md 0;
  }
}

// ===== Follows =====
.follows-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}
.follow-card {
  background: $card;
  border-radius: $radius-lg;
  padding: $spacing-lg;
  box-shadow: $shadow-sm;
}
.follow-header {
  margin-bottom: $spacing-sm;
}
.follow-title-row {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  h3 {
    margin: 0;
    font-size: 1.05rem;
    font-weight: 700;
    color: $foreground;
  }
}
.follow-status-text {
  font-size: 0.85rem;
  color: $text-secondary;
  margin: 4px 0 0;
}
.follow-progress {
  margin: $spacing-sm 0;
}
.follow-actions {
  display: flex;
  gap: $spacing-sm;
}

// ===== Detail Drawer =====
.detail-drawer {
  :deep(.el-drawer__header) {
    margin-bottom: 0;
    padding-bottom: $spacing-sm;
    border-bottom: 1px solid $border;
  }
}
.drawer-title {
  font-family: $font-display;
  font-size: 1.15rem;
  color: $foreground;
}
.drawer-loading {
  padding: $spacing-md;
}
.detail-content {
  padding: 0 $spacing-xs $spacing-xl;
}
.detail-cover {
  width: 100%;
  height: 220px;
  object-fit: cover;
  border-radius: $radius-lg;
  margin-bottom: $spacing-md;
}
.detail-title {
  font-family: $font-display;
  font-size: 1.4rem;
  color: $foreground;
  margin: 0 0 $spacing-sm;
}
.detail-badges {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
}
.detail-desc {
  font-size: 0.9rem;
  color: $text-secondary;
  line-height: 1.6;
  margin-bottom: $spacing-sm;
}
.detail-crowd {
  font-size: 0.85rem;
  color: $text-secondary;
  margin-bottom: $spacing-sm;
  display: flex;
  align-items: center;
  gap: 4px;
}
.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: $spacing-md;
}

// Nutrition cards
.nutrition-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
}
.nutri-card {
  text-align: center;
  padding: $spacing-sm $spacing-xs;
  border-radius: $radius-md;
  background: $muted;
  &.calories { border-left: 3px solid #F59E0B; }
  &.protein { border-left: 3px solid #EF4444; }
  &.fat { border-left: 3px solid #8B5CF6; }
  &.carbs { border-left: 3px solid #3B82F6; }
}
.nutri-value {
  display: block;
  font-size: 1.15rem;
  font-weight: 700;
  color: $foreground;
  font-family: $font-mono;
}
.nutri-label {
  font-size: 0.72rem;
  color: $text-secondary;
}

// Actions
.detail-actions {
  display: flex;
  gap: $spacing-sm;
  margin-bottom: $spacing-lg;
  .favorited {
    color: #F59E0B;
    border-color: #F59E0B;
  }
}

// Follow progress in detail
.follow-progress-section {
  background: $muted;
  border-radius: $radius-lg;
  padding: $spacing-md;
  margin-bottom: $spacing-lg;
  h4 {
    margin: 0 0 $spacing-sm;
    font-size: 0.95rem;
    color: $foreground;
  }
}
.progress-text {
  font-size: 0.82rem;
  color: $text-secondary;
  margin-top: $spacing-sm;
}
.completion-banner {
  background: $gradient-accent;
  color: #fff;
  padding: $spacing-sm $spacing-md;
  border-radius: $radius-md;
  text-align: center;
  font-weight: 700;
  margin-top: $spacing-sm;
}

// Days section
.days-section {
  margin-bottom: $spacing-lg;
  h4 {
    margin: 0 0 $spacing-sm;
    font-size: 0.95rem;
    color: $foreground;
  }
}
.day-tabs {
  :deep(.el-tabs__item) {
    font-size: 0.85rem;
  }
}
.day-nutrition-bar {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
  padding: $spacing-sm $spacing-md;
  background: $muted;
  border-radius: $radius-md;
  margin-bottom: $spacing-md;
  font-size: 0.82rem;
  color: $text-secondary;
  font-family: $font-mono;
}

// Meal sections
.meal-section {
  margin-bottom: $spacing-md;
  &:last-child {
    margin-bottom: 0;
  }
}
.meal-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-xs 0;
  border-bottom: 1px solid $border;
  margin-bottom: $spacing-sm;
}
.meal-icon {
  font-size: 1.1rem;
}
.meal-type-label {
  font-weight: 700;
  font-size: 0.92rem;
  color: $foreground;
}
.checkin-toggle {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  input[type="checkbox"] {
    accent-color: $accent;
    width: 16px;
    height: 16px;
  }
}
.checkin-label {
  font-size: 0.78rem;
  color: $accent;
  font-weight: 600;
}

.meal-item {
  padding: $spacing-sm;
  border-radius: $radius-md;
  background: $card;
  border: 1px solid $border-light;
  margin-bottom: $spacing-xs;
}
.item-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.item-name {
  font-weight: 600;
  font-size: 0.88rem;
  color: $foreground;
}
.item-portion {
  font-size: 0.78rem;
  color: $text-secondary;
  background: $muted;
  padding: 1px 8px;
  border-radius: $radius-full;
}
.item-desc {
  font-size: 0.78rem;
  color: $text-secondary;
  margin: 4px 0;
}
.item-macros {
  display: flex;
  gap: $spacing-sm;
  font-size: 0.72rem;
  color: $text-secondary;
  font-family: $font-mono;
}
.recipe-link {
  display: inline-block;
  margin-top: 4px;
  font-size: 0.78rem;
  color: $accent;
  cursor: pointer;
  font-weight: 600;
  &:hover {
    text-decoration: underline;
  }
}
.no-items {
  font-size: 0.82rem;
  color: $text-disabled;
  text-align: center;
  padding: $spacing-sm;
}

// Rating section
.rating-section {
  margin-bottom: $spacing-lg;
  h4 {
    margin: 0 0 $spacing-md;
    font-size: 0.95rem;
    color: $foreground;
  }
}
.my-rating {
  background: $muted;
  padding: $spacing-md;
  border-radius: $radius-md;
  margin-bottom: $spacing-md;
}
.rating-prompt {
  font-size: 0.85rem;
  color: $text-secondary;
  margin: 0 0 $spacing-xs;
}
.ratings-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}
.rating-item {
  padding: $spacing-sm $spacing-md;
  border-radius: $radius-md;
  background: $card;
  border: 1px solid $border-light;
}
.rating-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.rater-name {
  font-weight: 600;
  font-size: 0.85rem;
  color: $foreground;
}
.rating-review {
  font-size: 0.82rem;
  color: $text-secondary;
  margin: 4px 0;
  line-height: 1.5;
}
.rating-date {
  font-size: 0.72rem;
  color: $text-disabled;
}

// ===== Responsive =====
@media (max-width: $breakpoint-mobile) {
  .meal-plans-view {
    padding: $spacing-sm;
  }
  .page-title {
    font-size: 1.5rem;
  }
  .plans-grid {
    grid-template-columns: 1fr;
  }
  .nutrition-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  .filter-row {
    flex-direction: column;
    align-items: flex-start;
  }
  .search-row {
    flex-direction: column;
    .search-input {
      max-width: 100%;
    }
  }
  .recommended-scroll {
    gap: $spacing-sm;
  }
  .rec-card {
    flex: 0 0 180px;
  }
  .detail-cover {
    height: 160px;
  }
  .day-nutrition-bar {
    gap: $spacing-sm;
  }
}

@media (max-width: $breakpoint-tablet) and (min-width: $breakpoint-mobile) {
  .plans-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
