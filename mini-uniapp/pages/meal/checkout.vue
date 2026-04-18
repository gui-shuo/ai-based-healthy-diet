<script setup>
/**
 * 营养餐下单/结算页
 */
import { ref, computed, onMounted } from 'vue'
import NavBar from '../../components/NavBar.vue'
import { mealApi } from '../../services/api'
import { useUserStore } from '../../stores/user'
import { formatPrice, cosUrl, checkLogin } from '../../utils/common'

const userStore = useUserStore()

const mealId = ref('')
const meal = ref(null)
const quantity = ref(1)
const pickupTime = ref('')
const remark = ref('')
const loading = ref(false)
const submitting = ref(false)
const currentStore = ref({ name: 'NutriAI 旗舰店' })

const totalPrice = computed(() => {
  if (!meal.value) return 0
  return meal.value.price * quantity.value
})

// 取餐时间选项
const timeSlots = ref([
  '11:30 - 12:00',
  '12:00 - 12:30',
  '12:30 - 13:00',
  '17:30 - 18:00',
  '18:00 - 18:30',
])

onMounted(() => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  mealId.value = page.options?.id || ''
  fetchDetail()
})

async function fetchDetail() {
  loading.value = true
  try {
    const res = await mealApi.getDetail(mealId.value)
    const ni = res.nutritionInfo || {}
    meal.value = {
      ...res,
      image: res.imageUrl || res.image || '',
      price: res.salePrice ?? res.price,
      calories: ni.calories ?? res.calories ?? 0,
    }
  } catch (e) {
    console.error('获取餐品详情失败', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function changeQty(delta) {
  const next = quantity.value + delta
  if (next >= 1 && next <= 99) quantity.value = next
}

function selectTime(t) {
  pickupTime.value = t
}

async function submitOrder() {
  if (!checkLogin()) return
  if (!pickupTime.value) {
    uni.showToast({ title: '请选择取餐时间', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await mealApi.createOrder({
      items: [{ mealItemId: Number(mealId.value), quantity: quantity.value }],
      fulfillmentType: 'PICKUP',
      pickupTime: pickupTime.value,
      pickupLocation: currentStore.value?.name || 'NutriAI 旗舰店',
      remark: remark.value,
    })
    uni.showToast({ title: '下单成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e) {
    uni.showToast({ title: e.message || '下单失败，请重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <view class="page">
    <NavBar showBack title="确认订单" />

    <scroll-view scroll-y class="content" :enhanced="true" :show-scrollbar="false">
      <!-- 餐品信息 -->
      <view v-if="meal" class="meal-info">
        <image
          class="meal-info__image"
          :src="cosUrl(meal.image) || '/static/images/meal-placeholder.png'"
          mode="aspectFill"
        />
        <view class="meal-info__text">
          <text class="meal-info__name">{{ meal.name }}</text>
          <text class="meal-info__calories">{{ meal.calories }} kcal</text>
          <text class="meal-info__price">¥{{ formatPrice(meal.price) }}</text>
        </view>
      </view>

      <!-- 数量 -->
      <view class="section">
        <text class="section__title">数量</text>
        <view class="qty-control">
          <view class="qty-btn" @tap="changeQty(-1)">
            <text>−</text>
          </view>
          <text class="qty-value">{{ quantity }}</text>
          <view class="qty-btn" @tap="changeQty(1)">
            <text>+</text>
          </view>
        </view>
      </view>

      <!-- 取餐时间 -->
      <view class="section">
        <text class="section__title">取餐时间</text>
        <view class="time-slots">
          <view
            v-for="(t, idx) in timeSlots"
            :key="idx"
            class="time-slot"
            :class="{ 'time-slot--active': pickupTime === t }"
            @tap="selectTime(t)"
          >
            <text class="time-slot__text">{{ t }}</text>
          </view>
        </view>
      </view>

      <!-- 备注 -->
      <view class="section">
        <text class="section__title">备注</text>
        <textarea
          class="remark-input"
          v-model="remark"
          placeholder="有什么特殊要求？（可选）"
          maxlength="200"
          :auto-height="true"
        />
      </view>

      <view style="height: 160rpx;" />
    </scroll-view>

    <!-- 底部下单栏 -->
    <view class="order-bar safe-bottom">
      <view class="order-bar__total">
        <text class="order-bar__label">合计</text>
        <text class="order-bar__price">¥{{ formatPrice(totalPrice) }}</text>
      </view>
      <view
        class="order-bar__btn"
        :class="{ 'order-bar__btn--disabled': submitting }"
        @tap="submitOrder"
      >
        <text>{{ submitting ? '提交中...' : '立即下单' }}</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@import '../../styles/design-system.scss';

.page {
  min-height: 100vh;
  background: $surface;
}

.content {
  padding: 24rpx;
  height: calc(100vh - 100px);
}

// 餐品信息
.meal-info {
  display: flex;
  gap: 20rpx;
  padding: 24rpx;
  background: $surface-container-lowest;
  border-radius: $radius-xl;
  margin-bottom: 24rpx;

  &__image {
    width: 180rpx;
    height: 180rpx;
    border-radius: $radius-lg;
    flex-shrink: 0;
  }

  &__text {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }

  &__name {
    font-size: $font-lg;
    font-weight: 700;
    color: $on-surface;
    margin-bottom: 8rpx;
  }

  &__calories {
    font-size: $font-sm;
    color: $on-surface-variant;
    margin-bottom: 12rpx;
  }

  &__price {
    font-size: $font-xl;
    font-weight: 800;
    color: $primary;
  }
}

// 通用段落
.section {
  background: $surface-container-lowest;
  border-radius: $radius-xl;
  padding: 24rpx;
  margin-bottom: 20rpx;

  &__title {
    display: block;
    font-size: $font-base;
    font-weight: 600;
    color: $on-surface;
    margin-bottom: 16rpx;
  }
}

// 数量控制
.qty-control {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.qty-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $surface-container-low;
  border-radius: $radius-full;
  font-size: 36rpx;
  color: $on-surface;

  &:active {
    background: $surface-container;
  }
}

.qty-value {
  font-size: $font-xl;
  font-weight: 700;
  color: $on-surface;
  min-width: 60rpx;
  text-align: center;
}

// 取餐时间
.time-slots {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.time-slot {
  padding: 14rpx 24rpx;
  background: $surface-container-low;
  border-radius: $radius-lg;

  &--active {
    background: $primary-container;
  }

  &__text {
    font-size: $font-sm;
    color: $on-surface;
  }
}

// 备注
.remark-input {
  width: 100%;
  font-size: $font-base;
  color: $on-surface;
  background: $surface-container-low;
  border-radius: $radius-lg;
  padding: 16rpx 20rpx;
  box-sizing: border-box;
}

// 底部下单栏
.order-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 24rpx;
  background: rgba(255, 255, 255, 0.95);
  background: rgba(255, 255, 255, 0.95);
  z-index: 50;

  &__total {
    display: flex;
    align-items: baseline;
    gap: 8rpx;
  }

  &__label {
    font-size: $font-base;
    color: $on-surface-variant;
  }

  &__price {
    font-size: $font-2xl;
    font-weight: 800;
    color: $primary;
  }

  &__btn {
    padding: 20rpx 48rpx;
    background: $primary;
    color: $on-primary;
    border-radius: $radius-lg;
    font-size: $font-base;
    font-weight: 600;

    &--disabled {
      opacity: 0.5;
      pointer-events: none;
    }

    &:active {
      transform: scale(0.96);
    }
  }
}
</style>
