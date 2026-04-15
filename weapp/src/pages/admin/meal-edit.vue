<template>
  <view class="page">
    <view class="form-card">
      <view class="form-title">{{ isEdit ? '编辑营养餐' : '发布营养餐' }}</view>

      <view class="field">
        <text class="label required">标题</text>
        <input class="input" v-model="form.title" placeholder="输入营养餐名称" maxlength="50" />
      </view>

      <view class="field">
        <text class="label">封面图片</text>
        <view class="cover-upload" @click="chooseCover">
          <image v-if="form.coverImage" :src="form.coverImage" mode="aspectFill" class="cover-img" />
          <view v-else class="cover-placeholder">
            <text class="cover-plus">+</text>
            <text class="cover-hint">选择封面</text>
          </view>
        </view>
      </view>

      <view class="field">
        <text class="label required">描述</text>
        <textarea class="textarea" v-model="form.description" placeholder="描述此营养餐的特点和目标人群" maxlength="500" />
      </view>

      <view class="field-row">
        <view class="field half">
          <text class="label">计划类型</text>
          <picker :range="typeOptions" :value="typeIndex" @change="onTypeChange">
            <view class="picker-val">{{ form.type || '请选择' }}</view>
          </picker>
        </view>
        <view class="field half">
          <text class="label">饮食目标</text>
          <picker :range="goalOptions" :value="goalIndex" @change="onGoalChange">
            <view class="picker-val">{{ form.dietGoal || '请选择' }}</view>
          </picker>
        </view>
      </view>

      <view class="field-row">
        <view class="field half">
          <text class="label">目标热量 (kcal)</text>
          <input class="input" type="number" v-model="form.targetCalories" placeholder="如 1800" />
        </view>
        <view class="field half">
          <text class="label">持续天数</text>
          <input class="input" type="number" v-model="form.duration" placeholder="如 7" />
        </view>
      </view>

      <view class="field-row">
        <view class="field half">
          <text class="label">适合人群</text>
          <input class="input" v-model="form.suitableCrowd" placeholder="如 上班族" />
        </view>
        <view class="field half">
          <text class="label">难度</text>
          <picker :range="difficultyLabels" :value="difficultyIndex" @change="onDifficultyChange">
            <view class="picker-val">{{ difficultyLabel }}</view>
          </picker>
        </view>
      </view>

      <view class="field">
        <text class="label">标签</text>
        <input class="input" v-model="form.tags" placeholder="多个标签用逗号分隔" />
      </view>

      <view class="field-row toggle-row">
        <view class="toggle-item">
          <text class="label">上架</text>
          <switch :checked="form.active" @change="form.active = $event.detail.value" color="#10B981" />
        </view>
        <view class="toggle-item">
          <text class="label">推荐</text>
          <switch :checked="form.featured" @change="form.featured = $event.detail.value" color="#F59E0B" />
        </view>
      </view>
    </view>

    <view class="bottom-bar">
      <button class="btn-cancel" @click="goBack">取消</button>
      <button class="btn-save" :loading="saving" @click="handleSave">{{ isEdit ? '保存修改' : '发布' }}</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { adminMealApi } from '../../services/api'

const form = ref<any>({
  title: '', description: '', coverImage: '', type: '抗炎营养餐', dietGoal: '健康饮食',
  targetCalories: '', duration: '', suitableCrowd: '', difficulty: 'EASY',
  tags: '', active: true, featured: false
})
const saving = ref(false)
const isEdit = ref(false)
const editId = ref(0)

const typeOptions = ['抗炎营养餐', '减脂营养餐', '增肌营养餐', '养生营养餐', '其他']
const typeIndex = computed(() => Math.max(0, typeOptions.indexOf(form.value.type)))
const onTypeChange = (e: any) => { form.value.type = typeOptions[e.detail.value] }

const goalOptions = ['健康饮食', '减脂塑形', '增肌增重', '抗炎调理', '肠胃养护']
const goalIndex = computed(() => Math.max(0, goalOptions.indexOf(form.value.dietGoal)))
const onGoalChange = (e: any) => { form.value.dietGoal = goalOptions[e.detail.value] }

