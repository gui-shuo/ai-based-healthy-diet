<script setup>
/**
 * 设置页
 */
import { ref } from 'vue'
import NavBar from '../../components/NavBar.vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

function clearCache() {
  uni.showModal({
    title: '提示',
    content: '确定清除本地缓存吗？',
    success: (res) => {
      if (res.confirm) {
        uni.clearStorageSync()
        uni.showToast({ title: '缓存已清除', icon: 'success' })
      }
    }
  })
}

function logout() {
  uni.showModal({
    title: '提示',
    content: '确定退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        uni.switchTab({ url: '/pages/index/index' })
      }
    }
  })
}

function goToAbout() {
  uni.showToast({ title: 'NutriAI v1.0.0', icon: 'none' })
}
</script>

<template>
  <view class="page">
    <NavBar showBack title="设置" />

    <scroll-view scroll-y class="content" :enhanced="true" :show-scrollbar="false">
      <view class="menu-card">
        <view class="menu-item" @tap="goToAbout">
          <text class="menu-item__icon">ℹ️</text>
          <text class="menu-item__label">关于我们</text>
          <text class="menu-item__arrow">›</text>
        </view>
        <view class="menu-item" @tap="clearCache">
          <text class="menu-item__icon">🗑️</text>
          <text class="menu-item__label">清除缓存</text>
          <text class="menu-item__arrow">›</text>
        </view>
      </view>

      <view v-if="userStore.isLoggedIn" class="logout-btn" @tap="logout">
        <text>退出登录</text>
      </view>
    </scroll-view>
  </view>
</template>

<style lang="scss" scoped>
@import '../../styles/design-system.scss';

.page { min-height: 100vh; background: $surface; }
.content { padding: 24rpx; height: calc(100vh - 100px); }

.menu-card {
  background: $surface-container-lowest;
  border-radius: $radius-xl;
  overflow: hidden;
  margin-bottom: 32rpx;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 28rpx 24rpx;
  gap: 20rpx;

  & + & { border-top: 1rpx solid $surface-container; }

  &__icon { font-size: 36rpx; }
  &__label { flex: 1; font-size: $font-base; color: $on-surface; }
  &__arrow { font-size: $font-xl; color: $on-surface-variant; opacity: 0.5; }
}

.logout-btn {
  padding: 24rpx;
  background: $surface-container-lowest;
  text-align: center;
  border-radius: $radius-xl;
  font-size: $font-base;
  font-weight: 600;
  color: $error;

  &:active { background: $surface-container-low; }
}
</style>
