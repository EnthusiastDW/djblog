<template>
  <div class="not-found-page">
    <div class="not-found-content">
      <h1 class="error-code">404</h1>
      <p class="error-message">页面不存在</p>
      <p class="error-description">抱歉，您访问的页面不存在或已被删除</p>
      <el-button type="primary" @click="router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style lang="scss" scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-bg-color-page);
}

.not-found-content {
  text-align: center;
}

.error-code {
  font-size: 120px;
  font-weight: 700;
  color: var(--el-color-primary);
  line-height: 1;
  margin-bottom: 16px;
}

.error-message {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
}

.error-description {
  color: var(--el-text-color-secondary);
  margin-bottom: 24px;
}
</style>
