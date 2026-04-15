<template>
  <view class="page">
    <view class="search-bar">
      <input class="search-input" v-model="keyword" placeholder="搜索产品..." @confirm="loadList(true)" />
      <view class="add-btn" @click="goEdit(0)">
        <text>+ 发布</text>
      </view>
    </view>

    <!-- 分类筛选 -->
    <scroll-view scroll-x class="category-scroll">
      <view class="category-item" :class="{ active: category === c.key }"
        v-for="c in categories" :key="c.key" @click="category = c.key; loadList(true)">
        <text>{{ c.label }}</text>
      </view>
    </scroll-view>

    <view v-if="list.length" class="list">
      <view class="item-card" v-for="item in list" :key="item.id">
        <image v-if="item.imageUrl" class="item-img" :src="item.imageUrl" mode="aspectFill" />
        <view class="item-body">
          <view class="item-top">
            <text class="item-title">{{ item.name }}</text>
            <text class="item-price">¥{{ item.salePrice || item.price }}</text>
          </view>
          <view class="item-meta">
            <text>库存: {{ item.stock }}</text>
            <text>销量: {{ item.salesCount || 0 }}</text>
            <text class="tag" :class="'tag-' + item.status">{{ statusText(item.status) }}</text>
          </view>
          <view class="item-actions">
            <text class="action-btn edit" @click="goEdit(item.id)">编辑</text>
            <text class="action-btn" @click="setStatus(item, item.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE')">
              {{ item.status === 'ACTIVE' ? '下架' : '上架' }}
            </text>
            <text class="action-btn danger" @click="handleDelete(item)">删除</text>
          </view>
        </view>
      </view>
    </view>
    <view v-else-if="!loading" class="empty">
      <text>暂无产品</text>
      <view class="empty-btn" @click="goEdit(0)">去发布</view>
    </view>

    <view v-if="loading" class="loading-hint"><text>加载中...</text></view>
    <view v-else-if="noMore && list.length" class="loading-hint"><text>没有更多了</text></view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminProductApi, PRODUCT_CATEGORIES } from '../../services/api'

const list = ref<any[]>([])
const keyword = ref('')
const category = ref('')
const page = ref(1)
const loading = ref(false)
const noMore = ref(false)

const categories = PRODUCT_CATEGORIES

const statusText = (s: string) => {
  const map: Record<string, string> = { ACTIVE: '在售', INACTIVE: '下架', SOLD_OUT: '售罄' }
  return map[s] || s
}

const loadList = async (reset = false) => {
  if (reset) { page.value = 1; noMore.value = false }
  loading.value = true
  try {
    const res = await adminProductApi.getList({
      page: page.value, size: 10, keyword: keyword.value, category: category.value
    })
    if (res.code === 200) {
      const items = res.data?.content || res.data?.records || res.data || []
      list.value = reset ? items : [...list.value, ...items]
      noMore.value = items.length < 10
    }
  } catch { uni.showToast({ title: '加载失败', icon: 'none' }) }
  finally { loading.value = false }
}

const goEdit = (id: number) => uni.navigateTo({ url: `/pages/admin/product-edit?id=${id}` })

const setStatus = async (item: any, status: string) => {
  try {
    await adminProductApi.updateStatus(item.id, status)
    item.status = status
    uni.showToast({ title: '已更新', icon: 'success' })
  } catch { uni.showToast({ title: '操作失败', icon: 'none' }) }
}

const handleDelete = (item: any) => {
  uni.showModal({
    title: '确认删除', content: `确定删除「${item.name}」？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await adminProductApi.remove(item.id)
          list.value = list.value.filter(i => i.id !== item.id)
          uni.showToast({ title: '已删除', icon: 'success' })
        } catch { uni.showToast({ title: '删除失败', icon: 'none' }) }
      }
    }
  })
}

onMounted(() => loadList(true))
onReachBottom(() => { if (!noMore.value && !loading.value) { page.value++; loadList() } })
</script>

<style lang="scss" scoped>
.page { padding: 24rpx; background: #F5F7FA; min-height: 100vh; }
.search-bar { display: flex; gap: 16rpx; margin-bottom: 20rpx; }
.search-input {
  flex: 1; height: 72rpx; background: #fff; border-radius: 12rpx; padding: 0 24rpx;
  font-size: 28rpx; border: 1rpx solid #E2E8F0;
}
.add-btn {
  height: 72rpx; padding: 0 28rpx; background: #10B981; border-radius: 12rpx;
  display: flex; align-items: center; color: #fff; font-size: 28rpx; font-weight: 600; white-space: nowrap;
}
.category-scroll { white-space: nowrap; margin-bottom: 24rpx; }
.category-item {
  display: inline-block; padding: 12rpx 28rpx; margin-right: 16rpx; border-radius: 32rpx;
  background: #fff; font-size: 26rpx; color: #64748B; border: 1rpx solid #E2E8F0;
  &.active { background: #10B981; color: #fff; border-color: #10B981; }
}
.list { display: flex; flex-direction: column; gap: 20rpx; }
.item-card { background: #fff; border-radius: 16rpx; overflow: hidden; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04); }
.item-img { width: 100%; height: 220rpx; }
.item-body { padding: 20rpx 24rpx; }
.item-top { display: flex; justify-content: space-between; align-items: center; }
.item-title { font-size: 30rpx; font-weight: 600; color: #0F172A; flex: 1; }
.item-price { font-size: 32rpx; font-weight: 700; color: #EF4444; }
.item-meta { display: flex; gap: 20rpx; margin-top: 12rpx; font-size: 24rpx; color: #94A3B8; align-items: center; }
.tag {
  font-size: 22rpx; padding: 4rpx 12rpx; border-radius: 6rpx;
  &.tag-ACTIVE { background: #D1FAE5; color: #059669; }
  &.tag-INACTIVE { background: #FEE2E2; color: #DC2626; }
  &.tag-SOLD_OUT { background: #F1F5F9; color: #64748B; }
}
.item-actions {
  display: flex; gap: 16rpx; margin-top: 16rpx; padding-top: 16rpx; border-top: 1rpx solid #F1F5F9;
}
.action-btn {
  font-size: 26rpx; color: #10B981; padding: 8rpx 20rpx; border-radius: 8rpx; background: #F0FDF4;
  &.edit { color: #3B82F6; background: #EFF6FF; }
  &.danger { color: #EF4444; background: #FEF2F2; }
}
.empty { text-align: center; padding: 120rpx 0; color: #94A3B8; font-size: 28rpx; }
.empty-btn {
  margin-top: 24rpx; display: inline-block; padding: 16rpx 48rpx;
  background: #10B981; color: #fff; border-radius: 12rpx; font-size: 28rpx;
}
.loading-hint { text-align: center; padding: 32rpx; color: #94A3B8; font-size: 26rpx; }
</style>
