<template>
  <view class="page">
    <!-- 统计 -->
    <view class="stats-row" v-if="communityStats.totalPosts">
      <view class="stat-item">
        <text class="stat-val">{{ communityStats.totalPosts }}</text>
        <text class="stat-lab">帖子</text>
      </view>
      <view class="stat-item">
        <text class="stat-val">{{ communityStats.totalComments }}</text>
        <text class="stat-lab">评论</text>
      </view>
      <view class="stat-item">
        <text class="stat-val">{{ communityStats.activeUsers }}</text>
        <text class="stat-lab">活跃用户</text>
      </view>
    </view>

    <!-- 状态筛选 -->
    <scroll-view scroll-x class="tab-scroll">
      <view class="tab-item" :class="{ active: statusFilter === s.key }"
        v-for="s in statusTabs" :key="s.key" @click="statusFilter = s.key; loadPosts(true)">
        <text>{{ s.label }}</text>
      </view>
    </scroll-view>

    <view v-if="posts.length" class="post-list">
      <view class="post-card" v-for="post in posts" :key="post.id">
        <view class="post-header">
          <view class="post-author">
            <image v-if="post.user?.avatar" class="avatar" :src="post.user.avatar" />
            <view v-else class="avatar-placeholder">{{ (post.user?.nickname || '?')[0] }}</view>
            <text class="author-name">{{ post.user?.nickname || post.user?.username || '匿名' }}</text>
          </view>
          <text class="post-status" :class="'ps-' + post.status">{{ post.status === 'ACTIVE' ? '正常' : post.status === 'HIDDEN' ? '隐藏' : '已删' }}</text>
        </view>

        <text class="post-content">{{ post.content }}</text>

        <view class="post-images" v-if="post.images?.length">
          <image v-for="(img, idx) in post.images.slice(0, 3)" :key="idx" :src="img" mode="aspectFill" class="post-img" />
        </view>

        <view class="post-meta">
          <text>{{ post.category || '无分类' }}</text>
          <text>❤ {{ post.likeCount || 0 }}</text>
          <text>💬 {{ post.commentCount || 0 }}</text>
          <text>{{ formatTime(post.createdAt) }}</text>
        </view>

        <view class="post-actions">
          <text class="act" @click="handlePin(post)">{{ post.pinned ? '取消置顶' : '置顶' }}</text>
          <text class="act" @click="handleHide(post)" v-if="post.status === 'ACTIVE'">隐藏</text>
          <text class="act" @click="handleRestore(post)" v-if="post.status === 'HIDDEN'">恢复</text>
          <text class="act danger" @click="handleDeletePost(post)">删除</text>
        </view>
      </view>
    </view>
    <view v-else-if="!loading" class="empty"><text>暂无帖子</text></view>

    <view v-if="loading" class="loading-hint"><text>加载中...</text></view>
    <view v-else-if="noMore && posts.length" class="loading-hint"><text>没有更多了</text></view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminCommunityApi } from '../../services/api'
import { formatTime } from '../../utils'

const posts = ref<any[]>([])
const communityStats = ref<any>({})
const statusFilter = ref('')
const page = ref(1)
const loading = ref(false)
const noMore = ref(false)

const statusTabs = [
  { key: '', label: '全部' },
  { key: 'ACTIVE', label: '正常' },
  { key: 'HIDDEN', label: '已隐藏' }
]

const loadPosts = async (reset = false) => {
  if (reset) { page.value = 1; noMore.value = false }
  loading.value = true
  try {
    const res = await adminCommunityApi.getPosts({ page: page.value, size: 10, status: statusFilter.value })
    if (res.code === 200) {
      const items = res.data?.content || res.data?.records || res.data || []
      posts.value = reset ? items : [...posts.value, ...items]
      noMore.value = items.length < 10
    }
  } catch { uni.showToast({ title: '加载失败', icon: 'none' }) }
  finally { loading.value = false }
}

const loadStats = async () => {
  try {
    const res = await adminCommunityApi.getStats()
    if (res.code === 200) communityStats.value = res.data || {}
  } catch {}
}

