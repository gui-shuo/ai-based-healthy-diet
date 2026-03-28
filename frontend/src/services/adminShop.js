import api from './api'

// ==================== 营养师管理 ====================
export const getAdminNutritionists = (params = {}) =>
  api.get('/admin/nutritionists', { params })

export const getAdminNutritionist = (id) =>
  api.get(`/admin/nutritionists/${id}`)

export const createNutritionist = (data) =>
  api.post('/admin/nutritionists', data)

export const updateNutritionist = (id, data) =>
  api.put(`/admin/nutritionists/${id}`, data)

export const deleteNutritionist = (id) =>
  api.delete(`/admin/nutritionists/${id}`)

export const updateNutritionistStatus = (id, data) =>
  api.put(`/admin/nutritionists/${id}/status`, data)

// ==================== 产品管理 ====================
export const getAdminProducts = (params = {}) =>
  api.get('/admin/products', { params })

export const getAdminProduct = (id) =>
  api.get(`/admin/products/${id}`)

export const createProduct = (data) =>
  api.post('/admin/products', data)

export const updateProduct = (id, data) =>
  api.put(`/admin/products/${id}`, data)

export const deleteProduct = (id) =>
  api.delete(`/admin/products/${id}`)

export const updateProductStatus = (id, status) =>
  api.put(`/admin/products/${id}/status`, { status })

// 营养师职称选项
export const NutritionistTitles = [
  '初级营养师', '中级营养师', '高级营养师', '注册营养师'
]

// 营养师状态
export const NutritionistStatuses = {
  ONLINE: { label: '在线', type: 'success' },
  OFFLINE: { label: '离线', type: 'info' },
  BUSY: { label: '忙碌', type: 'warning' }
}

// 产品分类
export const ProductCategories = {
  SUPPLEMENT: '营养补充剂',
  PROTEIN: '蛋白质产品',
  VITAMIN: '维生素矿物质',
  ORGANIC: '有机食品',
  HEALTH_FOOD: '健康食品',
  EQUIPMENT: '健康设备'
}

// 产品状态
export const ProductStatuses = {
  ACTIVE: { label: '在售', type: 'success' },
  INACTIVE: { label: '下架', type: 'info' },
  SOLD_OUT: { label: '售罄', type: 'danger' }
}
