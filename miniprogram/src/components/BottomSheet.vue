<template>
  <view v-if="modelValue" class="bottom-sheet-mask" @tap="handleMaskTap">
    <view
      class="bottom-sheet"
      :class="{ 'bottom-sheet-show': modelValue }"
      :style="{ maxHeight: maxHeight }"
      @tap.stop
    >
      <!-- Handle bar -->
      <view class="sheet-handle">
        <view class="sheet-handle-bar" />
      </view>

      <!-- Header -->
      <view v-if="title || $slots.header" class="sheet-header">
        <slot name="header">
          <text class="sheet-title">{{ title }}</text>
        </slot>
        <view class="sheet-close pressable" @tap="close">
          <NutriIcon name="x" :size="36" color="#8896AB" />
        </view>
      </view>

      <!-- Content -->
      <scroll-view class="sheet-body" scroll-y :style="{ maxHeight: bodyMaxHeight }">
        <slot />
      </scroll-view>

      <!-- Footer -->
      <view v-if="$slots.footer" class="sheet-footer safe-bottom">
        <slot name="footer" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import NutriIcon from './NutriIcon.vue'

const props = withDefaults(defineProps<{
  modelValue: boolean
  title?: string
  maxHeight?: string
  closeOnMask?: boolean
}>(), {
  title: '',
  maxHeight: '80vh',
  closeOnMask: true
})

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'close'): void
}>()

const bodyMaxHeight = `calc(${props.maxHeight} - 160rpx)`

function close() {
  emit('update:modelValue', false)
  emit('close')
}

function handleMaskTap() {
  if (props.closeOnMask) close()
}
</script>

<style scoped lang="scss">
.bottom-sheet-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.5);
  z-index: $z-modal;
  display: flex;
  align-items: flex-end;
}

.bottom-sheet {
  width: 100%;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  animation: sheetSlideUp 300ms cubic-bezier(0.16, 1, 0.3, 1);
  overflow: hidden;
}

@keyframes sheetSlideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.sheet-handle {
  display: flex;
  justify-content: center;
  padding: 16rpx 0 8rpx;
}

.sheet-handle-bar {
  width: 64rpx;
  height: 8rpx;
  background: #E2E8F0;
  border-radius: 4rpx;
}

.sheet-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 32rpx 24rpx;
}

.sheet-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #1A1A2E;
}

.sheet-close {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #F5F7FA;
}

.sheet-body {
  padding: 0 32rpx 32rpx;
}

.sheet-footer {
  padding: 24rpx 32rpx;
  border-top: 1rpx solid #EAEFF5;
}
</style>
