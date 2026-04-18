<script setup>
/**
 * 登录页
 * 同步网页前端登录功能：邮箱+密码登录，注册需邮箱验证码
 */
import { ref } from 'vue'
import { useUserStore } from '../../stores/user'
import { authApi } from '../../services/api'

const userStore = useUserStore()

const mode = ref('login') // login | register
const form = ref({
  email: '',
  username: '',
  password: '',
  confirmPassword: '',
  captcha: '',
  captchaKey: '',
  emailCode: '',
  rememberMe: false,
})
const loading = ref(false)
const showPassword = ref(false)

// 验证码相关
const showCaptcha = ref(false)
const captchaImage = ref('')
const loginFailCount = ref(0)

// 邮箱验证码倒计时
const emailCodeCountdown = ref(0)
let emailCodeTimer = null

async function getCaptcha() {
  try {
    const res = await authApi.getCaptcha ? authApi.getCaptcha() : null
    // 后端 /auth/captcha 返回 {captchaImage, captchaKey}
    if (res) {
      captchaImage.value = res.captchaImage || ''
      form.value.captchaKey = res.captchaKey || ''
    }
  } catch (e) {
    console.error('获取验证码失败', e)
  }
}

function refreshCaptcha() {
  getCaptcha()
}

async function sendEmailCode() {
  if (!form.value.email) {
    uni.showToast({ title: '请输入邮箱', icon: 'none' })
    return
  }
  if (emailCodeCountdown.value > 0) return

  try {
    await authApi.sendEmailCode({ email: form.value.email })
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    emailCodeCountdown.value = 60
    emailCodeTimer = setInterval(() => {
      emailCodeCountdown.value--
      if (emailCodeCountdown.value <= 0) {
        clearInterval(emailCodeTimer)
        emailCodeTimer = null
      }
    }, 1000)
  } catch (e) {
    uni.showToast({ title: e.message || '发送失败', icon: 'none' })
  }
}

