<template>
  <div class="admin-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px" class="admin-aside">
        <div class="logo">
          <h2>管理后台</h2>
        </div>
        <el-menu :default-active="activeMenu" class="admin-menu" @select="handleMenuSelect">
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据看板</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/nutritionists">
            <el-icon><UserFilled /></el-icon>
            <span>营养师管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/ai-logs">
            <el-icon><ChatDotRound /></el-icon>
            <span>AI日志</span>
          </el-menu-item>
          <el-menu-item index="/admin/feedbacks">
            <el-icon><ChatLineSquare /></el-icon>
            <span>反馈管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/announcements">
            <el-icon><Bell /></el-icon>
            <span>公告管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/community">
            <el-icon><ChatDotRound /></el-icon>
            <span>社区管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/products">
            <el-icon><ShoppingCart /></el-icon>
            <span>产品管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/app-versions">
            <el-icon><Download /></el-icon>
            <span>APP版本</span>
          </el-menu-item>
          <el-menu-item index="/admin/config">
            <el-icon><Setting /></el-icon>
            <span>系统配置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部栏 -->
        <el-header class="admin-header">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }"> 首页 </el-breadcrumb-item>
            <el-breadcrumb-item>{{ breadcrumbTitle }}</el-breadcrumb-item>
          </el-breadcrumb>

          <div class="header-right">
            <AlertNotification />
            <span class="username">{{ userInfo.username }}</span>
            <el-button type="primary" link @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-button>
          </div>
        </el-header>

        <!-- 内容区 -->
        <el-main class="admin-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DataAnalysis,
  User,
  ChatDotRound,
  Setting,
  Bell,
  SwitchButton,
  ChatLineSquare,
  UserFilled,
  ShoppingCart,
  Download
} from '@element-plus/icons-vue'
import AlertNotification from '@/components/admin/AlertNotification.vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const userInfo = computed(() => ({
  username: authStore.user?.username || authStore.userName || 'Admin'
}))

const activeMenu = computed(() => route.path)

const breadcrumbTitle = computed(() => {
  const titles = {
    '/admin/dashboard': '数据看板',
    '/admin/users': '用户管理',
    '/admin/ai-logs': 'AI日志',
    '/admin/config': '系统配置',
    '/admin/announcements': '公告管理',
    '/admin/feedbacks': '反馈管理',
    '/admin/nutritionists': '营养师管理',
    '/admin/products': '产品管理',
    '/admin/community': '社区管理',
    '/admin/app-versions': 'APP版本管理'
  }
  return titles[route.path] || '管理后台'
})

const handleMenuSelect = index => {
  router.push(index)
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      center: true,
      customClass: 'custom-message-box',
      showClose: true,
      closeOnClickModal: false
    })

    await authStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  // 由路由守卫保证已登录，这里仅做二次确认
  if (!authStore.token) {
    ElMessage.error('请先登录')
    router.push('/login')
  }
})
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
  font-family: 'Patrick Hand', 'Kalam', cursive, sans-serif;
}

.el-container {
  height: 100%;
}

.admin-aside {
  background: #fdfbf7;
  color: #2d2d2d;
  border-right: 2.5px solid #2d2d2d;
  border-image: repeating-linear-gradient(
    180deg,
    #2d2d2d 0px,
    #2d2d2d 8px,
    transparent 8px,
    transparent 12px
  ) 1;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff9c4;
  border-bottom: 2px dashed #e5e0d8;
}

.logo h2 {
  color: #2d2d2d;
  margin: 0;
  font-size: 20px;
  font-family: 'ZCOOL KuaiLe', 'Kalam', cursive;
  letter-spacing: 1px;
}

.admin-menu {
  border: none;
  background: #fdfbf7;
}

.admin-menu :deep(.el-menu-item) {
  color: #2d2d2d;
  font-family: 'Patrick Hand', cursive, sans-serif;
  font-size: 15px;
  border-radius: 0;
  margin: 2px 8px;
  transition: all 0.2s;
}

.admin-menu :deep(.el-menu-item:hover) {
  color: #2d2d2d;
  background: rgba(229, 224, 216, 0.45);
  border-radius: 6px;
}

.admin-menu :deep(.el-menu-item.is-active) {
  color: #2d2d2d;
  font-weight: 700;
  background: rgba(255, 77, 77, 0.08);
  border-left: 3.5px solid #ff4d4d;
  border-radius: 0 6px 6px 0;
}

.admin-header {
  background: #fdfbf7;
  border-bottom: 2px dashed #e5e0d8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.username {
  color: #2d2d2d;
  font-size: 14px;
  font-family: 'Patrick Hand', cursive;
}

.admin-main {
  background: #fdfbf7;
  padding: 24px;
  overflow-y: auto;
}
</style>
