<template>
  <view class="page">
    <!-- Nav Bar -->
    <NutriNavBar :show-back="false" bg-color="#FFFFFF">
      <template #left>
        <text class="nav-brand">营养商城</text>
      </template>
      <template #title><view /></template>
      <template #right>
        <view class="nav-actions">
          <view class="nav-icon-btn pressable" @tap="openOrders">
            <NutriIcon name="receipt" :size="36" color="#1A1A2E" />
          </view>
          <view class="nav-icon-btn pressable" @tap="openCart">
            <NutriIcon name="cart" :size="36" color="#1A1A2E" />
            <view class="icon-badge" v-if="cartCount > 0">{{ cartCount > 99 ? '99+' : cartCount }}</view>
          </view>
        </view>
      </template>
    </NutriNavBar>

    <!-- Store Selector + Search -->
    <view :style="{ paddingTop: navBarTotalHeight + 'px' }">
      <StoreSelector
        :store-name="currentStore.name"
        :store-address="currentStore.address"
        @tap="onStoreTap"
      />

      <!-- Disclaimer -->
      <view class="disclaimer-tip" v-if="showDisclaimer">
        <NutriIcon name="alert" :size="28" color="#059669" />
        <text class="disclaimer-text">商品信息来源于第三方，仅供参考。营养补充剂不能替代正常饮食和药物治疗。</text>
        <view class="dismiss pressable" @tap="showDisclaimer = false">
          <NutriIcon name="x" :size="24" color="#8896AB" />
        </view>
      </view>

      <!-- Search Bar -->
      <view class="search-bar">
        <view class="search-inner">
          <NutriIcon name="search" :size="28" color="#8896AB" />
          <input
            class="search-input"
            v-model="keyword"
            placeholder="搜索营养食品..."
            confirm-type="search"
            @confirm="handleSearch"
          />
          <view v-if="keyword" class="clear-btn pressable" @tap="clearSearch">
            <NutriIcon name="x" :size="24" color="#8896AB" />
          </view>
        </view>
      </view>
    </view>

    <!-- Main Content: Luckin-style sidebar + right product area -->
    <view class="main-area" :style="{ top: mainAreaTop + 'px' }">
      <!-- Left Category Sidebar -->
      <scroll-view class="category-sidebar" scroll-y>
        <view
          class="cat-item"
          :class="{ active: currentCategory === '' }"
          @tap="switchCategory('')"
        >全部</view>
        <view
          v-for="cat in categories"
          :key="cat.id || cat.name"
          class="cat-item"
          :class="{ active: currentCategory === (cat.id || cat.name) }"
          @tap="switchCategory(cat.id || cat.name)"
        >{{ cat.name }}</view>
      </scroll-view>

      <!-- Right Product Area -->
      <scroll-view
        class="product-area"
        scroll-y
        @scrolltolower="onReachBottom_"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="onPullDown_"
      >
        <!-- Recommended Section -->
        <view class="recommend-section" v-if="!keyword && !currentCategory && recommended.length > 0">
          <view class="section-header">
            <NutriIcon name="sparkles" :size="28" color="#F59E0B" />
            <text class="section-title">为你推荐</text>
          </view>
          <scroll-view class="recommend-scroll" scroll-x :show-scrollbar="false">
            <view class="recommend-list">
              <view class="recommend-card pressable" v-for="item in recommended" :key="item.id" @tap="openDetail(item)">
                <image class="recommend-img" :src="item.imageUrl || item.image" mode="aspectFill" lazy-load />
                <text class="recommend-name">{{ item.name }}</text>
                <PriceTag :price="item.salePrice || item.price" size="sm" />
              </view>
            </view>
          </scroll-view>
        </view>

        <!-- Product Grid -->
        <view class="product-grid" v-if="products.length > 0">
          <view class="product-card" v-for="item in products" :key="item.id">
            <view class="card-tap-area pressable" @tap="openDetail(item)">
              <image class="product-img" :src="item.imageUrl || item.image" mode="aspectFill" lazy-load />
              <view v-if="item.isRecommended" class="rec-badge">推荐</view>
              <view class="product-info">
                <text class="product-name">{{ item.name }}</text>
                <view class="meta-row">
                  <text class="stars">{{ getStars(item.rating) }}</text>
                  <text class="sales-count">已售 {{ item.salesCount || 0 }}</text>
                </view>
                <view class="price-row">
                  <PriceTag :price="item.salePrice || item.price" :original-price="item.originalPrice" />
                </view>
              </view>
            </view>
            <view class="add-cart-btn pressable" @tap.stop="quickAddToCart(item)">
              <NutriIcon name="plus" :size="28" color="#fff" />
            </view>
          </view>
        </view>

        <!-- Empty State -->
        <EmptyState
          v-else-if="!loading"
          icon="bag"
          :title="keyword ? '未找到相关商品' : '暂无商品'"
          description="试试其他分类"
        />

        <!-- Loading & No More -->
        <view class="status-text" v-if="loading"><text>加载中...</text></view>
        <view class="status-text" v-if="!loading && noMore && products.length > 0"><text>— 没有更多了 —</text></view>

        <view style="height: 160rpx;" />
      </scroll-view>
    </view>

    <!-- Cart Bar -->
    <CartBar
      :total-count="cartCount"
      :total-price="cartTotal"
      @view-cart="openCart"
      @submit="openCheckout"
    />

    <!-- ===== Product Detail BottomSheet ===== -->
    <BottomSheet v-model="detailVisible" title="商品详情">
      <view class="detail-loading" v-if="detailLoading">
        <text>加载中...</text>
      </view>

      <template v-if="detailProduct && !detailLoading">
        <image class="detail-main-img" :src="detailProduct.imageUrl || detailProduct.image" mode="aspectFill" />

        <view class="detail-info-area">
          <text class="detail-name">{{ detailProduct.name }}</text>
          <view class="detail-brand-tag" v-if="detailProduct.brand">
            <text>{{ detailProduct.brand }}</text>
          </view>
          <text class="detail-brief" v-if="detailProduct.brief">{{ detailProduct.brief }}</text>

          <view class="detail-rating-row" v-if="detailProduct.rating">
            <text class="stars">{{ getStars(detailProduct.rating) }}</text>
            <text class="review-count">{{ detailProduct.reviewCount || 0 }} 评价</text>
          </view>

          <view class="detail-price-row">
            <PriceTag :price="detailProduct.salePrice || detailProduct.price" :original-price="detailProduct.originalPrice" size="lg" />
          </view>

          <view class="detail-stock-row">
            <view class="stock-tag" :class="{ danger: (detailProduct.stock ?? 999) <= 0 }">
              {{ (detailProduct.stock ?? 999) > 0 ? `库存 ${detailProduct.stock ?? '充足'}` : '已售罄' }}
            </view>
            <text class="detail-sales">已售 {{ detailProduct.salesCount || 0 }}件</text>
          </view>

          <view class="detail-tags" v-if="detailProduct.tags && detailProduct.tags.length">
            <view class="tag-chip" v-for="(t, i) in detailProduct.tags" :key="i">{{ t }}</view>
          </view>

          <view class="detail-section" v-if="detailProduct.description">
            <text class="detail-section-title">产品介绍</text>
            <text class="detail-section-body">{{ detailProduct.description }}</text>
          </view>

          <view class="detail-section" v-if="detailProduct.specifications && Object.keys(parseSpecs(detailProduct.specifications)).length">
            <text class="detail-section-title">规格参数</text>
            <view class="spec-table">
              <view class="spec-row" v-for="(v, k) in parseSpecs(detailProduct.specifications)" :key="k">
                <text class="spec-key">{{ k }}</text>
                <text class="spec-val">{{ v }}</text>
              </view>
            </view>
          </view>

          <view class="detail-qty-row">
            <text class="qty-label">数量</text>
            <view class="stepper">
              <view class="stepper-btn stepper-minus" :class="{ disabled: detailQty <= 1 }" @tap="detailQty > 1 && detailQty--">
                <NutriIcon name="minus" :size="24" color="#8896AB" />
              </view>
              <text class="stepper-count">{{ detailQty }}</text>
              <view class="stepper-btn stepper-plus" :class="{ disabled: detailQty >= maxDetailQty }" @tap="detailQty < maxDetailQty && detailQty++">
                <NutriIcon name="plus" :size="24" color="#fff" />
              </view>
            </view>
          </view>
        </view>
      </template>

      <template #footer v-if="detailProduct && !detailLoading">
        <view class="detail-footer">
          <view class="detail-view-full pressable" @tap="goFullDetail(detailProduct.id)">查看完整详情</view>
          <view class="detail-cart-btn pressable" :class="{ disabled: (detailProduct.stock ?? 999) <= 0 }" @tap="addToCartFromDetail">
            加入购物车
          </view>
        </view>
      </template>
    </BottomSheet>

    <!-- ===== Cart BottomSheet ===== -->
    <BottomSheet v-model="cartVisible" title="购物车">
      <EmptyState v-if="cart.length === 0" icon="cart" title="购物车是空的" />

      <template v-if="cart.length > 0">
        <view class="cart-item" v-for="(item, idx) in cart" :key="idx">
          <image class="cart-item-img" :src="item.imageUrl" mode="aspectFill" />
          <view class="cart-item-info">
            <text class="cart-item-name">{{ item.name }}</text>
            <PriceTag :price="item.salePrice" size="sm" />
            <view class="stepper small">
              <view class="stepper-btn stepper-minus" :class="{ disabled: item.quantity <= 1 }" @tap="changeCartQty(idx, -1)">
                <NutriIcon name="minus" :size="20" color="#8896AB" />
              </view>
              <text class="stepper-count">{{ item.quantity }}</text>
              <view class="stepper-btn stepper-plus" :class="{ disabled: item.quantity >= 10 }" @tap="changeCartQty(idx, 1)">
                <NutriIcon name="plus" :size="20" color="#fff" />
              </view>
            </view>
          </view>
          <view class="cart-item-del pressable" @tap="removeFromCart(idx)">
            <NutriIcon name="trash" :size="28" color="#8896AB" />
          </view>
        </view>
      </template>

      <template #footer v-if="cart.length > 0">
        <view class="cart-summary">
          <view class="cart-total-row">
            <text>合计：</text>
            <text class="cart-total-amount">¥{{ formatPrice(cartTotal) }}</text>
          </view>
          <view class="cart-checkout-btn pressable" @tap="openCheckout">去结算（{{ cartItemCount }}件）</view>
        </view>
      </template>
    </BottomSheet>

    <!-- ===== Checkout BottomSheet ===== -->
    <BottomSheet v-model="checkoutVisible" title="确认订单">
      <!-- Address Section -->
      <view class="ck-section">
        <view class="ck-section-head">
          <view class="ck-section-title-row">
            <NutriIcon name="map-pin" :size="28" color="#10B981" />
            <text class="ck-section-title">收货信息</text>
          </view>
          <text class="ck-link pressable" @tap="goAddressManage">管理</text>
        </view>

        <view v-if="savedAddresses.length > 0 && !useManualAddress">
          <view
            class="addr-item"
            :class="{ active: selectedAddressId === addr.id }"
            v-for="addr in savedAddresses.slice(0, 3)"
            :key="addr.id"
            @tap="selectAddress(addr)"
          >
            <view class="addr-radio">
              <view class="radio-dot" :class="{ checked: selectedAddressId === addr.id }"></view>
            </view>
            <view class="addr-body">
              <view class="addr-top">
                <text class="addr-name">{{ addr.receiverName }}</text>
                <text class="addr-phone">{{ addr.receiverPhone }}</text>
                <view class="addr-default" v-if="addr.isDefault">默认</view>
              </view>
              <text class="addr-detail">{{ getFullAddress(addr) }}</text>
            </view>
          </view>
          <view class="addr-new-link pressable" @tap="useManualAddress = true">
            <NutriIcon name="plus" :size="24" color="#10B981" />
            <text>使用新地址</text>
          </view>
        </view>

        <view v-else>
          <view class="addr-back pressable" v-if="savedAddresses.length > 0" @tap="useManualAddress = false">
            <NutriIcon name="arrow-left" :size="24" color="#10B981" />
            <text>选择已有地址</text>
          </view>
          <view class="form-group">
            <input class="form-input" v-model="checkoutForm.receiverName" placeholder="收货人姓名" />
          </view>
          <view class="form-group">
            <input class="form-input" v-model="checkoutForm.receiverPhone" placeholder="联系电话" type="number" maxlength="11" />
          </view>
          <view class="form-group">
            <textarea class="form-textarea" v-model="checkoutForm.receiverAddress" placeholder="详细收货地址" :maxlength="200" />
          </view>
        </view>
      </view>

      <!-- Remark -->
      <view class="ck-section">
        <text class="ck-section-title">备注</text>
        <view class="form-group">
          <input class="form-input" v-model="checkoutForm.remark" placeholder="选填" />
        </view>
      </view>

      <!-- Items -->
      <view class="ck-section">
        <text class="ck-section-title">商品清单</text>
        <view class="ck-item" v-for="(item, idx) in cart" :key="idx">
          <text class="ck-item-name">{{ item.name }} × {{ item.quantity }}</text>
          <text class="ck-item-price">¥{{ formatPrice(item.salePrice * item.quantity) }}</text>
        </view>
      </view>

      <!-- Total -->
      <view class="ck-total">
        <text>应付总额：</text>
        <text class="ck-total-amount">¥{{ formatPrice(cartTotal) }}</text>
      </view>

      <view class="ck-alert">
        <NutriIcon name="alert" :size="24" color="#059669" />
        <text>模拟支付模式：点击提交后将自动完成支付</text>
      </view>

      <template #footer>
        <view class="submit-btn pressable" :class="{ disabled: checkoutLoading }" @tap="handleCheckout">
          {{ checkoutLoading ? '提交中...' : '提交订单并支付' }}
        </view>
      </template>
    </BottomSheet>

    <!-- ===== Orders BottomSheet ===== -->
    <BottomSheet v-model="ordersVisible" title="我的订单">
      <view class="detail-loading" v-if="ordersLoading">
        <text>加载中...</text>
      </view>

      <EmptyState v-else-if="orders.length === 0" icon="receipt" title="暂无订单" />

      <template v-if="orders.length > 0 && !ordersLoading">
        <view class="order-card" v-for="order in orders" :key="order.orderNo">
          <view class="order-head">
            <text class="order-no">{{ order.orderNo }}</text>
            <text class="order-status" :class="orderStatusClass(order.orderStatus)">{{ orderStatusText(order.orderStatus) }}</text>
          </view>
          <view class="order-products">
            <view class="order-prod" v-for="(item, i) in (order.items || []).slice(0, 2)" :key="i">
              <text>{{ item.productName }} × {{ item.quantity }}</text>
            </view>
            <text class="order-more" v-if="(order.items || []).length > 2">等{{ order.items.length }}件商品</text>
          </view>
          <view class="order-foot">
            <view class="order-money">
              <text class="pay-status" :class="payStatusClass(order.paymentStatus)">{{ payStatusText(order.paymentStatus) }}</text>
              <text class="order-amount">¥{{ formatPrice(order.totalAmount) }}</text>
            </view>
            <view class="order-actions">
              <view class="action-chip warn pressable" v-if="order.orderStatus === 'PAID'" @tap="handleShip(order)">模拟发货</view>
              <view class="action-chip success pressable" v-if="order.orderStatus === 'SHIPPED'" @tap="handleReceive(order)">确认收货</view>
              <view class="action-chip danger pressable" v-if="['PAID','SHIPPED','DELIVERED'].includes(order.orderStatus)" @tap="handleRefund(order)">退款</view>
            </view>
          </view>
          <view class="order-tracking" v-if="order.trackingNo">
            <NutriIcon name="truck" :size="24" color="#8896AB" />
            <text>快递单号：{{ order.trackingNo }}</text>
          </view>
        </view>
      </template>
    </BottomSheet>

    <!-- ===== Order Result Popup ===== -->
    <view class="result-overlay" v-if="orderResultVisible" @tap="orderResultVisible = false">
      <view class="result-popup" @tap.stop>
        <NutriIcon :name="orderSuccess ? 'check' : 'alert'" :size="80" :color="orderSuccess ? '#10B981' : '#EF4444'" />
        <text class="result-title">{{ orderSuccess ? '下单成功' : '下单失败' }}</text>
        <text class="result-desc">{{ orderSuccess ? `订单号：${orderResultNo}` : orderResultError }}</text>
        <view class="result-close-btn pressable" @tap="orderResultVisible = false">{{ orderSuccess ? '完成' : '关闭' }}</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { productApi, addressApi } from '@/services/api'
