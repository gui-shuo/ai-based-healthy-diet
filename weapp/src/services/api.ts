// ========================================
// NutriAI API Service Layer
// ========================================
import { request, uploadFile } from '../utils/request'

// ==================== Auth ====================
export const authApi = {
  login(data: { username: string; password: string; captchaKey?: string; captchaCode?: string }) {
    return request({ url: '/auth/login', method: 'POST', data })
  },
  register(data: { username: string; email: string; password: string; verificationCode: string }) {
    return request({ url: '/auth/register', method: 'POST', data })
  },
  wxLogin(code: string) {
    return request({ url: '/auth/wx-login', method: 'POST', data: { code } })
  },
  logout() {
    return request({ url: '/auth/logout', method: 'POST', showError: false })
  },
  getCaptcha() {
    return request({ url: '/auth/captcha' })
  },
  sendEmailCode(email: string) {
    return request({ url: '/auth/send-code', method: 'POST', data: { email } })
  },
  checkUsername(username: string) {
    return request({ url: `/auth/check-username?username=${username}` })
  },
  checkEmail(email: string) {
    return request({ url: `/auth/check-email?email=${email}` })
  },
  forgotPassword(data: { email: string; code: string; newPassword: string }) {
    return request({ url: '/auth/forgot-password', method: 'POST', data })
  }
}

// ==================== User ====================
export const userApi = {
  getProfile() {
    return request({ url: '/user/profile' })
  },
  updateProfile(data: any) {
    return request({ url: '/user/profile', method: 'PUT', data })
  },
  uploadAvatar(filePath: string) {
    return uploadFile({ url: '/user/avatar', filePath, name: 'avatar' })
  },
  changePassword(data: { oldPassword: string; newPassword: string }) {
    return request({ url: '/user/password', method: 'PUT', data })
  }
}

// ==================== Products (Shop) ====================
export const productApi = {
  getList(params?: { category?: string; page?: number; size?: number }) {
    const query = new URLSearchParams()
    if (params?.category) query.append('category', params.category)
    if (params?.page !== undefined) query.append('page', String(params.page))
    if (params?.size) query.append('size', String(params.size))
    const qs = query.toString()
    return request({ url: `/products${qs ? '?' + qs : ''}` })
  },
  search(keyword: string, page = 0, size = 10) {
    return request({ url: `/products/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}` })
  },
  getRecommended() {
    return request({ url: '/products/recommended' })
  },
  getDetail(id: number) {
    return request({ url: `/products/${id}` })
  },
  getCategories() {
    return request({ url: '/products/categories' })
  },
  createOrder(data: {
    items: Array<{ productId: number; quantity: number; price: number }>
    receiverName: string
    receiverPhone: string
    receiverAddress: string
    remark?: string
  }) {
    return request({ url: '/products/orders', method: 'POST', data })
  },
  getOrders(params?: { page?: number; size?: number; status?: string }) {
    const query = new URLSearchParams()
    if (params?.page !== undefined) query.append('page', String(params.page))
    if (params?.size) query.append('size', String(params.size))
    if (params?.status) query.append('status', String(params.status))
    const qs = query.toString()
    return request({ url: `/products/orders${qs ? '?' + qs : ''}` })
  },
  getOrderDetail(orderNo: string) {
    return request({ url: `/products/orders/${orderNo}` })
  },
  payOrder(orderNo: string) {
    return request({ url: `/products/orders/${orderNo}/simulate-pay`, method: 'POST' })
  },
  confirmReceive(orderNo: string) {
    return request({ url: `/products/orders/${orderNo}/confirm-receive`, method: 'POST' })
  },
  refundOrder(orderNo: string) {
    return request({ url: `/products/orders/${orderNo}/simulate-refund`, method: 'POST' })
  }
}

