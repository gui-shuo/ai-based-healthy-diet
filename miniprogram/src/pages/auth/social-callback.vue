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
import { socialAuthApi } from '@/services/api'

const loading = ref(true)
const error = ref(false)
const statusMsg = ref('正在处理登录...')
const userStore = useUserStore()

async function handleBind(code: string, provider: string) {
  statusMsg.value = '正在绑定账号...'
  try {
    let res: any
    if (provider === 'qq') {
      res = await socialAuthApi.bindQq(code)
    } else if (provider === 'wechat') {
      res = await socialAuthApi.bindWechat?.(code)
    }
    if (res?.code === 200) {
      loading.value = false
      uni.showToast({ title: '绑定成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateTo({ url: '/pages/profile/account-binding' })
      }, 800)
    } else {
      throw new Error(res?.message || '绑定失败')
    }
  } catch (e: any) {
    error.value = true
    loading.value = false
    statusMsg.value = e?.message || '绑定失败'
  }
}

onLoad((options: any) => {
  const action = options?.action
  const code = options?.code
  const provider = options?.provider

  // Handle bind callback from web frontend redirect
  if (action === 'bind' && code) {
    handleBind(code, provider || 'qq')
    return
  }

  // Handle login callback
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

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: $background;
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
  background: $card;
  border: 1rpx solid $border;
  border-radius: $radius-2xl;
  box-shadow: $shadow-md;
}

.status-icon {
  font-size: 80rpx;
}

.status-msg {
  font-size: 28rpx;
  color: $muted-foreground;
  text-align: center;
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.btn-back {
  margin-top: 40rpx;
  background: $gradient-accent;
  color: #fff;
  border: none;
  border-radius: $radius-xl;
  box-shadow: $shadow-accent;
  height: 80rpx;
  line-height: 80rpx;
  padding: 0 60rpx;
  font-size: 28rpx;
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.btn-back:active {
  transform: translateY(1px);
  box-shadow: 0 2px 6px rgba(16, 185, 129, 0.15);
  opacity: 0.95;
}

.btn-back::after {
  border: none;
}
</style>
