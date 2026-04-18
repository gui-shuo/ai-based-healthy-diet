<template>
  <view class="page">
    <!-- Navigation Bar -->
    <NutriNavBar :show-back="false" bg-color="#FFFFFF" float>
      <template #left>
        <view class="nav-brand-wrap">
          <image class="nav-logo" src="/static/images/logo.png" mode="aspectFit" />
          <text class="nav-brand">NutriAI</text>
        </view>
      </template>
      <template #title><view /></template>
      <template #right>
        <view class="nav-actions">
          <view class="nav-icon-btn pressable" @tap="goTo('/pages/recipes/index')">
            <NutriIcon name="search" :size="36" color="#1A1A2E" />
          </view>
          <view class="nav-icon-btn pressable" @tap="goTo('/pages/announcements/index')">
            <NutriIcon name="bell" :size="36" color="#1A1A2E" />
            <view v-if="unreadCount > 0" class="unread-dot" />
          </view>
        </view>
      </template>
    </NutriNavBar>

    <scroll-view class="content" scroll-y :style="{ paddingTop: navBarTotalHeight + 'px' }">
      <!-- Store Selector -->
      <StoreSelector
        :store-name="currentStore.name"
        :store-address="currentStore.address"
        @tap="onStoreTap"
      />

      <!-- Hero Banner -->
      <view class="hero-section">
        <view class="hero-card pressable" @tap="goToMeals">
          <view class="hero-text-area">
            <view class="hero-label">
              <NutriIcon name="flame" :size="24" color="#fff" />
              <text class="hero-label-text">限时推荐</text>
            </view>
            <text class="hero-title">科学抗炎 · 营养定制</text>
            <text class="hero-desc">专业营养师配比，每日新鲜现做</text>
            <view class="hero-btn">
              <text class="hero-btn-text">立即点餐</text>
              <NutriIcon name="arrow-right" :size="28" color="#059669" />
            </view>
          </view>
          <view class="hero-visual">
            <NutriIcon name="salad" :size="140" color="rgba(255,255,255,0.35)" />
          </view>
        </view>
      </view>

      <!-- Quick Service Entries -->
      <view class="service-section">
        <view class="service-grid">
          <view
            class="service-item pressable"
            v-for="f in features"
            :key="f.label"
            @tap="handleFeatureTap(f)"
          >
            <view class="service-icon-wrap" :style="{ background: f.bg }">
              <NutriIcon :name="f.icon" :size="44" :color="f.color" />
            </view>
            <text class="service-label">{{ f.label }}</text>
          </view>
        </view>
      </view>

      <!-- Member Benefits Bar -->
      <view class="member-bar pressable" @tap="goTo('/pages/member/index')">
        <view class="member-left">
          <NutriIcon name="crown" :size="40" color="#92400E" />
          <view class="member-info">
            <text class="member-title" v-if="userStore.isLoggedIn">{{ userStore.userInfo?.nickname || '会员用户' }}</text>
            <text class="member-title" v-else>开通会员享专属优惠</text>
            <text class="member-desc">AI配额 · 专属折扣 · 积分翻倍</text>
          </view>
        </view>
        <view class="member-btn">
          <text class="member-btn-text">{{ userStore.isLoggedIn ? '查看权益' : '立即开通' }}</text>
          <NutriIcon name="chevron-right" :size="24" color="#FEF3C7" />
        </view>
      </view>

      <!-- Today's Recommended Meals -->
      <view class="section">
        <view class="section-hd">
          <view class="section-hd-left">
            <view class="section-bar" />
            <text class="section-name">今日推荐</text>
          </view>
          <view class="section-link pressable" @tap="goToMeals">
            <text class="section-link-text">全部</text>
            <NutriIcon name="chevron-right" :size="24" color="#8896AB" />
          </view>
        </view>
        <scroll-view class="meal-scroll" scroll-x :show-scrollbar="false">
          <view class="meal-card pressable" v-for="meal in featuredMeals" :key="meal.id" @tap="goToMealDetail(meal.id)">
            <view class="meal-img-wrap">
              <image class="meal-img" :src="meal.imageUrl || '/static/images/meal-placeholder.png'" mode="aspectFill" lazy-load />
              <view class="meal-cal-badge" v-if="meal.calories">
                <NutriIcon name="flame" :size="20" color="#fff" />
                <text class="meal-cal-text">{{ meal.calories }}kcal</text>
              </view>
            </view>
            <view class="meal-info">
              <text class="meal-name">{{ meal.name }}</text>
              <view class="meal-tag-row">
                <text class="meal-tag" v-for="tag in (meal.tags || []).slice(0, 2)" :key="tag">{{ tag }}</text>
              </view>
              <view class="meal-price-row">
                <PriceTag :price="meal.price" />
                <view class="meal-add-btn pressable">
                  <NutriIcon name="plus" :size="28" color="#fff" />
                </view>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- Featured Products -->
      <view class="section">
        <view class="section-hd">
          <view class="section-hd-left">
            <view class="section-bar" />
            <text class="section-name">营养好物</text>
          </view>
          <view class="section-link pressable" @tap="goToShop">
            <text class="section-link-text">进入商城</text>
            <NutriIcon name="chevron-right" :size="24" color="#8896AB" />
          </view>
        </view>
        <view class="product-grid">
          <view class="prod-card pressable" v-for="prod in featuredProducts" :key="prod.id" @tap="goToProductDetail(prod.id)">
            <view class="prod-img-wrap">
              <image class="prod-img" :src="prod.imageUrl || prod.image || '/static/images/product-placeholder.png'" mode="aspectFill" lazy-load />
            </view>
            <view class="prod-info">
              <text class="prod-name">{{ prod.name }}</text>
              <view class="prod-price-row">
                <PriceTag :price="formatPrice(prod.salePrice || prod.price)" size="sm" />
                <view class="prod-buy-btn pressable" @tap.stop="goToProductDetail(prod.id)">
                  <text class="prod-buy-text">购买</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- Footer -->
      <view class="app-footer">
        <view class="footer-line" />
        <text class="footer-text">{{ appStore.config.siteDescription || '智能营养，科学饮食' }}</text>
        <text class="footer-copy">{{ appStore.config.copyright || '© NutriAI' }}</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { mealApi, productApi, announcementApi } from '@/services/api'
