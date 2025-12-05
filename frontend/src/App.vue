<template>
  <div id="app" :class="{ dark: isDark }">
    <router-view v-slot="{ Component }">
      <transition 
        name="fade" 
        mode="out-in" 
        @before-enter="onBeforeEnter"
        @after-leave="onTransitionEnd"
      >
        <component :is="Component" :key="$route.fullPath" />
      </transition>
    </router-view>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/theme'

const router = useRouter()
const themeStore = useThemeStore()
const isDark = computed(() => themeStore.isDark)

// 过渡开始前清理
const onBeforeEnter = () => {
  // 立即清理遮罩层
  removeInvalidOverlays()
}

// 过渡结束回调，确保动画完成
const onTransitionEnd = () => {
  // 清理可能残留的遮罩层
  cleanupOverlays()
}

// 清理遮罩层的函数
const cleanupOverlays = () => {
  // 立即清理一次
  removeInvalidOverlays()
  
  // 延迟再清理一次，确保彻底
  setTimeout(() => {
    removeInvalidOverlays()
  }, 100)
  
  // 再延迟清理一次（针对慢速动画）
  setTimeout(() => {
    removeInvalidOverlays()
  }, 500)
}

const removeInvalidOverlays = () => {
  // 首先检查是否有活动的弹窗组件
  const hasMessageBox = document.querySelector('.el-message-box__wrapper')
  const hasDialog = document.querySelector('.el-dialog__wrapper')
  const hasDrawer = document.querySelector('.el-drawer__wrapper')
  
  // 如果有活动的弹窗，不要清理任何遮罩层
  // 因为 Element Plus 的遮罩层是直接添加到 body 下的，不在包装器内
  if (hasMessageBox || hasDialog || hasDrawer) {
    return
  }
  
  // 清理所有直接挂载在 body 下的遮罩层
  const bodyOverlays = document.querySelectorAll('body > .el-overlay, body > .el-loading-mask')
  
  if (bodyOverlays.length > 0) {
    console.log('🧹 清理所有孤立遮罩层:', bodyOverlays.length)
    bodyOverlays.forEach(overlay => {
      overlay.remove()
    })
  }
}

// 路由切换后清理遮罩层（由 overlayManager 统一管理）
onMounted(() => {
  // 添加全局点击监听，检测是否有遮罩层阻止点击
  document.addEventListener('click', (e) => {
    // 检查点击位置是否有遮罩层
    const elementsAtPoint = document.elementsFromPoint(e.clientX, e.clientY)
    const overlayElements = elementsAtPoint.filter(el => 
      el.classList && (el.classList.contains('el-overlay') || el.classList.contains('el-loading-mask'))
    )
    
    if (overlayElements.length > 0) {
      console.warn('⚠️ 检测到点击位置有遮罩层！立即清理...', overlayElements)
      
      // 立即移除所有遮罩层
      overlayElements.forEach(overlay => {
        console.log('💥 立即移除遮罩层:', overlay)
        overlay.remove()
      })
      
      // 再次清理所有遮罩层
      removeInvalidOverlays()
    }
  }, true) // 使用捕获阶段，优先级最高
  
  // 添加鼠标移动监听，实时检测遮罩层
  let lastCheckTime = 0
  document.addEventListener('mousemove', (e) => {
    const now = Date.now()
    // 节流：每 50ms 检查一次
    if (now - lastCheckTime < 50) {
      return
    }
    lastCheckTime = now
    
    // 检查鼠标位置是否有遮罩层
    const elementsAtPoint = document.elementsFromPoint(e.clientX, e.clientY)
    const overlayElements = elementsAtPoint.filter(el => 
      el.classList && (el.classList.contains('el-overlay') || el.classList.contains('el-loading-mask'))
    )
    
    if (overlayElements.length > 0) {
      // 检查是否有活动的弹窗
      const hasActiveModal = document.querySelector('.el-message-box__wrapper, .el-dialog__wrapper, .el-drawer__wrapper')
      
      if (!hasActiveModal) {
        console.warn('🖱️ 鼠标移动检测到遮罩层，立即清理...')
        overlayElements.forEach(overlay => overlay.remove())
      }
    }
  })
})
</script>

<style>
#app {
  min-height: 100vh;
  background-color: var(--el-bg-color);
  transition: background-color 0.3s ease;
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 防止过渡动画卡住导致的遮罩层问题 */
.fade-leave-active {
  position: absolute;
  width: 100%;
  z-index: -1 !important;
  pointer-events: none !important;
}

.fade-enter-active {
  z-index: 1 !important;
}

/* 最激进的策略：直接移除所有直接在 body 下的遮罩层 */
body > .el-overlay,
body > .el-loading-mask {
  /* 完全隐藏并禁用交互 */
  display: none !important;
  visibility: hidden !important;
  pointer-events: none !important;
  opacity: 0 !important;
  z-index: -9999 !important;
  /* 确保不占用空间 */
  position: absolute !important;
  width: 0 !important;
  height: 0 !important;
  overflow: hidden !important;
}

/* 只有在有效的 wrapper 内的遮罩层才显示 */
.el-message-box__wrapper > .el-overlay,
.el-dialog__wrapper > .el-overlay,
.el-drawer__wrapper > .el-overlay {
  display: block !important;
  visibility: visible !important;
  pointer-events: auto !important;
  opacity: 1 !important;
  z-index: 2000 !important;
  position: fixed !important;
  width: 100% !important;
  height: 100% !important;
  overflow: visible !important;
}

/* 确保所有交互元素可以点击 */
button, a, input, textarea, select, 
.el-button, .el-input, .el-select,
.el-card, .el-form, .el-table {
  pointer-events: auto !important;
  position: relative !important;
  z-index: 1 !important;
}
</style>