import { request } from '@/utils/request'
import { checkLogin } from '@/utils/common'
import NutriNavBar from '@/components/NutriNavBar.vue'
import NutriIcon from '@/components/NutriIcon.vue'
import StoreSelector from '@/components/StoreSelector.vue'
import PriceTag from '@/components/PriceTag.vue'
import EmptyState from '@/components/EmptyState.vue'
import CartBar from '@/components/CartBar.vue'
import BottomSheet from '@/components/BottomSheet.vue'

// ────── Constants ──────
const CART_KEY = 'nutriai_cart'
const PAGE_SIZE = 10
const categoryNameMap: Record<string, string> = {
  EQUIPMENT: '健身器材',
  HEALTH_FOOD: '营养食品',
  ORGANIC: '有机食品',
  PROTEIN: '蛋白质',
  SUPPLEMENT: '营养补剂',
  VITAMIN: '维生素'
}

// ────── Layout ──────
const navBarTotalHeight = ref(64)
const mainAreaTop = ref(64 + 40 + 44 + 36) // navBar + store + disclaimer + search

const currentStore = ref({
  name: 'NutriAI 营养商城',
  address: '点击选择最近门店'
})

// ────── Shared State ──────
const showDisclaimer = ref(true)
const keyword = ref('')
const currentCategory = ref('')
const categories = ref<any[]>([])
const products = ref<any[]>([])
const recommended = ref<any[]>([])
const loading = ref(false)
const noMore = ref(false)
const page = ref(0)
const refreshing = ref(false)

