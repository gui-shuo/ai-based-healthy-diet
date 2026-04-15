<template>
  <view class="page">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <input class="search-input" v-model="keyword" placeholder="搜索营养餐..." @confirm="loadList(true)" />
      <view class="add-btn" @click="goEdit(0)">
        <text>+ 发布</text>
      </view>
    </view>

    <!-- 列表 -->
    <view v-if="list.length" class="list">
      <view class="item-card" v-for="item in list" :key="item.id">
        <image v-if="item.coverImage" class="item-img" :src="item.coverImage" mode="aspectFill" />
        <view class="item-body">
          <view class="item-top">
            <text class="item-title">{{ item.title }}</text>
            <view class="item-tags">
              <text class="tag tag-active" v-if="item.active">上架</text>
              <text class="tag tag-inactive" v-else>下架</text>
              <text class="tag tag-featured" v-if="item.featured">推荐</text>
            </view>
          </view>
          <text class="item-desc">{{ item.description || '暂无描述' }}</text>
          <view class="item-meta">
            <text>{{ item.targetCalories || '-' }} kcal</text>
            <text>{{ item.duration || '-' }} 天</text>
            <text>{{ item.followerCount || 0 }} 人关注</text>
          </view>
          <view class="item-actions">
            <text class="action-btn edit" @click="goEdit(item.id)">编辑</text>
            <text class="action-btn" @click="toggleActive(item)">{{ item.active ? '下架' : '上架' }}</text>
            <text class="action-btn" @click="toggleFeatured(item)">{{ item.featured ? '取消推荐' : '推荐' }}</text>
            <text class="action-btn danger" @click="handleDelete(item)">删除</text>
          </view>
        </view>
      </view>
    </view>
    <view v-else-if="!loading" class="empty">
      <text>暂无营养餐数据</text>
      <view class="empty-btn" @click="goEdit(0)">去发布</view>
    </view>

    <!-- 加载更多 -->
    <view v-if="loading" class="loading-hint"><text>加载中...</text></view>
    <view v-else-if="noMore && list.length" class="loading-hint"><text>没有更多了</text></view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminMealApi } from '../../services/api'

const list = ref<any[]>([])
const keyword = ref('')
const page = ref(1)
const loading = ref(false)
const noMore = ref(false)

const loadList = async (reset = false) => {
  if (reset) { page.value = 1; noMore.value = false }
  loading.value = true
  try {
    const res = await adminMealApi.getList({ page: page.value, size: 10, keyword: keyword.value })
    if (res.code === 200) {
      const items = res.data?.content || res.data?.records || res.data || []
      if (reset) list.value = items
      else list.value = [...list.value, ...items]
      noMore.value = items.length < 10
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const goEdit = (id: number) => {
  uni.navigateTo({ url: `/pages/admin/meal-edit?id=${id}` })
}

const toggleActive = async (item: any) => {
  try {
    await adminMealApi.toggleActive(item.id)
    item.active = !item.active
    uni.showToast({ title: item.active ? '已上架' : '已下架', icon: 'success' })
  } catch { uni.showToast({ title: '操作失败', icon: 'none' }) }
}

const toggleFeatured = async (item: any) => {
  try {
    await adminMealApi.toggleFeatured(item.id)
    item.featured = !item.featured
    uni.showToast({ title: item.featured ? '已推荐' : '已取消', icon: 'success' })
  } catch { uni.showToast({ title: '操作失败', icon: 'none' }) }
}

const handleDelete = (item: any) => {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除「${item.title}」吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await adminMealApi.remove(item.id)
          list.value = list.value.filter(i => i.id !== item.id)
          uni.showToast({ title: '已删除', icon: 'success' })
        } catch { uni.showToast({ title: '删除失败', icon: 'none' }) }
      }
    }
  })
}

onMounted(() => loadList(true))

onReachBottom(() => {
  if (!noMore.value && !loading.value) {
    page.value++
    loadList()
  }
})
</script>

<style lang="scss" scoped>
.page { padding: 24rpx; background: #F5F7FA; min-height: 100vh; }
.search-bar { display: flex; gap: 16rpx; margin-bottom: 24rpx; }
.search-input {
  flex: 1; height: 72rpx; background: #fff; border-radius: 12rpx; padding: 0 24rpx;
  font-size: 28rpx; border: 1rpx solid #E2E8F0;
}
.add-btn {
  height: 72rpx; padding: 0 28rpx; background: #10B981; border-radius: 12rpx;
  display: flex; align-items: center; color: #fff; font-size: 28rpx; font-weight: 600;
  white-space: nowrap;
}
.list { display: flex; flex-direction: column; gap: 20rpx; }
.item-card {
  background: #fff; border-radius: 16rpx; overflow: hidden; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.item-img { width: 100%; height: 240rpx; }
.item-body { padding: 20rpx 24rpx; }
.item-top { display: flex; justify-content: space-between; align-items: flex-start; }
.item-title { font-size: 30rpx; font-weight: 600; color: #0F172A; flex: 1; }
.item-tags { display: flex; gap: 8rpx; flex-shrink: 0; margin-left: 12rpx; }
.tag {
  font-size: 22rpx; padding: 4rpx 12rpx; border-radius: 6rpx;
  &.tag-active { background: #D1FAE5; color: #059669; }
  &.tag-inactive { background: #FEE2E2; color: #DC2626; }
  &.tag-featured { background: #FEF3C7; color: #D97706; }
}
.item-desc { font-size: 26rpx; color: #64748B; margin-top: 8rpx; display: block;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-meta {
  display: flex; gap: 24rpx; margin-top: 12rpx; font-size: 24rpx; color: #94A3B8;
}
.item-actions {
  display: flex; gap: 16rpx; margin-top: 16rpx; padding-top: 16rpx; border-top: 1rpx solid #F1F5F9;
}
.action-btn {
  font-size: 26rpx; color: #10B981; padding: 8rpx 20rpx; border-radius: 8rpx;
  background: #F0FDF4;
  &.edit { color: #3B82F6; background: #EFF6FF; }
  &.danger { color: #EF4444; background: #FEF2F2; }
}
.empty { text-align: center; padding: 120rpx 0; color: #94A3B8; font-size: 28rpx; }
.empty-btn {
  margin-top: 24rpx; display: inline-block; padding: 16rpx 48rpx; background: #10B981;
  color: #fff; border-radius: 12rpx; font-size: 28rpx;
}
.loading-hint { text-align: center; padding: 32rpx; color: #94A3B8; font-size: 26rpx; }
</style>
