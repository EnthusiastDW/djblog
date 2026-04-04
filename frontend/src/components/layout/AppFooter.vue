<template>
  <footer class="app-footer">
    <div class="footer-container">
      <div class="footer-content">
        <p class="copyright">
          © {{ currentYear }} DJ Blog. All rights reserved.
        </p>
        <p class="icp" v-if="icpNumber">
          <a :href="'https://beian.miit.gov.cn/'" target="_blank" rel="noopener noreferrer">
            {{ icpNumber }}
          </a>
        </p>
        <p class="powered">
          Powered by Vue 3 + Spring Boot
        </p>
      </div>
      <div class="footer-stats">
        <span class="stat-item">
          <span class="stat-value">{{ todayVisitors }}</span>
          <span class="stat-label">今日</span>
        </span>
        <span class="stat-divider">|</span>
        <span class="stat-item">
          <span class="stat-value">{{ totalVisitors }}</span>
          <span class="stat-label">总访问</span>
        </span>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

const currentYear = computed(() => new Date().getFullYear())
const icpNumber = import.meta.env.VITE_ICP_NUMBER || '蜀ICP备2026015075号'

const todayVisitors = computed(() => appStore.todayVisitors)
const totalVisitors = computed(() => appStore.totalVisitors)

onMounted(() => {
  appStore.fetchVisitStats()
})
</script>

<style lang="scss" scoped>
.app-footer {
  padding: 16px 24px;
  background: var(--el-bg-color);
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.footer-content {
  display: flex;
  align-items: center;
  gap: 16px;
  color: var(--el-text-color-regular);
  font-size: 14px;
  
  p {
    margin: 0;
  }
  
  .copyright {
    color: var(--el-text-color-secondary);
  }
  
  .icp {
    a {
      color: var(--el-color-primary);
      text-decoration: none;
      
      &:hover {
        text-decoration: underline;
      }
    }
  }
  
  .powered {
    color: var(--el-text-color-secondary);
    font-size: 13px;
  }
}

.footer-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--el-text-color-secondary);
  font-size: 13px;

  .stat-item {
    display: flex;
    align-items: baseline;
    gap: 4px;
  }

  .stat-value {
    font-size: 16px;
    font-weight: 600;
    color: var(--el-color-primary);
  }

  .stat-label {
    font-size: 12px;
  }

  .stat-divider {
    color: var(--el-border-color);
  }
}

@media (max-width: 576px) {
  .footer-content {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