// ==================== Community ====================
export const communityApi = {
  getFeed(params?: { category?: string; page?: number; size?: number }) {
    const query = new URLSearchParams()
    if (params?.category) query.append('category', params.category)
    if (params?.page !== undefined) query.append('page', String(params.page))
    if (params?.size) query.append('size', String(params.size))
    const qs = query.toString()
    return request({ url: `/community/feed${qs ? '?' + qs : ''}` })
  },
  getPost(id: number) {
    return request({ url: `/community/posts/${id}` })
  },
  getUserPosts(userId: number, params?: { page?: number; size?: number }) {
    const query = new URLSearchParams()
    if (params?.page !== undefined) query.append('page', String(params.page))
    if (params?.size) query.append('size', String(params.size))
    const qs = query.toString()
    return request({ url: `/community/user/${userId}/posts${qs ? '?' + qs : ''}` })
  },
  createPost(data: { content: string; category: string; images?: string[]; videoUrl?: string }) {
    return request({ url: '/community/posts', method: 'POST', data })
  },
  deletePost(id: number) {
    return request({ url: `/community/posts/${id}`, method: 'DELETE' })
  },
  uploadMedia(filePath: string) {
    return uploadFile({ url: '/community/upload', filePath, name: 'file' })
  },
  getComments(postId: number, params?: { page?: number; size?: number }) {
    const query = new URLSearchParams()
    if (params?.page !== undefined) query.append('page', String(params.page))
    if (params?.size) query.append('size', String(params.size))
    const qs = query.toString()
    return request({ url: `/community/posts/${postId}/comments${qs ? '?' + qs : ''}` })
  },
  addComment(postId: number, data: { content: string }) {
    return request({ url: `/community/posts/${postId}/comments`, method: 'POST', data })
  },
  deleteComment(id: number) {
    return request({ url: `/community/comments/${id}`, method: 'DELETE' })
  },
  toggleLike(data: { targetId: number; targetType: 'POST' | 'COMMENT' }) {
    return request({ url: '/community/like', method: 'POST', data })
  },
  toggleFollow(userId: number) {
    return request({ url: `/community/follow/${userId}`, method: 'POST' })
  },
  getFollowStatus(userId: number) {
    return request({ url: `/community/follow/${userId}/status` })
  }
}

// ==================== Address ====================
export const addressApi = {
  getList() {
    return request({ url: '/addresses' })
  },
  add(data: {
    receiverName: string
    receiverPhone: string
    province: string
    city: string
    district: string
    detailAddress: string
    label?: string
    isDefault?: boolean
  }) {
    return request({ url: '/addresses', method: 'POST', data })
  },
  update(id: number, data: any) {
    return request({ url: `/addresses/${id}`, method: 'PUT', data })
  },
  remove(id: number) {
    return request({ url: `/addresses/${id}`, method: 'DELETE' })
  },
  setDefault(id: number) {
    return request({ url: `/addresses/${id}/default`, method: 'PUT' })
  }
}

// ==================== Member / VIP ====================
export const memberApi = {
  getInfo() {
    return request({ url: '/member/info' })
  },
  getPermissions() {
    return request({ url: '/member/permissions' })
  },
  signIn() {
    return request({ url: '/member/sign-in', method: 'POST' })
  },
  getSignInCalendar(params?: { year?: number; month?: number }) {
    const query = new URLSearchParams()
    if (params?.year) query.append('year', String(params.year))
    if (params?.month) query.append('month', String(params.month))
    const qs = query.toString()
    return request({ url: `/member/sign-in/calendar${qs ? '?' + qs : ''}` })
  }
}

export const vipApi = {
  getPlans() {
    return request({ url: '/vip/plans' })
  },
  createOrder(data: { planId: number; payType: string }) {
    return request({ url: '/vip/orders', method: 'POST', data })
  },
  simulatePay(orderNo: string) {
    return request({ url: `/vip/orders/${orderNo}/simulate-pay`, method: 'POST' })
  },
  getStatus() {
    return request({ url: '/vip/status' })
  }
}

