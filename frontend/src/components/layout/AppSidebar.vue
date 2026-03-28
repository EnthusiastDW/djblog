<template>
  <aside class="app-sidebar">
    <div class="sidebar-section">
      <h3 class="section-title">分类</h3>
      <div class="category-list">
        <router-link
          v-for="category in categories"
          :key="category.id"
          :to="`/category/${category.id}`"
          class="category-item"
        >
          <span class="category-name">{{ category.name }}</span>
          <span class="category-count">{{ category.postCount || 0 }}</span>
        </router-link>
        <el-empty v-if="categories.length === 0" description="暂无分类" :image-size="60" />
      </div>
    </div>

    <div class="sidebar-section">
      <h3 class="section-title">热门标签</h3>
      <div class="tag-cloud">
        <router-link
          v-for="tag in tags"
          :key="tag.id"
          :to="`/tag/${tag.id}`"
          class="tag-item"
          :style="{ fontSize: getTagSize(tag.postCount) + 'px' }"
        >
          {{ tag.name }}
        </router-link>
        <el-empty v-if="tags.length === 0" description="暂无标签" :image-size="60" />
      </div>
    </div>

    <div class="sidebar-section">
      <h3 class="section-title">最新文章</h3>
      <div class="recent-posts">
        <router-link
          v-for="post in recentPosts"
          :key="post.id"
          :to="`/article/${post.slug}`"
          class="recent-post-item"
        >
          <span class="post-title text-ellipsis">{{ post.title }}</span>
          <span class="post-date">{{ formatDate(post.createdAt, 'MM-DD') }}</span>
        </router-link>
        <el-empty v-if="recentPosts.length === 0" description="暂无文章" :image-size="60" />
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { categoryApi } from '@/api/category'
import { tagApi } from '@/api/tag'
import { postApi } from '@/api/post'
import { formatDate } from '@/utils/format'

const categories = ref([])
const tags = ref([])
const recentPosts = ref([])

onMounted(async () => {
  try {
    const [categoryRes, tagRes, postRes] = await Promise.all([
      categoryApi.getAll(),
      tagApi.getAll(),
      postApi.getList({ size: 5 })
    ])
    categories.value = categoryRes.data || []
    tags.value = (tagRes.data || []).slice(0, 20)
    recentPosts.value = postRes.data.records || []
  } catch (e) {
    console.error('加载侧边栏数据失败', e)
  }
})

function getTagSize(count) {
  const baseSize = 12
  const maxSize = 18
  const size = baseSize + Math.min(count || 0, 10)
  return Math.min(size, maxSize)
}
</script>

<style lang="scss" scoped>
.app-sidebar {
  height: 100%;
}

.sidebar-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-radius: 4px;
  color: var(--el-text-color-regular);
  text-decoration: none;
  transition: all 0.3s;

  &:hover {
    background: var(--el-fill-color-light);
    color: var(--el-color-primary);
  }
}

.category-count {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color);
  padding: 2px 8px;
  border-radius: 10px;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  color: var(--el-text-color-regular);
  text-decoration: none;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s;

  &:hover {
    color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
  }
}

.recent-posts {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recent-post-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  color: var(--el-text-color-regular);
  text-decoration: none;
  border-bottom: 1px dashed var(--el-border-color-lighter);
  transition: color 0.3s;
  cursor: pointer;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    color: var(--el-color-primary);
  }
}

.post-title {
  flex: 1;
  margin-right: 8px;
}

.post-date {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}
</style>
