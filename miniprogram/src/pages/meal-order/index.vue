<template>
  <view class="page">
    <!-- Nav Bar -->
    <NutriNavBar :show-back="false" bg-color="#FFFFFF">
      <template #left>
        <text class="nav-brand">营养餐</text>
      </template>
      <template #title><view /></template>
      <template #right>
        <view class="nav-icon-btn pressable" @tap="toggleSearch">
          <NutriIcon name="search" :size="36" color="#1A1A2E" />
        </view>
      </template>
    </NutriNavBar>

    <!-- Store Selector -->
    <view :style="{ paddingTop: navBarTotalHeight + 'px' }">
      <StoreSelector
        :store-name="currentStore.name"
        :store-address="currentStore.address"
        @tap="onStoreTap"
      />
    </view>

    <!-- Search Overlay -->
    <view v-if="showSearch" class="search-overlay">
      <view class="search-bar">
        <NutriIcon name="search" :size="32" color="#8896AB" />
        <input
          class="search-input"
          v-model="searchKeyword"
          placeholder="搜索营养餐..."
          focus
          @confirm="doSearch"
        />
        <text class="search-cancel pressable" @tap="toggleSearch">取消</text>
      </view>
    </view>

    <!-- Main Content: Luckin-style sidebar + list -->
    <view class="main-area" :style="{ top: mainAreaTop + 'px' }">
      <!-- Left Category Sidebar -->
      <scroll-view class="category-sidebar" scroll-y>
        <view
          v-for="cat in categories"
          :key="cat.value"
          class="cat-item"
          :class="{ active: activeCategory === cat.value }"
          @tap="selectCategory(cat.value)"
        >
          {{ cat.label }}
        </view>
      </scroll-view>

      <!-- Right Meal List -->
      <scroll-view class="meal-area" scroll-y @scrolltolower="onLoadMore">
        <!-- Loading -->
        <view v-if="loading && !meals.length" class="loading-box">
          <text class="loading-text">加载中...</text>
        </view>

        <!-- Meal List -->
        <view v-else class="meal-list">
          <view
            v-for="meal in filteredMeals"
            :key="meal.id"
            class="meal-card pressable"
            @tap="goToDetail(meal.id)"
          >
            <image class="meal-img" :src="meal.imageUrl || '/static/images/meal-placeholder.png'" mode="aspectFill" lazy-load />
            <view class="meal-info">
              <text class="meal-name">{{ meal.name }}</text>
              <text class="meal-desc">{{ meal.description || '新鲜现做，科学配比' }}</text>
              <view class="meal-tags">
                <text class="meal-tag" v-for="tag in (meal.tags || []).slice(0, 3)" :key="tag">{{ tag }}</text>
              </view>
              <view class="meal-row">
                <view class="meal-meta">
                  <PriceTag :price="meal.price" />
                  <text class="meal-cal" v-if="meal.calories">{{ meal.calories }}kcal</text>
                </view>
                <view class="stepper" v-if="getCartQty(meal.id) > 0" @tap.stop>
                  <view class="stepper-btn stepper-minus" @tap="changeQty(meal, -1)">
                    <NutriIcon name="minus" :size="24" color="#8896AB" />
                  </view>
                  <text class="stepper-count">{{ getCartQty(meal.id) }}</text>
                  <view class="stepper-btn stepper-plus" @tap="changeQty(meal, 1)">
                    <NutriIcon name="plus" :size="24" color="#fff" />
                  </view>
                </view>
                <view v-else class="add-btn pressable" @tap.stop="addToCart(meal)">
                  <NutriIcon name="plus" :size="28" color="#fff" />
                </view>
              </view>
            </view>
          </view>

          <EmptyState
            v-if="!filteredMeals.length && !loading"
            icon="utensils"
            title="暂无营养餐"
            description="换个分类试试"
          />
        </view>

        <view style="height: 160rpx;" />
      </scroll-view>
    </view>

    <!-- Cart Bar -->
    <CartBar
      :total-count="cartTotal.count"
      :total-price="cartTotal.price"
      @view-cart="goToCheckout"
      @submit="goToCheckout"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { mealApi } from '@/services/api'
import NutriNavBar from '@/components/NutriNavBar.vue'
import NutriIcon from '@/components/NutriIcon.vue'
import StoreSelector from '@/components/StoreSelector.vue'
import PriceTag from '@/components/PriceTag.vue'
import EmptyState from '@/components/EmptyState.vue'
import CartBar from '@/components/CartBar.vue'

const navBarTotalHeight = ref(64)
const mainAreaTop = ref(64 + 40) // navBar + storeSelector
const loading = ref(false)
const showSearch = ref(false)
const searchKeyword = ref('')
const activeCategory = ref('all')
const meals = ref<any[]>([])

