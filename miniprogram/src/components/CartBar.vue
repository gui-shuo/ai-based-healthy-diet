<template>
  <view v-if="totalCount > 0" class="cart-bar safe-bottom">
    <view class="cart-left pressable" @tap="$emit('viewCart')">
      <view class="cart-icon-wrap">
        <NutriIcon name="shopping-cart" :size="44" color="#fff" />
        <view v-if="totalCount > 0" class="cart-badge">{{ totalCount > 99 ? '99+' : totalCount }}</view>
      </view>
      <view class="cart-summary">
        <PriceTag :price="totalPrice" size="lg" />
        <text v-if="deliveryFee" class="cart-delivery">另需配送费 ¥{{ deliveryFee }}</text>
      </view>
    </view>
    <view class="cart-right">
      <button
        class="cart-submit-btn"
        :class="{ disabled: totalPrice < minOrderAmount }"
        :disabled="totalPrice < minOrderAmount"
        @tap="$emit('submit')"
      >
        <text v-if="totalPrice < minOrderAmount">¥{{ minOrderAmount }}起送</text>
        <text v-else>去结算</text>
      </button>
    </view>
  </view>
  <!-- Placeholder -->
  <view v-if="totalCount > 0" class="cart-bar-placeholder" />
</template>

<script setup lang="ts">
import NutriIcon from './NutriIcon.vue'
import PriceTag from './PriceTag.vue'

withDefaults(defineProps<{
  totalCount: number
  totalPrice: number
  deliveryFee?: number
  minOrderAmount?: number
}>(), {
  minOrderAmount: 0
})

defineEmits<{
  (e: 'viewCart'): void
  (e: 'submit'): void
}>()
</script>

<style scoped lang="scss">
.cart-bar {
  position: fixed;
  left: 24rpx;
  right: 24rpx;
  bottom: 24rpx;
  z-index: $z-cart-bar;
  display: flex;
  align-items: center;
  height: 100rpx;
  background: #1A1A2E;
  border-radius: 50rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 32rpx rgba(26, 26, 46, 0.3);
}

.cart-bar-placeholder {
  height: calc(100rpx + 48rpx + env(safe-area-inset-bottom));
}

.cart-left {
  flex: 1;
  display: flex;
  align-items: center;
  padding-left: 24rpx;
  gap: 16rpx;
  min-width: 0;
}

.cart-icon-wrap {
  position: relative;
  width: 80rpx;
  height: 80rpx;
  background: #10B981;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: -20rpx;
  box-shadow: 0 4rpx 16rpx rgba(16, 185, 129, 0.4);
}

.cart-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 32rpx;
  height: 32rpx;
  line-height: 32rpx;
  text-align: center;
  font-size: 20rpx;
  font-weight: 700;
  color: #fff;
  background: #EF4444;
  border-radius: 16rpx;
  padding: 0 8rpx;
}

.cart-summary {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

:deep(.price-tag) {
  color: #fff;
}

.cart-delivery {
  font-size: 20rpx;
  color: #8896AB;
  margin-top: 2rpx;
}

.cart-right {
  flex-shrink: 0;
  padding: 0 8rpx;
}

.cart-submit-btn {
  height: 80rpx;
  line-height: 80rpx;
  padding: 0 40rpx;
  background: #10B981;
  color: #fff;
  font-size: 28rpx;
  font-weight: 600;
  border: none;
  border-radius: 42rpx;
  white-space: nowrap;
}

.cart-submit-btn.disabled {
  background: #374151;
  color: #6B7280;
}
</style>
