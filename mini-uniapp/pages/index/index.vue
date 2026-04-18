<script setup>
/**
 * 首页 - 静态宣传健康饮食展示
 * Design: Digital Greenhouse
 */
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import NavBar from '../../components/NavBar.vue'
import { mealApi } from '../../services/api'
import { cosUrl } from '../../utils/common'

const userStore = useUserStore()

// 宣传Banner数据
const banners = ref([
  {
    id: 1,
    tag: '今日推荐',
    title: '轻卡活力轻食套餐',
    desc: '精选时令果蔬，搭配优质植物蛋白，为身体注入源源不断的自然能量。',
    image: 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=800&q=80',
    type: 'hero'
  },
  {
    id: 2,
    title: 'AI 定制营养方案',
    desc: '基于您的身体数据与饮食偏好，智能生成专属的一周健康食谱，告别选择困难。',
    image: 'https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=800&q=80',
    type: 'split'
  },
  {
    id: 3,
    title: '有机商城季末特惠',
    desc: '甄选全球优质有机食材，会员尊享限时折扣。让健康触手可及。',
    icon: 'eco',
    type: 'tonal'
  }
])

const recommendedMeals = ref([])

async function fetchRecommended() {
  try {
    const res = await mealApi.getRecommended()
    const list = Array.isArray(res) ? res : []
    recommendedMeals.value = list.slice(0, 3).map(m => ({
      ...m,
      image: m.imageUrl || m.image || '',
      price: m.salePrice ?? m.price,
    }))
    // 用第一个推荐餐品更新hero banner图片
    if (recommendedMeals.value.length > 0 && recommendedMeals.value[0].image) {
      banners.value[0].image = cosUrl(recommendedMeals.value[0].image)
      banners.value[0].title = recommendedMeals.value[0].name || banners.value[0].title
    }
  } catch (e) {
    // 保持默认banner
  }
}

function goToNotifications() {
  uni.navigateTo({ url: '/pages/profile/notifications' })
}

function goToProfile() {
  uni.switchTab({ url: '/pages/profile/index' })
}

onMounted(() => {
  fetchRecommended()
})
</script>

<template>
  <view class="page">
    <!-- 顶部导航栏 -->
    <NavBar>
      <template #left>
        <view class="nav-avatar" @tap="goToProfile">
          <image
            class="nav-avatar__img"
            :src="userStore.userAvatar"
            mode="aspectFill"
          />
        </view>
      </template>
      <template #center>
        <text class="nav-brand">NutriAI</text>
      </template>
      <template #right>
        <view class="nav-icon" @tap="goToNotifications">
          <image src="/static/icons/notification.png" class="nav-icon__img" mode="aspectFit"
            style="width: 44rpx; height: 44rpx; opacity: 0.7;" />
        </view>
      </template>
    </NavBar>

    <!-- 主内容区域 -->
    <scroll-view scroll-y class="content" :enhanced="true" :show-scrollbar="false">
      <!-- 欢迎区域 -->
      <view class="welcome">
        <text class="welcome__title">早上好，开启健康一天</text>
        <text class="welcome__subtitle">为您精选今日营养指南与新鲜好物。</text>
      </view>

      <!-- Banner 1: 英雄区 - 全幅图片带玻璃态覆盖 -->
      <view class="banner-hero">
        <image
          class="banner-hero__image"
          :src="banners[0].image"
          mode="aspectFill"
        />
        <view class="banner-hero__overlay" />
        <view class="banner-hero__content">
          <view class="banner-hero__glass">
            <view class="banner-hero__tag">
              <text class="banner-hero__tag-text">{{ banners[0].tag }}</text>
            </view>
            <text class="banner-hero__title">{{ banners[0].title }}</text>
            <text class="banner-hero__desc">{{ banners[0].desc }}</text>
          </view>
        </view>
      </view>

      <!-- Banner 2: 非对称布局 -->
      <view class="banner-split">
        <view class="banner-split__image-wrap">
          <image
            class="banner-split__image"
            :src="banners[1].image"
            mode="aspectFill"
          />
        </view>
        <view class="banner-split__text">
          <text class="banner-split__title">{{ banners[1].title }}</text>
          <text class="banner-split__desc">{{ banners[1].desc }}</text>
          <view class="banner-split__cta">
            <text class="banner-split__cta-text">探索更多方案</text>
            <text class="banner-split__cta-arrow">→</text>
          </view>
        </view>
      </view>

      <!-- Banner 3: 色调变化卡片 -->
      <view class="banner-tonal">
        <view class="banner-tonal__content">
          <text class="banner-tonal__title">{{ banners[2].title }}</text>
          <text class="banner-tonal__desc">{{ banners[2].desc }}</text>
        </view>
        <view class="banner-tonal__icon-wrap">
          <text class="banner-tonal__icon">🌿</text>
        </view>
        <!-- 装饰圆形 -->
        <view class="banner-tonal__decor" />
      </view>

      <!-- 底部安全距离 -->
      <view style="height: 40rpx;" />
    </scroll-view>
  </view>
