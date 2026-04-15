<template>
  <view class="page">
    <view class="form-card">
      <view class="form-title">{{ isEdit ? '编辑产品' : '发布产品' }}</view>

      <view class="field">
        <text class="label required">产品名称</text>
        <input class="input" v-model="form.name" placeholder="输入产品名称" maxlength="60" />
      </view>

      <view class="field">
        <text class="label">产品图片</text>
        <view class="cover-upload" @click="chooseCover">
          <image v-if="form.imageUrl" :src="form.imageUrl" mode="aspectFill" class="cover-img" />
          <view v-else class="cover-placeholder">
            <text class="cover-plus">+</text>
            <text class="cover-hint">选择图片</text>
          </view>
        </view>
      </view>

      <view class="field-row">
        <view class="field half">
          <text class="label required">分类</text>
          <picker :range="categoryLabels" :value="catIndex" @change="onCatChange">
            <view class="picker-val">{{ form.category || '请选择' }}</view>
          </picker>
        </view>
        <view class="field half">
          <text class="label">品牌</text>
          <input class="input" v-model="form.brand" placeholder="品牌名称" />
        </view>
      </view>

      <view class="field-row">
        <view class="field half">
          <text class="label required">原价 (元)</text>
          <input class="input" type="digit" v-model="form.price" placeholder="0.00" />
        </view>
        <view class="field half">
          <text class="label">促销价 (元)</text>
          <input class="input" type="digit" v-model="form.salePrice" placeholder="0.00" />
        </view>
      </view>

      <view class="field">
        <text class="label required">库存数量</text>
        <input class="input" type="number" v-model="form.stock" placeholder="输入库存" />
      </view>

      <view class="field">
        <text class="label">简要描述</text>
        <input class="input" v-model="form.briefDesc" placeholder="一句话描述" maxlength="100" />
      </view>

      <view class="field">
        <text class="label">详细描述</text>
        <textarea class="textarea" v-model="form.description" placeholder="详细描述产品信息" maxlength="2000" />
      </view>

      <view class="field">
        <text class="label">标签</text>
        <input class="input" v-model="form.tags" placeholder="多个标签用逗号分隔" />
      </view>

      <view class="field-row toggle-row">
        <view class="toggle-item">
          <text class="label">上架销售</text>
          <switch :checked="form.status === 'ACTIVE'" @change="form.status = $event.detail.value ? 'ACTIVE' : 'INACTIVE'" color="#10B981" />
        </view>
        <view class="toggle-item">
          <text class="label">推荐</text>
          <switch :checked="form.recommended" @change="form.recommended = $event.detail.value" color="#F59E0B" />
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
import { adminProductApi, PRODUCT_CATEGORIES } from '../../services/api'

const form = ref<any>({
  name: '', imageUrl: '', category: '', brand: '',
  price: '', salePrice: '', stock: '', briefDesc: '', description: '',
  tags: '', status: 'ACTIVE', recommended: false, sortOrder: 0
})
const saving = ref(false)
const isEdit = ref(false)
const editId = ref(0)

const categoryLabels = PRODUCT_CATEGORIES.filter(c => c.key).map(c => c.label)
const categoryKeys = PRODUCT_CATEGORIES.filter(c => c.key).map(c => c.key)
const catIndex = computed(() => Math.max(0, categoryKeys.indexOf(form.value.category)))
const onCatChange = (e: any) => { form.value.category = categoryKeys[e.detail.value] }

const chooseCover = () => {
  uni.chooseImage({
    count: 1, sizeType: ['compressed'],
    success: (res) => { form.value.imageUrl = res.tempFilePaths[0] }
  })
}

const goBack = () => uni.navigateBack()

const handleSave = async () => {
  if (!form.value.name.trim()) return uni.showToast({ title: '请输入名称', icon: 'none' })
  if (!form.value.category) return uni.showToast({ title: '请选择分类', icon: 'none' })
  if (!form.value.price) return uni.showToast({ title: '请输入价格', icon: 'none' })
  if (!form.value.stock) return uni.showToast({ title: '请输入库存', icon: 'none' })

  saving.value = true
  try {
    const data = {
      ...form.value,
      price: Number(form.value.price),
      salePrice: form.value.salePrice ? Number(form.value.salePrice) : null,
      stock: Number(form.value.stock)
    }
    if (isEdit.value) {
      await adminProductApi.update(editId.value, data)
      uni.showToast({ title: '保存成功', icon: 'success' })
    } else {
      await adminProductApi.create(data)
      uni.showToast({ title: '发布成功', icon: 'success' })
    }
    setTimeout(() => uni.navigateBack(), 800)
  } catch { uni.showToast({ title: '操作失败', icon: 'none' }) }
  finally { saving.value = false }
}

onMounted(() => {
  const pages = getCurrentPages()
  const current = pages[pages.length - 1] as any
  const id = Number(current?.options?.id || 0)
  if (id > 0) {
    isEdit.value = true
    editId.value = id
    adminProductApi.getDetail(id).then(res => {
      if (res.code === 200 && res.data) {
        const d = res.data
        form.value = {
          name: d.name || '', imageUrl: d.imageUrl || '', category: d.category || '',
          brand: d.brand || '', price: d.price || '', salePrice: d.salePrice || '',
          stock: d.stock || '', briefDesc: d.briefDesc || '', description: d.description || '',
          tags: d.tags || '', status: d.status || 'ACTIVE', recommended: d.recommended === true,
          sortOrder: d.sortOrder || 0
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
.btn-cancel { flex: 1; height: 84rpx; border-radius: 12rpx; background: #F1F5F9; color: #64748B; font-size: 30rpx; border: none; }
.btn-save { flex: 2; height: 84rpx; border-radius: 12rpx; background: #10B981; color: #fff; font-size: 30rpx; font-weight: 600; border: none; }
</style>
