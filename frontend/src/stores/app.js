import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import { usePreferredDark } from '@vueuse/core'

export const useAppStore = defineStore('app', () => {
  const preferredDark = usePreferredDark()
  const theme = ref(localStorage.getItem('theme') || (preferredDark.value ? 'dark' : 'light'))
  const sidebarCollapsed = ref(false)

  // 监听主题变化
  watch(theme, (newTheme) => {
    localStorage.setItem('theme', newTheme)
    document.documentElement.classList.toggle('dark', newTheme === 'dark')
  }, { immediate: true })

  function toggleTheme() {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
  }

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  return {
    theme,
    sidebarCollapsed,
    toggleTheme,
    toggleSidebar
  }
})