const difficultyOptions = ['EASY', 'MEDIUM', 'HARD']
const difficultyLabels = ['简单', '中等', '困难']
const difficultyIndex = computed(() => Math.max(0, difficultyOptions.indexOf(form.value.difficulty)))
const difficultyLabel = computed(() => difficultyLabels[difficultyIndex.value])
const onDifficultyChange = (e: any) => { form.value.difficulty = difficultyOptions[e.detail.value] }

const chooseCover = () => {
  uni.chooseImage({
    count: 1, sizeType: ['compressed'],
    success: (res) => { form.value.coverImage = res.tempFilePaths[0] }
  })
}

const goBack = () => uni.navigateBack()

const handleSave = async () => {
  if (!form.value.title.trim()) {
    return uni.showToast({ title: '请输入标题', icon: 'none' })
  }
  if (!form.value.description.trim()) {
    return uni.showToast({ title: '请输入描述', icon: 'none' })
  }
  saving.value = true
  try {
    const data = {
      ...form.value,
      targetCalories: Number(form.value.targetCalories) || null,
      duration: Number(form.value.duration) || null
    }
    if (isEdit.value) {
      await adminMealApi.update(editId.value, data)
      uni.showToast({ title: '保存成功', icon: 'success' })
    } else {
      await adminMealApi.create(data)
      uni.showToast({ title: '发布成功', icon: 'success' })
    }
    setTimeout(() => uni.navigateBack(), 800)
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  const pages = getCurrentPages()
  const current = pages[pages.length - 1] as any
  const id = Number(current?.options?.id || 0)
  if (id > 0) {
    isEdit.value = true
    editId.value = id
    adminMealApi.getDetail(id).then(res => {
      if (res.code === 200 && res.data) {
        const d = res.data
        form.value = {
          title: d.title || '', description: d.description || '', coverImage: d.coverImage || '',
          type: d.type || '抗炎营养餐', dietGoal: d.dietGoal || '健康饮食',
          targetCalories: d.targetCalories || '', duration: d.duration || '',
          suitableCrowd: d.suitableCrowd || '', difficulty: d.difficulty || 'EASY',
          tags: d.tags || '', active: d.active !== false, featured: d.featured === true
        }
      }
    })
  }
})
</script>

<style lang="scss" scoped>
.page { padding: 24rpx; padding-bottom: 160rpx; background: #F5F7FA; min-height: 100vh; }
.form-card { background: #fff; border-radius: 16rpx; padding: 32rpx 24rpx; }
.form-title { font-size: 34rpx; font-weight: 700; color: #0F172A; margin-bottom: 32rpx; }
.field { margin-bottom: 28rpx; }
.field-row { display: flex; gap: 20rpx; margin-bottom: 28rpx; }
.half { flex: 1; }
.label { font-size: 26rpx; color: #334155; font-weight: 500; display: block; margin-bottom: 12rpx;
  &.required::before { content: '*'; color: #EF4444; margin-right: 4rpx; }
}
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
.cover-upload { width: 100%; height: 240rpx; border: 2rpx dashed #CBD5E1; border-radius: 12rpx; overflow: hidden; }
.cover-img { width: 100%; height: 100%; }
.cover-placeholder {
  width: 100%; height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center;
  .cover-plus { font-size: 60rpx; color: #CBD5E1; }
  .cover-hint { font-size: 24rpx; color: #94A3B8; margin-top: 8rpx; }
}
.toggle-row { align-items: center; }
.toggle-item { display: flex; align-items: center; gap: 16rpx; flex: 1; }
.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0; background: #fff;
  padding: 20rpx 32rpx; display: flex; gap: 20rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.06);
}
.btn-cancel {
  flex: 1; height: 84rpx; border-radius: 12rpx; background: #F1F5F9;
  color: #64748B; font-size: 30rpx; border: none;
}
.btn-save {
  flex: 2; height: 84rpx; border-radius: 12rpx; background: #10B981;
  color: #fff; font-size: 30rpx; font-weight: 600; border: none;
}
</style>
