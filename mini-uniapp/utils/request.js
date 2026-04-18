/**
 * NutriAI 请求封装
 * - JWT双Token认证
 * - 自动刷新Token
 * - 统一错误处理
 */

const BASE_URL = 'https://nutriai.itshuo.me/api'

// Token刷新锁，防止并发刷新
let isRefreshing = false
let refreshSubscribers = []

function onTokenRefreshed(newToken) {
  refreshSubscribers.forEach(cb => cb(newToken))
  refreshSubscribers = []
}

function addRefreshSubscriber(callback) {
  refreshSubscribers.push(callback)
}

/**
 * 刷新Token
 */
async function refreshToken() {
  const refreshTokenStr = uni.getStorageSync('refreshToken')
  if (!refreshTokenStr) {
    throw new Error('No refresh token')
  }

  const res = await uni.request({
    url: `${BASE_URL}/auth/refresh`,
    method: 'POST',
    data: { refreshToken: refreshTokenStr },
    header: { 'Content-Type': 'application/json' }
  })

  if (res.statusCode === 200 && res.data?.data) {
    const { accessToken, refreshToken: newRefreshToken } = res.data.data
    uni.setStorageSync('accessToken', accessToken)
    if (newRefreshToken) {
      uni.setStorageSync('refreshToken', newRefreshToken)
    }
    return accessToken
  }
  throw new Error('Token refresh failed')
}

/**
 * 统一请求方法
 */
function request(options = {}) {
  const {
    url,
    method = 'GET',
    data,
    header = {},
    showLoading = false,
    loadingText = '加载中...',
    showError = true,
    timeout = 15000
  } = options

  return new Promise((resolve, reject) => {
    if (showLoading) {
      uni.showLoading({ title: loadingText, mask: true })
    }

    const token = uni.getStorageSync('accessToken')
    const requestHeader = {
      'Content-Type': 'application/json',
      ...header
    }
    if (token) {
      requestHeader['Authorization'] = `Bearer ${token}`
    }

    uni.request({
      url: url.startsWith('http') ? url : `${BASE_URL}${url}`,
      method,
      data,
      header: requestHeader,
      timeout,
      success: async (res) => {
        if (showLoading) uni.hideLoading()

        // 成功
        if (res.statusCode >= 200 && res.statusCode < 300) {
          // Auto-unwrap ApiResponse { code, message, data }
          const body = res.data
          if (body && typeof body === 'object' && !Array.isArray(body) && 'code' in body && 'data' in body) {
            resolve(body.data)
          } else {
            resolve(body)
          }
          return
        }

        // 401 - Token过期，尝试刷新
        if (res.statusCode === 401) {
          if (!isRefreshing) {
            isRefreshing = true
            try {
              const newToken = await refreshToken()
              isRefreshing = false
              onTokenRefreshed(newToken)
              // 重试原请求
              requestHeader['Authorization'] = `Bearer ${newToken}`
              uni.request({
                url: url.startsWith('http') ? url : `${BASE_URL}${url}`,
                method,
                data,
                header: requestHeader,
                timeout,
                success: (retryRes) => {
                  if (retryRes.statusCode >= 200 && retryRes.statusCode < 300) {
                    resolve(retryRes.data)
                  } else {
                    handleError(retryRes, showError, reject)
                  }
                },
                fail: (err) => {
                  handleNetworkError(err, showError, reject)
                }
              })
            } catch (err) {
              isRefreshing = false
              refreshSubscribers = []
              // 刷新失败，清除登录态
              clearAuth()
              if (showError) {
                uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
              }
              reject({ code: 401, message: '登录已过期' })
            }
          } else {
            // 等待Token刷新
            return new Promise((retryResolve, retryReject) => {
              addRefreshSubscriber((newToken) => {
                requestHeader['Authorization'] = `Bearer ${newToken}`
                uni.request({
                  url: url.startsWith('http') ? url : `${BASE_URL}${url}`,
                  method,
                  data,
                  header: requestHeader,
                  timeout,
                  success: (retryRes) => {
                    if (retryRes.statusCode >= 200 && retryRes.statusCode < 300) {
                      retryResolve(retryRes.data)
                    } else {
                      handleError(retryRes, showError, retryReject)
                    }
                  },
                  fail: (err) => {
                    handleNetworkError(err, showError, retryReject)
                  }
                })
              })
            }).then(resolve).catch(reject)
          }
          return
        }

        // 其他错误
        handleError(res, showError, reject)
      },
      fail: (err) => {
        if (showLoading) uni.hideLoading()
        handleNetworkError(err, showError, reject)
      }
    })
  })
}

function handleError(res, showError, reject) {
  const message = res.data?.message || res.data?.error || `请求失败(${res.statusCode})`
  if (showError) {
    uni.showToast({ title: message, icon: 'none' })
  }
  reject({ code: res.statusCode, message, data: res.data })
}

function handleNetworkError(err, showError, reject) {
  const message = '网络连接失败，请检查网络'
  if (showError) {
    uni.showToast({ title: message, icon: 'none' })
  }
  reject({ code: -1, message, error: err })
}

function clearAuth() {
  uni.removeStorageSync('accessToken')
  uni.removeStorageSync('refreshToken')
  uni.removeStorageSync('userInfo')
}

/**
 * 文件上传
 */
function uploadFile(options = {}) {
  const {
    url,
    filePath,
    name = 'file',
    formData = {},
    showLoading = true
  } = options

  return new Promise((resolve, reject) => {
    if (showLoading) {
      uni.showLoading({ title: '上传中...', mask: true })
    }

    const token = uni.getStorageSync('accessToken')
    const header = {}
    if (token) {
      header['Authorization'] = `Bearer ${token}`
    }

    uni.uploadFile({
      url: url.startsWith('http') ? url : `${BASE_URL}${url}`,
      filePath,
      name,
      formData,
      header,
      success: (res) => {
        if (showLoading) uni.hideLoading()
        if (res.statusCode >= 200 && res.statusCode < 300) {
          try {
            const data = JSON.parse(res.data)
            resolve(data)
          } catch (e) {
            resolve(res.data)
          }
        } else {
          reject({ code: res.statusCode, message: '上传失败' })
        }
      },
      fail: (err) => {
        if (showLoading) uni.hideLoading()
        reject({ code: -1, message: '上传失败', error: err })
      }
    })
  })
}

// 快捷方法
const get = (url, data, options = {}) => request({ url, method: 'GET', data, ...options })
const post = (url, data, options = {}) => request({ url, method: 'POST', data, ...options })
const put = (url, data, options = {}) => request({ url, method: 'PUT', data, ...options })
const del = (url, data, options = {}) => request({ url, method: 'DELETE', data, ...options })

export { request, get, post, put, del, uploadFile, BASE_URL }
export default { request, get, post, put, del, uploadFile }
