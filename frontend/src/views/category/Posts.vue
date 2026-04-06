<template>
  <div class="category-posts-page">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/categories' }">分类</el-breadcrumb-item>
        <el-breadcrumb-item
          v-for="ancestor in ancestors"
          :key="ancestor.id"
          :to="ancestor.id !== category?.id ? { path: `/category/${ancestor.id}` } : undefined"
        >
          {{ ancestor.name }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="category-info" v-if="category">
      <div class="category-header">
        <h1 class="category-title">{{ category.name }}</h1>
        <p class="category-desc">{{ category.description || '暂无描述' }}</p>
      </div>

      <div v-if="hasChildren" class="include-children-bar">
        <el-switch
          v-model="includeChildren"
          active-text="包含子分类文章"
          inactive-text="仅当前分类"
          @change="handleIncludeChange"
        />
        <span v-if="includeChildren && childPostCount > 0" class="child-count-hint">
          （含子分类共 {{ currentPostCount }} 篇）
        </span>
      </div>
    </div>

    <div class="post-list" v-loading="loading">
      <template v-if="posts.length > 0">
        <PostCard v-for="post in posts" :key="post.id" :post="post" />
      </template>
      <el-empty v-else description="该分类下暂无文章" />
    </div>

    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { categoryApi } from '@/api/category'
import { postApi } from '@/api/post'
import PostCard from '@/components/post/PostCard.vue'

const route = useRoute()

const category = ref(null)
const ancestors = ref([])
const posts = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const includeChildren = ref(false)
const hasChildren = ref(false)
const childPostCount = ref(0)
const currentPostCount = ref(0)

async function fetchCategory() {
  try {
    const res = await categoryApi.getDetail(route.params.id)
    category.value = res.data
  } catch (e) {
    console.error('获取分类失败', e)
  }

  try {
    const res = await categoryApi.getAncestors(route.params.id)
    ancestors.value = res.data || []
  } catch (e) {
    console.error('获取祖先链路失败', e)
    ancestors.value = category.value ? [category.value] : []
  }
}

async function fetchPosts() {
  loading.value = true
  try {
    if (includeChildren.value) {
      const res = await postApi.getListByCategoryIds({
        current: currentPage.value,
        size: pageSize.value,
        categoryIds: route.params.id
      })
      posts.value = res.data.records || []
      total.value = res.data.total || 0
      currentPostCount.value = total.value
    } else {
      const res = await postApi.getList({
        current: currentPage.value,
        size: pageSize.value,
        categoryId: route.params.id
      })
      posts.value = res.data.records || []
      total.value = res.data.total || 0
      currentPostCount.value = total.value
    }
  } catch (e) {
    console.error('获取文章失败', e)
  } finally {
    loading.value = false
  }
}

async function checkChildren() {
  try {
    const res = await categoryApi.getTree()
    const tree = res.data || []
    const findCategory = (nodes, id) => {
      for (const node of nodes) {
        if (node.id == id) return node
        if (node.children) {
          const found = findCategory(node.children, id)
          if (found) return found
        }
      }
      return null
    }
    const current = findCategory(tree, route.params.id)
    if (current) {
      hasChildren.value = current.children && current.children.length > 0
      childPostCount.value = current.totalPostCount ? current.totalPostCount - (current.postCount || 0) : 0
    }
  } catch (e) {
    console.error('获取子分类信息失败', e)
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchPosts()
}

function handleIncludeChange() {
  currentPage.value = 1
  fetchPosts()
}

watch(() => route.params.id, () => {
  currentPage.value = 1
  includeChildren.value = false
  fetchCategory()
  checkChildren()
  fetchPosts()
}, { immediate: true })
</script>

<style lang="scss" scoped>
.category-posts-page {
  max-width: 900px;
}

.page-header {
  margin-bottom: 16px;
}

.category-info {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
}

.category-header {
  margin-bottom: 12px;
}

.category-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
}

.category-desc {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.include-children-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--el-border-color-lighter);

  .child-count-hint {
    color: var(--el-text-color-secondary);
    font-size: 13px;
  }
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
