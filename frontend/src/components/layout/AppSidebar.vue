<template>
  <aside class="app-sidebar">
    <div class="sidebar-section">
      <h3 class="section-title">分类</h3>
      <div class="category-list">
        <router-link
          v-for="category in displayTopCategories"
          :key="category.id"
          :to="`/categories?categoryId=${category.id}`"
          class="category-item"
        >
          <span class="category-name">{{ category.name }}</span>
          <span class="category-count">{{ category.totalPostCount || 0 }}</span>
        </router-link>
        <router-link v-if="categoryCount > 10" to="/categories" class="more-link">
          更多...
        </router-link>
        <el-empty v-if="categoryTree.length === 0" description="暂无分类" :image-size="60" />
      </div>
    </div>

    <div class="sidebar-section">
      <h3 class="section-title">热门标签</h3>
      <div class="tag-cloud">
        <router-link
          v-for="tag in displayTags"
          :key="tag.id"
          :to="`/tag/${tag.id}`"
          class="tag-item"
          :style="{ fontSize: getTagSize(tag.postCount) + 'px' }"
        >
          {{ tag.name }}
        </router-link>
        <router-link v-if="tagCount > 10" to="/tags" class="more-link">
          更多...
        </router-link>
        <el-empty v-if="tags.length === 0" description="暂无标签" :image-size="60" />
      </div>
    </div>

    <div class="sidebar-section">
      <h3 class="section-title">热门文章</h3>
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
import { ref, onMounted, computed } from 'vue'
import { categoryApi } from '@/api/category'
import { tagApi } from '@/api/tag'
import { postApi } from '@/api/post'
import { formatDate } from '@/utils/format'

const categoryTree = ref([])
const tags = ref([])
const recentPosts = ref([])
const categoryCount = ref(0)
const tagCount = ref(0)
const advertisement = ref(null)

/**
 * 只显示顶级分类
 */
const displayTopCategories = computed(() => {
  return categoryTree.value.slice(0, 10)
})

const displayTags = computed(() => {
  return tags.value.slice(0, 10)
})

onMounted(async () => {
  try {
    const [categoryRes, tagRes, postListRes] = await Promise.all([
      categoryApi.getTree(),
      tagApi.getAll(),
      postApi.getList({ current: 1, size: 0 })
    ])
    categoryTree.value = categoryRes.data || []
    // 计算总分类数（含所有层级）
    let totalCount = 0
    function countNodes(nodes) {
      for (const node of nodes) {
        totalCount++
        if (node.children) countNodes(node.children)
      }
    }
    countNodes(categoryTree.value)
    categoryCount.value = totalCount
    tags.value = (tagRes.data || []).slice(0, 10)
    tagCount.value = tagRes.data?.length || 0
    
    // 单独获取热门文章用于展示
    const popularRes = await postApi.getPopular({ size: 10 })
    recentPosts.value = popularRes.data || []
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
  padding-top: 0;
}

.ad-section {
  margin-bottom: 24px;
  border-radius: 8px;
  overflow: hidden;
}

.ad-link {
  display: block;
  text-decoration: none;
}

.ad-image {
  width: 100%;
  height: auto;
  display: block;
  transition: opacity 0.3s;
  
  &:hover {
    opacity: 0.9;
  }
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
  gap: 2px;
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

.more-link {
  display: block;
  text-align: center;
  padding: 8px;
  color: var(--el-color-primary);
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
  
  &:hover {
    color: var(--el-color-primary-light-3);
  }
}
</style>
