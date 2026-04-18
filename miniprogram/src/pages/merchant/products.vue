<template>
  <view class="page">
    <view class="nav-bar">
      <view class="nav-bar-inner">
        <view class="nav-back pressable" @tap="goBack">
          <NutriIcon name="arrow-left" :size="36" color="#1F2937" />
        </view>
        <text class="nav-title">产品管理</text>
        <view class="nav-action pressable" @tap="showAddSheet = true">
          <NutriIcon name="plus" :size="32" color="#10B981" />
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="scroll-body" :style="{ paddingTop: navHeight + 'px' }"
      @scrolltolower="loadMore" :lower-threshold="100">
      <!-- Search -->
      <view class="search-bar">
        <NutriIcon name="search" :size="28" color="#9CA3AF" />
        <input class="search-input" v-model="keyword" placeholder="搜索产品"
          @confirm="handleSearch" :adjust-position="true" />
      </view>

      <!-- Product List -->
      <view v-if="loading && products.length === 0" class="loading-wrap">
        <text class="loading-text">加载中...</text>
      </view>
      <view v-else-if="products.length === 0" class="empty-wrap">
        <NutriIcon name="shopping-bag" :size="64" color="#D1D5DB" />
        <text class="empty-text">暂无产品数据</text>
      </view>
      <view v-else>
        <view v-for="item in products" :key="item.id" class="card product-card">
          <view class="product-row">
            <image v-if="item.imageUrl" :src="item.imageUrl" class="product-img" mode="aspectFill" />
            <view v-else class="product-img-placeholder">
              <NutriIcon name="shopping-bag" :size="40" color="#D1D5DB" />
            </view>
            <view class="product-info">
              <text class="product-name">{{ item.name }}</text>
              <view class="product-tags">
                <text class="tag">{{ item.category }}</text>
                <text v-if="item.brand" class="tag brand">{{ item.brand }}</text>
              </view>
              <view class="product-price-row">
                <text class="product-price">¥{{ item.salePrice }}</text>
                <text v-if="item.originalPrice" class="product-origin">¥{{ item.originalPrice }}</text>
                <text class="product-stock">库存: {{ item.stock }}</text>
              </view>
            </view>
          </view>
          <view class="product-actions">
            <view class="action-chip" :class="statusClass(item.status)" @tap="cycleStatus(item)">
              {{ statusLabel(item.status) }}
            </view>
            <view class="action-btn pressable" @tap="editProduct(item)">
              <NutriIcon name="edit" :size="28" color="#3B82F6" />
            </view>
            <view class="action-btn pressable" @tap="confirmDelete(item)">
              <NutriIcon name="trash" :size="28" color="#EF4444" />
            </view>
          </view>
        </view>
      </view>
      <view v-if="loading && products.length > 0" class="loading-more">
        <text>加载更多...</text>
      </view>
      <view class="safe-bottom" />
    </scroll-view>

    <!-- Add/Edit BottomSheet -->
    <BottomSheet v-model="showAddSheet" :title="editingId ? '编辑产品' : '添加产品'" max-height="85vh">
      <view class="form-group">
        <text class="form-label">产品名称 *</text>
        <input class="form-input" v-model="form.name" placeholder="请输入产品名称" :adjust-position="true" />
      </view>
      <view class="form-group">
        <text class="form-label">分类 *</text>
        <view class="chip-row">
          <view v-for="c in productCategories" :key="c.value" class="chip pressable"
            :class="{ active: form.category === c.value }" @tap="form.category = c.value">{{ c.label }}</view>
        </view>
      </view>
      <view class="form-group">
        <text class="form-label">品牌</text>
        <input class="form-input" v-model="form.brand" placeholder="品牌名称" :adjust-position="true" />
      </view>
      <view class="form-group">
        <text class="form-label">图片URL</text>
        <input class="form-input" v-model="form.imageUrl" placeholder="输入图片链接" :adjust-position="true" />
      </view>
      <view class="form-group">
        <text class="form-label">简介</text>
        <textarea class="form-textarea" v-model="form.brief" placeholder="简短描述" auto-height :adjust-position="true" />
      </view>
      <view class="form-row">
        <view class="form-group flex-1">
          <text class="form-label">原价</text>
          <input class="form-input" v-model="form.originalPrice" type="digit" placeholder="0.00" :adjust-position="true" />
        </view>
        <view class="form-group flex-1">
          <text class="form-label">售价 *</text>
          <input class="form-input" v-model="form.salePrice" type="digit" placeholder="0.00" :adjust-position="true" />
        </view>
      </view>
      <view class="form-group">
        <text class="form-label">库存</text>
        <input class="form-input" v-model="form.stock" type="number" placeholder="999" :adjust-position="true" />
      </view>
      <template #footer>
        <button class="btn-primary sheet-save-btn" :loading="saving" @tap="saveProduct">
          {{ editingId ? '保存修改' : '创建产品' }}
        </button>
      </template>
    </BottomSheet>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, reactive } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { adminApi } from '@/services/api'
