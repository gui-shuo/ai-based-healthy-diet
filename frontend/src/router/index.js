import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/ProfileView.vue'),
    meta: { requiresAuth: true, title: '个人中心' }
  },
  {
    path: '/membership',
    name: 'Membership',
    component: () => import('@/views/MemberView.vue'),
    meta: { requiresAuth: true, title: '会员中心' }
  },
  {
    path: '/ai-chat',
    name: 'AIChat',
    component: () => import('@/views/AIChatView.vue'),
    meta: { requiresAuth: true, title: 'AI营养师' }
  },
  {
    path: '/food-records',
    name: 'FoodRecords',
    component: () => import('@/views/FoodRecordView.vue'),
    meta: { requiresAuth: true, title: '饮食记录' }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/AdminView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true, title: '后台管理' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/HomeView.vue'),
    meta: { title: '页面未找到' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - AI健康饮食规划助手` : 'AI健康饮食规划助手'

  // 如果已登录，不允许访问登录/注册页面
  if (to.meta.hideForAuth && authStore.isLoggedIn) {
    ElMessage.info('您已登录')
    next({ name: 'Home' })
    return
  }

  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    if (!authStore.isLoggedIn) {
      ElMessage.warning('请先登录')
      next({
        name: 'Login',
        query: { redirect: to.fullPath }
      })
      return
    }

    // 检查是否需要管理员权限
    if (to.meta.requiresAdmin && !authStore.isAdmin) {
      ElMessage.error('您没有访问权限')
      next({ name: 'Home' })
      return
    }
  }

  next()
})

// 全局后置钩子
router.afterEach((to, from) => {
  // 可以在这里添加页面访问统计等逻辑
  console.log(`路由跳转: ${from.path} -> ${to.path}`)
})

export default router
