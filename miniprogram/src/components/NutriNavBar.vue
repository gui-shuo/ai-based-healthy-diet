<template>
  <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
    <view class="nav-bar-inner" :class="{ 'nav-bar-transparent': transparent }">
      <view class="nav-left">
        <view v-if="showBack" class="nav-back pressable" @tap="handleBack">
          <NutriIcon name="arrow-left" :size="36" :color="textColor" />
        </view>
        <slot name="left" />
      </view>

      <view class="nav-center">
        <slot name="title">
          <text class="nav-title" :style="{ color: textColor }">{{ title }}</text>
        </slot>
      </view>

      <view class="nav-right">
        <slot name="right" />
      </view>
    </view>
  </view>
  <!-- Placeholder to offset content below fixed nav -->
  <view v-if="!float" :style="{ height: navTotalHeight + 'px' }" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import NutriIcon from './NutriIcon.vue'

const props = withDefaults(defineProps<{
  title?: string
  showBack?: boolean
  transparent?: boolean
  float?: boolean
  bgColor?: string
  textColor?: string
}>(), {
  title: '',
  showBack: true,
  transparent: false,
  float: false,
  bgColor: '#FFFFFF',
  textColor: '#1A1A2E'
})

const emit = defineEmits<{
  (e: 'back'): void
}>()

const statusBarHeight = ref(20)
const navBarHeight = 44
const navTotalHeight = ref(64)

const sysInfo = uni.getWindowInfo()
statusBarHeight.value = sysInfo.statusBarHeight || 20
navTotalHeight.value = statusBarHeight.value + navBarHeight

function handleBack() {
  emit('back')
  uni.navigateBack({
    fail: () => uni.switchTab({ url: '/pages/index/index' })
  })
}
</script>

<style scoped lang="scss">
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: $z-nav;
  background: v-bind(bgColor);
}

.nav-bar-inner {
  height: 44px;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  position: relative;
}

.nav-bar-transparent {
  background: transparent;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
  min-width: 80rpx;
}

.nav-back {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.nav-center {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
  min-width: 80rpx;
  justify-content: flex-end;
}
</style>