import NutriIcon from '@/components/NutriIcon.vue'
import BottomSheet from '@/components/BottomSheet.vue'

const navHeight = computed(() => {
  const sysInfo = uni.getSystemInfoSync()
  return (sysInfo.statusBarHeight || 44) + 44
})

const productCategories = [
  { value: 'SUPPLEMENT', label: '营养补剂' },
  { value: 'PROTEIN', label: '蛋白粉' },
  { value: 'VITAMIN', label: '维生素' },
  { value: 'ORGANIC', label: '有机食品' },
  { value: 'HEALTH_FOOD', label: '健康食品' },
  { value: 'EQUIPMENT', label: '健身器材' }
]

const products = ref<any[]>([])
const loading = ref(false)
const keyword = ref('')
const currentPage = ref(0)
const hasMore = ref(true)
const showAddSheet = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)

const form = reactive({
  name: '', category: 'SUPPLEMENT', brand: '', imageUrl: '',
  brief: '', originalPrice: '', salePrice: '', stock: '999'
})

function goBack() { uni.navigateBack() }

function resetForm() {
  form.name = ''; form.category = 'SUPPLEMENT'; form.brand = ''
  form.imageUrl = ''; form.brief = ''
  form.originalPrice = ''; form.salePrice = ''; form.stock = '999'
  editingId.value = null
}

function statusLabel(s: string) {
  return s === 'ON_SALE' ? '在售' : s === 'SOLD_OUT' ? '售罄' : '下架'
}
function statusClass(s: string) {
  return s === 'ON_SALE' ? 'on' : s === 'SOLD_OUT' ? 'warn' : 'off'
}
function cycleStatus(item: any) {
  const next = item.status === 'ON_SALE' ? 'OFF_SALE' : 'ON_SALE'
  toggleStatus(item, next)
}

async function loadProducts(reset = false) {
  if (loading.value) return
  if (reset) { currentPage.value = 0; hasMore.value = true; products.value = [] }
  if (!hasMore.value) return
  loading.value = true
  try {
    const res = await adminApi.getProducts({ keyword: keyword.value || undefined, page: currentPage.value, size: 15 })
    if (res.code === 200) {
      const page = res.data
      const list = page?.content || []
      if (reset) products.value = list
      else products.value = [...products.value, ...list]
      hasMore.value = !page?.last
      currentPage.value++
    }
  } catch {} finally { loading.value = false }
}

function handleSearch() { loadProducts(true) }
function loadMore() { loadProducts() }

function editProduct(item: any) {
  editingId.value = item.id
  form.name = item.name || ''
  form.category = item.category || 'SUPPLEMENT'
  form.brand = item.brand || ''
  form.imageUrl = item.imageUrl || ''
  form.brief = item.brief || ''
  form.originalPrice = item.originalPrice?.toString() || ''
  form.salePrice = item.salePrice?.toString() || ''
  form.stock = item.stock?.toString() || '999'
  showAddSheet.value = true
}

async function saveProduct() {
  if (!form.name.trim()) { uni.showToast({ title: '请输入名称', icon: 'none' }); return }
  if (!form.salePrice) { uni.showToast({ title: '请输入售价', icon: 'none' }); return }
  saving.value = true
  try {
    const data: any = {
      name: form.name, category: form.category, brand: form.brand || undefined,
      imageUrl: form.imageUrl || undefined, brief: form.brief || undefined,
      originalPrice: form.originalPrice ? Number(form.originalPrice) : undefined,
      salePrice: Number(form.salePrice),
      stock: form.stock ? Number(form.stock) : 999
    }
    const res = editingId.value
      ? await adminApi.updateProduct(editingId.value, data)
      : await adminApi.createProduct(data)
    if (res.code === 200) {
      uni.showToast({ title: editingId.value ? '修改成功' : '创建成功', icon: 'success' })
      showAddSheet.value = false
      resetForm()
      loadProducts(true)
    } else {
      uni.showToast({ title: res.message || '操作失败', icon: 'none' })
    }
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  } finally { saving.value = false }
}

async function toggleStatus(item: any, status: string) {
  try {
    const res = await adminApi.updateProductStatus(item.id, status)
    if (res.code === 200) {
      item.status = status
      uni.showToast({ title: statusLabel(status), icon: 'success' })
    }
  } catch {}
}

