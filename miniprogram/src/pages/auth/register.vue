<template>
  <view class="page">
    <view class="header">
      <text class="title">创建账号</text>
      <text class="subtitle">加入 NutriAI，开启智能饮食之旅</text>
    </view>

    <view class="form-section">
      <!-- Email -->
      <view class="input-group">
        <text class="input-label">邮箱 <text class="required">*</text></text>
        <input
          v-model="form.email"
          class="input"
          placeholder="请输入邮箱"
          @blur="checkEmailAvail"
        />
        <text v-if="emailStatus === 'taken'" class="field-hint error">邮箱已被注册</text>
        <text v-if="emailStatus === 'ok'" class="field-hint success">邮箱可用</text>
      </view>

      <!-- Email Code -->
      <view class="code-row">
        <view class="input-group code-input-wrap">
          <text class="input-label">邮箱验证码 <text class="required">*</text></text>
          <input
            v-model="form.emailCode"
            class="input"
            placeholder="请输入验证码"
          />
        </view>
        <button
          class="btn-code"
          :disabled="codeCooldown > 0 || !form.email"
          @tap="sendEmailCode"
        >
          {{ codeCooldown > 0 ? `${codeCooldown}s` : '获取验证码' }}
        </button>
      </view>

      <!-- Password -->
      <view class="input-group">
        <text class="input-label">密码 <text class="required">*</text></text>
        <input
          v-model="form.password"
          class="input"
          type="password"
          placeholder="请输入密码（至少6位）"
        />
      </view>

      <!-- Confirm Password -->
      <view class="input-group">
        <text class="input-label">确认密码 <text class="required">*</text></text>
        <input
          v-model="form.confirmPassword"
          class="input"
          type="password"
          placeholder="请再次输入密码"
        />
      </view>

      <!-- Username (optional) -->
      <view class="input-group">
        <text class="input-label">用户名（选填）</text>
        <input
          v-model="form.username"
          class="input"
          placeholder="不填则自动生成"
          @blur="checkUsernameAvail"
        />
        <text v-if="form.username && usernameStatus === 'taken'" class="field-hint error">用户名已被占用</text>
        <text v-if="form.username && usernameStatus === 'ok'" class="field-hint success">用户名可用</text>
      </view>

      <!-- Nickname -->
      <view class="input-group">
        <text class="input-label">昵称</text>
        <input
          v-model="form.nickname"
          class="input"
          placeholder="请输入昵称（选填）"
        />
      </view>

      <!-- Phone -->
      <view class="input-group">
        <text class="input-label">手机号</text>
        <input
          v-model="form.phone"
          class="input"
          type="number"
          placeholder="请输入手机号（选填）"
        />
      </view>

      <!-- Invitation Code -->
      <view class="input-group">
        <text class="input-label">邀请码</text>
        <input
          v-model="form.invitationCode"
          class="input"
          placeholder="请输入邀请码（选填）"
        />
      </view>

      <!-- Captcha -->
      <view class="captcha-row">
        <view class="input-group captcha-input-wrap">
          <text class="input-label">图形验证码 <text class="required">*</text></text>
          <input
            v-model="form.captchaCode"
            class="input"
            placeholder="请输入验证码"
          />
        </view>
        <view class="captcha-img-wrap" @tap="loadCaptcha">
          <image
            v-if="captchaImage"
            class="captcha-img"
            :src="captchaImage"
            mode="aspectFit"
          />
          <text v-else class="captcha-placeholder">点击获取</text>
        </view>
      </view>

      <!-- Submit -->
      <button
        class="btn-primary"
        :loading="loading"
        :disabled="loading"
        @tap="handleRegister"
      >
        注册
      </button>

      <view class="login-link">
        <text class="link-text" @tap="goLogin">已有账号？去登录</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { authApi } from '@/services/api'

const form = reactive({
  username: '',
  email: '',
  emailCode: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  invitationCode: '',
  captchaCode: ''
})

const captchaImage = ref('')
const captchaKey = ref('')
const loading = ref(false)
const codeCooldown = ref(0)
const usernameStatus = ref<'' | 'ok' | 'taken'>('')
const emailStatus = ref<'' | 'ok' | 'taken'>('')

let usernameTimer: ReturnType<typeof setTimeout> | null = null
let emailTimer: ReturnType<typeof setTimeout> | null = null
let cooldownTimer: ReturnType<typeof setInterval> | null = null

onLoad(() => {
  loadCaptcha()
})

async function loadCaptcha() {
  try {
    const res = await authApi.getCaptcha() as any
    if (res.code === 200 && res.data) {
      captchaKey.value = res.data.captchaKey
      captchaImage.value = res.data.captchaImage
    }
  } catch {
    // silently fail
  }
}