// ────── Utility ──────
function formatPrice(val: number | undefined): string {
  return (val || 0).toFixed(2)
}

function getStars(rating: number | undefined): string {
  const r = Math.round(rating || 0)
  return '★'.repeat(r) + '☆'.repeat(5 - r)
}

function parseSpecs(specs: any): Record<string, string> {
  if (!specs) return {}
  if (typeof specs === 'string') {
    try { return JSON.parse(specs) } catch { return { '规格': specs } }
  }
  if (typeof specs === 'object') return specs
  return {}
}

function extractList(data: any): any[] {
  return data?.content || data?.records || data?.list || (Array.isArray(data) ? data : [])
}

// ────── Data Loading ──────
async function loadCategories() {
  try {
    const res = await productApi.getCategories()
    const raw = res.data || []
    categories.value = raw.map((c: any) => {
      if (typeof c === 'string') return { id: c, name: categoryNameMap[c] || c }
      return c
    })
  } catch { /* ignore */ }
}

async function loadRecommended() {
  try {
    const res = await productApi.getRecommended()
    recommended.value = extractList(res.data)
  } catch { /* ignore */ }
}

async function loadProducts(isRefresh = false) {
  if (loading.value) return
  loading.value = true
  try {
    const params: any = { page: page.value, size: PAGE_SIZE }
    if (currentCategory.value) params.category = currentCategory.value
    if (keyword.value.trim()) params.keyword = keyword.value.trim()

    const res = keyword.value.trim()
      ? await productApi.search(keyword.value.trim())
      : await productApi.getProducts(params)

    const list = extractList(res.data)
    products.value = isRefresh ? list : [...products.value, ...list]
    noMore.value = list.length < PAGE_SIZE
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function switchCategory(cat: string) {
  if (currentCategory.value === cat) return
  currentCategory.value = cat
  keyword.value = ''
  refreshData()
}

function handleSearch() {
  currentCategory.value = ''
  refreshData()
}

function clearSearch() {
  keyword.value = ''
  refreshData()
}

function refreshData() {
  page.value = 0
  noMore.value = false
  loadProducts(true)
}

function onStoreTap() {
  uni.showToast({ title: '门店选择即将上线', icon: 'none' })
}

// ────── Product Detail ──────
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailProduct = ref<any>(null)
const detailQty = ref(1)
const maxDetailQty = computed(() => Math.min(detailProduct.value?.stock ?? 10, 10))

async function openDetail(item: any) {
  detailQty.value = 1
  detailProduct.value = item
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await productApi.getProduct(item.id)
    if (res.data) {
      const p = res.data
      if (!p.price && p.salePrice) p.price = p.salePrice
      detailProduct.value = p
    }
  } catch { /* keep list data as fallback */ }
  finally { detailLoading.value = false }
}

function goFullDetail(id: number) {
  detailVisible.value = false
  uni.navigateTo({ url: `/pages/product-shop/detail?id=${id}` })
}

// ────── Cart ──────
function loadCartFromStorage(): any[] {
  try {
    const raw = uni.getStorageSync(CART_KEY)
    return raw ? (typeof raw === 'string' ? JSON.parse(raw) : raw) : []
  } catch { return [] }
}

function saveCart() {
  try { uni.setStorageSync(CART_KEY, JSON.stringify(cart.value)) } catch { /* ignore */ }
}

const cart = ref<any[]>(loadCartFromStorage())
const cartVisible = ref(false)

const cartCount = computed(() => cart.value.reduce((s, i) => s + i.quantity, 0))
const cartItemCount = computed(() => cart.value.length)
const cartTotal = computed(() => cart.value.reduce((s, i) => s + i.salePrice * i.quantity, 0))

watch(cart, saveCart, { deep: true })

function quickAddToCart(p: any) {
  if (!checkLogin()) return
  const existing = cart.value.find(c => c.productId === p.id)
  if (existing) {
    if (existing.quantity < 10) existing.quantity++
  } else {
    cart.value.push({
      productId: p.id,
      name: p.name,
      imageUrl: p.imageUrl || p.image || '',
      salePrice: p.salePrice || p.price || 0,
      quantity: 1
    })
  }
  uni.vibrateShort({ type: 'light' })
  uni.showToast({ title: `已添加 ${p.name}`, icon: 'none', duration: 1200 })
}

function addToCartFromDetail() {
  if (!detailProduct.value || (detailProduct.value.stock ?? 999) <= 0) return
  if (!checkLogin()) return
  const p = detailProduct.value
  const existing = cart.value.find(c => c.productId === p.id)
  if (existing) {
    existing.quantity = Math.min(existing.quantity + detailQty.value, 10)
  } else {
    cart.value.push({
      productId: p.id,
      name: p.name,
      imageUrl: p.imageUrl || p.image || '',
      salePrice: p.salePrice || p.price || 0,
      quantity: detailQty.value
    })
  }
  detailVisible.value = false
  uni.showToast({ title: `已添加 ${p.name} × ${detailQty.value}`, icon: 'none', duration: 1200 })
}

function changeCartQty(idx: number, delta: number) {
  const item = cart.value[idx]
  if (!item) return
  const nv = item.quantity + delta
  if (nv < 1 || nv > 10) return
  item.quantity = nv
}

function removeFromCart(idx: number) {
  cart.value.splice(idx, 1)
}

function openCart() {
  if (!checkLogin()) return
  cartVisible.value = true
}

// ────── Checkout ──────
const checkoutVisible = ref(false)
const checkoutLoading = ref(false)
const checkoutForm = ref({ receiverName: '', receiverPhone: '', receiverAddress: '', remark: '' })
const savedAddresses = ref<any[]>([])
const selectedAddressId = ref<number>(0)
const useManualAddress = ref(false)

const orderResultVisible = ref(false)
const orderSuccess = ref(false)
const orderResultNo = ref('')
const orderResultError = ref('')

function getFullAddress(addr: any): string {
  return `${addr.province || ''}${addr.city || ''}${addr.district || ''}${addr.detailAddress || ''}`
}

function selectAddress(addr: any) {
  selectedAddressId.value = addr.id
}

function goAddressManage() {
  uni.navigateTo({ url: '/pages/address/index' })
}

async function loadSavedAddresses() {
  try {
    const res = await addressApi.getList()
    savedAddresses.value = res.data || []
    if (savedAddresses.value.length > 0) {
      const def = savedAddresses.value.find((a: any) => a.isDefault) || savedAddresses.value[0]
      selectedAddressId.value = def.id
      useManualAddress.value = false
    } else {
      useManualAddress.value = true
    }
  } catch {
    savedAddresses.value = []
    useManualAddress.value = true
  }
}

function openCheckout() {
  if (!cart.value.length) return
  cartVisible.value = false
  checkoutForm.value = { receiverName: '', receiverPhone: '', receiverAddress: '', remark: '' }
  selectedAddressId.value = 0
  useManualAddress.value = false
  checkoutVisible.value = true
  loadSavedAddresses()
}

async function handleCheckout() {
  if (checkoutLoading.value) return
  let receiverName: string, receiverPhone: string, receiverAddress: string
  const { remark } = checkoutForm.value

  if (selectedAddressId.value && !useManualAddress.value) {
    const addr = savedAddresses.value.find(a => a.id === selectedAddressId.value)
    if (!addr) return uni.showToast({ title: '请选择收货地址', icon: 'none' })
    receiverName = addr.receiverName
    receiverPhone = addr.receiverPhone
    receiverAddress = getFullAddress(addr)
  } else {
    receiverName = checkoutForm.value.receiverName
    receiverPhone = checkoutForm.value.receiverPhone
    receiverAddress = checkoutForm.value.receiverAddress
  }

  if (!receiverName || !receiverPhone || !receiverAddress) {
    return uni.showToast({ title: '请填写完整收货信息', icon: 'none' })
  }

  checkoutLoading.value = true
  try {
    const items = cart.value.map(c => ({ productId: c.productId, quantity: c.quantity }))
    const res = await productApi.createOrder({ items, receiverName, receiverPhone, receiverAddress, remark })
    const order = res.data
    const orderNo = order?.orderNo || order

    await productApi.simulatePay(orderNo)

    cart.value = []
    checkoutVisible.value = false
    orderSuccess.value = true
    orderResultNo.value = orderNo
    orderResultVisible.value = true
  } catch (e: any) {
    checkoutVisible.value = false
    orderSuccess.value = false
    orderResultError.value = e?.message || '订单创建失败'
    orderResultVisible.value = true
  } finally {
    checkoutLoading.value = false
  }
}

// Listen for address selection from address manage page
uni.$on('address-selected', (addr: any) => {
  if (!savedAddresses.value.find((a: any) => a.id === addr.id)) {
    savedAddresses.value.unshift(addr)
  }
  selectedAddressId.value = addr.id
  useManualAddress.value = false
})

onUnmounted(() => { uni.$off('address-selected') })

// ────── Orders ──────
const ordersVisible = ref(false)
const ordersLoading = ref(false)
const orders = ref<any[]>([])

function orderStatusText(s: string): string {
  return ({ PENDING_PAYMENT: '待支付', PAID: '已支付', SHIPPED: '已发货', DELIVERED: '已送达', COMPLETED: '已完成', CANCELLED: '已取消', REFUNDED: '已退款' } as any)[s] || s
}
function orderStatusClass(s: string): string {
  return ({ PENDING_PAYMENT: 'warn', PAID: 'success', SHIPPED: 'info', DELIVERED: 'success', COMPLETED: 'info', CANCELLED: 'muted', REFUNDED: 'danger' } as any)[s] || ''
}
function payStatusText(s: string): string {
  return ({ PAID: '已支付', PENDING: '待支付', REFUNDED: '已退款', EXPIRED: '已超时' } as any)[s] || s
}
function payStatusClass(s: string): string {
  return ({ PAID: 'success', PENDING: 'warn', REFUNDED: 'danger', EXPIRED: 'muted' } as any)[s] || ''
}

async function openOrders() {
  if (!checkLogin()) return
  ordersVisible.value = true
  ordersLoading.value = true
  try {
    const res = await productApi.getOrders({ page: 0, size: 20 })
    orders.value = extractList(res.data)
  } catch {
    uni.showToast({ title: '获取订单失败', icon: 'none' })
  } finally {
    ordersLoading.value = false
  }
}

async function handleShip(order: any) {
  try {
    await request({ url: `/products/orders/${order.orderNo}/simulate-ship`, method: 'POST' })
    uni.showToast({ title: '已模拟发货', icon: 'none' })
    openOrders()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

async function handleReceive(order: any) {
  try {
    await productApi.confirmReceive(order.orderNo)
    uni.showToast({ title: '已确认收货', icon: 'none' })
    openOrders()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

async function handleRefund(order: any) {
  try {
    await productApi.simulateRefund(order.orderNo)
    uni.showToast({ title: '退款成功', icon: 'none' })
    openOrders()
  } catch {
    uni.showToast({ title: '退款失败', icon: 'none' })
  }
}

// ────── Lifecycle ──────
function onReachBottom_() {
  if (noMore.value || loading.value) return
  page.value++
  loadProducts()
}

function onPullDown_() {
  refreshing.value = true
  page.value = 0
  noMore.value = false
  Promise.all([loadProducts(true), loadRecommended()])
    .then(() => { refreshing.value = false })
}

onShow(() => {
  const windowInfo = uni.getWindowInfo()
  const statusBarHeight = windowInfo.statusBarHeight || 0
  navBarTotalHeight.value = statusBarHeight + 44

  // Approximate mainAreaTop: navBar + store(~40px) + disclaimer(~30px) + search(~44px)
  mainAreaTop.value = navBarTotalHeight.value + 114

  loadCategories()
  loadRecommended()
  refreshData()
  cart.value = loadCartFromStorage()
})
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: $background;
}

/* Nav */
.nav-brand {
  font-size: 34rpx;
  font-weight: 700;
  color: $accent;
  letter-spacing: 2rpx;
}
.nav-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.nav-icon-btn {
  width: 64rpx; height: 64rpx;
  display: flex; align-items: center; justify-content: center;
  background: $muted;
  border-radius: 50%;
  position: relative;
}
.icon-badge {
  position: absolute;
  top: -6rpx; right: -6rpx;
  min-width: 32rpx; height: 32rpx;
  line-height: 32rpx;
  text-align: center;
  font-size: 20rpx;
  font-weight: 600;
  color: $card;
  background: linear-gradient(135deg, $accent, $accent-secondary);
  border-radius: 100rpx;
  padding: 0 8rpx;
}

/* Disclaimer */
.disclaimer-tip {
  display: flex;
  align-items: center;
  gap: 8rpx;
  background: #ECFDF5;
  border-radius: 16rpx;
  padding: 12rpx 40rpx 12rpx 16rpx;
  font-size: 22rpx;
  color: $accent-secondary;
  margin: 12rpx 32rpx;
  position: relative;
}
.disclaimer-text { flex: 1; }
.dismiss {
  position: absolute;
  right: 12rpx; top: 50%;
  transform: translateY(-50%);
}

/* Search Bar */
.search-bar {
  padding: 8rpx 32rpx 12rpx;
}
.search-inner {
  display: flex;
  align-items: center;
  background: $card;
  border-radius: 100rpx;
  padding: 0 24rpx;
  height: 72rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
  gap: 12rpx;
}
.search-input {
  flex: 1;
  font-size: 28rpx;
  height: 72rpx;
  color: $foreground;
}
.clear-btn { padding: 8rpx; }

/* Main Content Area */
.main-area {
  position: fixed;
  left: 0; right: 0; bottom: 0;
  display: flex;
}

/* Category Sidebar */
.category-sidebar {
  width: $sidebar-width;
  background: $sidebar-bg;
  flex-shrink: 0;
  height: 100%;
}
.cat-item {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100rpx;
  font-size: 26rpx;
  color: $muted-foreground;
  font-weight: 500;
  transition: all $duration-fast $ease-out;

  &.active {
    background: $sidebar-active-bg;
    color: $sidebar-active-color;
    font-weight: 600;

    &::before {
      content: '';
      position: absolute;
      left: 0; top: 20%; height: 60%;
      width: 6rpx;
      border-radius: 0 6rpx 6rpx 0;
      background: $accent;
    }
  }
}

/* Product Area */
.product-area {
  flex: 1;
  height: 100%;
  background: $background;
}

/* Recommended */
.recommend-section {
  padding: 20rpx 0;
}
.section-header {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 0 20rpx;
  margin-bottom: 12rpx;
}
.section-title {
  font-size: 28rpx;
  font-weight: 700;
  color: $foreground;
}
.recommend-scroll {
  white-space: nowrap;
  padding: 0 12rpx;
}
.recommend-list {
  display: inline-flex;
  gap: 16rpx;
}
.recommend-card {
  width: 220rpx;
  background: $card;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
  padding-bottom: 12rpx;
  flex-shrink: 0;
}
.recommend-img {
  width: 220rpx;
  height: 200rpx;
}
.recommend-name {
  display: block;
  font-size: 24rpx;
  color: $foreground;
  margin-top: 8rpx;
  padding: 0 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Product Grid */
.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  padding: 16rpx 16rpx;
  gap: 16rpx;
}
.product-card {
  background: $card;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
  position: relative;
  min-width: 0;
}
.card-tap-area { position: relative; }
.product-img {
  width: 100%;
  height: 280rpx;
}
.rec-badge {
  position: absolute;
  top: 8rpx; right: 8rpx;
  font-size: 18rpx;
  color: $card;
  background: linear-gradient(135deg, $accent, $accent-secondary);
  padding: 4rpx 14rpx;
  border-radius: 100rpx;
  font-weight: 600;
}
.product-info {
  padding: 12rpx 16rpx 48rpx;
}
.product-name {
  font-size: 24rpx;
  color: $foreground;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 66rpx;
}
.meta-row {
  display: flex;
  align-items: center;
  gap: 6rpx;
  margin-top: 6rpx;
}
.stars {
  font-size: 20rpx;
  color: #F59E0B;
  letter-spacing: -2rpx;
}
.sales-count {
  font-size: 20rpx;
  color: $muted-foreground;
}
.price-row { margin-top: 8rpx; }
.add-cart-btn {
  position: absolute;
  right: 12rpx; bottom: 12rpx;
  width: 48rpx; height: 48rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, $accent, $accent-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(16,185,129,0.3);
}

/* Status Text */
.status-text {
  text-align: center;
  padding: 30rpx;
  font-size: 24rpx;
  color: $muted-foreground;
}

/* ===== Detail Modal ===== */
.detail-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
  font-size: 28rpx;
  color: $muted-foreground;
}
.detail-main-img {
  width: 100%;
  height: 480rpx;
}
.detail-info-area {
  padding: 20rpx 32rpx;
}
.detail-name {
  font-size: 32rpx;
  font-weight: 700;
  color: $foreground;
  line-height: 1.5;
  display: block;
}
.detail-brand-tag {
  display: inline-block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: $accent-secondary;
  background: #ECFDF5;
  padding: 4rpx 16rpx;
  border-radius: 100rpx;
}
.detail-brief {
  display: block;
  font-size: 26rpx;
  color: $muted-foreground;
  margin-top: 10rpx;
  line-height: 1.5;
}
.detail-rating-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 12rpx;
}
.review-count {
  font-size: 24rpx;
  color: $muted-foreground;
}
.detail-price-row {
  margin-top: 12rpx;
}
.detail-stock-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-top: 12rpx;
}
.stock-tag {
  font-size: 22rpx;
  color: $card;
  background: linear-gradient(135deg, $accent, $accent-secondary);
  padding: 4rpx 16rpx;
  border-radius: 100rpx;

  &.danger { background: #EF4444; }
}
.detail-sales {
  font-size: 24rpx;
  color: $muted-foreground;
}
.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 12rpx;
}
.tag-chip {
  font-size: 22rpx;
  color: $accent-secondary;
  background: #ECFDF5;
  padding: 4rpx 16rpx;
  border-radius: 100rpx;
}
.detail-section {
  margin-top: 24rpx;
  padding: 20rpx;
  background: $muted;
  border-radius: 20rpx;
}
.detail-section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $foreground;
  margin-bottom: 10rpx;
  display: block;
  padding-left: 16rpx;
  border-left: 6rpx solid $accent;
}
.detail-section-body {
  font-size: 26rpx;
  color: $muted-foreground;
  line-height: 1.8;
  display: block;
}
.spec-table { margin-top: 8rpx; }
.spec-row {
  display: flex;
  padding: 12rpx 0;
  & + .spec-row { border-top: 1rpx solid $border; }
}
.spec-key {
  width: 180rpx;
  flex-shrink: 0;
  font-size: 26rpx;
  color: $muted-foreground;
}
.spec-val {
  flex: 1;
  font-size: 26rpx;
  color: $foreground;
}
.detail-qty-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 24rpx;
  padding-top: 16rpx;
}
.qty-label {
  font-size: 28rpx;
  font-weight: 600;
  color: $foreground;
}

