import { defineStore } from 'pinia'
import { ref, watch, computed } from 'vue'
import { usePreferredDark } from '@vueuse/core'
import api from '@/api/index'

export const useAppStore = defineStore('app', () => {
  const preferredDark = usePreferredDark()

  // 主题模式: 'light' | 'dark' | 'system'
  const themeMode = ref(localStorage.getItem('themeMode') || 'system')

  // 计算实际应用的主题
  const theme = computed(() => {
    if (themeMode.value === 'system') {
      return preferredDark.value ? 'dark' : 'light'
    }
    return themeMode.value
  })

  // 监听 theme 变化，切换 dark 类
  watch(theme, (newTheme) => {
    document.documentElement.classList.toggle('dark', newTheme === 'dark')
  }, { immediate: true })

  // 监听 themeMode 变化，保存到 localStorage
  watch(themeMode, (newMode) => {
    localStorage.setItem('themeMode', newMode)
  })

  const sidebarCollapsed = ref(false)
  const leftSidebarVisible = ref(false)
  const bgImage = ref(localStorage.getItem('bgImage') || '')
  const bgOpacity = ref(parseFloat(localStorage.getItem('bgOpacity')) || 0.2)
  const todayVisitors = ref(0)
  const totalVisitors = ref(0)

  watch(bgImage, (newBgImage) => {
    localStorage.setItem('bgImage', newBgImage)
  })

  watch(bgOpacity, (newBgOpacity) => {
    localStorage.setItem('bgOpacity', newBgOpacity.toString())
  })

  function toggleTheme() {
    // 三种模式循环切换: light -> dark -> system -> light
    if (themeMode.value === 'light') {
      themeMode.value = 'dark'
    } else if (themeMode.value === 'dark') {
      themeMode.value = 'system'
    } else {
      themeMode.value = 'light'
    }
  }

  function setThemeMode(mode) {
    themeMode.value = mode
  }

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function toggleLeftSidebar() {
    leftSidebarVisible.value = !leftSidebarVisible.value
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
        if (res.data.bgOpacity !== undefined && res.data.bgOpacity !== null) {
          bgOpacity.value = parseFloat(res.data.bgOpacity)
        }
      }
    } catch (e) {
      console.error('获取设置失败', e)
    }
  }

  async function fetchVisitStats() {
    try {
      const res = await api.get('/visit/stats')
      todayVisitors.value = res.data?.todayVisitors ?? 0
      totalVisitors.value = res.data?.totalVisitors ?? 0
    } catch (e) {
      console.error('获取访问统计失败', e)
    }
  }

  return {
    theme,
    themeMode,
    sidebarCollapsed,
    leftSidebarVisible,
    bgImage,
    bgOpacity,
    todayVisitors,
    totalVisitors,
    toggleTheme,
    setThemeMode,
    toggleSidebar,
    toggleLeftSidebar,
    setBackground,
    fetchSettings,
    fetchVisitStats
  }
})
