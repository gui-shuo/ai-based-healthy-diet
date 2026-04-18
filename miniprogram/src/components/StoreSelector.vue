<template>
  <view class="store-selector pressable" @tap="$emit('tap')">
    <view class="store-info">
      <NutriIcon name="map-pin" :size="32" color="#10B981" />
      <view class="store-text">
        <text class="store-name">{{ storeName || '选择门店' }}</text>
        <text v-if="storeAddress" class="store-addr">{{ storeAddress }}</text>
      </view>
    </view>
    <NutriIcon name="chevron-right" :size="28" color="#8896AB" />
  </view>
</template>

<script setup lang="ts">
import NutriIcon from './NutriIcon.vue'

defineProps<{
  storeName?: string
  storeAddress?: string
}>()

defineEmits<{
  (e: 'tap'): void
}>()
</script>

<style scoped lang="scss">
.store-selector {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 24rpx;
  background: #fff;
}

.store-info {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex: 1;
  min-width: 0;
}

.store-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.store-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1A1A2E;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.store-addr {
  font-size: 22rpx;
  color: #8896AB;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-top: 4rpx;
}
</style>
