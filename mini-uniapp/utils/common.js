/**
 * 通用工具函数
 */

/**
 * 检查登录状态，未登录则跳转登录页
 */
export function checkLogin() {
  const token = uni.getStorageSync('accessToken')
  if (!token) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return false
  }
  return true
}

/**
 * 格式化时间
 */
export function formatTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

/**
 * 格式化日期 YYYY-MM-DD
 */
export function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

/**
 * 格式化价格 (分 -> 元)
 */
export function formatPrice(price) {
  if (price === null || price === undefined) return '0.00'
  return (Number(price)).toFixed(2)
}

/**
 * 截断文本
 */
export function truncate(text, length = 50) {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

/**
 * 默认头像
 */
export function defaultAvatar(avatar) {
  return avatar || '/static/images/default-avatar.png'
}

/**
 * 防抖
 */
export function debounce(fn, delay = 300) {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
      timer = null
    }, delay)
  }
}

/**
 * 节流
 */
export function throttle(fn, delay = 300) {
  let lastTime = 0
  return function (...args) {
    const now = Date.now()
    if (now - lastTime >= delay) {
      fn.apply(this, args)
      lastTime = now
    }
  }
}

/**
 * COS图片URL处理 (加上域名前缀)
 */
export function cosUrl(path) {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `https://cos.itshuo.me/${path}`
}
