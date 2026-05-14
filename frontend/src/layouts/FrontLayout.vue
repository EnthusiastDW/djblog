<template>
  <div class="front-layout">
    <div class="bg-image" :style="bgStyle"></div>
    <div class="bg-overlay" :style="{ opacity: appStore.bgOpacity }"></div>
    <header class="front-header">
      <app-header />
    </header>
    <div class="front-main-container">
      <!-- 左侧边栏 -->
      <aside class="left-sidebar" :class="{ 'left-sidebar-hidden': isMobile && !appStore.leftSidebarVisible }">
        <app-left-sidebar />
      </aside>
      <main class="front-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
      <!-- 右侧边栏 -->
      <aside class="front-aside" :class="{ 'sidebar-hidden': isMobile && appStore.sidebarCollapsed }">
        <app-sidebar />
      </aside>
    </div>
    <!-- 左侧边栏遮罩 -->
    <div class="left-sidebar-overlay" :class="{ 'overlay-visible': isMobile && appStore.leftSidebarVisible }" @click="handleToggleLeftSidebar"></div>
    <!-- 右侧边栏遮罩 -->
    <div class="sidebar-overlay" :class="{ 'overlay-visible': isMobile && !appStore.sidebarCollapsed }" @click="handleToggleSidebar"></div>
    <footer class="front-footer">
      <app-footer />
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import { useMediaQuery } from '@vueuse/core'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppLeftSidebar from '@/components/layout/AppLeftSidebar.vue'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const isMobile = useMediaQuery('(max-width: 992px)')

function handleToggleSidebar() {
  appStore.toggleSidebar()
}

function handleToggleLeftSidebar() {
  appStore.toggleLeftSidebar()
}

watch(isMobile, (newIsMobile) => {
  if (newIsMobile) {
    appStore.sidebarCollapsed = true
  }
}, { immediate: true })

const bgStyle = computed(() => {
  const baseStyle = {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    zIndex: -2,
    backgroundSize: 'cover',
    backgroundPosition: 'center'
  }
  
  if (appStore.bgImage) {
    return {
      ...baseStyle,
      backgroundImage: `url("${appStore.bgImage}")`
    }
  }
  
  return baseStyle
})

// 动态设置 CSS 变量
watch(() => appStore.bgOpacity, (newOpacity) => {
  document.documentElement.style.setProperty('--component-opacity', newOpacity.toString())
}, { immediate: true })

onMounted(() => {
  appStore.fetchSettings()
})
</script>

<style lang="scss">
.front-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.bg-image {
  // 背景图片样式由内联样式动态设置
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: -2;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-color: #f5f7fa;
}

.bg-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: -1;
  background: var(--el-bg-color-page);
  pointer-events: none;
  transition: opacity 0.3s ease;
}

.front-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: rgba(255, 255, 255, var(--component-opacity, 0.8));
  z-index: 100;
  backdrop-filter: blur(calc(var(--component-opacity, 0.8) * 12.5px));
}

.front-main-container {
  padding-top: 60px;
  flex: 1;
  display: flex;
}

.left-sidebar {
  background: rgba(255, 255, 255, var(--component-opacity, 0.8));
  padding: 16px;
  backdrop-filter: blur(calc(var(--component-opacity, 0.8) * 12.5px));
  width: 280px;
  transition: transform 0.3s ease;
}

.left-sidebar-hidden {
  display: none;
}

.front-main {
  padding: 24px;
  background: rgba(255, 255, 255, var(--component-opacity, 0.8));
  backdrop-filter: blur(calc(var(--component-opacity, 0.8) * 12.5px));
  flex: 1;
}

.front-aside {
  background: rgba(255, 255, 255, var(--component-opacity, 0.8));
  padding: 16px;
  backdrop-filter: blur(calc(var(--component-opacity, 0.8) * 12.5px));
  width: 300px;
  transition: transform 0.3s ease;
}

.sidebar-hidden {
  display: none;
}

.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 99;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.3s ease, visibility 0.3s ease;
}

.left-sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 99;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.3s ease, visibility 0.3s ease;
}

.overlay-visible {
  opacity: 1;
  visibility: visible;
}

.front-footer {
  width: 100%;
  height: auto;
  padding: 0;
  background: rgba(255, 255, 255, var(--component-opacity, 0.8));
  backdrop-filter: blur(calc(var(--component-opacity, 0.8) * 12.5px));
}

// 暗色主题支持
.dark .bg-image {
  background-color: #141414 !important;
}

.dark .front-header {
  background: rgba(20, 20, 20, var(--component-opacity, 0.8)) !important;
}

.dark .front-main {
  background: rgba(20, 20, 20, var(--component-opacity, 0.8)) !important;
}

.dark .front-aside {
  background: rgba(20, 20, 20, var(--component-opacity, 0.8)) !important;
}

.dark .left-sidebar {
  background: rgba(20, 20, 20, var(--component-opacity, 0.8)) !important;
}

.dark .front-footer {
  background: rgba(20, 20, 20, var(--component-opacity, 0.8)) !important;
}

.dark .sidebar-overlay {
  background: rgba(0, 0, 0, 0.7) !important;
}

@media (max-width: 992px) {
  .left-sidebar {
    position: fixed;
    top: 60px;
    left: 0;
    bottom: 0;
    z-index: 100;
    transform: translateX(-100%);
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
    overflow-y: auto;
    -webkit-overflow-scrolling: touch;
  }

  .left-sidebar:not(.left-sidebar-hidden) {
    transform: translateX(0);
  }

  .left-sidebar-hidden {
    display: block;
    transform: translateX(-100%);
  }

  .front-aside {
    position: fixed;
    top: 60px;
    right: 0;
    bottom: 0;
    z-index: 100;
    transform: translateX(100%);
    box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
    overflow-y: auto;
    -webkit-overflow-scrolling: touch;
  }

  .front-aside:not(.sidebar-hidden) {
    transform: translateX(0);
  }

  .sidebar-hidden {
    display: block;
    transform: translateX(100%);
  }

  .front-main {
    width: 100%;
  }
}
</style>
