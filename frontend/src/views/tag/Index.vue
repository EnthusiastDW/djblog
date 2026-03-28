<template>
  <div class="tag-page">
    <div class="page-header">
      <h1 class="page-title">标签</h1>
      <p class="tag-count">共 {{ tags.length }} 个标签</p>
    </div>

    <div class="tag-cloud" v-loading="loading">
      <router-link
        v-for="tag in tags"
        :key="tag.id"
        :to="`/tag/${tag.id}`"
        class="tag-item"
        :style="{ fontSize: getTagSize(tag.postCount) + 'px' }"
      >
        #{{ tag.name }}
        <span class="tag-count">({{ tag.postCount || 0 }})</span>
      </router-link>
      <el-empty v-if="tags.length === 0" description="暂无标签" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { tagApi } from '@/api/tag'

const tags = ref([])
const loading = ref(false)

async function fetchTags() {
  loading.value = true
  try {
    const res = await tagApi.getAll()
    tags.value = res.data || []
  } catch (e) {
    console.error('获取标签失败', e)
  } finally {
    loading.value = false
  }
}

function getTagSize(count) {
  const baseSize = 14
  const maxSize = 22
  const size = baseSize + Math.min(count || 0, 10)
  return Math.min(size, maxSize)
}

onMounted(() => {
  fetchTags()
})
</script>

<style lang="scss" scoped>
.tag-page {
  max-width: 900px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
}

.tag-count {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.tag-cloud {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 32px;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}

.tag-item {
  color: var(--el-text-color-regular);
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 20px;
  background: var(--el-fill-color-light);
  transition: all 0.3s;

  &:hover {
    color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
    transform: scale(1.05);
  }
}

.tag-count {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
