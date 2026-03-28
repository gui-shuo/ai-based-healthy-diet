import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { request, setToken, setRefreshToken, clearTokens, getToken } from '@/utils/request'

interface UserInfo {
  id: number
  username: string
  nickname: string
  email: string
  avatar: string
  role: string
  status: string
  phone?: string
}

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo | null>(null)
  const isLoggedIn = computed(() => !!getToken() && !!userInfo.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  // 账号密码登录
  async function login(username: string, password: string, captchaKey?: string, captchaCode?: string) {
    const res = await request<any>({
      url: '/auth/login',
      method: 'POST',
      data: { username, password, captchaKey, captchaCode }
    })
    if (res.code === 200) {
      setToken(res.data.accessToken)
      setRefreshToken(res.data.refreshToken)
      userInfo.value = res.data.userInfo
      uni.setStorageSync('userInfo', res.data.userInfo)
    }
    return res
  }

  // 微信登录
  async function wxLogin(code: string) {
    const res = await request<any>({
      url: '/auth/wx-login',
      method: 'POST',
      data: { code }
    })
    if (res.code === 200) {
      setToken(res.data.accessToken)
      setRefreshToken(res.data.refreshToken)
      userInfo.value = res.data.userInfo
      uni.setStorageSync('userInfo', res.data.userInfo)
    }
    return res
  }

  // 注册
  async function register(data: any) {
    return request({ url: '/auth/register', method: 'POST', data })
  }

  // 登出
  function logout() {
    clearTokens()
    userInfo.value = null
    uni.removeStorageSync('userInfo')
    uni.reLaunch({ url: '/pages/auth/login' })
  }

  // 从缓存恢复状态
  function restore() {
    const cached = uni.getStorageSync('userInfo')
    if (cached && getToken()) {
      userInfo.value = cached
    }
  }

  // 刷新用户信息
  async function fetchUserInfo() {
    try {
      const res = await request<UserInfo>({ url: '/auth/me', showError: false })
      if (res.code === 200) {
        userInfo.value = res.data
        uni.setStorageSync('userInfo', res.data)
      }
    } catch {}
  }

  return { userInfo, isLoggedIn, isAdmin, login, wxLogin, register, logout, restore, fetchUserInfo }
})
