<template>
  <el-container class="front-layout">
    <div class="bg-image" :style="bgStyle"></div>
    <div class="bg-overlay" :style="{ opacity: bgOpacity }"></div>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const router = useRouter()
const bgImage = ref('')
const bgOpacity = ref(0.3)

const bgStyle = computed(() => {
  if (!bgImage.value) return { display: 'none' }
  return {
    backgroundImage: `url("${bgImage.value}")`,
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    zIndex: -2,
    backgroundSize: 'cover',
    backgroundPosition: 'center'
  }
})

onMounted(async () => {
  const localBgImage = localStorage.getItem('bgImage')
  const localOpacity = localStorage.getItem('bgOpacity')
  if (localBgImage) bgImage.value = localBgImage
  if (localOpacity) bgOpacity.value = parseFloat(localOpacity)
  
  try {
    const res = await fetch('/api/setting', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
      }
    })
    const data = await res.json()
    console.log('Settings response:', data)
    if (data.code === 200 && data.data) {
      if (data.data.bgImage) {
        bgImage.value = data.data.bgImage
        localStorage.setItem('bgImage', data.data.bgImage)
      }
      if (data.data.bgOpacity) {
        bgOpacity.value = parseFloat(data.data.bgOpacity)
        localStorage.setItem('bgOpacity', data.data.bgOpacity)
      }
    }
  } catch (e) {
    console.error('获取背景设置失败', e)
  }
})
</script>

<style lang="scss" scoped>
.front-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.bg-image {
  display: none;
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
}

.front-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-lighter);
  z-index: 100;
  padding: 0;
}

.front-main-container {
  margin-top: 60px;
  flex: 1;
}

.front-main {
  padding: 24px;
  background: var(--el-bg-color-page);
  min-height: calc(100vh - 120px);
}

.front-aside {
  background: var(--el-bg-color);
  padding: 16px;
  border-left: 1px solid var(--el-border-color-lighter);
}

.front-footer {
  height: auto;
  padding: 0;
  background: var(--el-bg-color);
  border-top: 1px solid var(--el-border-color-lighter);
}

@media (max-width: 992px) {
  .front-aside {
    display: none;
  }
}
</style>