const currentStore = ref({
  name: 'NutriAI 营养餐厅',
  address: '点击选择最近门店'
})

const CART_KEY = 'nutriai_meal_cart'

const categories = [
  { value: 'all', label: '全部' },
  { value: 'anti-inflammatory', label: '抗炎餐' },
  { value: 'low-gi', label: '低GI餐' },
  { value: 'high-protein', label: '高蛋白' },
  { value: 'salad', label: '轻食沙拉' },
  { value: 'soup', label: '汤品' }
]

const mockMeals = [
  { id: 1, name: '地中海抗炎沙拉', price: '28.00', calories: 380, tags: ['抗炎', '低GI'], category: 'anti-inflammatory', description: '新鲜蔬菜搭配橄榄油，富含抗氧化成分', imageUrl: '' },
  { id: 2, name: '三文鱼藜麦碗', price: '42.00', calories: 520, tags: ['高蛋白', '抗炎'], category: 'high-protein', description: '挪威三文鱼搭配有机藜麦，Omega-3丰富', imageUrl: '' },
  { id: 3, name: '姜黄鸡胸套餐', price: '35.00', calories: 450, tags: ['抗炎', '增肌'], category: 'anti-inflammatory', description: '姜黄腌制鸡胸肉，搭配糙米和时蔬', imageUrl: '' },
  { id: 4, name: '牛油果虾仁沙拉', price: '38.00', calories: 420, tags: ['低GI', '高蛋白'], category: 'salad', description: '新鲜牛油果搭配大虾仁，健康轻食', imageUrl: '' },
  { id: 5, name: '番茄牛腩汤', price: '32.00', calories: 350, tags: ['抗炎', '暖胃'], category: 'soup', description: '慢炖番茄牛腩，营养滋补', imageUrl: '' },
  { id: 6, name: '低GI杂粮饭套餐', price: '26.00', calories: 400, tags: ['低GI', '高纤维'], category: 'low-gi', description: '五谷杂粮饭搭配清蒸时蔬', imageUrl: '' }
]

const filteredMeals = computed(() => {
  let list = meals.value
  if (activeCategory.value !== 'all') {
    list = list.filter(m => m.category === activeCategory.value || (m.tags || []).some((t: string) => {
      const catMap: Record<string, string[]> = {
        'anti-inflammatory': ['抗炎'],
        'low-gi': ['低GI'],
        'high-protein': ['高蛋白'],
        'salad': ['沙拉', '轻食'],
        'soup': ['汤', '汤品']
      }
      return (catMap[activeCategory.value] || []).some(k => t.includes(k))
    }))
  }
  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.trim().toLowerCase()
    list = list.filter(m => m.name.toLowerCase().includes(kw))
  }
  return list
})

// ── Cart ──
const cart = ref<any[]>([])
const cartTotal = computed(() => {
  const count = cart.value.reduce((s: number, c: any) => s + c.quantity, 0)
  const price = cart.value.reduce((s: number, c: any) => s + Number(c.price) * c.quantity, 0)
  return { count, price }
})

function getCart(): any[] {
  return JSON.parse(uni.getStorageSync(CART_KEY) || '[]')
}

function saveCart() {
  uni.setStorageSync(CART_KEY, JSON.stringify(cart.value))
}

function getCartQty(mealId: number | string): number {
  return cart.value.find((c: any) => c.id === mealId)?.quantity || 0
}

function addToCart(meal: any) {
  const existing = cart.value.find((c: any) => c.id === meal.id)
  if (existing) {
    existing.quantity += 1
  } else {
    cart.value.push({ id: meal.id, name: meal.name, price: meal.price, imageUrl: meal.imageUrl, quantity: 1 })
  }
  saveCart()
  uni.vibrateShort({ type: 'light' })
}

function changeQty(meal: any, delta: number) {
  const existing = cart.value.find((c: any) => c.id === meal.id)
  if (!existing) return
  const nv = existing.quantity + delta
  if (nv <= 0) {
    cart.value = cart.value.filter((c: any) => c.id !== meal.id)
  } else {
    existing.quantity = nv
  }
  saveCart()
}

function selectCategory(val: string) {
  activeCategory.value = val
}

function toggleSearch() {
  showSearch.value = !showSearch.value
  if (!showSearch.value) searchKeyword.value = ''
}

function doSearch() {
  showSearch.value = false
}

function onStoreTap() {
  uni.showToast({ title: '门店选择即将上线', icon: 'none' })
}

function onLoadMore() {
  // TODO: pagination
}

function goToDetail(id: number | string) {
  uni.navigateTo({ url: `/pages/meal-order/detail?id=${id}` })
}

function goToCheckout() {
  uni.navigateTo({ url: '/pages/meal-order/checkout' })
}