import NutriNavBar from '@/components/NutriNavBar.vue'
import NutriIcon from '@/components/NutriIcon.vue'
import StoreSelector from '@/components/StoreSelector.vue'
import PriceTag from '@/components/PriceTag.vue'

const userStore = useUserStore()
const appStore = useAppStore()

const navBarTotalHeight = ref(0)
const unreadCount = ref(0)

// Store selection (to be connected to store API later)
const currentStore = ref({
  name: 'NutriAI 营养餐厅',
  address: '点击选择最近门店'
})

const mockMeals = [
  { id: 1, name: '地中海抗炎沙拉', price: '28.00', calories: 380, tags: ['抗炎', '低GI'], imageUrl: '' },
  { id: 2, name: '三文鱼藜麦碗', price: '42.00', calories: 520, tags: ['高蛋白', '抗炎'], imageUrl: '' },
  { id: 3, name: '姜黄鸡胸套餐', price: '35.00', calories: 450, tags: ['抗炎', '增肌'], imageUrl: '' }
]

const mockProducts = [
  { id: 1, name: '深海鱼油 Omega-3', price: '128.00', imageUrl: '' },
  { id: 2, name: '有机姜黄粉', price: '68.00', imageUrl: '' },
  { id: 3, name: '益生菌冻干粉', price: '98.00', imageUrl: '' }
]

const featuredMeals = ref<any[]>(mockMeals)
const featuredProducts = ref<any[]>(mockProducts)
const hasLoaded = ref(false)

const features = [
  { icon: 'sparkles', label: 'AI营养师', path: '/pages/ai-chat/index', isTab: true, bg: 'linear-gradient(135deg, #ECFDF5, #D1FAE5)', color: '#10B981' },
  { icon: 'camera', label: '食物识别', path: '/pages/food-recognition/index', bg: 'linear-gradient(135deg, #EFF6FF, #DBEAFE)', color: '#3B82F6' },
  { icon: 'clipboard', label: '饮食计划', path: '/pages/diet-plan/index', bg: 'linear-gradient(135deg, #F5F3FF, #EDE9FE)', color: '#8B5CF6' },
  { icon: 'edit', label: '饮食记录', path: '/pages/food-records/index', bg: 'linear-gradient(135deg, #FFFBEB, #FEF3C7)', color: '#F59E0B' },
  { icon: 'stethoscope', label: '营养咨询', path: '/pages/consultation/index', bg: 'linear-gradient(135deg, #FFF1F2, #FFE4E6)', color: '#EF4444' },
  { icon: 'book-open', label: '食谱库', path: '/pages/recipes/index', bg: 'linear-gradient(135deg, #FFF7ED, #FFEDD5)', color: '#F97316' },
  { icon: 'shopping-bag', label: '营养商城', path: '/pages/product-shop/index', isTab: true, bg: 'linear-gradient(135deg, #F0FDF4, #DCFCE7)', color: '#22C55E' },
  { icon: 'user', label: '个人中心', path: '/pages/profile/index', isTab: true, bg: 'linear-gradient(135deg, #F8FAFC, #F1F5F9)', color: '#64748B' }
]

