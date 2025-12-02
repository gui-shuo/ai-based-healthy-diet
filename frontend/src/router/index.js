import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

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
    path: '/user',
    component: () => import('@/layouts/UserLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/user/ProfileView.vue'),
        meta: { title: '个人资料' }
      },
      {
        path: 'records',
        name: 'DietRecords',
        component: () => import('@/views/user/DietRecordsView.vue'),
        meta: { title: '饮食记录' }
      },
      {
        path: 'plans',
        name: 'DietPlans',
        component: () => import('@/views/user/DietPlansView.vue'),
        meta: { title: '我的计划' }
      },
      {
        path: 'favorites',
        name: 'Favorites',
        component: () => import('@/views/user/FavoritesView.vue'),
        meta: { title: '收藏夹' }
      }
    ]
  },
  {
    path: '/membership',
    name: 'Membership',
    component: () => import('@/views/membership/MembershipView.vue'),
    meta: { requiresAuth: true, title: '会员中心' }
  },
  {
    path: '/ai-chat',
    name: 'AIChat',
    component: () => import('@/views/ai/ChatView.vue'),
    meta: { requiresAuth: true, title: 'AI营养师' }
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: { title: '数据看板' }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/admin/UserManagementView.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'ai-logs',
        name: 'AILogs',
        component: () => import('@/views/admin/AILogsView.vue'),
        meta: { title: 'AI日志' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFoundView.vue'),
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

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  // 设置页面标题
  document.title = to.meta.title 
    ? `${to.meta.title} - AI健康饮食规划助手` 
    : 'AI健康饮食规划助手'
  
  // 检查是否需要登录
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }
  
  // 检查是否需要管理员权限
  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    next({ name: 'Home' })
    return
  }
  
  next()
})

export default router
