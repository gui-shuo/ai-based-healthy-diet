import { defineStore } from 'pinia'
import { ref } from 'vue'
import { request } from '@/utils/request'

interface AppConfig {
  siteName: string
  siteDescription: string
  contactEmail: string
  contactPhone: string
  copyright: string
  icp: string
}

export const useAppStore = defineStore('app', () => {
  const config = ref<AppConfig>({
    siteName: 'NutriAI 智能营养助手',
    siteDescription: 'AI驱动的个性化健康饮食规划平台',
    contactEmail: '',
    contactPhone: '',
    copyright: '',
    icp: ''
  })

  async function fetchConfig() {
    try {
      const res = await request<any>({ url: '/public/config', showError: false })
      if (res.code === 200 && res.data) {
        const d = res.data
        config.value = {
          siteName: d.site_name || d.siteName || config.value.siteName,
          siteDescription: d.site_description || d.siteDescription || config.value.siteDescription,
          contactEmail: d.contact_email || d.contactEmail || '',
          contactPhone: d.contact_phone || d.contactPhone || '',
          copyright: d.copyright || '',
          icp: d.icp || ''
        }
      }
    } catch {}
  }

  return { config, fetchConfig }
})
