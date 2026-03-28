import { ref } from 'vue'
import api from '@/services/api'

const CACHE_KEY = 'nutriai_public_config'

/**
 * 公开配置组合式函数
 * 使用 localStorage 缓存消除首次渲染闪烁
 */
export function usePublicConfig() {
  // 优先从缓存读取，避免页面闪烁
  let cached = {}
  try { cached = JSON.parse(localStorage.getItem(CACHE_KEY) || '{}') } catch { cached = {} }
  const config = ref(cached)
  const loading = ref(false)
  const error = ref(null)

  /**
   * 加载所有公开配置（先用缓存渲染，再异步更新）
   */
  const loadConfig = async () => {
    loading.value = true
    error.value = null

    try {
      const { data } = await api.get('/public/config')

      if (data.code === 200) {
        config.value = data.data
        // 写入缓存供下次立即使用
        try { localStorage.setItem(CACHE_KEY, JSON.stringify(data.data)) } catch {}
        return config.value
      } else {
        error.value = data.message
        console.error('加载公开配置失败:', data.message)
      }
    } catch (err) {
      error.value = err.message
      console.error('加载公开配置失败:', err)
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取特定配置值
   */
  const getConfig = (key, defaultValue = '') => {
    return config.value[key] || defaultValue
  }

  /**
   * 应用网站配置到页面
   */
  const applyConfig = () => {
    // 应用网站名称
    if (config.value['system.site_name']) {
      document.title = config.value['system.site_name']
    }

    // 应用网站图标
    if (config.value['system.site_icon']) {
      const link = document.querySelector("link[rel*='icon']") || document.createElement('link')
      link.type = 'image/x-icon'
      link.rel = 'shortcut icon'
      link.href = config.value['system.site_icon']
      document.getElementsByTagName('head')[0].appendChild(link)
    }

    // 检查维护模式
    if (config.value['system.maintenance_mode'] === 'true') {
      console.warn('系统正在维护中')
      // 可以在这里显示维护页面
    }
  }

  /**
   * 定期刷新配置（可选）
   */
  const startAutoRefresh = (intervalMs = 60000) => {
    const intervalId = setInterval(() => {
      loadConfig().then(() => {
        applyConfig()
      })
    }, intervalMs)

    // 返回清理函数
    return () => clearInterval(intervalId)
  }

  return {
    config,
    loading,
    error,
    loadConfig,
    getConfig,
    applyConfig,
    startAutoRefresh
  }
}