async function fetchMeals() {
  loading.value = true
  try {
    const res = await mealApi.getList({ page: 0, size: 20 })
    const list = res.data?.content || res.data?.records || (Array.isArray(res.data) ? res.data : [])
    if (res.code === 200 && list.length) {
      meals.value = list.map((m: any) => ({
        ...m,
        price: m.salePrice || m.sale_price || m.price,
        calories: m.nutritionInfo?.calories || m.nutrition_info?.calories || 0,
        description: m.brief || m.description,
        tags: m.tags || []
      }))
    } else {
      meals.value = mockMeals
    }
  } catch {
    meals.value = mockMeals
  } finally {
    loading.value = false
  }
}

onShow(() => {
  const windowInfo = uni.getWindowInfo()
  const statusBarHeight = windowInfo.statusBarHeight || 0
  navBarTotalHeight.value = statusBarHeight + 44
  mainAreaTop.value = navBarTotalHeight.value + 40

  cart.value = getCart()
  fetchMeals()
})
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: $background;
}

/* Nav */
.nav-brand {
  font-size: 36rpx;
  font-weight: 700;
  color: $accent;
  letter-spacing: 2rpx;
}
.nav-icon-btn {
  width: 64rpx; height: 64rpx;
  display: flex; align-items: center; justify-content: center;
  background: $muted;
  border-radius: 50%;
}

/* Search Overlay */
.search-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  z-index: $z-overlay;
  background: rgba(0,0,0,0.4);
}
.search-bar {
  display: flex;
  align-items: center;
  padding: 24rpx 32rpx;
  gap: 16rpx;
  background: $card;
}
.search-input {
  flex: 1;
  height: 72rpx;
  background: $muted;
  border-radius: 48rpx;
  padding: 0 28rpx;
  font-size: 28rpx;
  color: $foreground;
}
.search-cancel {
  font-size: 28rpx;
  color: $accent;
  font-weight: 500;
}

/* Main Content Area — Luckin-style sidebar + right list */
.main-area {
  position: fixed;
  left: 0; right: 0; bottom: 0;
  display: flex;
}

/* Left Category Sidebar */
.category-sidebar {
  width: $sidebar-width;
  background: $sidebar-bg;
  flex-shrink: 0;
  height: 100%;
}
.cat-item {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100rpx;
  font-size: 26rpx;
  color: $muted-foreground;
  font-weight: 500;
  transition: all $duration-fast $ease-out;

  &.active {
    background: $sidebar-active-bg;
    color: $sidebar-active-color;
    font-weight: 600;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 20%;
      height: 60%;
      width: 6rpx;
      border-radius: 0 6rpx 6rpx 0;
      background: $accent;
    }
  }
}

/* Right Meal List */
.meal-area {
  flex: 1;
  height: 100%;
  background: $background;
}

/* Loading */
.loading-box {
  text-align: center;
  padding: 80rpx 0;
}
.loading-text {
  font-size: 28rpx;
  color: $muted-foreground;
}

/* Meal List */
.meal-list {
  padding: 16rpx 20rpx 0;
}
.meal-card {
  display: flex;
  background: $card;
  border-radius: 20rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.meal-img {
  width: 200rpx;
  height: 200rpx;
  flex-shrink: 0;
  background: $muted;
}
.meal-info {
  flex: 1;
  padding: 16rpx 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}
.meal-name {
  font-size: 28rpx;
  font-weight: 600;
  color: $foreground;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.meal-desc {
  font-size: 22rpx;
  color: $muted-foreground;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-top: 4rpx;
}
.meal-tags {
  display: flex;
  gap: 8rpx;
  margin-top: 8rpx;
  flex-wrap: wrap;
}
.meal-tag {
  font-size: 20rpx;
  color: $accent-secondary;
  background: #ECFDF5;
  padding: 2rpx 12rpx;
  border-radius: 48rpx;
  font-weight: 500;
}
.meal-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8rpx;
}
.meal-meta {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}
.meal-cal {
  font-size: 20rpx;
  color: $muted-foreground;
}

/* Add Button */
.add-btn {
  width: 52rpx; height: 52rpx;
  background: linear-gradient(135deg, $accent, $accent-secondary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(16,185,129,0.3);
}

/* Stepper (inline quantity +/-) */
.stepper {
  display: flex;
  align-items: center;
  gap: 4rpx;
}
.stepper-btn {
  width: 44rpx; height: 44rpx;
  display: flex; align-items: center; justify-content: center;
  border-radius: 50%;
}
.stepper-minus {
  background: $muted;
}
.stepper-plus {
  background: linear-gradient(135deg, $accent, $accent-secondary);
  box-shadow: 0 2rpx 8rpx rgba(16,185,129,0.25);
}
.stepper-count {
  min-width: 48rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 600;
  color: $foreground;
}
</style>
