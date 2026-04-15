<template>
  <view class="page">
    <view class="top-bar">
      <text class="page-title">反馈管理</text>
      <view class="stats-row" v-if="stats.total">
        <text class="stat-tag pending">待处理 {{ stats.pending || 0 }}</text>
        <text class="stat-tag processing">处理中 {{ stats.processing || 0 }}</text>
      </view>
    </view>

    <!-- 状态筛选 -->
    <scroll-view scroll-x class="tab-scroll">
      <view class="tab-item" :class="{ active: statusFilter === s.key }"
        v-for="s in statusTabs" :key="s.key" @click="statusFilter = s.key; loadList(true)">
        <text>{{ s.label }}</text>
      </view>
    </scroll-view>

    <view v-if="list.length" class="feedback-list">
      <view class="fb-card" v-for="item in list" :key="item.id">
        <view class="fb-header">
          <text class="fb-type" :class="'ft-' + item.type">{{ typeText(item.type) }}</text>
          <text class="fb-status" :class="'fs-' + item.status">{{ statusText(item.status) }}</text>
        </view>
        <text class="fb-title">{{ item.title }}</text>
        <text class="fb-desc">{{ item.description }}</text>
        <view class="fb-user" v-if="item.user">
          <text>提交者: {{ item.user.nickname || item.user.username }}</text>
          <text>{{ formatTime(item.createdAt) }}</text>
        </view>

        <!-- 回复区 -->
        <view class="fb-reply" v-if="item.adminReply">
          <text class="reply-label">管理员回复:</text>
          <text class="reply-content">{{ item.adminReply }}</text>
        </view>

        <view class="fb-actions">
          <text class="act" @click="openReply(item)" v-if="item.status !== 'CLOSED'">回复</text>
          <text class="act" @click="changeStatus(item, 'PROCESSING')" v-if="item.status === 'PENDING'">处理中</text>
          <text class="act" @click="changeStatus(item, 'RESOLVED')" v-if="item.status === 'PROCESSING'">已解决</text>
          <text class="act" @click="changeStatus(item, 'CLOSED')">关闭</text>
        </view>
      </view>
    </view>
    <view v-else-if="!loading" class="empty"><text>暂无反馈</text></view>
    <view v-if="loading" class="loading-hint"><text>加载中...</text></view>

    <!-- 回复弹窗 -->
    <view class="modal-mask" v-if="showReply" @click="showReply = false">
      <view class="modal" @click.stop>
        <text class="modal-title">回复反馈</text>
        <textarea class="textarea" v-model="replyContent" placeholder="输入回复内容..." />
        <view class="modal-btns">
          <button class="btn-cancel" @click="showReply = false">取消</button>
          <button class="btn-save" :loading="saving" @click="handleReply">发送</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminFeedbackApi } from '../../services/api'
import { formatTime } from '../../utils'

const list = ref<any[]>([])
const stats = ref<any>({})
const statusFilter = ref('')
const page = ref(1)
const loading = ref(false)
const noMore = ref(false)
const showReply = ref(false)
const saving = ref(false)
const replyContent = ref('')
const replyTarget = ref<any>(null)

const statusTabs = [
  { key: '', label: '全部' },
  { key: 'PENDING', label: '待处理' },
  { key: 'PROCESSING', label: '处理中' },
  { key: 'RESOLVED', label: '已解决' },
  { key: 'CLOSED', label: '已关闭' }
]

const typeText = (t: string) => {
  const map: Record<string, string> = { BUG: '问题', FEATURE: '功能', SUGGESTION: '建议', OTHER: '其他' }
  return map[t] || t || '反馈'
}
const statusText = (s: string) => {
  const map: Record<string, string> = { PENDING: '待处理', PROCESSING: '处理中', RESOLVED: '已解决', CLOSED: '已关闭' }
  return map[s] || s
}

const loadList = async (reset = false) => {
  if (reset) { page.value = 1; noMore.value = false }
  loading.value = true
  try {
    const res = await adminFeedbackApi.getList({ page: page.value, size: 10, status: statusFilter.value })
    if (res.code === 200) {
      const items = res.data?.content || res.data?.records || res.data || []
      list.value = reset ? items : [...list.value, ...items]
      noMore.value = items.length < 10
    }
  } catch { uni.showToast({ title: '加载失败', icon: 'none' }) }
  finally { loading.value = false }
}

const loadStats = async () => {
  try {
    const res = await adminFeedbackApi.getStats()
    if (res.code === 200) stats.value = res.data || {}
  } catch {}
}

const openReply = (item: any) => {
  replyTarget.value = item
  replyContent.value = ''
  showReply.value = true
}

