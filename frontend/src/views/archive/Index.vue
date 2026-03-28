<template>
  <div class="archive-page">
    <div class="page-header">
      <h1 class="page-title">文章归档</h1>
      <p class="archive-count">共 {{ totalCount }} 篇文章</p>
    </div>

    <div class="archive-list" v-loading="loading">
      <div v-for="yearGroup in archives" :key="yearGroup.year" class="archive-section">
        <h2 class="archive-year">
          {{ yearGroup.year }}年
          <span class="year-count">({{ yearGroup.totalCount }} 篇)</span>
        </h2>
        <div class="year-months">
          <div v-for="month in yearGroup.months" :key="month.archiveMonth" class="month-group">
            <h3 class="month-title">
              <router-link :to="`/archives/${month.archiveMonth}`">
                {{ month.archiveMonth.substring(5) }}月
                <span class="month-count">({{ month.count }} 篇)</span>
              </router-link>
            </h3>
          </div>
        </div>
      </div>
      <el-empty v-if="archives.length === 0" description="暂无文章" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { postApi } from '@/api/post'
import { formatDate } from '@/utils/format'

const archives = ref([])
const loading = ref(false)

const totalCount = computed(() => {
  return archives.value.reduce((sum, yearGroup) => sum + (yearGroup.totalCount || 0), 0)
})

async function fetchArchives() {
  loading.value = true
  try {
    const res = await postApi.getArchives(12)
    archives.value = res.data || []
  } catch (e) {
    console.error('获取归档失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchArchives()
})
</script>

<style lang="scss" scoped>
.archive-page {
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

.archive-count {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.archive-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.archive-section {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 20px;
}

.archive-year {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  color: var(--el-text-color-primary);

  .year-count {
    font-size: 14px;
    font-weight: normal;
    color: var(--el-text-color-secondary);
  }
}

.year-months {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
}

.month-title {
  font-size: 14px;
  font-weight: normal;
  margin: 0;

  a {
    color: var(--el-text-color-regular);
    text-decoration: none;
    display: block;
    padding: 8px 12px;
    background: var(--el-fill-color-light);
    border-radius: 6px;
    transition: all 0.3s;

    &:hover {
      color: var(--el-color-primary);
      background: var(--el-color-primary-light-9);
    }
  }

  .month-count {
    color: var(--el-text-color-secondary);
    font-size: 12px;
  }
}
</style>
