import api from './api'

// 帖子
export const getFeed = (params = {}) => api.get('/community/feed', { params })
export const getPostDetail = (id) => api.get(`/community/posts/${id}`)
export const getUserPosts = (userId, params = {}) => api.get(`/community/user/${userId}/posts`, { params })
export const createPost = (data) => api.post('/community/posts', data)
export const deletePost = (id) => api.delete(`/community/posts/${id}`)

// 媒体上传
export const uploadMedia = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return api.post('/community/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  })
}

// 评论
export const getComments = (postId, params = {}) => api.get(`/community/posts/${postId}/comments`, { params })
export const addComment = (postId, data) => api.post(`/community/posts/${postId}/comments`, data)
export const deleteComment = (id) => api.delete(`/community/comments/${id}`)

// 点赞
export const toggleLike = (data) => api.post('/community/like', data)

// 关注
export const toggleFollow = (userId) => api.post(`/community/follow/${userId}`)
export const getFollowStatus = (userId) => api.get(`/community/follow/${userId}/status`)

// 管理后台
export const adminGetPosts = (params = {}) => api.get('/community/admin/posts', { params })
export const adminTogglePin = (id) => api.put(`/community/admin/posts/${id}/pin`)
export const adminUpdateStatus = (id, status) => api.put(`/community/admin/posts/${id}/status`, { status })
export const adminDeletePost = (id) => api.delete(`/community/admin/posts/${id}`)
export const adminDeleteComment = (id) => api.delete(`/community/admin/comments/${id}`)
export const adminGetStats = () => api.get('/community/admin/stats')

export const PostCategories = [
  { code: '饮食打卡', name: '饮食打卡', icon: '📸', color: '#67c23a' },
  { code: '食谱分享', name: '食谱分享', icon: '🍳', color: '#e6a23c' },
  { code: '健身日记', name: '健身日记', icon: '💪', color: '#409eff' },
  { code: '营养知识', name: '营养知识', icon: '📚', color: '#9b59b6' },
  { code: '减脂心得', name: '减脂心得', icon: '🔥', color: '#f56c6c' },
  { code: '问答交流', name: '问答交流', icon: '💬', color: '#00bcd4' }
]