const handleReply = async () => {
  if (!replyContent.value.trim()) return uni.showToast({ title: '请输入内容', icon: 'none' })
  saving.value = true
  try {
    await adminFeedbackApi.reply(replyTarget.value.id, replyContent.value)
    replyTarget.value.adminReply = replyContent.value
    if (replyTarget.value.status === 'PENDING') replyTarget.value.status = 'PROCESSING'
    showReply.value = false
    uni.showToast({ title: '回复成功', icon: 'success' })
  } catch { uni.showToast({ title: '回复失败', icon: 'none' }) }
  finally { saving.value = false }
}

const changeStatus = async (item: any, status: string) => {
  try {
    await adminFeedbackApi.updateStatus(item.id, status)
    item.status = status
    uni.showToast({ title: '已更新', icon: 'success' })
  } catch { uni.showToast({ title: '操作失败', icon: 'none' }) }
}

onMounted(() => { loadStats(); loadList(true) })
onReachBottom(() => { if (!noMore.value && !loading.value) { page.value++; loadList() } })
</script>

<style lang="scss" scoped>
.page { padding: 24rpx; background: #F5F7FA; min-height: 100vh; }
.top-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.page-title { font-size: 34rpx; font-weight: 700; color: #0F172A; }
.stats-row { display: flex; gap: 12rpx; }
.stat-tag {
  font-size: 22rpx; padding: 4rpx 14rpx; border-radius: 6rpx;
  &.pending { background: #FEE2E2; color: #DC2626; }
  &.processing { background: #FEF3C7; color: #D97706; }
}
.tab-scroll { white-space: nowrap; margin-bottom: 20rpx; }
.tab-item {
  display: inline-block; padding: 14rpx 32rpx; margin-right: 16rpx; border-radius: 32rpx;
  background: #fff; font-size: 26rpx; color: #64748B; border: 1rpx solid #E2E8F0;
  &.active { background: #10B981; color: #fff; border-color: #10B981; }
}
.feedback-list { display: flex; flex-direction: column; gap: 20rpx; }
.fb-card { background: #fff; border-radius: 16rpx; padding: 24rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04); }
.fb-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.fb-type {
  font-size: 22rpx; padding: 4rpx 14rpx; border-radius: 6rpx;
  &.ft-BUG { background: #FEE2E2; color: #DC2626; }
  &.ft-FEATURE { background: #DBEAFE; color: #2563EB; }
  &.ft-SUGGESTION { background: #D1FAE5; color: #059669; }
  &.ft-OTHER { background: #F1F5F9; color: #64748B; }
}
.fb-status {
  font-size: 22rpx; padding: 4rpx 14rpx; border-radius: 6rpx;
  &.fs-PENDING { background: #FEE2E2; color: #DC2626; }
  &.fs-PROCESSING { background: #FEF3C7; color: #D97706; }
  &.fs-RESOLVED { background: #D1FAE5; color: #059669; }
  &.fs-CLOSED { background: #F1F5F9; color: #64748B; }
}
.fb-title { font-size: 30rpx; font-weight: 600; color: #0F172A; display: block; }
.fb-desc { font-size: 26rpx; color: #64748B; margin-top: 8rpx; display: block;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.fb-user { display: flex; justify-content: space-between; margin-top: 12rpx; font-size: 24rpx; color: #94A3B8; }
.fb-reply {
  margin-top: 16rpx; padding: 16rpx; background: #F0FDF4; border-radius: 12rpx;
  .reply-label { font-size: 24rpx; color: #059669; font-weight: 500; display: block; }
  .reply-content { font-size: 26rpx; color: #334155; margin-top: 6rpx; display: block; }
}
.fb-actions { display: flex; gap: 16rpx; margin-top: 16rpx; padding-top: 16rpx; border-top: 1rpx solid #F1F5F9; }
.act { font-size: 26rpx; color: #10B981; padding: 8rpx 20rpx; border-radius: 8rpx; background: #F0FDF4; }
.modal-mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center; z-index: 999; padding: 32rpx;
}
.modal { background: #fff; border-radius: 20rpx; padding: 40rpx 32rpx; width: 100%; }
.modal-title { font-size: 34rpx; font-weight: 700; color: #0F172A; margin-bottom: 24rpx; display: block; }
.textarea {
  width: 100%; min-height: 200rpx; border: 1rpx solid #E2E8F0; border-radius: 12rpx;
  padding: 16rpx 20rpx; font-size: 28rpx; box-sizing: border-box;
}
.modal-btns { display: flex; gap: 20rpx; margin-top: 24rpx; }
.btn-cancel { flex: 1; height: 84rpx; border-radius: 12rpx; background: #F1F5F9; color: #64748B; font-size: 30rpx; border: none; }
.btn-save { flex: 2; height: 84rpx; border-radius: 12rpx; background: #10B981; color: #fff; font-size: 30rpx; font-weight: 600; border: none; }
.empty { text-align: center; padding: 120rpx 0; color: #94A3B8; font-size: 28rpx; }
.loading-hint { text-align: center; padding: 32rpx; color: #94A3B8; font-size: 26rpx; }
</style>
