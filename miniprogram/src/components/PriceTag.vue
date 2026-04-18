<template>
  <view class="price-tag" :class="[sizeClass]">
    <text class="price-symbol">¥</text>
    <text class="price-int">{{ intPart }}</text>
    <text v-if="decPart" class="price-dec">.{{ decPart }}</text>
    <text v-if="originalPrice" class="price-original">¥{{ originalPrice }}</text>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  price: number | string
  originalPrice?: number | string
  size?: 'sm' | 'md' | 'lg'
}>(), {
  size: 'md'
})

const sizeClass = computed(() => `price-${props.size}`)

const intPart = computed(() => {
  const p = Number(props.price)
  return isNaN(p) ? '0' : Math.floor(p).toString()
})

const decPart = computed(() => {
  const p = Number(props.price)
  if (isNaN(p)) return ''
  const d = (p % 1).toFixed(2).slice(2)
  return d === '00' ? '' : d
})
</script>

<style scoped lang="scss">
.price-tag {
  display: inline-flex;
  align-items: baseline;
  color: #EF4444;
  font-weight: 700;
}

.price-symbol {
  font-size: 22rpx;
  margin-right: 2rpx;
}
.price-int {
  font-size: 36rpx;
  line-height: 1;
}
.price-dec {
  font-size: 22rpx;
}

/* Sizes */
.price-sm .price-symbol { font-size: 18rpx; }
.price-sm .price-int { font-size: 28rpx; }
.price-sm .price-dec { font-size: 18rpx; }

.price-lg .price-symbol { font-size: 28rpx; }
.price-lg .price-int { font-size: 48rpx; }
.price-lg .price-dec { font-size: 28rpx; }

.price-original {
  font-size: 22rpx;
  color: #A0AEC0;
  font-weight: 400;
  text-decoration: line-through;
  margin-left: 8rpx;
}
</style>
