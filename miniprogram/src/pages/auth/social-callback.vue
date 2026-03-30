<template>
  <view class="page">
    <view v-if="loading" class="status-box">
      <text class="status-icon">🔄</text>
      <text class="status-msg">{{ statusMsg }}</text>
    </view>
    <view v-else-if="error" class="status-box">
      <text class="status-icon">❌</text>
      <text class="status-msg">{{ statusMsg }}</text>
      <button class="btn-back" @tap="goLogin">返回登录</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { setToken, setRefreshToken } from '@/utils/request'

const loading = ref(true)
const error = ref(false)
const statusMsg = ref('正在处理登录...')
const userStore = useUserStore()

onLoad((options: any) => {
  const token = options?.token
  const refreshToken = options?.refreshToken

  if (!token) {
    error.value = true
    statusMsg.value = '登录失败：未获取到认证信息'
    loading.value = false
    return
  }

  setToken(token)
  if (refreshToken) setRefreshToken(refreshToken)
  userStore.restore()

  userStore.fetchUserInfo().finally(() => {
    loading.value = false
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 800)
  })
})

function goLogin() {
  uni.redirectTo({ url: '/pages/auth/login' })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.status-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
  padding: 60rpx;
}
.status-icon {
  font-size: 80rpx;
}
.status-msg {
  font-size: 28rpx;
  color: #666;
  text-align: center;
}
.btn-back {
  margin-top: 40rpx;
  background: #07c160;
  color: #fff;
  border: none;
  border-radius: 44rpx;
  height: 80rpx;
  line-height: 80rpx;
  padding: 0 60rpx;
  font-size: 28rpx;
}
.btn-back::after {
  border: none;
}
</style>
