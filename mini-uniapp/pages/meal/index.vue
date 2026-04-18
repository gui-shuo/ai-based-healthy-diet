<script setup>
/**
 * 营养餐列表页 - 左侧分类导航 + 右侧商品列表
 * 支持门店切换、自提模式
 */
import { ref, computed, onMounted } from 'vue'
import NavBar from '../../components/NavBar.vue'
import { mealApi } from '../../services/api'
import { useCartStore } from '../../stores/cart'
import { formatPrice, cosUrl, checkLogin } from '../../utils/common'

const cartStore = useCartStore()

// 分类数据 - 从后端加载
const categoryIcons = { ANTI_INFLAMMATORY: '🔥', LOW_FAT: '🥗', HIGH_PROTEIN: '💪', VEGETARIAN: '🌿' }
const defaultCategories = [
  { id: '', name: '全部', icon: '⭐' },
  { id: 'ANTI_INFLAMMATORY', name: '抗炎餐', icon: '🔥' },
  { id: 'LOW_FAT', name: '减脂轻食', icon: '🥗' },
  { id: 'HIGH_PROTEIN', name: '高蛋白餐', icon: '💪' },
  { id: 'VEGETARIAN', name: '素食', icon: '🌿' },
]
const categories = ref(defaultCategories)

const activeCategory = ref('')
const meals = ref([])
const loading = ref(false)
const pickupMode = ref('self') // self: 自提, delivery: 外送

// 当前门店
const currentStore = ref({
  name: 'NutriAI 旗舰店',
  distance: '1.2km',
})

// 标准化餐品数据（后端字段 → 页面字段）
function normalizeMeal(m) {
  const ni = m.nutritionInfo || {}
  return {
    ...m,
    image: m.imageUrl || m.image || '',
    price: m.salePrice ?? m.price,
    tag: (m.tags || [])[0] || m.tag || '',
    calories: ni.calories ?? m.calories ?? 0,
    protein: ni.protein ?? m.protein ?? 0,
    fat: ni.fat ?? m.fat ?? 0,
    carbs: ni.carbs ?? m.carbs ?? 0,
  }
}

// 切换分类
function selectCategory(id) {
  activeCategory.value = id
  fetchMeals()
}

// 获取营养餐列表
async function fetchMeals() {
  loading.value = true
  try {
    const params = { page: 0, size: 20 }
    if (activeCategory.value) params.category = activeCategory.value
    const res = await mealApi.getList(params)
    const list = res?.content || (Array.isArray(res) ? res : [])
    meals.value = list.map(normalizeMeal)
  } catch (e) {
    console.error('获取营养餐失败', e)
    meals.value = []
  } finally {
    loading.value = false
  }
}

// 加载分类列表
async function fetchCategories() {
  try {
    const res = await mealApi.getCategories()
    if (Array.isArray(res) && res.length > 0) {
      categories.value = [
        { id: '', name: '全部', icon: '⭐' },
        ...res.map(c => ({ id: c, name: c, icon: categoryIcons[c] || '🍽️' }))
      ]
    }
  } catch (e) {
    // 使用默认分类
  }
}

// 跳转详情
function goToDetail(id) {
  uni.navigateTo({ url: `/pages/meal/detail?id=${id}` })
}

// 跳转搜索
function goToSearch() {
  uni.navigateTo({ url: '/pages/meal/search' })
}

// 切换门店
function switchStore() {
  uni.showToast({ title: '门店选择功能开发中', icon: 'none' })
}

// 加入购物车
async function addToCart(meal) {
  if (!checkLogin()) return
  await cartStore.addItem({ itemType: 'MEAL', itemId: meal.id, quantity: 1 })
}

onMounted(() => {
  fetchCategories()
  fetchMeals()
})
</script>