// ==================== Announcements ====================
export const announcementApi = {
  getList(params?: { page?: number; size?: number }) {
    const query = new URLSearchParams()
    if (params?.page !== undefined) query.append('page', String(params.page))
    if (params?.size) query.append('size', String(params.size))
    const qs = query.toString()
    return request({ url: `/announcements${qs ? '?' + qs : ''}` })
  },
  getUnreadCount() {
    return request({ url: '/announcements/unread-count' })
  },
  markRead(id: number) {
    return request({ url: `/announcements/${id}/read`, method: 'POST' })
  }
}

// ==================== Feedback ====================
export const feedbackApi = {
  submit(data: { type: string; content: string; contact?: string }) {
    return request({ url: '/feedback', method: 'POST', data })
  }
}

// ==================== Public Config ====================
export const configApi = {
  getPublicConfig() {
    return request({ url: '/public/config', showError: false })
  }
}

// ==================== Constants ====================
export const POST_CATEGORIES = [
  { key: '', label: '全部', icon: '🔥' },
  { key: '饮食打卡', label: '饮食打卡', icon: '🍽️' },
  { key: '食谱分享', label: '食谱分享', icon: '📝' },
  { key: '健身日记', label: '健身日记', icon: '💪' },
  { key: '营养知识', label: '营养知识', icon: '📚' },
  { key: '减脂心得', label: '减脂心得', icon: '🏃' },
  { key: '问答交流', label: '问答交流', icon: '❓' }
]

export const MEAL_TYPES = [
  { key: 'breakfast', label: '早餐', icon: '🌅' },
  { key: 'lunch', label: '午餐', icon: '☀️' },
  { key: 'dinner', label: '晚餐', icon: '🌙' },
  { key: 'snack', label: '加餐', icon: '🍎' }
]

export const PRODUCT_CATEGORIES = [
  { key: '', label: '全部' },
  { key: '抗炎套餐', label: '抗炎套餐' },
  { key: '营养套餐', label: '营养套餐' },
  { key: '蛋白粉', label: '蛋白粉' },
  { key: '维生素', label: '维生素' },
  { key: '膳食补充剂', label: '膳食补充剂' },
  { key: '有机食品', label: '有机食品' },
  { key: '营养食品', label: '营养食品' }
]

// ==================== Admin ====================

// 管理后台 - 仪表盘
export const adminDashboardApi = {
  getStats() {
    return request({ url: '/admin/dashboard/stats' })
  },
  getUserGrowth(days = 7) {
    return request({ url: `/admin/dashboard/user-growth?days=${days}` })
  },
  getOverview() {
    return request({ url: '/admin/dashboard/overview' })
  }
}

// 管理后台 - 营养餐管理
export const adminMealApi = {
  getList(params: { page?: number; size?: number; keyword?: string; type?: string }) {
    const query = new URLSearchParams()
    if (params.page) query.append('page', String(params.page - 1))
    if (params.size) query.append('size', String(params.size))
    if (params.keyword) query.append('keyword', params.keyword)
    if (params.type) query.append('type', params.type)
    return request({ url: `/admin/meal-plans?${query}` })
  },
  getDetail(id: number) {
    return request({ url: `/admin/meal-plans/${id}` })
  },
  create(data: any) {
    return request({ url: '/admin/meal-plans', method: 'POST', data })
  },
  update(id: number, data: any) {
    return request({ url: `/admin/meal-plans/${id}`, method: 'PUT', data })
  },
  remove(id: number) {
    return request({ url: `/admin/meal-plans/${id}`, method: 'DELETE' })
  },
  toggleActive(id: number) {
    return request({ url: `/admin/meal-plans/${id}/toggle-active`, method: 'PUT' })
  },
  toggleFeatured(id: number) {
    return request({ url: `/admin/meal-plans/${id}/toggle-featured`, method: 'PUT' })
  }
}

