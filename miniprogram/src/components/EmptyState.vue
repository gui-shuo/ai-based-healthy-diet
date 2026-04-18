<template>
  <view class="empty-state">
    <NutriIcon :name="icon" :size="120" color="#CBD5E1" />
    <text class="empty-title">{{ title }}</text>
    <text v-if="description" class="empty-desc">{{ description }}</text>
    <view v-if="actionText" class="empty-action">
      <button class="btn-primary btn-sm btn-pill" @tap="$emit('action')">
        {{ actionText }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import NutriIcon from './NutriIcon.vue'

withDefaults(defineProps<{
  icon?: string
  title?: string
  description?: string
  actionText?: string
}>(), {
  icon: 'info',
  title: '暂无内容'
})

defineEmits<{
  (e: 'action'): void
}>()
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 48rpx;
  gap: 20rpx;
}

.empty-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #8896AB;
  margin-top: 16rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #A0AEC0;
  text-align: center;
  line-height: 1.5;
}

.empty-action {
  margin-top: 24rpx;
}
</style>