function confirmDelete(item: any) {
  uni.showModal({
    title: '确认删除',
    content: `确定删除「${item.name}」？`,
    success: async (r) => {
      if (r.confirm) {
        try {
          const res = await adminApi.deleteProduct(item.id)
          if (res.code === 200) {
            uni.showToast({ title: '已删除', icon: 'success' })
            loadProducts(true)
          }
        } catch {}
      }
    }
  })
}

onShow(() => { loadProducts(true) })
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: #F5F7FA; }
.nav-bar {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100; background: #fff;
  .nav-bar-inner {
    display: flex; align-items: center; justify-content: space-between;
    height: 44px; padding: 0 24rpx; margin-top: var(--status-bar-height, 44px);
  }
  .nav-back, .nav-action { padding: 12rpx; }
  .nav-title { font-size: 34rpx; font-weight: 600; color: #1F2937; }
}
.scroll-body { padding: 24rpx 24rpx 0; }
.search-bar {
  display: flex; align-items: center; gap: 12rpx;
  background: #fff; border-radius: 16rpx; padding: 0 24rpx; height: 80rpx; margin-bottom: 20rpx;
  .search-input { flex: 1; font-size: 28rpx; }
}
.loading-wrap, .empty-wrap {
  display: flex; flex-direction: column; align-items: center; padding: 120rpx 0; gap: 16rpx;
  .loading-text, .empty-text { font-size: 26rpx; color: #9CA3AF; }
}
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.product-card {
  .product-row { display: flex; gap: 20rpx; }
  .product-img { width: 160rpx; height: 120rpx; border-radius: 12rpx; flex-shrink: 0; }
  .product-img-placeholder {
    width: 160rpx; height: 120rpx; border-radius: 12rpx; background: #F3F4F6;
    display: flex; align-items: center; justify-content: center; flex-shrink: 0;
  }
  .product-info { flex: 1; min-width: 0; }
  .product-name { font-size: 28rpx; font-weight: 600; color: #1F2937; display: block; margin-bottom: 8rpx; }
  .product-tags { display: flex; gap: 8rpx; flex-wrap: wrap; margin-bottom: 8rpx; }
  .tag {
    font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 6rpx;
    background: rgba(59,130,246,0.1); color: #3B82F6;
    &.brand { background: rgba(245,158,11,0.1); color: #D97706; }
  }
  .product-price-row { display: flex; align-items: center; gap: 12rpx; }
  .product-price { font-size: 28rpx; color: #EF4444; font-weight: 600; }
  .product-origin { font-size: 22rpx; color: #9CA3AF; text-decoration: line-through; }
  .product-stock { font-size: 22rpx; color: #6B7280; margin-left: auto; }
}
.product-actions {
  display: flex; align-items: center; gap: 16rpx; margin-top: 16rpx; padding-top: 16rpx;
  border-top: 1px solid #F3F4F6;
  .action-chip {
    font-size: 22rpx; padding: 6rpx 16rpx; border-radius: 8rpx;
    &.on { background: rgba(16,185,129,0.1); color: #10B981; }
    &.off { background: rgba(239,68,68,0.1); color: #EF4444; }
    &.warn { background: #FEF3C7; color: #D97706; }
  }
  .action-btn { margin-left: auto; padding: 8rpx; &:first-of-type { margin-left: auto; } }
}
.form-group { margin-bottom: 24rpx; }
.form-label { font-size: 26rpx; color: #374151; font-weight: 500; display: block; margin-bottom: 12rpx; }
.form-input {
  width: 100%; height: 80rpx; background: #F9FAFB; border: 1px solid #E5E7EB;
  border-radius: 12rpx; padding: 0 24rpx; font-size: 28rpx; box-sizing: border-box;
}
.form-textarea {
  width: 100%; min-height: 120rpx; background: #F9FAFB; border: 1px solid #E5E7EB;
  border-radius: 12rpx; padding: 16rpx 24rpx; font-size: 28rpx; box-sizing: border-box;
}
.form-row { display: flex; gap: 20rpx; .flex-1 { flex: 1; } }
.chip-row { display: flex; flex-wrap: wrap; gap: 12rpx; }
.chip {
  font-size: 24rpx; padding: 8rpx 20rpx; border-radius: 8rpx;
  background: #F3F4F6; color: #6B7280;
  &.active { background: #10B981; color: #fff; }
}
.btn-primary {
  width: 100%; height: 88rpx; background: #10B981; color: #fff;
  border-radius: 16rpx; font-size: 30rpx; font-weight: 600;
  display: flex; align-items: center; justify-content: center; border: none;
}
.sheet-save-btn { margin: 0; }
.loading-more { text-align: center; padding: 24rpx; font-size: 24rpx; color: #9CA3AF; }
.pressable { &:active { opacity: 0.7; } }
.safe-bottom { height: 60rpx; }
</style>
