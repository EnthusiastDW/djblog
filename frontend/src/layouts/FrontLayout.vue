<template>
  <div class="front-layout">
    <div class="bg-image" :style="bgStyle"></div>
    <div class="bg-overlay" :style="{ opacity: appStore.bgOpacity }"></div>
    <header class="front-header">
      <app-header />
    </header>
    <div class="front-main-container">
      <main class="front-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
      <aside class="front-aside" :class="{ 'sidebar-hidden': isMobile && appStore.sidebarCollapsed }">
        <app-sidebar />
      </aside>
    </div>
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
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const isMobile = useMediaQuery('(max-width: 992px)')

function handleToggleSidebar() {
  appStore.toggleSidebar()
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
  background: rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid var(--el-border-color-lighter);
  z-index: 100;
  backdrop-filter: blur(10px);
}

.front-main-container {
  padding-top: 60px;
  flex: 1;
  display: flex;
}

.front-main {
  padding: 24px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  flex: 1;
}

.front-aside {
  background: rgba(255, 255, 255, 0.8);
  padding: 16px;
  border-left: 1px solid var(--el-border-color-lighter);
  backdrop-filter: blur(10px);
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

.overlay-visible {
  opacity: 1;
  visibility: visible;
}

.front-footer {
  width: 100%;
  height: auto;
  padding: 0;
  background: rgba(255, 255, 255, 0.8);
  border-top: 1px solid var(--el-border-color-lighter);
  backdrop-filter: blur(10px);
}

// 暗色主题支持
.dark .bg-image {
  background-color: #141414 !important;
}

.dark .front-header {
  background: rgba(20, 20, 20, 0.8) !important;
}

.dark .front-main {
  background: rgba(20, 20, 20, 0.8) !important;
}

.dark .front-aside {
  background: rgba(20, 20, 20, 0.8) !important;
}

.dark .front-footer {
  background: rgba(20, 20, 20, 0.8) !important;
}

.dark .sidebar-overlay {
  background: rgba(0, 0, 0, 0.7) !important;
}

@media (max-width: 992px) {
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
