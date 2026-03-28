import api from './api'

/**
 * 会员服务API
 */

/** 获取会员信息 */
export const getMemberInfo = () => api.get('/member/info')

/** 获取会员权限概要（AI配额、VIP状态、功能权限等） */
export const getMemberPermissions = () => api.get('/member/permissions')

/**
 * 每日签到（返回获得成长值，0表示已签到）
 */
export const dailySignIn = () => api.post('/member/sign-in')

/**
 * 获取本月签到日期列表（日数字数组，如 [1,5,8]）
 */
export const getSignInCalendar = () => api.get('/member/sign-in/calendar')

/**
 * 获取成长值记录（分页）
 * @param {number} page
 * @param {number} size
 */
export const getGrowthRecords = (page = 0, size = 200) =>
  api.get('/member/growth-records', { params: { page, size } })

/** 生成邀请链接 */
export const generateInvitationLink = () => api.get('/member/invitation/generate')

/**
 * 查询邀请记录（分页）
 * @param {number} page
 * @param {number} size
 */
export const getInvitationRecords = (page = 0, size = 10) =>
  api.get('/member/invitation/records', { params: { page, size } })

// ========== VIP充值 ==========

/** 获取所有 VIP 套餐 */
export const getVipPlans = () => api.get('/vip/plans')

/**
 * 创建充值订单
 * @param {number} planId 套餐ID
 * @param {string} payType 支付方式：alipay/wxpay/qqpay
 */
export const createVipOrder = (planId, payType = 'alipay') =>
  api.post('/vip/orders', { planId, payType })

/**
 * 模拟支付确认
 * @param {string} orderNo
 */
export const simulateVipPayment = orderNo => api.post(`/vip/orders/${orderNo}/simulate-pay`)

/**
 * 模拟退款
 * @param {string} orderNo
 */
export const simulateVipRefund = orderNo => api.post(`/vip/orders/${orderNo}/simulate-refund`)

/**
 * 查询订单支付状态（前端轮询）
 * @param {string} orderNo
 */
export const queryVipOrderStatus = orderNo => api.get(`/vip/orders/${orderNo}/status`)

/** 获取当前用户 VIP 状态 */
export const getVipStatus = () => api.get('/vip/status')

/**
 * 获取订单历史
 * @param {number} page
 * @param {number} size
 */
export const getVipOrderHistory = (page = 0, size = 10) =>
  api.get('/vip/orders', { params: { page, size } })

// ========== 常量 ==========

export const VipPlanBadges = {
  TRIAL: { name: '体验卡', color: '#67c23a' },
  MONTHLY: { name: '月卡', color: '#409eff' },
  QUARTERLY: { name: '季卡', color: '#e6a23c' },
  YEARLY: { name: '年卡', color: '#f56c6c' },
  LIFETIME: { name: '终身卡', color: '#764ba2' }
}