const handlePin = async (post: any) => {
  try {
    await adminCommunityApi.togglePin(post.id)
    post.pinned = !post.pinned
    uni.showToast({ title: post.pinned ? '已置顶' : '取消置顶', icon: 'success' })
  } catch { uni.showToast({ title: '操作失败', icon: 'none' }) }
}

const handleHide = async (post: any) => {
  try {
    await adminCommunityApi.updateStatus(post.id, 'HIDDEN')
    post.status = 'HIDDEN'
    uni.showToast({ title: '已隐藏', icon: 'success' })
  } catch { uni.showToast({ title: '操作失败', icon: 'none' }) }
}

const handleRestore = async (post: any) => {
  try {
    await adminCommunityApi.updateStatus(post.id, 'ACTIVE')
    post.status = 'ACTIVE'
    uni.showToast({ title: '已恢复', icon: 'success' })
  } catch { uni.showToast({ title: '操作失败', icon: 'none' }) }
}

const handleDeletePost = (post: any) => {
  uni.showModal({
    title: '确认删除', content: '此操作不可恢复，确定删除？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await adminCommunityApi.deletePost(post.id)
          posts.value = posts.value.filter(p => p.id !== post.id)
          uni.showToast({ title: '已删除', icon: 'success' })
        } catch { uni.showToast({ title: '删除失败', icon: 'none' }) }
      }
    }
  })
}

onMounted(() => { loadStats(); loadPosts(true) })
onReachBottom(() => { if (!noMore.value && !loading.value) { page.value++; loadPosts() } })
</script>

<style lang="scss" scoped>
.page { padding: 24rpx; background: #F5F7FA; min-height: 100vh; }
.stats-row {
  display: flex; background: #fff; border-radius: 16rpx; margin-bottom: 20rpx; padding: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.stat-item { flex: 1; text-align: center; }
.stat-val { font-size: 36rpx; font-weight: 700; color: #0F172A; display: block; }
.stat-lab { font-size: 24rpx; color: #94A3B8; display: block; margin-top: 4rpx; }
.tab-scroll { white-space: nowrap; margin-bottom: 20rpx; }
.tab-item {
  display: inline-block; padding: 14rpx 32rpx; margin-right: 16rpx; border-radius: 32rpx;
  background: #fff; font-size: 26rpx; color: #64748B; border: 1rpx solid #E2E8F0;
  &.active { background: #10B981; color: #fff; border-color: #10B981; }
}
.post-list { display: flex; flex-direction: column; gap: 20rpx; }
.post-card { background: #fff; border-radius: 16rpx; padding: 24rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04); }
.post-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.post-author { display: flex; align-items: center; gap: 12rpx; }
.avatar { width: 56rpx; height: 56rpx; border-radius: 50%; }
.avatar-placeholder {
  width: 56rpx; height: 56rpx; border-radius: 50%; background: #10B981; color: #fff;
  display: flex; align-items: center; justify-content: center; font-size: 24rpx;
}
.author-name { font-size: 28rpx; color: #334155; font-weight: 500; }
.post-status {
  font-size: 22rpx; padding: 4rpx 14rpx; border-radius: 6rpx;
  &.ps-ACTIVE { background: #D1FAE5; color: #059669; }
  &.ps-HIDDEN { background: #FEF3C7; color: #D97706; }
  &.ps-DELETED { background: #FEE2E2; color: #DC2626; }
}
.post-content { font-size: 28rpx; color: #334155; line-height: 1.6; display: -webkit-box;
  -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
.post-images { display: flex; gap: 12rpx; margin-top: 16rpx; }
.post-img { width: 200rpx; height: 200rpx; border-radius: 12rpx; }
.post-meta { display: flex; gap: 20rpx; margin-top: 16rpx; font-size: 24rpx; color: #94A3B8; }
.post-actions {
  display: flex; gap: 16rpx; margin-top: 16rpx; padding-top: 16rpx; border-top: 1rpx solid #F1F5F9;
}
.act { font-size: 26rpx; color: #10B981; padding: 8rpx 20rpx; border-radius: 8rpx; background: #F0FDF4;
  &.danger { color: #EF4444; background: #FEF2F2; }
}
.empty { text-align: center; padding: 120rpx 0; color: #94A3B8; font-size: 28rpx; }
.loading-hint { text-align: center; padding: 32rpx; color: #94A3B8; font-size: 26rpx; }
</style>