/* Stepper */
.stepper {
  display: flex;
  align-items: center;
  gap: 4rpx;
}
.stepper-btn {
  width: 52rpx; height: 52rpx;
  display: flex; align-items: center; justify-content: center;
  border-radius: 50%;
  &.disabled { opacity: 0.4; }
}
.stepper-minus { background: $muted; }
.stepper-plus {
  background: linear-gradient(135deg, $accent, $accent-secondary);
  box-shadow: 0 2rpx 8rpx rgba(16,185,129,0.25);
}
.stepper-count {
  min-width: 48rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 600;
  color: $foreground;
}
.stepper.small {
  .stepper-btn { width: 44rpx; height: 44rpx; }
  .stepper-count { min-width: 40rpx; font-size: 24rpx; }
}

.detail-footer {
  display: flex;
  gap: 16rpx;
}
.detail-view-full {
  flex: 1;
  text-align: center;
  padding: 22rpx;
  background: $muted;
  border-radius: 48rpx;
  font-size: 28rpx;
  color: $foreground;
}
.detail-cart-btn {
  flex: 2;
  text-align: center;
  padding: 22rpx;
  background: linear-gradient(135deg, $accent, $accent-secondary);
  color: $card;
  font-size: 28rpx;
  font-weight: 600;
  border-radius: 48rpx;
  box-shadow: 0 4rpx 16rpx rgba(16,185,129,0.3);
  &.disabled { opacity: 0.5; }
}