</template>

<style lang="scss" scoped>
@import '../../styles/design-system.scss';

.page {
  min-height: 100vh;
  background-color: #ffffff;
  overflow-x: hidden;
  width: 100vw;
  max-width: 100vw;
  box-sizing: border-box;
}

.content {
  height: calc(100vh - 88px);
  padding: 0 24rpx;
  overflow-x: hidden;
  box-sizing: border-box;
}

// 导航栏元素
.nav-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: $radius-full;
  overflow: hidden;
  border: 4rpx solid #fff;
  box-shadow: $shadow-sm;

  &__img {
    width: 100%;
    height: 100%;
  }
}

.nav-brand {
  font-size: $font-xl;
  font-weight: 800;
  color: $primary;
  letter-spacing: -0.02em;
}

.nav-icon {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-full;
}

// 欢迎区域
.welcome {
  padding: 32rpx 0 16rpx;

  &__title {
    display: block;
    font-size: $font-2xl;
    font-weight: 700;
    color: $on-surface;
    letter-spacing: -0.01em;
    margin-bottom: 8rpx;
  }

  &__subtitle {
    display: block;
    font-size: $font-base;
    color: $on-surface-variant;
    line-height: $leading-relaxed;
  }
}

// Banner 1: 英雄区
.banner-hero {
  position: relative;
  border-radius: $radius-2xl;
  overflow: hidden;
  box-shadow: $shadow-lg;
  margin-bottom: 24rpx;
  max-width: 100%;

  &__image {
    width: 100%;
    height: 480rpx;
    display: block;
  }

  &__overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(to right, rgba($primary, 0.75), transparent);
  }

  &__content {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 32rpx;
  }

  &__glass {
    background: rgba(255, 255, 255, 0.25);
    border-radius: $radius-lg;
    padding: 32rpx;
    border: 1rpx solid rgba(255, 255, 255, 0.15);
  }

  &__tag {
    display: inline-block;
    background: #fff;
    padding: 6rpx 20rpx;
    border-radius: $radius-full;
    margin-bottom: 16rpx;

    &-text {
      font-size: $font-xs;
      font-weight: 600;
      color: $primary;
      letter-spacing: 0.05em;
    }
  }

  &__title {
    display: block;
    font-size: $font-2xl;
    font-weight: 700;
    color: #fff;
    margin-bottom: 12rpx;
  }

  &__desc {
    display: block;
    font-size: $font-sm;
    color: rgba(255, 255, 255, 0.9);
    line-height: $leading-relaxed;
  }
}

// Banner 2: 非对称布局
.banner-split {
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-radius: $radius-2xl;
  overflow: hidden;
  box-shadow: $shadow-md;
  margin-bottom: 24rpx;
  max-width: 100%;

  &__image-wrap {
    height: 300rpx;
    overflow: hidden;
  }

  &__image {
    width: 100%;
    height: 100%;
  }

  &__text {
    padding: 32rpx;
  }

  &__title {
    display: block;
    font-size: $font-xl;
    font-weight: 700;
    color: $on-surface;
    margin-bottom: 12rpx;
  }

  &__desc {
    display: block;
    font-size: $font-sm;
    color: $on-surface-variant;
    line-height: $leading-relaxed;
    margin-bottom: 20rpx;
  }

  &__cta {
    display: flex;
    align-items: center;

    &-text {
      font-size: $font-sm;
      font-weight: 500;
      color: $primary;
    }

    &-arrow {
      font-size: $font-base;
      color: $primary;
      margin-left: 8rpx;
    }
  }
}

// Banner 3: 色调卡片
.banner-tonal {
  position: relative;
  background: #ffffff;
  border-radius: $radius-2xl;
  padding: 40rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: $shadow-md;
  overflow: hidden;
  margin-bottom: 24rpx;
  max-width: 100%;

  &__content {
    flex: 1;
    z-index: 1;
    padding-right: 24rpx;
  }

  &__title {
    display: block;
    font-size: $font-2xl;
    font-weight: 800;
    color: $on-surface;
    margin-bottom: 12rpx;
  }

  &__desc {
    display: block;
    font-size: $font-sm;
    color: $on-surface-variant;
    line-height: $leading-relaxed;
  }

  &__icon-wrap {
    width: 120rpx;
    height: 120rpx;
    background: #fff;
    border-radius: $radius-full;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8rpx 32rpx rgba($primary, 0.1);
    z-index: 1;
    flex-shrink: 0;
  }

  &__icon {
    font-size: 56rpx;
  }

  &__decor {
    position: absolute;
    right: -40rpx;
    bottom: -40rpx;
    width: 300rpx;
    height: 300rpx;
    border-radius: $radius-full;
    background: rgba($primary, 0.06);
  }
}
</style>
