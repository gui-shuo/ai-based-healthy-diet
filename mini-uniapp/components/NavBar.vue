<script setup>
/**
 * 自定义导航栏 - 适配状态栏高度
 * Digital Greenhouse 设计: 玻璃态 + 无边框
 */
import { ref, onMounted } from 'vue'

const props = defineProps({
  title: { type: String, default: '' },
  showBack: { type: Boolean, default: false },
  transparent: { type: Boolean, default: false },
  bgColor: { type: String, default: '' },
})

const statusBarHeight = ref(20)
const navBarHeight = ref(44)

onMounted(() => {
  const sysInfo = uni.getSystemInfoSync()
  statusBarHeight.value = sysInfo.statusBarHeight || 20
  // #ifdef MP-WEIXIN
  const menuRect = uni.getMenuButtonBoundingClientRect()
  navBarHeight.value = (menuRect.top - sysInfo.statusBarHeight) * 2 + menuRect.height
  // #endif
})

function goBack() {
  uni.navigateBack({ delta: 1, fail: () => {
    uni.switchTab({ url: '/pages/index/index' })
  }})
}
</script>

<template>
  <view class="navbar-wrapper">
    <!-- 占位 -->
    <view :style="{ height: statusBarHeight + navBarHeight + 'px' }"></view>
    <!-- 实际导航栏 -->
    <view
      class="navbar"
      :class="{ 'navbar--transparent': transparent }"
      :style="{
        paddingTop: statusBarHeight + 'px',
        height: statusBarHeight + navBarHeight + 'px',
        backgroundColor: bgColor || (transparent ? 'transparent' : '#ffffff'),
        backdropFilter: 'none',
        webkitBackdropFilter: 'none',
      }"
    >
      <view class="navbar__content" :style="{ height: navBarHeight + 'px' }">
        <!-- 左侧 -->
        <view class="navbar__left" @tap="showBack && goBack()">
          <slot name="left">
            <view v-if="showBack" class="navbar__back">
              <text class="navbar__back-icon">‹</text>
            </view>
          </slot>
        </view>
        <!-- 标题 -->
        <view class="navbar__center">
          <slot name="center">
            <text class="navbar__title">{{ title }}</text>
          </slot>
        </view>
        <!-- 右侧 -->
        <view class="navbar__right">
          <slot name="right"></slot>
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@import '../styles/design-system.scss';

.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;

  &--transparent {
    background: transparent !important;
  }

  &__content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24rpx;
  }

  &__left, &__right {
    width: 80rpx;
    display: flex;
    align-items: center;
  }

  &__right {
    justify-content: flex-end;
  }

  &__back {
    width: 64rpx;
    height: 64rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: $radius-full;

    &-icon {
      font-size: 48rpx;
      color: $on-surface;
      font-weight: 300;
      line-height: 1;
    }
  }

  &__center {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__title {
    font-size: $font-lg;
    font-weight: 700;
    color: $on-surface;
    letter-spacing: -0.02em;
  }
}
</style>
