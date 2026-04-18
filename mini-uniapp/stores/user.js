/**
 * 用户Store - 认证状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, userApi, memberApi } from '../services/api'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(uni.getStorageSync('accessToken') || '')
  const refreshTokenStr = ref(uni.getStorageSync('refreshToken') || '')
  const userInfo = ref(JSON.parse(uni.getStorageSync('userInfo') || 'null'))
  const permissions = ref(null)

  // Getters
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)
  const userName = computed(() => userInfo.value?.nickname || userInfo.value?.username || '用户')
  const userAvatar = computed(() => {
    const avatar = userInfo.value?.avatar
    if (!avatar) return '/static/images/default-avatar.png'
    if (avatar.startsWith('http')) return avatar
    return `https://cos.itshuo.me/${avatar}`
  })
  const userRole = computed(() => userInfo.value?.role || 'USER')
  const isVip = computed(() => permissions.value?.isVip || false)
  const memberLevel = computed(() => permissions.value?.memberLevel || 'NORMAL')

  // Actions
  async function login(loginData) {
    const res = await authApi.login(loginData)
    if (res) {
      setAuth(res)
      await fetchProfile()
    }
    return res
  }

  async function wxLogin(code) {
    const res = await authApi.wxLogin({ code })
    if (res) {
      setAuth(res)
      await fetchProfile()
    }
    return res
  }

  function setAuth(data) {
    token.value = data.accessToken
    refreshTokenStr.value = data.refreshToken
    uni.setStorageSync('accessToken', data.accessToken)
    uni.setStorageSync('refreshToken', data.refreshToken)
    if (data.userInfo) {
      userInfo.value = data.userInfo
      uni.setStorageSync('userInfo', JSON.stringify(data.userInfo))
    }
  }

  async function fetchProfile() {
    try {
      const res = await userApi.getProfile()
      if (res) {
        userInfo.value = res
        uni.setStorageSync('userInfo', JSON.stringify(res))
      }
    } catch (e) {
      console.error('获取用户信息失败', e)
    }
  }

  async function fetchPermissions() {
    try {
      const res = await memberApi.getPermissions()
      if (res) {
        permissions.value = res
      }
    } catch (e) {
      console.error('获取权限失败', e)
    }
  }

  function logout() {
    token.value = ''
    refreshTokenStr.value = ''
    userInfo.value = null
    permissions.value = null
    uni.removeStorageSync('accessToken')
    uni.removeStorageSync('refreshToken')
    uni.removeStorageSync('userInfo')
    // 尝试通知后端
    authApi.logout().catch(() => {})
  }

  // 恢复登录态
  async function restoreSession() {
    if (token.value && userInfo.value) {
      try {
        await fetchProfile()
        await fetchPermissions()
      } catch (e) {
        // Token无效，清除
        if (e.code === 401) {
          logout()
        }
      }
    }
  }

  return {
    token,
    userInfo,
    permissions,
    isLoggedIn,
    userName,
    userAvatar,
    userRole,
    isVip,
    memberLevel,
    login,
    wxLogin,
    setAuth,
    fetchProfile,
    fetchPermissions,
    logout,
    restoreSession,
  }
})