async function handleLogin() {
  if (!form.value.email || !form.value.password) {
    uni.showToast({ title: '请输入邮箱和密码', icon: 'none' })
    return
  }
  if (showCaptcha.value && !form.value.captcha) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    await userStore.login({
      username: form.value.email,
      password: form.value.password,
      captcha: form.value.captcha || undefined,
      captchaKey: form.value.captchaKey || undefined,
      rememberMe: form.value.rememberMe,
    })
    loginFailCount.value = 0
    showCaptcha.value = false
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1000)
  } catch (e) {
    loginFailCount.value++
    // 失败3次后显示验证码
    if (loginFailCount.value >= 3 && !showCaptcha.value) {
      showCaptcha.value = true
      await getCaptcha()
    } else if (showCaptcha.value) {
      refreshCaptcha()
    }
    uni.showToast({ title: e.message || '登录失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!form.value.email || !form.value.password || !form.value.confirmPassword) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  if (form.value.password !== form.value.confirmPassword) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }
  if (!form.value.emailCode) {
    uni.showToast({ title: '请输入邮箱验证码', icon: 'none' })
    return
  }
  // 密码强度校验：大小写字母+数字
  const pwdRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&.#^~_+=\-]{6,20}$/
  if (!pwdRegex.test(form.value.password)) {
    uni.showToast({ title: '密码需6-20位，含大小写字母和数字', icon: 'none' })
    return
  }
  loading.value = true
  try {
    // 注册前先获取验证码
    if (!form.value.captchaKey) {
      await getCaptcha()
    }
    await authApi.register({
      email: form.value.email,
      username: form.value.username || undefined,
      password: form.value.password,
      confirmPassword: form.value.confirmPassword,
      emailCode: form.value.emailCode,
      captcha: form.value.captcha || undefined,
      captchaKey: form.value.captchaKey || undefined,
    })
    uni.showToast({ title: '注册成功，请登录', icon: 'success' })
    mode.value = 'login'
    form.value.password = ''
    form.value.confirmPassword = ''
  } catch (e) {
    uni.showToast({ title: e.message || '注册失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function wxLogin() {
  // #ifdef MP-WEIXIN
  loading.value = true
  try {
    const [err, loginRes] = await uni.login({ provider: 'weixin' })
    if (err || !loginRes?.code) {
      uni.showToast({ title: '获取微信授权失败', icon: 'none' })
      return
    }
    await userStore.wxLogin(loginRes.code)
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1000)
  } catch (e) {
    uni.showToast({ title: e.message || '微信登录失败', icon: 'none' })
  } finally {
    loading.value = false
  }
  // #endif
}

function switchMode() {
  mode.value = mode.value === 'login' ? 'register' : 'login'
}
</script>

<template>
  <view class="page">
    <!-- 顶部装饰 -->
    <view class="header">
      <view class="header__decor-1" />
      <view class="header__decor-2" />
      <view class="header__content">
        <text class="header__brand">🌿</text>
        <text class="header__title">NutriAI</text>
        <text class="header__subtitle">
          {{ mode === 'login' ? '欢迎回来' : '创建账户' }}
        </text>
      </view>
    </view>

    <!-- 表单区域 -->
    <view class="form-area">
      <view class="form-card">
        <!-- 邮箱 -->
        <view class="input-group">
          <text class="input-group__icon">📧</text>
          <input
            class="input-group__field"
            v-model="form.email"
            placeholder="请输入邮箱"
            type="text"
          />
        </view>

        <!-- 注册模式：用户名（选填） -->
        <view v-if="mode === 'register'" class="input-group">
          <text class="input-group__icon">👤</text>
          <input
            class="input-group__field"
            v-model="form.username"
            placeholder="用户名（选填）"
            type="text"
            maxlength="20"
          />
        </view>

        <!-- 密码 -->
        <view class="input-group">
          <text class="input-group__icon">🔒</text>
          <input
            class="input-group__field"
            v-model="form.password"
            placeholder="请输入密码"
            :password="!showPassword"
          />
          <text
            class="input-group__toggle"
            @tap="showPassword = !showPassword"
          >
            {{ showPassword ? '🙈' : '👁️' }}
          </text>
        </view>

        <!-- 注册模式：确认密码 -->
        <view v-if="mode === 'register'" class="input-group">
          <text class="input-group__icon">🔒</text>
          <input
            class="input-group__field"
            v-model="form.confirmPassword"
            placeholder="确认密码"
            :password="!showPassword"
          />
        </view>

        <!-- 注册模式：邮箱验证码 -->
        <view v-if="mode === 'register'" class="input-group">
          <text class="input-group__icon">✉️</text>
          <input
            class="input-group__field"
            v-model="form.emailCode"
            placeholder="邮箱验证码"
            type="number"
            maxlength="6"
          />
          <view
            class="input-group__code-btn"
            :class="{ 'input-group__code-btn--disabled': emailCodeCountdown > 0 }"
            @tap="sendEmailCode"
          >
            <text class="input-group__code-text">
              {{ emailCodeCountdown > 0 ? `${emailCodeCountdown}s` : '获取验证码' }}
            </text>
          </view>
        </view>

        <!-- 验证码（登录失败3次后显示） -->
        <view v-if="showCaptcha" class="input-group">
          <text class="input-group__icon">🔐</text>
          <input
            class="input-group__field"
            v-model="form.captcha"
            placeholder="请输入验证码"
            maxlength="4"
          />
          <image
            v-if="captchaImage"
            class="input-group__captcha-img"
            :src="captchaImage"
            mode="aspectFit"
            @tap="refreshCaptcha"
          />
          <text v-else class="input-group__captcha-loading" @tap="refreshCaptcha">刷新</text>
        </view>

        <!-- 登录模式：记住我 -->
        <view v-if="mode === 'login'" class="remember-row">
          <view class="remember-check" @tap="form.rememberMe = !form.rememberMe">
            <view class="remember-check__box" :class="{ 'remember-check__box--active': form.rememberMe }">
              <text v-if="form.rememberMe" class="remember-check__icon">✓</text>
            </view>
            <text class="remember-check__label">记住我（7天免登录）</text>
          </view>
        </view>

        <!-- 主按钮 -->
        <view
          class="primary-btn"
          :class="{ 'primary-btn--loading': loading }"
          @tap="mode === 'login' ? handleLogin() : handleRegister()"
        >
          <text>{{ loading ? '处理中...' : (mode === 'login' ? '登录' : '注册') }}</text>
        </view>

        <!-- 切换登录/注册 -->
        <view class="switch-mode" @tap="switchMode">
          <text class="switch-mode__text">
            {{ mode === 'login' ? '没有账号？立即注册' : '已有账号？去登录' }}
          </text>
        </view>
      </view>

      <!-- 分割线 -->
      <view class="divider">
        <view class="divider__line" />
        <text class="divider__text">其他方式</text>
        <view class="divider__line" />
      </view>

      <!-- 微信快捷登录 -->
      <!-- #ifdef MP-WEIXIN -->
      <view class="wx-btn" @tap="wxLogin">
        <text class="wx-btn__icon">💬</text>
        <text class="wx-btn__text">微信快捷登录</text>
      </view>
      <!-- #endif -->
    </view>

    <!-- 底部返回 -->
    <view class="footer" @tap="() => uni.navigateBack()">
      <text class="footer__text">暂不登录</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@import '../../styles/design-system.scss';

.page {
  min-height: 100vh;
  background: #ffffff;
  display: flex;
  flex-direction: column;
}

// 顶部装饰区
.header {
  position: relative;
  padding: 120rpx 40rpx 60rpx;
  overflow: hidden;

  &__decor-1 {
    position: absolute;
    top: -100rpx;
    right: -80rpx;
    width: 400rpx;
    height: 400rpx;
    border-radius: $radius-full;
    background: rgba($primary, 0.08);
  }

  &__decor-2 {
    position: absolute;
    top: 40rpx;
    left: -120rpx;
    width: 300rpx;
    height: 300rpx;
    border-radius: $radius-full;
    background: rgba($secondary, 0.06);
  }

  &__content {
    position: relative;
    z-index: 1;
  }

  &__brand {
    font-size: 80rpx;
    display: block;
    margin-bottom: 16rpx;
  }

  &__title {
    display: block;
    font-size: $font-4xl;
    font-weight: 800;
    color: $primary;
    letter-spacing: -0.02em;
    margin-bottom: 8rpx;
  }

  &__subtitle {
    display: block;
    font-size: $font-xl;
    color: $on-surface-variant;
    font-weight: 400;
  }
}

// 表单区域
.form-area {
  flex: 1;
  padding: 0 32rpx;
}

.form-card {
  background: $surface-container-lowest;
  border-radius: $radius-2xl;
  padding: 32rpx;
  box-shadow: $shadow-md;
}

.input-group {
  display: flex;
  align-items: center;
  gap: 16rpx;
  background: $surface-container-low;
  border-radius: $radius-lg;
  padding: 20rpx 24rpx;
  margin-bottom: 20rpx;

  &__icon {
    font-size: 32rpx;
    flex-shrink: 0;
  }

  &__field {
    flex: 1;
    font-size: $font-base;
    color: $on-surface;
  }

  &__toggle {
    font-size: 32rpx;
    padding: 4rpx 8rpx;
  }

  &__code-btn {
    background: $primary;
    padding: 8rpx 20rpx;
    border-radius: $radius-md;
    flex-shrink: 0;

    &--disabled {
      background: $outline;
      opacity: 0.5;
    }
  }

  &__code-text {
    font-size: $font-xs;
    color: #ffffff;
    white-space: nowrap;
  }

  &__captcha-img {
    width: 180rpx;
    height: 60rpx;
    flex-shrink: 0;
    border-radius: $radius-sm;
  }

  &__captcha-loading {
    font-size: $font-xs;
    color: $primary;
    flex-shrink: 0;
    padding: 4rpx 16rpx;
  }
}

.remember-row {
  padding: 8rpx 0 16rpx;
}

.remember-check {
  display: flex;
  align-items: center;
  gap: 12rpx;

  &__box {
    width: 36rpx;
    height: 36rpx;
    border: 2rpx solid $outline;
    border-radius: $radius-sm;
    display: flex;
    align-items: center;
    justify-content: center;

    &--active {
      background: $primary;
      border-color: $primary;
    }
  }

  &__icon {
    font-size: 24rpx;
    color: #ffffff;
  }

  &__label {
    font-size: $font-sm;
    color: $on-surface-variant;
  }
}

.primary-btn {
  padding: 24rpx;
  background: $primary;
  color: $on-primary;
  text-align: center;
  border-radius: $radius-lg;
  font-size: $font-base;
  font-weight: 700;
  margin-top: 8rpx;

  &--loading {
    opacity: 0.6;
    pointer-events: none;
  }

  &:active {
    transform: scale(0.98);
  }
}

.switch-mode {
  text-align: center;
  padding: 20rpx 0 4rpx;

  &__text {
    font-size: $font-sm;
    color: $primary;
  }
}

// 分割线
.divider {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 40rpx 0;

  &__line {
    flex: 1;
    height: 1rpx;
    background: $surface-container;
  }

  &__text {
    font-size: $font-xs;
    color: $on-surface-variant;
  }
}

// 微信按钮
.wx-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 24rpx;
  background: #07c160;
  border-radius: $radius-lg;

  &__icon {
    font-size: 36rpx;
  }

  &__text {
    font-size: $font-base;
    font-weight: 600;
    color: #fff;
  }

  &:active {
    opacity: 0.9;
  }
}

// 底部
.footer {
  padding: 32rpx;
  text-align: center;

  &__text {
    font-size: $font-sm;
    color: $on-surface-variant;
  }
}
</style>
