<script setup>
/**
 * 商城搜索页
 */
import { ref } from 'vue'
import { productApi } from '../../services/api'
import { formatPrice, cosUrl, debounce } from '../../utils/common'

const keyword = ref('')
const results = ref([])
const loading = ref(false)
const searched = ref(false)

const hotKeywords = ['蛋白粉', '藜麦', '坚果', '橄榄油', '奇亚籽', '蛋白棒']

const doSearch = debounce(async () => {
  if (!keyword.value.trim()) {
    results.value = []
    searched.value = false
    return
  }
  loading.value = true
  searched.value = true
  try {
    const res = await productApi.search({ keyword: keyword.value, page: 0, size: 20 })
    const list = res?.content || (Array.isArray(res) ? res : [])
    results.value = list.map(p => ({
      ...p,
      image: p.imageUrl || p.image || '',
      price: p.salePrice ?? p.price,
    }))
  } catch (e) {
    results.value = []
  } finally {
    loading.value = false
  }
}, 300)

function onInput(e) {
  keyword.value = e.detail.value
  doSearch()
}

function selectHot(kw) {
  keyword.value = kw
  doSearch()
}

function goToDetail(id) {
  uni.navigateTo({ url: `/pages/shop/detail?id=${id}` })
}

function clearKeyword() {
  keyword.value = ''
  results.value = []
  searched.value = false
}
</script>

<template>
  <view class="page">
    <view class="search-bar">
      <view class="search-input">
        <text class="search-input__icon">🔍</text>
        <input
          class="search-input__field"
          :value="keyword"
          placeholder="搜索商品名称..."
          confirm-type="search"
          @input="onInput"
          @confirm="doSearch"
          focus
        />
        <text v-if="keyword" class="search-input__clear" @tap="clearKeyword">✕</text>
      </view>
    </view>

    <scroll-view scroll-y class="content" :enhanced="true" :show-scrollbar="false">
      <view v-if="!searched" class="hot-section">
        <text class="hot-section__title">热门搜索</text>
        <view class="hot-tags">
          <view
            v-for="(kw, idx) in hotKeywords"
            :key="idx"
            class="hot-tag"
            @tap="selectHot(kw)"
          >
            <text class="hot-tag__text">{{ kw }}</text>
          </view>
        </view>
      </view>

      <view v-if="loading" class="state-tip">
        <text>搜索中...</text>
      </view>

      <view v-else-if="searched && results.length === 0" class="state-tip">
        <text>未找到相关商品</text>
      </view>

      <!-- 结果网格 -->
      <view class="result-grid">
        <view
          v-for="item in results"
          :key="item.id"
          class="result-card"
          @tap="goToDetail(item.id)"
        >
          <image
            class="result-card__image"
            :src="cosUrl(item.image) || '/static/images/product-placeholder.png'"
            mode="aspectFill"
          />
          <view class="result-card__info">
            <text class="result-card__name">{{ item.name }}</text>
            <text class="result-card__price">¥{{ formatPrice(item.price) }}</text>
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
  background: #ffffff;
}

.content {
  padding: 0 24rpx;
}

.search-bar {
  padding: 16rpx 24rpx;
}

.search-input {
  display: flex;
  align-items: center;
  background: $surface-container-low;
  border-radius: $radius-full;
  padding: 16rpx 24rpx;
  gap: 12rpx;

  &__icon { font-size: 32rpx; flex-shrink: 0; }
  &__field { flex: 1; font-size: $font-base; color: $on-surface; }
  &__clear { font-size: 28rpx; color: $on-surface-variant; padding: 8rpx; }
}

.hot-section {
  padding: 24rpx 0;
  &__title {
    display: block;
    font-size: $font-base;
    font-weight: 600;
    color: $on-surface;
    margin-bottom: 16rpx;
  }
}

.hot-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.hot-tag {
  background: $surface-container-low;
  padding: 12rpx 24rpx;
  border-radius: $radius-full;
  &__text { font-size: $font-sm; color: $on-surface; }
}

.state-tip {
  padding: 100rpx 0;
  text-align: center;
  color: $on-surface-variant;
}

.result-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.result-card {
  width: calc(50% - 8rpx);
  background: $surface-container-lowest;
  border-radius: $radius-xl;
  overflow: hidden;
  box-shadow: $shadow-sm;

  &__image {
    width: 100%;
    height: 300rpx;
  }

  &__info {
    padding: 16rpx;
  }

  &__name {
    display: block;
    font-size: $font-base;
    font-weight: 600;
    color: $on-surface;
    margin-bottom: 8rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__price {
    font-size: $font-lg;
    font-weight: 800;
    color: $primary;
  }
}
</style>
