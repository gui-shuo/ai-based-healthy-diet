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

// 初始化遮罩层管理器（使用更保守的策略）
overlayManager.init()

// 不再在路由切换时激进清理遮罩层
// 让 Element Plus 自己管理对话框的生命周期

app.mount('#app')