function checkUsernameAvail() {
  usernameStatus.value = ''
  if (!form.username.trim()) return
  if (usernameTimer) clearTimeout(usernameTimer)
  usernameTimer = setTimeout(async () => {
    try {
      const res = await authApi.checkUsername(form.username.trim()) as any
      usernameStatus.value = res.code === 200 && res.data === true ? 'ok' : 'taken'
    } catch {
      usernameStatus.value = ''
    }
  }, 500)
}

function checkEmailAvail() {
  emailStatus.value = ''
  if (!form.email.trim()) return
  if (emailTimer) clearTimeout(emailTimer)
  emailTimer = setTimeout(async () => {
    try {
      const res = await authApi.checkEmail(form.email.trim()) as any
      emailStatus.value = res.code === 200 && res.data === true ? 'ok' : 'taken'
    } catch {
      emailStatus.value = ''
    }
  }, 500)
}

async function sendEmailCode() {
  if (!form.email.trim()) {
    return uni.showToast({ title: '请先输入邮箱', icon: 'none' })
  }
  try {
    const res = await authApi.sendEmailCode(form.email.trim()) as any
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

function validate(): boolean {
  if (form.username.trim() && usernameStatus.value === 'taken') {
    uni.showToast({ title: '用户名已被占用', icon: 'none' }); return false
  }
  if (!form.email.trim()) {
    uni.showToast({ title: '请输入邮箱', icon: 'none' }); return false
  }
  if (!form.emailCode.trim()) {
    uni.showToast({ title: '请输入邮箱验证码', icon: 'none' }); return false
  }
  if (!form.password) {
    uni.showToast({ title: '请输入密码', icon: 'none' }); return false
  }
  if (form.password.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' }); return false
  }
  if (form.password !== form.confirmPassword) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' }); return false
  }
  if (!form.captchaCode.trim()) {
    uni.showToast({ title: '请输入图形验证码', icon: 'none' }); return false
  }
  return true
}

async function handleRegister() {
  if (!validate()) return

  loading.value = true
  try {
    const res = await authApi.register({
      username: form.username.trim() || undefined,
      email: form.email.trim(),
      emailCode: form.emailCode.trim(),
      password: form.password,
      nickname: form.nickname.trim() || undefined,
      phone: form.phone.trim() || undefined,
      invitationCode: form.invitationCode.trim() || undefined,
      captchaKey: captchaKey.value,
      captchaCode: form.captchaCode.trim()
    }) as any
    if (res.code === 200) {
      uni.showToast({ title: '注册成功', icon: 'success' })
      setTimeout(() => {
        uni.redirectTo({ url: '/pages/auth/login' })
      }, 1000)
    } else {
      uni.showToast({ title: res.message || '注册失败', icon: 'none' })
      loadCaptcha()
    }
  } catch {
    uni.showToast({ title: '网络错误', icon: 'none' })
    loadCaptcha()
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
  padding: 60rpx 0 40rpx;
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

.form-section {
  padding-top: 8rpx;
}

.input-group {
  margin-bottom: 24rpx;
}

.input-label {
  display: block;
  font-size: 26rpx;
  color: #5a5a5a;
  margin-bottom: 10rpx;
  font-weight: 500;
  font-family: 'Patrick Hand', 'PingFang SC', 'Microsoft YaHei', cursive;
}

.required {
  color: #ff4d4d;
}

.input {
  width: 100%;
  height: 84rpx;
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

.field-hint {
  display: block;
  font-size: 22rpx;
  margin-top: 6rpx;
  padding-left: 8rpx;

  &.error {
    color: #ff4d4d;
  }

  &.success {
    color: #2d5da1;
  }
}

.code-row {
  display: flex;
  align-items: flex-end;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.code-input-wrap {
  flex: 1;
  margin-bottom: 0;
}

.btn-code {
  flex-shrink: 0;
  height: 84rpx;
  line-height: 84rpx;
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

.captcha-row {
  display: flex;
  align-items: flex-end;
  gap: 16rpx;
  margin-bottom: 40rpx;
}

.captcha-input-wrap {
  flex: 1;
  margin-bottom: 0;
}

.captcha-img-wrap {
  width: 220rpx;
  height: 84rpx;
  border-radius: 185px 10px 155px 10px / 10px 155px 10px 185px;
  overflow: hidden;
  background: #e5e0d8;
  border: 2rpx solid #e5e0d8;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.captcha-img {
  width: 100%;
  height: 100%;
}

.captcha-placeholder {
  font-size: 24rpx;
  color: rgba(45, 45, 45, 0.4);
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
  font-family: 'Patrick Hand', 'PingFang SC', 'Microsoft YaHei', cursive;

  &:active {
    transform: translate(2px, 2px);
    box-shadow: none;
  }

  &[disabled] {
    opacity: 0.6;
  }
}

.login-link {
  text-align: center;
  padding: 32rpx 0;
}

.link-text {
  font-size: 26rpx;
  color: #2d5da1;
  font-family: 'Patrick Hand', 'PingFang SC', 'Microsoft YaHei', cursive;
}
</style>
