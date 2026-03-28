<template>
  <footer class="app-footer">
    <div class="footer-container">
      <div class="footer-content">
        <p class="copyright">
          © {{ currentYear }} DJ Blog. All rights reserved.
        </p>
        <p class="powered">
          Powered by Vue 3 + Spring Boot
        </p>
        <p class="icp" v-if="icpNumber">
          <a :href="'https://beian.miit.gov.cn/'" target="_blank" rel="noopener noreferrer">
            {{ icpNumber }}
          </a>
        </p>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'
import { Link } from '@element-plus/icons-vue'

const currentYear = computed(() => new Date().getFullYear())

// 从环境变量或配置中读取备案号
const icpNumber = import.meta.env.VITE_ICP_NUMBER || ''
</script>

<style lang="scss" scoped>
.app-footer {
  padding: 24px;
  background: var(--el-bg-color);
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: center;
  align-items: center;
}

.footer-content {
  text-align: center;
}

.copyright {
  color: var(--el-text-color-regular);
  font-size: 14px;
  margin-bottom: 4px;
}

.powered {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  margin-bottom: 8px;
}

.icp {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  
  a {
    color: var(--el-color-primary);
    text-decoration: none;
    
    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