/* ===== Cart Modal ===== */
.cart-item {
  display: flex;
  align-items: center;
  padding: 16rpx 32rpx;
  gap: 16rpx;
  & + .cart-item { border-top: 1rpx solid $border; }
}
.cart-item-img {
  width: 120rpx; height: 120rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
}
.cart-item-info {
  flex: 1;
  min-width: 0;
}
.cart-item-name {
  font-size: 26rpx;
  color: $foreground;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6rpx;
}
.cart-item-del {
  padding: 12rpx;
  flex-shrink: 0;
}
.cart-summary {
  padding: 0;
}
.cart-total-row {
  display: flex;
  align-items: baseline;
  justify-content: flex-end;
  margin-bottom: 16rpx;
  font-size: 28rpx;
  color: $foreground;
}
.cart-total-amount {
  font-size: 40rpx;
  color: #EF4444;
  font-weight: 700;
  margin-left: 8rpx;
}
.cart-checkout-btn {
  text-align: center;
  padding: 24rpx;
  background: linear-gradient(135deg, $accent, $accent-secondary);
  color: $card;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: 48rpx;
  box-shadow: 0 4rpx 16rpx rgba(16,185,129,0.3);
}

/* ===== Checkout ===== */
.ck-section {
  padding: 16rpx 32rpx;
  & + .ck-section { border-top: 1rpx solid $border; }
}
.ck-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}
.ck-section-title-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.ck-section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $foreground;
  display: block;
  margin-bottom: 10rpx;
}
.ck-link {
  font-size: 26rpx;
  color: $accent;
}