<template>
  <view class="page">
    <!-- 顶部导航 -->
    <NavBar title="NutriAI">
      <template #right>
        <view class="nav-search" @tap="goToSearch">
          <text class="nav-search__text">🔍</text>
        </view>
      </template>
    </NavBar>

    <!-- 取餐方式 & 门店信息 -->
    <view class="store-bar">
      <!-- 取餐方式切换 -->
      <view class="pickup-toggle">
        <view
          class="pickup-toggle__item"
          :class="{ 'pickup-toggle__item--active': pickupMode === 'self' }"
          @tap="pickupMode = 'self'"
        >
          <text>🏪</text>
          <text class="pickup-toggle__label">自提</text>
        </view>
        <view
          class="pickup-toggle__item"
          :class="{ 'pickup-toggle__item--active': pickupMode === 'delivery' }"
          @tap="pickupMode = 'delivery'"
        >
          <text>🚚</text>
          <text class="pickup-toggle__label">外送</text>
        </view>
      </view>
      <!-- 门店信息 -->
      <view class="store-info" @tap="switchStore">
        <text class="store-info__name">{{ currentStore.name }}</text>
        <text class="store-info__distance">{{ currentStore.distance }}</text>
        <text class="store-info__switch">切换门店 ›</text>
      </view>
    </view>

    <!-- 主体内容: 左侧分类 + 右侧列表 -->
    <view class="main-content">
      <!-- 左侧分类导航 -->
      <scroll-view scroll-y class="category-nav" :enhanced="true" :show-scrollbar="false">
        <view
          v-for="cat in categories"
          :key="cat.id"
          class="category-item"
          :class="{ 'category-item--active': activeCategory === cat.id }"
          @tap="selectCategory(cat.id)"
        >
          <text class="category-item__icon">{{ cat.icon }}</text>
          <text class="category-item__name">{{ cat.name }}</text>
        </view>
      </scroll-view>

      <!-- 右侧商品列表 -->
      <scroll-view scroll-y class="meal-list" :enhanced="true" :show-scrollbar="false">
        <!-- 分类标题 -->
        <view class="section-header">
          <text class="section-header__title">
            {{ categories.find(c => c.id === activeCategory)?.name }}推荐
          </text>
          <text class="section-header__subtitle">为您精选健康美味</text>
        </view>

        <!-- 加载中 -->
        <view v-if="loading" class="loading-state">
          <text class="loading-state__text">加载中...</text>
        </view>

        <!-- 餐品卡片 -->
        <view
          v-for="meal in meals"
          :key="meal.id"
          class="meal-card"
          @tap="goToDetail(meal.id)"
        >
          <view class="meal-card__image-wrap">
            <image
              class="meal-card__image"
              :src="cosUrl(meal.image) || '/static/images/meal-placeholder.png'"
              mode="aspectFill"
            />
            <view v-if="meal.tag" class="meal-card__badge">
              <text class="meal-card__badge-text">{{ meal.tag }}</text>
            </view>
          </view>
          <view class="meal-card__info">
            <text class="meal-card__name">{{ meal.name }}</text>
            <text class="meal-card__macros">
              P:{{ meal.protein }}g | F:{{ meal.fat }}g | C:{{ meal.carbs }}g
            </text>
            <view class="meal-card__footer">
              <view class="meal-card__price">
                <text class="meal-card__price-current">¥{{ formatPrice(meal.price) }}</text>
                <text v-if="meal.originalPrice" class="meal-card__price-original">
                  ¥{{ formatPrice(meal.originalPrice) }}
                </text>
              </view>
              <view class="meal-card__add" @tap.stop="addToCart(meal)">
                <text class="meal-card__add-icon">+</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-if="!loading && meals.length === 0" class="empty-state">
          <text class="empty-state__text">暂无餐品</text>
        </view>

        <view style="height: 40rpx;" />
      </scroll-view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@import '../../styles/design-system.scss';

.page {
  min-height: 100vh;
  background-color: $surface;
  display: flex;
  flex-direction: column;
}

// 搜索按钮
.nav-search {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;

  &__text {
    font-size: 40rpx;
  }
}

// 门店栏
.store-bar {
  padding: 16rpx 24rpx;
  background: $surface-container-lowest;
}

