<template>
  <div class="category-page">
    <div class="page-header">
      <h1 class="page-title">分类</h1>
      <p class="category-count">共 {{ categories.length }} 个分类</p>
    </div>

    <div class="category-grid" v-loading="loading">
      <router-link
        v-for="category in categories"
        :key="category.id"
        :to="`/category/${category.id}`"
        class="category-card"
      >
        <div class="category-icon">
          <el-icon :size="32"><Folder /></el-icon>
        </div>
        <div class="category-info">
          <h3 class="category-name">{{ category.name }}</h3>
          <p class="category-desc text-ellipsis">{{ category.description || '暂无描述' }}</p>
          <span class="category-count">{{ category.postCount || 0 }} 篇文章</span>
        </div>
      </router-link>
      <el-empty v-if="categories.length === 0" description="暂无分类" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { categoryApi } from '@/api/category'
import { Folder } from '@element-plus/icons-vue'

const categories = ref([])
const loading = ref(false)

async function fetchCategories() {
  loading.value = true
  try {
    const res = await categoryApi.getAll()
    categories.value = res.data || []
  } catch (e) {
    console.error('获取分类失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<style lang="scss" scoped>
.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
}

.category-count {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.category-card {
  display: flex;
  gap: 16px;
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 20px;
  text-decoration: none;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);

    .category-name {
      color: var(--el-color-primary);
    }
  }
}

.category-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-color-primary-light-9);
  border-radius: 12px;
  color: var(--el-color-primary);
  flex-shrink: 0;
}

.category-info {
  flex: 1;
  min-width: 0;
}

.category-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}

.category-desc {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  margin-bottom: 8px;
}

.category-count {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
