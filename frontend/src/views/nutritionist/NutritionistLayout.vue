<template>
  <div class="nutritionist-layout" v-if="ready">
    <!-- Pending/Rejected state -->
    <div v-if="approvalState !== 'APPROVED'" class="approval-state-container">
      <div class="approval-card">
        <template v-if="approvalState === 'PENDING'">
          <div class="state-icon">⏳</div>
          <h2>您的入驻申请正在审核中</h2>
          <p>管理员将在1-3个工作日内完成审核，审核通过后您即可开始提供营养咨询服务。</p>
          <el-tag type="warning" size="large">待审核</el-tag>
        </template>
        <template v-else-if="approvalState === 'REJECTED'">
          <div class="state-icon">❌</div>
          <h2>入驻申请未通过</h2>
          <p>很抱歉，您的营养师入驻申请未通过审核。如有疑问请联系管理员。</p>
          <el-tag type="danger" size="large">已拒绝</el-tag>
        </template>
        <div class="approval-actions">
          <el-button @click="$router.push('/')">返回首页</el-button>
          <el-button type="primary" @click="checkStatus" :loading="checking">刷新状态</el-button>
        </div>
      </div>
    </div>

    <!-- Approved: full layout -->
    <template v-else>
      <aside class="sidebar">
        <div class="sidebar-header">
          <el-avatar :size="48" :src="profile?.avatar" class="n-avatar">
            {{ profile?.name?.charAt(0) || 'N' }}
          </el-avatar>
          <div class="profile-info">
            <h3>{{ profile?.name || '营养师' }}</h3>
            <p>{{ profile?.title }}</p>
          </div>
        </div>
        <div class="status-switch">
          <span>在线状态</span>
          <el-switch
            v-model="isOnline"
            active-text="在线"
            inactive-text="离线"
            @change="toggleStatus"
            :loading="statusLoading"
          />
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/nutritionist/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>工作台</span>
          </el-menu-item>
          <el-menu-item index="/nutritionist/consultations">
            <el-icon><List /></el-icon>
            <span>咨询记录</span>
          </el-menu-item>
          <el-menu-item index="/nutritionist/chat">
            <el-icon><ChatDotRound /></el-icon>
            <span>消息中心</span>
            <el-badge v-if="unreadCount > 0" :value="unreadCount" class="unread-badge" />
          </el-menu-item>
        </el-menu>
        <div class="sidebar-footer">
          <el-button text @click="$router.push('/')">
            <el-icon><HomeFilled /></el-icon>
            返回首页
          </el-button>
          <el-button text type="danger" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出
          </el-button>
        </div>
      </aside>
      <main class="main-content">
        <router-view :profile="profile" />
      </main>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DataAnalysis, ChatDotRound, HomeFilled, SwitchButton, List } from '@element-plus/icons-vue'
import { getNutritionistProfile, updateNutritionistStatus } from '@/services/consultation'
import { useAuthStore } from '@/stores/auth'
import message from '@/utils/message'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const profile = ref(null)
const statusLoading = ref(false)
const ready = ref(false)
const checking = ref(false)
const approvalState = ref('PENDING')
const unreadCount = ref(0)

const activeMenu = computed(() => {
  if (route.path.startsWith('/nutritionist/chat')) return '/nutritionist/chat'
  return route.path
})

const isOnline = computed({
  get: () => profile.value?.status === 'ONLINE',
  set: () => {}
})

provide('nutritionistProfile', profile)

onMounted(async () => {
  await checkStatus()
  ready.value = true
})

async function checkStatus() {
  checking.value = true
  try {
    const res = await getNutritionistProfile()
    if (res.data.code === 200) {
      profile.value = res.data.data
      approvalState.value = res.data.data?.approvalStatus || 'PENDING'
    }
  } catch (e) {
    console.error('获取营养师信息失败', e)
    approvalState.value = 'PENDING'
  } finally {
    checking.value = false
  }
}

async function toggleStatus(val) {
  statusLoading.value = true
  try {
    const status = val ? 'ONLINE' : 'OFFLINE'
    const res = await updateNutritionistStatus(status)
    if (res.data.code === 200) {
      profile.value = res.data.data
      message.success(val ? '已上线' : '已下线')
    }
  } catch (e) {
    message.error('状态更新失败')
  } finally {
    statusLoading.value = false
  }
}

function handleLogout() {
  authStore.logout()
  router.push('/nutritionist/login')
}
</script>

<style scoped lang="scss">
.nutritionist-layout {
  display: flex;
  min-height: 100vh;
  background: #fdfbf7;
  font-family: 'Patrick Hand', cursive;
}

.approval-state-container {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #fdfbf7;
}

.approval-card {
  text-align: center;
  background: #fdfbf7;
  padding: 48px;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  box-shadow: 4px 4px 0px 0px #2d2d2d;
  border: 2px solid #2d2d2d;
  max-width: 480px;

  .state-icon { font-size: 64px; margin-bottom: 16px; }
  h2 { color: #2d2d2d; margin: 0 0 12px; font-size: 22px; font-family: 'Kalam', cursive; }
  p { color: #2d2d2d; margin: 0 0 20px; line-height: 1.6; font-size: 14px; font-family: 'Patrick Hand', cursive; }

  .approval-actions {
    display: flex;
    gap: 12px;
    justify-content: center;
    margin-top: 24px;
  }
}

.sidebar {
  width: 240px;
  background: #fdfbf7;
  border-right: 2px dashed #2d2d2d;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;

  .sidebar-header {
    padding: 24px 20px;
    display: flex;
    align-items: center;
    gap: 12px;
    border-bottom: 2px dashed #e5e0d8;
    background: #fdfbf7;

    .n-avatar {
      background: #2d5da1;
      color: #fdfbf7;
      font-weight: 600;
      border: 2px solid #2d2d2d;
    }

    .profile-info {
      h3 { margin: 0; font-size: 16px; font-weight: 600; color: #2d2d2d; font-family: 'Kalam', cursive; }
      p { margin: 2px 0 0; font-size: 12px; color: #2d2d2d; font-family: 'Patrick Hand', cursive; }
    }
  }

  .status-switch {
    padding: 16px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 2px dashed #e5e0d8;
    font-size: 14px;
    color: #2d2d2d;
    font-family: 'Patrick Hand', cursive;
  }

  .sidebar-menu {
    border-right: none;
    flex: 1;
    background: transparent;
    font-family: 'Patrick Hand', cursive;

    :deep(.el-menu-item) {
      font-family: 'Patrick Hand', cursive;
      color: #2d2d2d;
      &.is-active {
        color: #2d5da1;
        background: #e5e0d8;
        border-right: 3px solid #2d5da1;
      }
      &:hover {
        background: #e5e0d8;
      }
    }
  }

  .sidebar-footer {
    padding: 12px 16px;
    border-top: 2px dashed #e5e0d8;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }
}

.unread-badge {
  margin-left: 8px;
}

.main-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: #fdfbf7;
}
</style>
