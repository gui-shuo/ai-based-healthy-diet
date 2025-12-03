import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

// 创建axios实例
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 是否正在刷新token
let isRefreshing = false
// 重试队列
let requests = []

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 添加请求ID
    config.headers['X-Request-ID'] = `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`

    // 添加认证token
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers['Authorization'] = `Bearer ${authStore.token}`
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response
  },
  async (error) => {
    const config = error.config

    // 401错误，token过期
    if (error.response?.status === 401) {
      if (!isRefreshing) {
        isRefreshing = true
        const authStore = useAuthStore()

        try {
          const refreshed = await authStore.refreshAccessToken()
          if (refreshed) {
            requests.forEach(cb => cb(authStore.token))
            requests = []
            return api.request(config)
          }
        } catch (e) {
          authStore.logout()
          router.push('/login')
        } finally {
          isRefreshing = false
        }
      }

      return new Promise(resolve => {
        requests.push(token => {
          config.headers['Authorization'] = `Bearer ${token}`
          resolve(api.request(config))
        })
      })
    }

    return Promise.reject(error)
  }
)

export default api
