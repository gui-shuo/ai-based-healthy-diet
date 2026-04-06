import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { ElMessage, ElMessageBox } from 'element-plus'
import App from './App.vue'
import router from './router'

// 样式导入
import './styles/main.scss'
import 'element-plus/theme-chalk/dark/css-vars.css'

// ---- Global ElMessage patch ----
// Every call across all files gets showClose + grouping automatically.
;['success', 'error', 'warning', 'info'].forEach(type => {
  const orig = ElMessage[type]
  ElMessage[type] = (options) => {
    if (typeof options === 'string') options = { message: options }
    return orig({ showClose: true, grouping: true, offset: 0, ...options })
  }
})

// ---- Global ElMessageBox patch ----
// Every confirm/prompt/alert gets consistent styling + showClose.
const msgBoxDefaults = {
  showClose: true,
  closeOnClickModal: false,
  draggable: false,
  customClass: 'nutriai-msgbox',
  confirmButtonText: '确定',
  cancelButtonText: '取消',
}
;['confirm', 'prompt', 'alert'].forEach(method => {
  const orig = ElMessageBox[method]
  ElMessageBox[method] = (message, title, options) => {
    if (typeof title === 'object') {
      options = title
      title = ''
    }
    return orig(message, title, { ...msgBoxDefaults, ...options })
  }
})

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

app.mount('#app')
