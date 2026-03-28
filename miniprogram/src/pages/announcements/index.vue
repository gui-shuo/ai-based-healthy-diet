<template>
  <view class="page">
    <!-- Pull to refresh handled by onPullDownRefresh -->
    <view v-if="loading && !announcements.length" class="loading-state">
      <text class="text-secondary">加载中...</text>
    </view>

    <view v-else-if="announcements.length" class="announcement-list">
      <view
        v-for="item in announcements"
        :key="item.id"
        class="announcement-card"
        :class="{ important: item.priority === 'HIGH' || item.priority === 'IMPORTANT' }"
        @tap="toggleExpand(item.id)"
      >
        <view class="announcement-header">
          <view class="title-row">
            <view
              v-if="item.priority === 'HIGH' || item.priority === 'IMPORTANT'"
              class="priority-badge"
            >
              重要
            </view>
            <text class="announcement-title">{{ item.title }}</text>
          </view>
          <text class="announcement-date text-sm text-secondary">
            {{ formatTime(item.publishedAt || item.createdAt) }}
          </text>
        </view>

        <!-- Preview (collapsed) -->
        <view v-if="expandedId !== item.id" class="announcement-preview">
          {{ truncateText(item.content, 80) }}
        </view>

        <!-- Full content (expanded) -->
        <view v-else class="announcement-content">
          {{ item.content }}
        </view>

        <view class="expand-hint text-sm text-secondary">
          {{ expandedId === item.id ? '收起' : '展开查看' }}
        </view>
      </view>

      <!-- Load More -->
      <view v-if="hasMore" class="load-more" @tap="loadMore">
        <text class="text-secondary text-sm">{{ loadingMore ? '加载中...' : '点击加载更多' }}</text>
      </view>

      <view v-else class="load-more">
        <text class="text-secondary text-sm">没有更多了</text>
      </view>
    </view>

    <view v-else class="empty-state">
      <text class="empty-icon">📢</text>
      <text class="empty-text">暂无公告</text>
    </view>

    <view class="safe-bottom"></view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { announcementApi } from '@/services/api'
import { formatTime } from '@/utils/common'

const announcements = ref<any[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const page = ref(0)
const pageSize = 10
const hasMore = ref(true)
const expandedId = ref<number | null>(null)

function truncateText(text: string, max: number) {
  if (!text || text.length <= max) return text
  return text.substring(0, max) + '...'
}

function toggleExpand(id: number) {
  expandedId.value = expandedId.value === id ? null : id
}

async function loadAnnouncements(isRefresh = false) {
  if (isRefresh) {
    page.value = 0
    hasMore.value = true
  }
  if (!hasMore.value && !isRefresh) return

  loading.value = page.value === 0
  loadingMore.value = page.value > 0

  try {
    const res = await announcementApi.getList({ page: page.value, size: pageSize })
    if (res.code === 200) {
      const list = res.data?.content || res.data?.records || res.data?.list || res.data || []
      if (isRefresh) {
        announcements.value = list
      } else {
        announcements.value = [...announcements.value, ...list]
      }
      hasMore.value = list.length >= pageSize
    }
  } catch {} finally {
    loading.value = false
    loadingMore.value = false
  }
}

function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  page.value++
  loadAnnouncements()
}

onPullDownRefresh(async () => {
  await loadAnnouncements(true)
  uni.stopPullDownRefresh()
})

onReachBottom(() => {
  loadMore()
})

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20rpx 0 30rpx;
}

.loading-state {
  text-align: center;
  padding: 100rpx 0;
}

.announcement-list {
  padding: 0 24rpx;
}

.announcement-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  transition: all 0.2s;

  &.important {
    border-left: 6rpx solid #ee0a24;
  }
}

.announcement-header {
  margin-bottom: 16rpx;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.priority-badge {
  background: #ee0a24;
  color: #fff;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  white-space: nowrap;
  font-weight: 600;
}

.announcement-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.announcement-date {
  display: block;
}

.announcement-preview {
  font-size: 26rpx;
  color: #666;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.announcement-content {
  font-size: 26rpx;
  color: #333;
  line-height: 1.8;
  word-break: break-all;
}

.expand-hint {
  margin-top: 12rpx;
  text-align: right;
}

.load-more {
  text-align: center;
  padding: 30rpx 0;
}

.safe-bottom {
  height: env(safe-area-inset-bottom);
}
</style>
