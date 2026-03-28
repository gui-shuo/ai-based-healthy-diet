<template>
  <div class="member-view">
    <!-- 顶部导航栏 -->
    <nav class="top-nav">
      <div class="nav-inner">
        <div class="nav-left">
          <el-button text @click="router.push('/')">
            <el-icon><ArrowLeft /></el-icon>
            返回首页
          </el-button>
          <h2 class="page-title">🍃 NutriAI 营养卡</h2>
        </div>
      </div>
    </nav>

    <main class="main-area">
      <!-- VIP 状态卡片 -->
      <div class="vip-status-banner" :class="{ 'is-vip': permissions?.isVip }">
        <div class="banner-content">
          <div class="banner-left">
            <div class="vip-icon">{{ permissions?.isVip ? '💎' : '🌱' }}</div>
            <div class="vip-info">
              <h3>{{ permissions?.planName || '免费用户' }}</h3>
              <p v-if="permissions?.isVip && permissions?.vipExpireAt">
                有效期至 {{ formatDate(permissions.vipExpireAt) }}
              </p>
              <p v-else-if="!permissions?.isVip">开通营养卡，解锁全部AI营养功能</p>
            </div>
          </div>
          <div class="banner-right">
            <div class="quota-display">
              <span class="quota-label">今日AI配额</span>
              <span class="quota-value">
                <template v-if="permissions?.aiQuotaTotal === -1">∞</template>
                <template v-else>{{ permissions?.aiQuotaRemain ?? 0 }} / {{ permissions?.aiQuotaTotal ?? 3 }}</template>
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 营养卡套餐 -->
      <VipPurchasePanel @vip-activated="refreshAll" />

      <!-- 底部两列：签到 + 邀请 -->
      <div class="engagement-grid">
        <SignInCalendar @signed="refreshAll" />
        <InvitationPanel :member-info="memberInfo" />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getMemberInfo, getMemberPermissions } from '@/services/member'
import SignInCalendar from '@/components/member/SignInCalendar.vue'
import InvitationPanel from '@/components/member/InvitationPanel.vue'
import VipPurchasePanel from '@/components/member/VipPurchasePanel.vue'
import message from '@/utils/message'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()

const memberInfo = ref(null)
const permissions = ref(null)

const refreshAll = async () => {
  try {
    const [memberRes, permRes] = await Promise.all([
      getMemberInfo().catch(() => null),
      getMemberPermissions().catch(() => null)
    ])
    if (memberRes?.data?.code === 200) memberInfo.value = memberRes.data.data
    if (permRes?.data?.code === 200) permissions.value = permRes.data.data
    authStore.fetchPermissions()
  } catch (err) {
    console.error('刷新会员信息失败:', err)
  }
}

const formatDate = (dt) => {
  if (!dt) return ''
  return new Date(dt).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

onMounted(() => refreshAll())

onBeforeUnmount(() => {
  setTimeout(() => {
    document.querySelectorAll('body > .el-overlay').forEach(el => el.remove())
  }, 50)
})
</script>

<style scoped lang="scss">
.member-view {
  min-height: 100vh;
  background: #f5f7fa;
}

.top-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.nav-inner {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 20px;
  height: 52px;
  display: flex;
  align-items: center;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title {
  font-size: 17px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.main-area {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}

.vip-status-banner {
  background: linear-gradient(135deg, #e8f5e9 0%, #f1f8e9 100%);
  border: 1px solid #c8e6c9;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;

  &.is-vip {
    background: linear-gradient(135deg, #e8eaf6 0%, #f3e5f5 100%);
    border-color: #c5cae9;
  }

  .banner-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .banner-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .vip-icon {
    font-size: 32px;
  }

  .vip-info {
    h3 {
      font-size: 16px;
      font-weight: 600;
      color: #1f2937;
      margin: 0 0 2px;
    }
    p {
      font-size: 12px;
      color: #6b7280;
      margin: 0;
    }
  }

  .banner-right {
    text-align: right;
  }

  .quota-display {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 2px;

    .quota-label {
      font-size: 11px;
      color: #9ca3af;
    }
    .quota-value {
      font-size: 20px;
      font-weight: 700;
      color: #667eea;
    }
  }
}

.engagement-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 16px;
}

@media (max-width: 768px) {
  .main-area {
    padding: 12px;
  }
  .engagement-grid {
    grid-template-columns: 1fr;
  }
  .vip-status-banner .banner-content {
    flex-direction: column;
    gap: 10px;
    text-align: center;
  }
  .banner-right {
    text-align: center !important;
  }
  .quota-display {
    align-items: center !important;
  }
}
</style>
