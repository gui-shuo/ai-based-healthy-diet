/**
 * 遮罩层管理器
 * 用于拦截和管理 Element Plus 的遮罩层创建
 */

class OverlayManager {
  constructor() {
    this.activeModals = new Set()
    this.cleanupInterval = null
    this.observer = null
  }

  /**
   * 初始化管理器
   */
  init() {
    console.log('🎯 OverlayManager 初始化...')
    
    // 启动定时清理
    this.startCleanup()
    
    // 启动 DOM 监听
    this.startObserver()
    
    // 拦截 Element Plus 的遮罩层创建
    this.interceptOverlayCreation()
  }

  /**
   * 注册活动的弹窗
   */
  registerModal(type, id) {
    this.activeModals.add(`${type}-${id}`)
    console.log('📝 注册弹窗:', type, id, '当前活动弹窗数:', this.activeModals.size)
  }

  /**
   * 注销活动的弹窗
   */
  unregisterModal(type, id) {
    this.activeModals.delete(`${type}-${id}`)
    console.log('🗑️ 注销弹窗:', type, id, '当前活动弹窗数:', this.activeModals.size)
    
    // 如果没有活动的弹窗，立即清理所有遮罩层
    if (this.activeModals.size === 0) {
      setTimeout(() => this.cleanupAllOverlays(), 50)
    }
  }

  /**
   * 检查是否有活动的弹窗
   */
  hasActiveModals() {
    return this.activeModals.size > 0
  }

  /**
   * 清理所有孤立的遮罩层
   */
  cleanupAllOverlays() {
    if (this.hasActiveModals()) {
      return
    }

    // 检查是否有活动的对话框（更准确的检测）
    const hasActiveDialog = document.querySelector('.el-dialog')
    const hasActiveMessageBox = document.querySelector('.el-message-box')
    const hasActiveDrawer = document.querySelector('.el-drawer')
    
    if (hasActiveDialog || hasActiveMessageBox || hasActiveDrawer) {
      return
    }

    const overlays = document.querySelectorAll('body > .el-overlay, body > .el-loading-mask')
    
    overlays.forEach(overlay => {
      // 只清理真正空的遮罩层
      const hasContent = overlay.querySelector('.el-dialog, .el-message-box, .el-drawer')
      if (!hasContent && overlay.children.length === 0) {
        console.log('🧹 清理空遮罩层')
        overlay.remove()
      }
    })
  }

  /**
   * 启动定时清理
   */
  startCleanup() {
    if (this.cleanupInterval) {
      return
    }

    // 降低清理频率到 2000ms，避免过于激进
    this.cleanupInterval = setInterval(() => {
      this.cleanupAllOverlays()
    }, 2000)
  }

  /**
   * 停止定时清理
   */
  stopCleanup() {
    if (this.cleanupInterval) {
      clearInterval(this.cleanupInterval)
      this.cleanupInterval = null
    }
  }

  /**
   * 启动 DOM 监听
   */
  startObserver() {
    if (this.observer) {
      return
    }

    this.observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        mutation.addedNodes.forEach((node) => {
          if (node.nodeType === 1 && node.classList && node.classList.contains('el-overlay')) {
            // 延迟检查，给 Element Plus 足够时间创建完整的结构
            setTimeout(() => {
              // 检查遮罩层内是否有内容
              const hasContent = node.querySelector('.el-dialog, .el-message-box, .el-drawer')
              
              // 检查是否有活动的对话框
              const hasActiveDialog = document.querySelector('.el-dialog')
              const hasActiveMessageBox = document.querySelector('.el-message-box')
              const hasActiveDrawer = document.querySelector('.el-drawer')
              
              // 只有在没有任何活动弹窗且遮罩层为空时才移除
              if (!hasActiveDialog && !hasActiveMessageBox && !hasActiveDrawer && 
                  !hasContent && node.children.length === 0 && node.parentElement === document.body) {
                console.log('👀 检测到空遮罩层，移除')
                node.remove()
              }
            }, 500) // 延迟 500ms，给 Element Plus 足够的时间
          }
        })
      })
    })

    this.observer.observe(document.body, {
      childList: true,
      subtree: false
    })
  }

  /**
   * 停止 DOM 监听
   */
  stopObserver() {
    if (this.observer) {
      this.observer.disconnect()
      this.observer = null
    }
  }

  /**
   * 拦截 Element Plus 的遮罩层创建
   */
  interceptOverlayCreation() {
    // 不再拦截 appendChild，因为会导致 MessageBox 的遮罩层被误拦截
    // Element Plus 创建 MessageBox 时，遮罩层会先于包装器创建
    // 所以在遮罩层创建时检查，包装器还不存在，会被误判为孤立遮罩层
    
    console.log('✅ 遮罩层拦截器已禁用（改用延迟清理策略）')
  }

  /**
   * 销毁管理器
   */
  destroy() {
    this.stopCleanup()
    this.stopObserver()
    this.activeModals.clear()
  }
}

// 创建单例
const overlayManager = new OverlayManager()

export default overlayManager
