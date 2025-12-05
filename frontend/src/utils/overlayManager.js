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

    const overlays = document.querySelectorAll('body > .el-overlay, body > .el-loading-mask')
    
    if (overlays.length > 0) {
      console.log('🧹 清理孤立遮罩层:', overlays.length)
      overlays.forEach(overlay => {
        overlay.remove()
      })
    }
  }

  /**
   * 启动定时清理
   */
  startCleanup() {
    if (this.cleanupInterval) {
      return
    }

    // 提高清理频率到 100ms（之前是 200ms）
    this.cleanupInterval = setInterval(() => {
      this.cleanupAllOverlays()
    }, 100)
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
            // 延迟检查，给 Element Plus 时间创建完整的结构
            // MessageBox 创建时，遮罩层会先于包装器创建
            setTimeout(() => {
              // 检查是否在有效的包装器内
              const isInWrapper = node.closest('.el-message-box__wrapper, .el-dialog__wrapper, .el-drawer__wrapper')
              
              // 检查是否有活动的弹窗
              const hasActiveModal = document.querySelector('.el-message-box__wrapper, .el-dialog__wrapper, .el-drawer__wrapper')
              
              // 只有在没有活动弹窗且不在包装器内时才移除
              if (!hasActiveModal && !isInWrapper && node.parentElement === document.body) {
                console.log('👀 检测到孤立遮罩层，延迟移除:', node)
                node.remove()
              }
            }, 100) // 延迟 100ms，给 Element Plus 足够的时间创建包装器
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
