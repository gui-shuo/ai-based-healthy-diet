import api from './api'

// 用户提交反馈
export const submitFeedback = (data) => api.post('/feedback', data)

// 获取用户自己的反馈列表
export const getMyFeedbacks = (page = 0, size = 10) =>
  api.get('/feedback/my', { params: { page, size } })

// 管理员：获取所有反馈
export const getAllFeedbacks = (params = {}) =>
  api.get('/feedback/admin/list', { params })

// 管理员：回复反馈
export const replyFeedback = (id, reply, status) =>
  api.post(`/feedback/admin/${id}/reply`, { reply, status })

// 管理员：更新反馈状态
export const updateFeedbackStatus = (id, status) =>
  api.put(`/feedback/admin/${id}/status`, { status })

// 管理员：获取反馈统计
export const getFeedbackStats = () => api.get('/feedback/admin/stats')

// 反馈类型
export const FeedbackTypes = {
  BUG: { code: 'BUG', name: 'Bug报告', icon: '🐛', color: '#f56c6c' },
  FEATURE: { code: 'FEATURE', name: '功能建议', icon: '💡', color: '#409eff' },
  SUGGESTION: { code: 'SUGGESTION', name: '意见建议', icon: '📝', color: '#e6a23c' },
  OTHER: { code: 'OTHER', name: '其他', icon: '📋', color: '#909399' }
}

// 反馈状态
export const FeedbackStatus = {
  PENDING: { code: 'PENDING', name: '待处理', color: '#e6a23c', type: 'warning' },
  PROCESSING: { code: 'PROCESSING', name: '处理中', color: '#409eff', type: 'primary' },
  RESOLVED: { code: 'RESOLVED', name: '已解决', color: '#67c23a', type: 'success' },
  CLOSED: { code: 'CLOSED', name: '已关闭', color: '#909399', type: 'info' }
}
