<template>
  <view class="page">
    <view class="header">
      <text class="title">找回密码</text>
      <text class="subtitle">通过邮箱验证重置密码</text>
    </view>

    <!-- Step Indicator -->
    <view class="steps">
      <view
        v-for="(s, i) in stepLabels"
        :key="i"
        class="step"
        :class="{ active: step >= i + 1, done: step > i + 1 }"
      >
        <view class="step-circle">
          <text class="step-num">{{ step > i + 1 ? '✓' : i + 1 }}</text>
        </view>
        <text class="step-label">{{ s }}</text>
      </view>
    </view>

    <!-- Step 1: Email Verification -->
    <view v-if="step === 1" class="form-section">
      <view class="input-group">
        <text class="input-label">注册邮箱</text>
        <input
          v-model="form.email"
          class="input"
          placeholder="请输入注册时使用的邮箱"
        />
      </view>

      <view class="code-row">
        <view class="input-group code-input-wrap">
          <text class="input-label">验证码</text>
          <input
            v-model="form.code"
            class="input"
            placeholder="请输入邮箱验证码"
          />
        </view>
        <button
          class="btn-code"
          :disabled="codeCooldown > 0 || !form.email"
          @tap="sendCode"
        >
          {{ codeCooldown > 0 ? `${codeCooldown}s` : '获取验证码' }}
        </button>
      </view>

      <button
        class="btn-primary"
        :loading="loading"
        :disabled="loading"
        @tap="verifyCode"
      >
        下一步
      </button>
    </view>

    <!-- Step 2: Reset Password -->
    <view v-if="step === 2" class="form-section">
      <view class="input-group">
        <text class="input-label">新密码</text>
        <input
          v-model="form.newPassword"
          class="input"
          type="password"
          placeholder="请输入新密码（至少6位）"
        />
      </view>

      <view class="input-group">
        <text class="input-label">确认新密码</text>
        <input
          v-model="form.confirmPassword"
          class="input"
          type="password"
          placeholder="请再次输入新密码"
        />
      </view>

      <button
        class="btn-primary"
        :loading="loading"
        :disabled="loading"
        @tap="resetPassword"
      >
        重置密码
      </button>
    </view>

    <!-- Step 3: Success -->
    <view v-if="step === 3" class="success-section">
      <text class="success-icon">✅</text>
      <text class="success-title">密码重置成功</text>
      <text class="success-desc">请使用新密码登录</text>
      <button class="btn-primary" @tap="goLogin">返回登录</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { authApi } from '@/services/api'

const stepLabels = ['验证邮箱', '重置密码', '完成']
const step = ref(1)
const loading = ref(false)
const codeCooldown = ref(0)
let cooldownTimer: ReturnType<typeof setInterval> | null = null
let resetToken = ''

const form = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

async function sendCode() {
  if (!form.email.trim()) {
    return uni.showToast({ title: '请输入邮箱', icon: 'none' })
  }
  try {
    const res = await authApi.forgotPassword({ email: form.email.trim() }) as any
    if (res.code === 200) {
      uni.showToast({ title: '验证码已发送', icon: 'success' })
      startCooldown()
    } else {
      uni.showToast({ title: res.message || '发送失败', icon: 'none' })
    }
  } catch {
    uni.showToast({ title: '发送失败', icon: 'none' })
  }
}

function startCooldown() {
  codeCooldown.value = 60
  if (cooldownTimer) clearInterval(cooldownTimer)
  cooldownTimer = setInterval(() => {
    codeCooldown.value--
    if (codeCooldown.value <= 0 && cooldownTimer) {
      clearInterval(cooldownTimer)
      cooldownTimer = null
    }
  }, 1000)
}

async function verifyCode() {
  if (!form.email.trim()) {
    return uni.showToast({ title: '请输入邮箱', icon: 'none' })
  }
  if (!form.code.trim()) {
    return uni.showToast({ title: '请输入验证码', icon: 'none' })
  }

  loading.value = true
  try {
    const res = await authApi.resetPassword({
      email: form.email.trim(),
      code: form.code.trim(),
      step: 'verify'
    }) as any
    if (res.code === 200) {
      resetToken = res.data?.token || form.code.trim()
      step.value = 2
    } else {
      uni.showToast({ title: res.message || '验证码错误', icon: 'none' })
    }
  } catch {
    // If the API doesn't have a separate verify step, proceed to step 2
    resetToken = form.code.trim()
    step.value = 2
  } finally {
    loading.value = false
  }
}

