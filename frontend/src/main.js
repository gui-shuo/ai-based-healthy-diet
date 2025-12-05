import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import overlayManager from './utils/overlayManager'

// 样式导入
import './styles/main.scss'
import 'element-plus/theme-chalk/dark/css-vars.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// 初始化遮罩层管理器
overlayManager.init()

// 在路由切换前和切换后都清理遮罩层
router.beforeEach((to, from, next) => {
  // 路由切换前立即清理
  overlayManager.cleanupAllOverlays()
  next()
})

router.afterEach(() => {
  // 路由切换后立即清理
  overlayManager.cleanupAllOverlays()
  
  // 再延迟清理一次，确保彻底
  setTimeout(() => {
    overlayManager.cleanupAllOverlays()
  }, 50)
  
  // 第三次清理
  setTimeout(() => {
    overlayManager.cleanupAllOverlays()
  }, 200)
})

app.mount('#app')
