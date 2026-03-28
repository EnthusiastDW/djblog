<template>
  <el-container class="front-layout">
    <div class="bg-image" :style="bgStyle"></div>
    <div class="bg-overlay" :style="{ opacity: appStore.bgOpacity }"></div>
    <el-header class="front-header">
      <app-header />
    </el-header>
    <el-container class="front-main-container">
      <el-main class="front-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
      <el-aside class="front-aside" width="300px">
        <app-sidebar />
      </el-aside>
    </el-container>
    <el-footer class="front-footer">
      <app-footer />
    </el-footer>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

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
  padding: 0;
  backdrop-filter: blur(10px);
}

.front-main-container {
  margin-top: 60px;
  flex: 1;
}

.front-main {
  padding: 24px;
  background: rgba(255, 255, 255, 0.8);
  min-height: calc(100vh - 120px);
  backdrop-filter: blur(10px);
}

.front-aside {
  background: rgba(255, 255, 255, 0.8);
  padding: 16px;
  border-left: 1px solid var(--el-border-color-lighter);
  backdrop-filter: blur(10px);
}

.front-footer {
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

@media (max-width: 992px) {
  .front-aside {
    display: none;
  }
}
</style>
