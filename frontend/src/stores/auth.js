import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref(localStorage.getItem('nutri_token') || null)
  const user = ref(JSON.parse(localStorage.getItem('nutri_user') || 'null'))

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => {
    return user.value?.role === 'admin' || user.value?.role === 'super_admin'
  })

  // Actions
  async function login(credentials) {
    try {
      const response = await api.post('/auth/login', credentials)
      
      token.value = response.token
      user.value = response.user
      
      localStorage.setItem('nutri_token', response.token)
      localStorage.setItem('nutri_user', JSON.stringify(response.user))
      
      return response
    } catch (error) {
      throw error
    }
  }

  async function register(userData) {
    try {
      const response = await api.post('/auth/register', userData)
      
      token.value = response.token
      user.value = response.user
      
      localStorage.setItem('nutri_token', response.token)
      localStorage.setItem('nutri_user', JSON.stringify(response.user))
      
      return response
    } catch (error) {
      throw error
    }
  }

  async function logout() {
    try {
      await api.post('/auth/logout')
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      token.value = null
      user.value = null
      localStorage.removeItem('nutri_token')
      localStorage.removeItem('nutri_user')
    }
  }

  async function refreshToken() {
    try {
      const response = await api.post('/auth/refresh-token')
      token.value = response.token
      localStorage.setItem('nutri_token', response.token)
      return response
    } catch (error) {
      await logout()
      throw error
    }
  }

  return {
    token,
    user,
    isLoggedIn,
    isAdmin,
    login,
    register,
    logout,
    refreshToken
  }
})