.addr-item {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  padding: 16rpx;
  background: $muted;
  border-radius: 20rpx;
  margin-bottom: 10rpx;
  transition: all $duration-fast $ease-out;
  &.active { background: #ECFDF5; }
}
.addr-radio { padding-top: 6rpx; }
.radio-dot {
  width: 36rpx; height: 36rpx;
  border: 2rpx solid $border;
  border-radius: 50%;
  &.checked {
    border-color: $accent;
    background: $accent;
    position: relative;
    &::after {
      content: '';
      position: absolute;
      top: 50%; left: 50%;
      transform: translate(-50%, -50%);
      width: 16rpx; height: 16rpx;
      border-radius: 50%;
      background: $card;
    }
  }
}
.addr-body { flex: 1; min-width: 0; }
.addr-top {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 6rpx;
  flex-wrap: wrap;
}
.addr-name {
  font-size: 28rpx;
  font-weight: 600;
  color: $foreground;
}
.addr-phone {
  font-size: 26rpx;
  color: $muted-foreground;
}
.addr-default {
  font-size: 18rpx;
  color: $card;
  background: linear-gradient(135deg, $accent, $accent-secondary);
  padding: 2rpx 12rpx;
  border-radius: 100rpx;
}
.addr-detail {
  font-size: 24rpx;
  color: $muted-foreground;
  line-height: 1.4;
  display: block;
}
.addr-new-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 14rpx;
  font-size: 26rpx;
  color: $accent;
  border: 1rpx dashed $accent;
  border-radius: 20rpx;
}
.addr-back {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 26rpx;
  color: $accent;
  margin-bottom: 12rpx;
}