function onStoreTap() {
  // TODO: Navigate to store selection page (Phase 4/5)
  uni.showToast({ title: '门店选择即将上线', icon: 'none' })
}

async function fetchFeaturedMeals() {
  try {
    const res = await mealApi.getRecommended()
    if (res.code === 200 && res.data?.length) {
      featuredMeals.value = res.data.map((m: any) => ({
        ...m,
        price: m.salePrice || m.price || m.sale_price,
        calories: m.nutritionInfo?.calories || m.nutrition_info?.calories || 0,
        tags: m.tags || []
      }))
    }
  } catch {
    // keep mock data
  }
}

async function fetchFeaturedProducts() {
  try {
    const res = await productApi.getRecommended()
    if (res.code === 200 && res.data?.length) {
      featuredProducts.value = res.data
    }
  } catch {
    // keep mock data
  }
}

async function fetchUnreadCount() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await announcementApi.getUnreadCount()
    if (res.code === 200) {
      unreadCount.value = res.data?.count || 0
    }
  } catch {}
}

function formatPrice(val: any) {
  const n = Number(val)
  return isNaN(n) ? val : n.toFixed(2)
}

function goToMeals() {
  uni.switchTab({ url: '/pages/meal-order/index' })
}

function goToMealDetail(id: number | string) {
  uni.navigateTo({ url: `/pages/meal-order/detail?id=${id}` })
}

function goToShop() {
  uni.switchTab({ url: '/pages/product-shop/index' })
}

function goToProductDetail(id: number | string) {
  uni.navigateTo({ url: `/pages/product-shop/detail?id=${id}` })
}

function handleFeatureTap(item: { path: string; isTab?: boolean }) {
  if (item.isTab) {
    uni.switchTab({ url: item.path })
  } else {
    uni.navigateTo({ url: item.path })
  }
}

function goTo(url: string) {
  uni.navigateTo({ url })
}

onShow(() => {
  const windowInfo = uni.getWindowInfo()
  const statusBarHeight = windowInfo.statusBarHeight || 0
  navBarTotalHeight.value = statusBarHeight + 44

  userStore.restore()
  if (!hasLoaded.value) {
    hasLoaded.value = true
    setTimeout(() => {
      fetchFeaturedMeals()
      fetchFeaturedProducts()
      fetchUnreadCount()
    }, 500)
  } else {
    fetchUnreadCount()
  }
})
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: $background;
}

/* ---- Navigation ---- */
.nav-brand-wrap {
  display: flex;
  align-items: center;
}
.nav-logo {
  width: 52rpx;
  height: 52rpx;
  margin-right: 12rpx;
  border-radius: 12rpx;
}
.nav-brand {
  font-size: 36rpx;
  font-weight: 800;
  color: $accent;
  letter-spacing: 1rpx;
}
.nav-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.nav-icon-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $muted;
  border-radius: 50%;
  position: relative;
}
.unread-dot {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  width: 16rpx;
  height: 16rpx;
  background: #EF4444;
  border-radius: 50%;
  border: 2rpx solid #fff;
}

.content {
  height: 100vh;
}

/* ---- Hero Section ---- */
.hero-section {
  padding: 16rpx 32rpx 0;
}
.hero-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, $accent, $accent-secondary);
  border-radius: $radius-xl;
  padding: 40rpx 32rpx;
  position: relative;
  overflow: hidden;
}
.hero-text-area {
  flex: 1;
  z-index: 1;
}
.hero-label {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 16rpx;
  background: rgba(255, 255, 255, 0.2);
  padding: 6rpx 20rpx;
  border-radius: $radius-full;
}
.hero-label-text {
  font-size: 22rpx;
  color: #fff;
  font-weight: 500;
}
.hero-title {
  display: block;
  font-size: 38rpx;
  font-weight: 800;
  color: #fff;
  letter-spacing: 2rpx;
  margin-bottom: 8rpx;
}
.hero-desc {
  display: block;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 24rpx;
}
.hero-btn {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  background: #fff;
  padding: 16rpx 36rpx;
  border-radius: $radius-full;
  transition: transform $duration-fast;
}
.hero-btn-text {
  font-size: 26rpx;
  font-weight: 600;
  color: $accent-secondary;
}
.hero-visual {
  z-index: 1;
  margin-right: -8rpx;
}

/* ---- Service Grid ---- */
.service-section {
  padding: 24rpx 32rpx 0;
}
.service-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24rpx 0;
  background: $card;
  border-radius: $radius-xl;
  padding: 32rpx 16rpx;
  box-shadow: $shadow-sm;
}
.service-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}
.service-icon-wrap {
  width: 96rpx;
  height: 96rpx;
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
}
.service-label {
  font-size: 22rpx;
  font-weight: 500;
  color: $foreground;
  text-align: center;
}

