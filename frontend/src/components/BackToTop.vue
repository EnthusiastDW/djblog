<template>
  <Teleport to="body">
    <div 
      class="back-to-top" 
      :class="{ 'is-visible': showButton }"
      @click="scrollToTop"
    >
      <div class="progress-ring">
        <svg class="progress-svg" viewBox="0 0 36 36">
          <circle
            class="progress-circle"
            cx="18"
            cy="18"
            r="16"
          />
          <circle
            class="progress-bar"
            cx="18"
            cy="18"
            r="16"
            :style="progressStyle"
          />
        </svg>
        <el-icon class="back-icon"><ArrowUp /></el-icon>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ArrowUp } from '@element-plus/icons-vue'

const showButton = ref(false)
const scrollProgress = ref(0)

const progressStyle = computed(() => {
  const strokeDashoffset = 100 - scrollProgress.value
  return {
    strokeDasharray: '100',
    strokeDashoffset: strokeDashoffset
  }
})

function handleScroll() {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = document.documentElement.clientHeight
  
  const scrollPercentage = (scrollTop / (scrollHeight - clientHeight)) * 100
  scrollProgress.value = Math.min(100, Math.max(0, scrollPercentage))
  
  showButton.value = scrollTop > 300
}

function scrollToTop() {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  // 初始化时触发一次
  handleScroll()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style lang="scss" scoped>
.back-to-top {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--el-color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  z-index: 1000;
  opacity: 0;
  transform: translateY(20px);
  pointer-events: none;
  
  &.is-visible {
    opacity: 1;
    transform: translateY(0);
    pointer-events: auto;
  }
  
  &:hover {
    background: var(--el-color-primary-light-3);
    transform: translateY(-2px);
  }
  
  &:active {
    transform: translateY(0);
  }
}

.progress-ring {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
  position: absolute;
  top: 0;
  left: 0;
}

.progress-circle {
  fill: none;
  stroke: rgba(255, 255, 255, 0.3);
  stroke-width: 2;
}

.progress-bar {
  fill: none;
  stroke: #fff;
  stroke-width: 2;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.3s ease;
}

.back-icon {
  font-size: 20px;
  z-index: 1;
}

// 响应式调整
@media (max-width: 768px) {
  .back-to-top {
    bottom: 24px;
    right: 24px;
    width: 44px;
    height: 44px;
  }
  
  .back-icon {
    font-size: 18px;
  }
}
</style>