.form-group { margin-bottom: 12rpx; }
.form-input {
  width: 100%; max-width: 100%;
  height: 80rpx;
  border: 1rpx solid $border;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  background: $muted;
  box-sizing: border-box;
}
.form-textarea {
  width: 100%; max-width: 100%;
  height: 140rpx;
  border: 1rpx solid $border;
  border-radius: 16rpx;
  padding: 16rpx 24rpx;
  font-size: 28rpx;
  background: $muted;
  box-sizing: border-box;
}

.ck-item {
  display: flex;
  justify-content: space-between;
  padding: 10rpx 0;
}
.ck-item-name {
  font-size: 26rpx;
  color: $foreground;
  flex: 1;
}
.ck-item-price {
  font-size: 26rpx;
  color: $foreground;
}
.ck-total {
  display: flex;
  align-items: baseline;
  justify-content: flex-end;
  padding: 16rpx 32rpx;
  font-size: 28rpx;
  color: $foreground;
}
.ck-total-amount {
  font-size: 40rpx;
  color: #EF4444;
  font-weight: 700;
  margin-left: 8rpx;
}
.ck-alert {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin: 8rpx 32rpx 16rpx;
  padding: 12rpx 16rpx;
  background: #ECFDF5;
  border-radius: 16rpx;
  font-size: 24rpx;
  color: $accent-secondary;
}
.submit-btn {
  text-align: center;
  padding: 24rpx;
  background: linear-gradient(135deg, $accent, $accent-secondary);
  color: $card;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: 48rpx;
  box-shadow: 0 4rpx 16rpx rgba(16,185,129,0.3);
  &.disabled { opacity: 0.6; }
}