/* ---- Member Bar ---- */
.member-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 24rpx 32rpx 0;
  padding: 24rpx 28rpx;
  background: linear-gradient(135deg, #FEF3C7, #FDE68A);
  border-radius: $radius-lg;
}
.member-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
  min-width: 0;
}
.member-info {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}
.member-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #92400E;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.member-desc {
  font-size: 20rpx;
  color: #B45309;
  margin-top: 4rpx;
}
.member-btn {
  display: flex;
  align-items: center;
  gap: 4rpx;
  background: #92400E;
  padding: 10rpx 24rpx;
  border-radius: $radius-full;
  flex-shrink: 0;
}
.member-btn-text {
  font-size: 22rpx;
  font-weight: 600;
  color: #FEF3C7;
}

/* ---- Section Header ---- */
.section {
  margin-top: 32rpx;
}
.section-hd {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  margin-bottom: 20rpx;
}
.section-hd-left {
  display: flex;
  align-items: center;
}
.section-bar {
  width: 6rpx;
  height: 30rpx;
  background: linear-gradient(180deg, $accent, $accent-secondary);
  border-radius: 3rpx;
  margin-right: 12rpx;
}
.section-name {
  font-size: 32rpx;
  font-weight: 700;
  color: $foreground;
}
.section-link {
  display: flex;
  align-items: center;
  gap: 4rpx;
}
.section-link-text {
  font-size: 24rpx;
  color: $muted-foreground;
}

/* ---- Meal Cards ---- */
.meal-scroll {
  white-space: nowrap;
  padding-left: 32rpx;
}
.meal-card {
  display: inline-flex;
  flex-direction: column;
  width: 300rpx;
  margin-right: 20rpx;
  background: $card;
  border-radius: $radius-xl;
  overflow: hidden;
  box-shadow: $shadow-sm;
  vertical-align: top;
}
.meal-img-wrap {
  position: relative;
  width: 300rpx;
  height: 200rpx;
}
.meal-img {
  width: 300rpx;
  height: 200rpx;
  background: $muted;
}
.meal-cal-badge {
  position: absolute;
  bottom: 12rpx;
  right: 12rpx;
  display: flex;
  align-items: center;
  gap: 4rpx;
  background: rgba(0, 0, 0, 0.55);
  padding: 4rpx 14rpx;
  border-radius: $radius-full;
}
.meal-cal-text {
  font-size: 20rpx;
  color: #fff;
  font-weight: 500;
}
.meal-info {
  padding: 16rpx 20rpx 20rpx;
}
.meal-name {
  font-size: 28rpx;
  font-weight: 600;
  color: $foreground;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
  margin-bottom: 8rpx;
}
.meal-tag-row {
  display: flex;
  gap: 8rpx;
  margin-bottom: 12rpx;
}
.meal-tag {
  font-size: 20rpx;
  color: $accent-secondary;
  background: #ECFDF5;
  padding: 4rpx 12rpx;
  border-radius: $radius-sm;
  font-weight: 500;
}
.meal-price-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.meal-add-btn {
  width: 48rpx;
  height: 48rpx;
  background: linear-gradient(135deg, $accent, $accent-secondary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(16, 185, 129, 0.3);
}

/* ---- Product Grid ---- */
.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
  padding: 0 32rpx;
}
.prod-card {
  background: $card;
  border-radius: $radius-lg;
  overflow: hidden;
  box-shadow: $shadow-sm;
}
.prod-img-wrap {
  width: 100%;
  height: 200rpx;
}
.prod-img {
  width: 100%;
  height: 200rpx;
  background: $muted;
}
.prod-info {
  padding: 12rpx 16rpx 16rpx;
}
.prod-name {
  font-size: 24rpx;
  font-weight: 500;
  color: $foreground;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
  margin-bottom: 8rpx;
}
.prod-price-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.prod-buy-btn {
  background: #ECFDF5;
  padding: 6rpx 16rpx;
  border-radius: $radius-sm;
}
.prod-buy-text {
  font-size: 20rpx;
  color: $accent-secondary;
  font-weight: 600;
}

/* ---- Footer ---- */
.app-footer {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48rpx 32rpx 140rpx;
}
.footer-line {
  width: 64rpx;
  height: 4rpx;
  background: $border;
  border-radius: 2rpx;
  margin-bottom: 24rpx;
}
.footer-text {
  font-size: 22rpx;
  color: $muted-foreground;
  margin-bottom: 8rpx;
}
.footer-copy {
  font-size: 20rpx;
  color: #C0C8D4;
}
</style>
