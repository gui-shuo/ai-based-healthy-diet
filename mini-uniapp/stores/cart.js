/**
 * 购物车Store
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { cartApi } from '../services/api'

export const useCartStore = defineStore('cart', () => {
  // State
  const items = ref([])
  const loading = ref(false)

  // Getters
  const totalCount = computed(() => {
    return items.value.reduce((sum, item) => {
      if (item.selected) return sum + item.quantity
      return sum
    }, 0)
  })

  const totalPrice = computed(() => {
    return items.value.reduce((sum, item) => {
      if (item.selected) return sum + item.price * item.quantity
      return sum
    }, 0)
  })

  const selectedItems = computed(() => items.value.filter(item => item.selected))
  const isAllSelected = computed(() => items.value.length > 0 && items.value.every(item => item.selected))
  const isEmpty = computed(() => items.value.length === 0)

  // Actions
  async function fetchCart() {
    loading.value = true
    try {
      const res = await cartApi.getCart()
      // Cart API returns direct array (no ApiResponse wrapper)
      const list = Array.isArray(res) ? res : (res || [])
      items.value = list.map(item => ({
        ...item,
        image: item.imageUrl || item.image,
        price: item.price || item.salePrice,
      }))
    } catch (e) {
      console.error('获取购物车失败', e)
    } finally {
      loading.value = false
    }
  }

  async function addItem({ itemType, itemId, quantity }) {
    try {
      await cartApi.addItem({ itemType: itemType || 'PRODUCT', itemId, quantity: quantity || 1 })
      await fetchCart()
      uni.showToast({ title: '已加入购物车', icon: 'success' })
    } catch (e) {
      uni.showToast({ title: e.message || '加入失败', icon: 'none' })
    }
  }

  async function updateQuantity(id, quantity) {
    if (quantity < 1) return
    try {
      await cartApi.updateQuantity(id, quantity)
      const item = items.value.find(i => i.id === id)
      if (item) item.quantity = quantity
    } catch (e) {
      console.error('更新数量失败', e)
    }
  }

  async function toggleSelect(id) {
    const item = items.value.find(i => i.id === id)
    if (!item) return
    try {
      const newSelected = !item.selected
      await cartApi.toggleSelect(id, newSelected)
      item.selected = newSelected
    } catch (e) {
      console.error('切换选择失败', e)
    }
  }

  async function toggleSelectAll() {
    const newState = !isAllSelected.value
    try {
      await cartApi.selectAll(newState)
      items.value.forEach(item => { item.selected = newState })
    } catch (e) {
      console.error('全选切换失败', e)
    }
  }

  async function removeItem(id) {
    try {
      await cartApi.removeItem(id)
      items.value = items.value.filter(i => i.id !== id)
    } catch (e) {
      console.error('删除失败', e)
    }
  }

  async function clearCart() {
    try {
      await cartApi.clearCart()
      items.value = []
    } catch (e) {
      console.error('清空购物车失败', e)
    }
  }

  return {
    items,
    loading,
    totalCount,
    totalPrice,
    selectedItems,
    isAllSelected,
    isEmpty,
    fetchCart,
    addItem,
    updateQuantity,
    toggleSelect,
    toggleSelectAll,
    removeItem,
    clearCart,
  }
})
