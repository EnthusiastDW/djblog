import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import { usePreferredDark } from '@vueuse/core'
import api from '@/api/index'

export const useAppStore = defineStore('app', () => {
  const preferredDark = usePreferredDark()
  const theme = ref(localStorage.getItem('theme') || (preferredDark.value ? 'dark' : 'light'))
  const sidebarCollapsed = ref(false)
  const bgImage = ref(localStorage.getItem('bgImage') || '')
  const bgOpacity = ref(parseFloat(localStorage.getItem('bgOpacity')) || 0.3)

  watch(theme, (newTheme) => {
    localStorage.setItem('theme', newTheme)
    document.documentElement.classList.toggle('dark', newTheme === 'dark')
  }, { immediate: true })

  watch(bgImage, (newBgImage) => {
    localStorage.setItem('bgImage', newBgImage)
  })

  watch(bgOpacity, (newBgOpacity) => {
    localStorage.setItem('bgOpacity', newBgOpacity.toString())
  })

  function toggleTheme() {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
  }

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setBackground(image, opacity) {
    if (image !== undefined) {
      bgImage.value = image
    }
    if (opacity !== undefined) {
      bgOpacity.value = opacity
    }
  }

  async function fetchSettings() {
    try {
      const res = await api.get('/setting')
      if (res.data) {
        if (res.data.bgImage) {
          bgImage.value = res.data.bgImage
        }
        if (res.data.bgOpacity) {
          bgOpacity.value = parseFloat(res.data.bgOpacity)
        }
      }
    } catch (e) {
      console.error('获取设置失败', e)
    }
  }

  return {
    theme,
    sidebarCollapsed,
    bgImage,
    bgOpacity,
    toggleTheme,
    toggleSidebar,
    setBackground,
    fetchSettings
  }
})
