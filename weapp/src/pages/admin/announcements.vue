<template>
  <view class="page">
    <view class="top-bar">
      <text class="page-title">公告管理</text>
      <view class="add-btn" @click="openCreate">+ 新建</view>
    </view>

    <view v-if="list.length" class="list">
      <view class="card" v-for="item in list" :key="item.id">
        <view class="card-header">
          <view class="type-tag" :class="'t-' + item.type">{{ typeText(item.type) }}</view>
          <switch :checked="item.enabled !== false" @change="toggleEnabled(item, $event)" color="#10B981" style="transform: scale(0.8);" />
        </view>
        <text class="card-title">{{ item.title }}</text>
        <text class="card-content">{{ item.content }}</text>
        <view class="card-meta">
          <text>优先级: {{ item.priority || 0 }}</text>
          <text>{{ formatTime(item.createdAt) }}</text>
        </view>
        <view class="card-actions">
          <text class="act edit" @click="openEdit(item)">编辑</text>
          <text class="act danger" @click="handleDelete(item)">删除</text>
        </view>
      </view>
    </view>
    <view v-else-if="!loading" class="empty"><text>暂无公告</text></view>
    <view v-if="loading" class="loading-hint"><text>加载中...</text></view>

    <!-- 编辑弹窗 -->
    <view class="modal-mask" v-if="showForm" @click="showForm = false">
      <view class="modal" @click.stop>
        <text class="modal-title">{{ editItem ? '编辑公告' : '新建公告' }}</text>
        <view class="field">
          <text class="label">标题</text>
          <input class="input" v-model="formData.title" placeholder="公告标题" />
        </view>
        <view class="field">
          <text class="label">内容</text>
          <textarea class="textarea" v-model="formData.content" placeholder="公告内容" />
        </view>
        <view class="field-row">
          <view class="field half">
            <text class="label">类型</text>
            <picker :range="typeLabels" :value="typeIdx" @change="onTypeChange">
              <view class="picker-val">{{ typeText(formData.type) }}</view>
            </picker>
          </view>
          <view class="field half">
            <text class="label">优先级</text>
            <input class="input" type="number" v-model="formData.priority" placeholder="0-10" />
          </view>
        </view>
        <view class="modal-btns">
          <button class="btn-cancel" @click="showForm = false">取消</button>
          <button class="btn-save" :loading="saving" @click="handleSave">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { adminAnnouncementApi } from '../../services/api'
import { formatTime } from '../../utils'

const list = ref<any[]>([])
const loading = ref(false)
const showForm = ref(false)
const saving = ref(false)
const editItem = ref<any>(null)
const formData = ref<any>({ title: '', content: '', type: 'info', priority: 0, enabled: true })

const typeKeys = ['info', 'warning', 'error']
const typeLabels = ['通知', '警告', '紧急']
const typeIdx = computed(() => Math.max(0, typeKeys.indexOf(formData.value.type)))
const onTypeChange = (e: any) => { formData.value.type = typeKeys[e.detail.value] }

const typeText = (t: string) => {
  const map: Record<string, string> = { info: '通知', warning: '警告', error: '紧急' }
  return map[t] || t
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await adminAnnouncementApi.getList()
    if (res.code === 200) list.value = res.data || []
  } catch { uni.showToast({ title: '加载失败', icon: 'none' }) }
  finally { loading.value = false }
}

const openCreate = () => {
  editItem.value = null
  formData.value = { title: '', content: '', type: 'info', priority: 0, enabled: true }
  showForm.value = true
}

const openEdit = (item: any) => {
  editItem.value = item
  formData.value = { title: item.title, content: item.content, type: item.type || 'info', priority: item.priority || 0, enabled: item.enabled !== false }
  showForm.value = true
}

const handleSave = async () => {
  if (!formData.value.title.trim()) return uni.showToast({ title: '请输入标题', icon: 'none' })
  if (!formData.value.content.trim()) return uni.showToast({ title: '请输入内容', icon: 'none' })
  saving.value = true
  try {
    const data = { ...formData.value, priority: Number(formData.value.priority) || 0 }
    if (editItem.value) {
      await adminAnnouncementApi.update(editItem.value.id, data)
    } else {
      await adminAnnouncementApi.create(data)
    }
    showForm.value = false
    uni.showToast({ title: '保存成功', icon: 'success' })
    loadList()
  } catch { uni.showToast({ title: '操作失败', icon: 'none' }) }
  finally { saving.value = false }
}

