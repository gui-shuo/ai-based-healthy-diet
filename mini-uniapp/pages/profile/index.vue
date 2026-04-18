<script setup>
/**
 * 我的 - 个人中心首页
 * Design: Digital Greenhouse
 */
import { ref, computed } from 'vue'
import { useUserStore } from '../../stores/user'
import NavBar from '../../components/NavBar.vue'
import { checkLogin } from '../../utils/common'

const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)

// 菜单分组
const menuGroups = ref([
  {
    title: '我的订单',
    items: [
      { icon: '📋', label: '全部订单', url: '/pages/profile/orders', badge: '' },
      { icon: '🎫', label: '我的优惠券', url: '/pages/profile/coupons', badge: '' },
    ]
  },
  {
    title: '服务',
    items: [
      { icon: '📍', label: '收货地址', url: '/pages/profile/address' },
      { icon: '🔔', label: '消息通知', url: '/pages/profile/notifications' },
      { icon: '💬', label: '意见反馈', url: '/pages/profile/feedback' },
      { icon: '⚙️', label: '设置', url: '/pages/profile/settings' },
    ]
  }
])

function goToLogin() {
  uni.navigateTo({ url: '/pages/auth/login' })
}

function goToEdit() {
  if (!checkLogin()) return
  uni.navigateTo({ url: '/pages/profile/edit' })
}

function goToPage(url) {
  if (!checkLogin()) return
  uni.navigateTo({ url })
}
</script>

<template>
  <view class="page">
    <NavBar>
      <template #center>
        <text class="nav-title">我的</text>
      </template>
      <template #right>
        <view class="nav-settings" @tap="goToPage('/pages/profile/settings')">
          <text style="font-size: 40rpx;">⚙️</text>
        </view>
      </template>
    </NavBar>

    <scroll-view scroll-y class="content" :enhanced="true" :show-scrollbar="false">
      <!-- 用户信息卡片 -->
      <view class="user-card">
        <!-- 装饰背景 -->
        <view class="user-card__bg" />

        <view v-if="isLoggedIn" class="user-card__info" @tap="goToEdit">
          <image
            class="user-card__avatar"
            :src="userStore.userAvatar"
            mode="aspectFill"
          />
          <view class="user-card__text">
            <text class="user-card__name">{{ userStore.userName }}</text>
            <view v-if="userStore.isVip" class="user-card__vip">
              <text class="user-card__vip-icon">👑</text>
              <text class="user-card__vip-text">VIP会员</text>
            </view>
            <text class="user-card__edit">编辑资料 ›</text>
          </view>
        </view>

        <view v-else class="user-card__info" @tap="goToLogin">
          <view class="user-card__avatar user-card__avatar--default">
            <text style="font-size: 64rpx;">👤</text>
          </view>
          <view class="user-card__text">
            <text class="user-card__name">点击登录</text>
            <text class="user-card__desc">登录享受更多服务</text>
          </view>
        </view>
      </view>

      <!-- 快捷入口 -->
      <view class="quick-actions">
        <view class="quick-action" @tap="goToPage('/pages/profile/orders')">
          <text class="quick-action__icon">📦</text>
          <text class="quick-action__label">我的订单</text>
        </view>
        <view class="quick-action" @tap="goToPage('/pages/profile/coupons')">
          <text class="quick-action__icon">🎫</text>
          <text class="quick-action__label">优惠券</text>
        </view>
        <view class="quick-action" @tap="goToPage('/pages/profile/address')">
          <text class="quick-action__icon">📍</text>
          <text class="quick-action__label">地址</text>
        </view>
        <view class="quick-action" @tap="goToPage('/pages/profile/notifications')">
          <text class="quick-action__icon">🔔</text>
          <text class="quick-action__label">消息</text>
        </view>
      </view>

      <!-- 菜单分组 -->
      <view
        v-for="(group, gIdx) in menuGroups"
        :key="gIdx"
        class="menu-group"
      >
        <text class="menu-group__title">{{ group.title }}</text>
        <view class="menu-group__card">
          <view
            v-for="(item, iIdx) in group.items"
            :key="iIdx"
            class="menu-item"
            @tap="goToPage(item.url)"
          >
            <text class="menu-item__icon">{{ item.icon }}</text>
            <text class="menu-item__label">{{ item.label }}</text>
            <text class="menu-item__arrow">›</text>
          </view>
        </view>
      </view>

      <view style="height: 40rpx;" />
    </scroll-view>
  </view>
</template>

<style lang="scss" scoped>
@import '../../styles/design-system.scss';

.page {
  min-height: 100vh;
  background: $surface;
}

.content {
  height: calc(100vh - 88px);
}

.nav-title {
  font-size: $font-xl;
  font-weight: 800;
  color: $on-surface;
}

.nav-settings {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

// 用户卡片
.user-card {
  position: relative;
  margin: 16rpx 24rpx;
  border-radius: $radius-2xl;
  overflow: hidden;
  background: $surface-container-lowest;
  box-shadow: $shadow-md;

  &__bg {
    position: absolute;
    top: -60rpx;
    right: -60rpx;
    width: 300rpx;
    height: 300rpx;
    border-radius: $radius-full;
    background: rgba($primary, 0.08);
  }

  &__info {
    display: flex;
    align-items: center;
    gap: 24rpx;
    padding: 40rpx;
    position: relative;
    z-index: 1;
  }

  &__avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: $radius-full;
    flex-shrink: 0;

    &--default {
      background: $surface-container-low;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  &__text {
    flex: 1;
  }

  &__name {
    display: block;
    font-size: $font-xl;
    font-weight: 700;
    color: $on-surface;
    margin-bottom: 6rpx;
  }

  &__desc {
    display: block;
    font-size: $font-sm;
    color: $on-surface-variant;
  }

  &__vip {
    display: inline-flex;
    align-items: center;
    gap: 6rpx;
    background: linear-gradient(135deg, #f5e6cc, #e8d5b0);
    padding: 4rpx 14rpx;
    border-radius: $radius-full;
    margin-bottom: 6rpx;

    &-icon {
      font-size: 24rpx;
    }

    &-text {
      font-size: 22rpx;
      font-weight: 600;
      color: #8b6914;
    }
  }

  &__edit {
    font-size: $font-sm;
    color: $primary;
  }
}

// 快捷入口
.quick-actions {
  display: flex;
  justify-content: space-around;
  padding: 24rpx;
  margin: 0 24rpx 24rpx;
  background: $surface-container-lowest;
  border-radius: $radius-xl;
  box-shadow: $shadow-sm;
}

.quick-action {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;

  &__icon {
    font-size: 48rpx;
  }

  &__label {
    font-size: $font-xs;
    color: $on-surface;
    font-weight: 500;
  }
}

// 菜单分组
.menu-group {
  margin: 0 24rpx 24rpx;

  &__title {
    display: block;
    font-size: $font-sm;
    font-weight: 600;
    color: $on-surface-variant;
    margin-bottom: 12rpx;
    padding-left: 8rpx;
  }

  &__card {
    background: $surface-container-lowest;
    border-radius: $radius-xl;
    overflow: hidden;
    box-shadow: $shadow-sm;
  }
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 28rpx 24rpx;
  gap: 20rpx;

  & + & {
    border-top: 1rpx solid $surface-container;
  }

  &__icon {
    font-size: 36rpx;
    flex-shrink: 0;
  }

  &__label {
    flex: 1;
    font-size: $font-base;
    color: $on-surface;
  }

  &__arrow {
    font-size: $font-xl;
    color: $on-surface-variant;
    opacity: 0.5;
  }
}
</style>