async function resetPassword() {
  if (!form.newPassword) {
    return uni.showToast({ title: '请输入新密码', icon: 'none' })
  }
  if (form.newPassword.length < 6) {
    return uni.showToast({ title: '密码至少6位', icon: 'none' })
  }
  if (form.newPassword !== form.confirmPassword) {
    return uni.showToast({ title: '两次密码不一致', icon: 'none' })
  }

  loading.value = true
  try {
    const res = await authApi.resetPassword({
      email: form.email.trim(),
      code: resetToken,
      newPassword: form.newPassword
    }) as any
    if (res.code === 200) {
      step.value = 3
    } else {
      uni.showToast({ title: res.message || '重置失败', icon: 'none' })
    }
  } catch {
    uni.showToast({ title: '网络错误', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function goLogin() {
  uni.navigateBack({
    fail: () => {
      uni.redirectTo({ url: '/pages/auth/login' })
    }
  })
}
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: #fdfbf7;
  padding: 0 48rpx 60rpx;
}

.header {
  padding: 48rpx 0 32rpx;
}

.title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: #2d2d2d;
  margin-bottom: 12rpx;
  font-family: 'Kalam', 'ZCOOL KuaiLe', 'PingFang SC', cursive;
}

.subtitle {
  font-size: 26rpx;
  color: rgba(45, 45, 45, 0.4);
  font-family: 'Patrick Hand', 'PingFang SC', 'Microsoft YaHei', cursive;
}

/* Steps */
.steps {
  display: flex;
  justify-content: space-between;
  padding: 24rpx 16rpx 48rpx;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.step-circle {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: #e5e0d8;
  border: 2px solid #2d2d2d;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
  transition: background 0.3s;
}

.step-num {
  font-size: 26rpx;
  color: #fff;
  font-weight: 600;
  font-family: 'Patrick Hand', 'PingFang SC', 'Microsoft YaHei', cursive;
}

.step-label {
  font-size: 22rpx;
  color: rgba(45, 45, 45, 0.4);
  font-family: 'Patrick Hand', 'PingFang SC', 'Microsoft YaHei', cursive;
}

.step.active .step-circle {
  background: #ff4d4d;
}

.step.active .step-label {
  color: #ff4d4d;
  font-weight: 500;
}

.step.done .step-circle {
  background: #2d5da1;
}

/* Form */
.form-section {
  padding-top: 8rpx;
}

.input-group {
  margin-bottom: 28rpx;
}

.input-label {
  display: block;
  font-size: 26rpx;
  color: #5a5a5a;
  margin-bottom: 12rpx;
  font-weight: 500;
  font-family: 'Patrick Hand', 'PingFang SC', 'Microsoft YaHei', cursive;
}

.input {
  width: 100%;
  height: 88rpx;
  background: #fdfbf7;
  border-radius: 185px 10px 155px 10px / 10px 155px 10px 185px;
  padding: 0 28rpx;
  font-size: 28rpx;
  color: #2d2d2d;
  box-sizing: border-box;
  border: 2rpx solid #e5e0d8;

  &:focus {
    border-color: #ff4d4d;
    background: #fff;
  }
}

.code-row {
  display: flex;
  align-items: flex-end;
  gap: 16rpx;
  margin-bottom: 40rpx;
}

.code-input-wrap {
  flex: 1;
  margin-bottom: 0;
}

.btn-code {
  flex-shrink: 0;
  height: 88rpx;
  line-height: 88rpx;
  padding: 0 24rpx;
  font-size: 24rpx;
  color: #2d5da1;
  background: #fdfbf7;
  border: 2rpx solid #2d5da1;
  border-radius: 185px 10px 155px 10px / 10px 155px 10px 185px;
  white-space: nowrap;
  font-family: 'Patrick Hand', 'PingFang SC', 'Microsoft YaHei', cursive;

  &[disabled] {
    color: rgba(45, 45, 45, 0.4);
    background: #e5e0d8;
    border-color: #e5e0d8;
  }
}

.btn-primary {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #ff4d4d;
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  border: 2px solid #2d2d2d;
  box-shadow: 4px 4px 0px 0px #2d2d2d;
  margin-top: 16rpx;
  font-family: 'Patrick Hand', 'PingFang SC', 'Microsoft YaHei', cursive;

  &:active {
    transform: translate(2px, 2px);
    box-shadow: none;
  }

  &[disabled] {
    opacity: 0.6;
  }
}

/* Success */
.success-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 80rpx;
}

.success-icon {
  font-size: 100rpx;
  margin-bottom: 32rpx;
}

.success-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #2d2d2d;
  margin-bottom: 16rpx;
  font-family: 'Kalam', 'ZCOOL KuaiLe', 'PingFang SC', cursive;
}

.success-desc {
  font-size: 28rpx;
  color: rgba(45, 45, 45, 0.4);
  margin-bottom: 60rpx;
}
</style>