const toggleEnabled = async (item: any, e: any) => {
  try {
    await adminAnnouncementApi.update(item.id, { ...item, enabled: e.detail.value })
    item.enabled = e.detail.value
  } catch { uni.showToast({ title: '操作失败', icon: 'none' }) }
}

const handleDelete = (item: any) => {
  uni.showModal({
    title: '确认删除', content: `确定删除公告「${item.title}」？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await adminAnnouncementApi.remove(item.id)
          list.value = list.value.filter(i => i.id !== item.id)
          uni.showToast({ title: '已删除', icon: 'success' })
        } catch { uni.showToast({ title: '删除失败', icon: 'none' }) }
      }
    }
  })
}

onMounted(loadList)
</script>

<style lang="scss" scoped>
.page { padding: 24rpx; background: #F5F7FA; min-height: 100vh; }
.top-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx; }
.page-title { font-size: 34rpx; font-weight: 700; color: #0F172A; }
.add-btn {
  padding: 12rpx 28rpx; background: #10B981; border-radius: 12rpx;
  color: #fff; font-size: 28rpx; font-weight: 600;
}
.list { display: flex; flex-direction: column; gap: 20rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04); }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.type-tag {
  font-size: 22rpx; padding: 4rpx 16rpx; border-radius: 6rpx;
  &.t-info { background: #DBEAFE; color: #2563EB; }
  &.t-warning { background: #FEF3C7; color: #D97706; }
  &.t-error { background: #FEE2E2; color: #DC2626; }
}
.card-title { font-size: 30rpx; font-weight: 600; color: #0F172A; display: block; }
.card-content { font-size: 26rpx; color: #64748B; margin-top: 8rpx; display: block;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-meta { display: flex; gap: 24rpx; margin-top: 12rpx; font-size: 24rpx; color: #94A3B8; }
.card-actions { display: flex; gap: 16rpx; margin-top: 16rpx; padding-top: 16rpx; border-top: 1rpx solid #F1F5F9; }
.act {
  font-size: 26rpx; padding: 8rpx 20rpx; border-radius: 8rpx;
  &.edit { color: #3B82F6; background: #EFF6FF; }
  &.danger { color: #EF4444; background: #FEF2F2; }
}

.modal-mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center; z-index: 999; padding: 32rpx;
}
.modal {
  background: #fff; border-radius: 20rpx; padding: 40rpx 32rpx; width: 100%; max-height: 80vh; overflow-y: auto;
}
.modal-title { font-size: 34rpx; font-weight: 700; color: #0F172A; margin-bottom: 32rpx; display: block; }
.field { margin-bottom: 24rpx; }
.field-row { display: flex; gap: 20rpx; margin-bottom: 24rpx; }
.half { flex: 1; }
.label { font-size: 26rpx; color: #334155; font-weight: 500; display: block; margin-bottom: 10rpx; }
.input {
  width: 100%; height: 76rpx; border: 1rpx solid #E2E8F0; border-radius: 12rpx;
  padding: 0 20rpx; font-size: 28rpx; box-sizing: border-box;
}
.textarea {
  width: 100%; min-height: 160rpx; border: 1rpx solid #E2E8F0; border-radius: 12rpx;
  padding: 16rpx 20rpx; font-size: 28rpx; box-sizing: border-box;
}
.picker-val {
  height: 76rpx; line-height: 76rpx; border: 1rpx solid #E2E8F0; border-radius: 12rpx;
  padding: 0 20rpx; font-size: 28rpx; color: #334155;
}
.modal-btns { display: flex; gap: 20rpx; margin-top: 32rpx; }
.btn-cancel { flex: 1; height: 84rpx; border-radius: 12rpx; background: #F1F5F9; color: #64748B; font-size: 30rpx; border: none; }
.btn-save { flex: 2; height: 84rpx; border-radius: 12rpx; background: #10B981; color: #fff; font-size: 30rpx; font-weight: 600; border: none; }
.empty { text-align: center; padding: 120rpx 0; color: #94A3B8; font-size: 28rpx; }
.loading-hint { text-align: center; padding: 32rpx; color: #94A3B8; font-size: 26rpx; }
</style>
