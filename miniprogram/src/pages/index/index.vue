<template>
  <view class="page">
    <!-- Custom Navigation Bar -->
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-bar-content">
        <text class="nav-title">NutriAI</text>
        <view class="nav-bell" @tap="goTo('/pages/announcements/index')">
          <text class="bell-icon">🔔</text>
        </view>
      </view>
    </view>

    <view class="content" :style="{ paddingTop: navBarTotalHeight + 'px' }">
      <!-- User Greeting -->
      <view class="greeting-section" @tap="handleGreetingTap">
        <image
          v-if="userStore.isLoggedIn"
          class="avatar"
          :src="defaultAvatar(userStore.userInfo?.avatar)"
          mode="aspectFill"
        />
        <view v-else class="avatar-placeholder">👤</view>
        <view class="greeting-text">
          <text class="greeting-hello" v-if="userStore.isLoggedIn">
            你好, {{ userStore.userInfo?.nickname || userStore.userInfo?.username }}
          </text>
          <text class="greeting-hello" v-else>你好, 请登录</text>
          <text class="greeting-sub">{{ appStore.config.siteDescription }}</text>
        </view>
      </view>

      <!-- Feature Grid -->
      <view class="grid">
        <view
          v-for="item in features"
          :key="item.label"
          class="grid-item card"
          @tap="handleFeatureTap(item)"
        >
          <view class="grid-item-left">
            <text class="grid-icon">{{ item.icon }}</text>
            <view class="grid-label-wrap">
              <text class="grid-label">{{ item.label }}</text>
              <text v-if="item.badge" class="vip-badge">VIP</text>
            </view>
          </view>
          <text class="grid-arrow">›</text>
        </view>
      </view>

      <!-- App Description -->
      <view class="app-desc" v-if="appStore.config.siteDescription">
        <text class="desc-text">{{ appStore.config.siteDescription }}</text>
        <text class="copy-text" v-if="appStore.config.copyright">{{ appStore.config.copyright }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { defaultAvatar } from '@/utils/common'

const userStore = useUserStore()
const appStore = useAppStore()

const statusBarHeight = ref(0)
const navBarTotalHeight = ref(0)

const features = [
  { icon: '🤖', label: 'AI营养师', path: '/pages/ai-chat/index', isTab: true },
  { icon: '📋', label: 'AI饮食计划', path: '/pages/diet-plan/index', badge: 'VIP' },
  { icon: '📸', label: 'AI食物识别', path: '/pages/food-recognition/index' },
  { icon: '📝', label: '饮食记录', path: '/pages/food-records/index' },
  { icon: '👨‍⚕️', label: '营养师咨询', path: '/pages/consultation/index' },
  { icon: '🛒', label: '营养商城', path: '/pages/product-shop/index' },
  { icon: '🌐', label: '营养圈', path: '/pages/community/index', isTab: true },
  { icon: '💬', label: '意见反馈', path: '/pages/feedback/index' },
  { icon: '📢', label: '系统公告', path: '/pages/announcements/index' }
]

onShow(() => {
  const sysInfo = uni.getSystemInfoSync()
  statusBarHeight.value = sysInfo.statusBarHeight || 0
  navBarTotalHeight.value = statusBarHeight.value + 44

  userStore.restore()
  appStore.fetchConfig()
})

function handleGreetingTap() {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
  }
}

function handleFeatureTap(item: { path: string; isTab?: boolean }) {
  if (item.isTab) {
    uni.switchTab({ url: item.path })
  } else {
    uni.navigateTo({ url: item.path })
  }
}

function goTo(url: string) {
  uni.navigateTo({ url })
}
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: #fdfbf7;
}

.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  background: #ff4d4d;
  border-bottom: 2px solid #2d2d2d;
}

.nav-bar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 32rpx;
}

.nav-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: 2rpx;
  font-family: 'Kalam', 'ZCOOL KuaiLe', 'PingFang SC', cursive;
}

.nav-bell {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bell-icon {
  font-size: 40rpx;
}

.content {
  padding: 24rpx 24rpx 48rpx;
}

.greeting-section {
  display: flex;
  align-items: center;
  padding: 32rpx;
  margin-bottom: 24rpx;
  background: #fff;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  border: 2px solid #2d2d2d;
  box-shadow: 4px 4px 0px 0px #2d2d2d;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  margin-right: 24rpx;
  border: 2px solid #2d2d2d;
}

.avatar-placeholder {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  margin-right: 24rpx;
  background: #fff9c4;
  border: 2px solid #2d2d2d;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
}

.greeting-text {
  display: flex;
  flex-direction: column;
}

.greeting-hello {
  font-size: 32rpx;
  font-weight: 600;
  color: #2d2d2d;
  margin-bottom: 8rpx;
  font-family: 'Kalam', 'ZCOOL KuaiLe', 'PingFang SC', cursive;
}

.greeting-sub {
  font-size: 24rpx;
  color: rgba(45, 45, 45, 0.4);
}

.grid {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  margin-bottom: 32rpx;
}

.grid-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 32rpx;
  background: #fff;
  border-radius: 15px 225px 15px 255px / 255px 15px 225px 15px;
  border: 2px solid #2d2d2d;
  box-shadow: 3px 3px 0px 0px rgba(45, 45, 45, 0.1);
  transition: transform 0.15s, box-shadow 0.15s;

  &:active {
    transform: translate(2px, 2px);
    box-shadow: none;
  }
}

.grid-item-left {
  display: flex;
  align-items: center;
}

.grid-icon {
  font-size: 44rpx;
  margin-right: 24rpx;
}

.grid-label-wrap {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.grid-label {
  font-size: 30rpx;
  font-weight: 500;
  color: #2d2d2d;
  font-family: 'Patrick Hand', 'PingFang SC', 'Microsoft YaHei', cursive;
}

.vip-badge {
  font-size: 20rpx;
  color: #fff;
  background: #ff4d4d;
  padding: 2rpx 12rpx;
  border-radius: 185px 10px 155px 10px / 10px 155px 10px 185px;
  border: 1px solid #2d2d2d;
  font-weight: 600;
  font-family: 'Patrick Hand', 'PingFang SC', 'Microsoft YaHei', cursive;
}

.grid-arrow {
  font-size: 36rpx;
  color: rgba(45, 45, 45, 0.4);
}

.app-desc {
  text-align: center;
  padding: 32rpx 0 24rpx;
  border-top: 2px dashed #e5e0d8;
}

.desc-text {
  display: block;
  font-size: 24rpx;
  color: rgba(45, 45, 45, 0.3);
  margin-bottom: 8rpx;
}

.copy-text {
  display: block;
  font-size: 22rpx;
  color: rgba(45, 45, 45, 0.25);
}
</style>