// 管理后台 - 产品管理
export const adminProductApi = {
  getList(params: { page?: number; size?: number; keyword?: string; category?: string }) {
    const query = new URLSearchParams()
    if (params.page) query.append('page', String(params.page - 1))
    if (params.size) query.append('size', String(params.size))
    if (params.keyword) query.append('keyword', params.keyword)
    if (params.category) query.append('category', params.category)
    return request({ url: `/admin/products?${query}` })
  },
  getDetail(id: number) {
    return request({ url: `/admin/products/${id}` })
  },
  create(data: any) {
    return request({ url: '/admin/products', method: 'POST', data })
  },
  update(id: number, data: any) {
    return request({ url: `/admin/products/${id}`, method: 'PUT', data })
  },
  remove(id: number) {
    return request({ url: `/admin/products/${id}`, method: 'DELETE' })
  },
  updateStatus(id: number, status: string) {
    return request({ url: `/admin/products/${id}/status?status=${status}`, method: 'PUT' })
  }
}

// 管理后台 - 订单管理
export const adminOrderApi = {
  getList(params: { page?: number; size?: number; status?: string; orderNo?: string }) {
    const query = new URLSearchParams()
    if (params.page) query.append('page', String(params.page - 1))
    if (params.size) query.append('size', String(params.size))
    if (params.status) query.append('status', params.status)
    if (params.orderNo) query.append('orderNo', params.orderNo)
    return request({ url: `/admin/products/orders?${query}` })
  },
  getDetail(orderNo: string) {
    return request({ url: `/admin/products/orders/${orderNo}` })
  },
  updateStatus(orderNo: string, status: string) {
    return request({ url: `/admin/products/orders/${orderNo}/status?status=${status}`, method: 'PUT' })
  },
  ship(orderNo: string, data: { trackingNo?: string; carrier?: string }) {
    return request({ url: `/admin/products/orders/${orderNo}/ship`, method: 'PUT', data })
  }
}

// 管理后台 - 社区管理
export const adminCommunityApi = {
  getPosts(params: { page?: number; size?: number; status?: string; category?: string }) {
    const query = new URLSearchParams()
    if (params.page) query.append('page', String(params.page - 1))
    if (params.size) query.append('size', String(params.size))
    if (params.status) query.append('status', params.status)
    if (params.category) query.append('category', params.category)
    return request({ url: `/community/admin/posts?${query}` })
  },
  getStats() {
    return request({ url: '/community/admin/stats' })
  },
  togglePin(id: number) {
    return request({ url: `/community/admin/posts/${id}/pin`, method: 'PUT' })
  },
  updateStatus(id: number, status: string) {
    return request({ url: `/community/admin/posts/${id}/status?status=${status}`, method: 'PUT' })
  },
  deletePost(id: number) {
    return request({ url: `/community/admin/posts/${id}`, method: 'DELETE' })
  },
  deleteComment(id: number) {
    return request({ url: `/community/admin/comments/${id}`, method: 'DELETE' })
  }
}

// 管理后台 - 公告管理
export const adminAnnouncementApi = {
  getList() {
    return request({ url: '/admin/announcements' })
  },
  create(data: any) {
    return request({ url: '/admin/announcements', method: 'POST', data })
  },
  update(id: number, data: any) {
    return request({ url: `/admin/announcements/${id}`, method: 'PUT', data })
  },
  remove(id: number) {
    return request({ url: `/admin/announcements/${id}`, method: 'DELETE' })
  }
}

// 管理后台 - 反馈管理
export const adminFeedbackApi = {
  getList(params: { page?: number; size?: number; status?: string; type?: string }) {
    const query = new URLSearchParams()
    if (params.page) query.append('page', String(params.page - 1))
    if (params.size) query.append('size', String(params.size))
    if (params.status) query.append('status', params.status)
    if (params.type) query.append('type', params.type)
    return request({ url: `/feedback/admin/list?${query}` })
  },
  getStats() {
    return request({ url: '/feedback/admin/stats' })
  },
  reply(id: number, content: string) {
    return request({ url: `/feedback/admin/${id}/reply`, method: 'POST', data: { content } })
  },
  updateStatus(id: number, status: string) {
    return request({ url: `/feedback/admin/${id}/status?status=${status}`, method: 'PUT' })
  }
}