.pickup-toggle {
  display: flex;
  background: $surface-container-low;
  border-radius: $radius-full;
  padding: 4rpx;
  margin-bottom: 16rpx;
  width: 240rpx;

  &__item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8rpx;
    padding: 12rpx 0;
    border-radius: $radius-full;
    transition: all $transition-normal;

    &--active {
      background: #fff;
      box-shadow: $shadow-sm;
    }
  }

  &__label {
    font-size: $font-sm;
    font-weight: 500;
    color: $on-surface;
  }
}

.store-info {
  display: flex;
  align-items: center;
  gap: 12rpx;

  &__name {
    font-size: $font-base;
    font-weight: 600;
    color: $on-surface;
  }

  &__distance {
    font-size: $font-xs;
    color: $on-surface-variant;
  }

  &__switch {
    font-size: $font-xs;
    color: $primary;
    margin-left: auto;
  }
}

// 主体布局
.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

// 左侧分类导航
.category-nav {
  width: 168rpx;
  background: $surface-container-low;
  height: calc(100vh - 280px);
  flex-shrink: 0;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24rpx 8rpx;
  position: relative;
  transition: all $transition-normal;

  &--active {
    background: $surface-container-lowest;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 16rpx;
      bottom: 16rpx;
      width: 6rpx;
      background: $primary;
      border-radius: 0 $radius-sm $radius-sm 0;
    }
  }

  &__icon {
    font-size: 40rpx;
    margin-bottom: 8rpx;
  }

  &__name {
    font-size: $font-xs;
    color: $on-surface;
    text-align: center;
    line-height: 1.3;
  }
}

// 右侧商品列表
.meal-list {
  flex: 1;
  height: calc(100vh - 280px);
  padding: 0 20rpx;
}

.section-header {
  padding: 24rpx 0 16rpx;

  &__title {
    display: block;
    font-size: $font-lg;
    font-weight: 700;
    color: $on-surface;
    margin-bottom: 4rpx;
  }

  &__subtitle {
    display: block;
    font-size: $font-xs;
    color: $on-surface-variant;
  }
}

// 餐品卡片
.meal-card {
  display: flex;
  gap: 20rpx;
  padding: 20rpx;
  background: $surface-container-lowest;
  border-radius: $radius-lg;
  margin-bottom: 16rpx;

  &__image-wrap {
    position: relative;
    width: 200rpx;
    height: 200rpx;
    border-radius: $radius-lg;
    overflow: hidden;
    flex-shrink: 0;
  }

  &__image {
    width: 100%;
    height: 100%;
  }

  &__badge {
    position: absolute;
    top: 8rpx;
    left: 8rpx;
    background: $primary;
    padding: 4rpx 12rpx;
    border-radius: $radius-sm;

    &-text {
      font-size: $font-xs;
      color: $on-primary;
      font-weight: 500;
    }
  }

  &__info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-width: 0;
  }

  &__name {
    font-size: $font-md;
    font-weight: 600;
    color: $on-surface;
    margin-bottom: 8rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__macros {
    font-size: $font-xs;
    color: $on-surface-variant;
    margin-bottom: 12rpx;
  }

  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  &__price {
    display: flex;
    align-items: baseline;
    gap: 8rpx;

    &-current {
      font-size: $font-lg;
      font-weight: 700;
      color: $primary;
    }

    &-original {
      font-size: $font-xs;
      color: $on-surface-variant;
      text-decoration: line-through;
    }
  }

  &__add {
    width: 56rpx;
    height: 56rpx;
    background: $primary-container;
    border-radius: $radius-full;
    display: flex;
    align-items: center;
    justify-content: center;

    &-icon {
      font-size: 36rpx;
      color: $on-primary;
      font-weight: 300;
      line-height: 1;
    }
  }
}

.loading-state, .empty-state {
  padding: 80rpx 0;
  text-align: center;

  &__text {
    font-size: $font-base;
    color: $on-surface-variant;
  }
}
</style>