/* ===== Orders ===== */
.order-card {
  margin: 12rpx 24rpx;
  padding: 20rpx;
  background: $card;
  border-radius: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.order-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10rpx;
}
.order-no {
  font-size: 22rpx;
  color: $muted-foreground;
}
.order-status {
  font-size: 22rpx;
  padding: 4rpx 14rpx;
  border-radius: 100rpx;
  font-weight: 600;
  background: $muted;
  color: $muted-foreground;
  &.success { background: rgba(34,197,94,0.1); color: #16A34A; }
  &.warn { background: rgba(245,158,11,0.1); color: #D97706; }
  &.danger { background: rgba(239,68,68,0.1); color: #DC2626; }
  &.info { background: rgba(59,130,246,0.1); color: #2563EB; }
  &.muted { background: $muted; color: $muted-foreground; }
}
.order-products { margin-bottom: 10rpx; }
.order-prod {
  font-size: 26rpx;
  color: $foreground;
  padding: 4rpx 0;
}
.order-more {
  font-size: 24rpx;
  color: $muted-foreground;
}
.order-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 10rpx;
  border-top: 1rpx solid $border;
}
.order-money {
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.pay-status {
  font-size: 22rpx;
  padding: 2rpx 10rpx;
  border-radius: 100rpx;
  background: $muted;
  color: $muted-foreground;
  &.success { background: rgba(34,197,94,0.1); color: #16A34A; }
  &.warn { background: rgba(245,158,11,0.1); color: #D97706; }
  &.danger { background: rgba(239,68,68,0.1); color: #DC2626; }
}
.order-amount {
  font-size: 30rpx;
  font-weight: 700;
  color: #EF4444;
}
.order-actions {
  display: flex;
  gap: 8rpx;
}
.action-chip {
  font-size: 22rpx;
  padding: 8rpx 20rpx;
  border-radius: 100rpx;
  font-weight: 600;
  &.success { background: rgba(34,197,94,0.1); color: #16A34A; }
  &.warn { background: rgba(245,158,11,0.1); color: #D97706; }
  &.danger { background: rgba(239,68,68,0.1); color: #DC2626; }
}
.order-tracking {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 10rpx;
  padding-top: 8rpx;
  border-top: 1rpx solid $border;
  font-size: 22rpx;
  color: $muted-foreground;
}

/* ===== Result Popup ===== */
.result-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: $z-modal;
  display: flex;
  align-items: center;
  justify-content: center;
}
.result-popup {
  width: 560rpx;
  background: $card;
  border-radius: 32rpx;
  box-shadow: 0 8rpx 40rpx rgba(0,0,0,0.12);
  padding: 60rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.result-title {
  font-size: 36rpx;
  font-weight: 700;
  color: $foreground;
  margin-top: 20rpx;
  margin-bottom: 12rpx;
}
.result-desc {
  font-size: 26rpx;
  color: $muted-foreground;
  text-align: center;
  margin-bottom: 32rpx;
  word-break: break-all;
}
.result-close-btn {
  width: 100%;
  text-align: center;
  padding: 24rpx;
  background: linear-gradient(135deg, $accent, $accent-secondary);
  color: $card;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: 48rpx;
  box-shadow: 0 4rpx 16rpx rgba(16,185,129,0.3);
}
</style>
