<template>
  <div class="category-page">
    <div class="page-header">
      <h1 class="page-title">分类</h1>
      <p class="category-count">共 {{ totalCategoryCount }} 个分类</p>
    </div>

    <div class="category-layout">
      <div class="category-tree-card" v-loading="treeLoading">
        <div class="tree-header">
          <el-icon><FolderOpened /></el-icon>
          <span>分类目录</span>
        </div>
        <div class="tree-content">
          <el-tree
            v-if="treeData.length > 0"
            :data="treeData"
            :props="treeProps"
            node-key="id"
            :default-expand-all="false"
            :expand-on-click-node="false"
            :accordion="true"
            :current-node-key="selectedCategoryId"
            @node-click="handleNodeClick"
            ref="treeRef"
          >
            <template #default="{ node, data }">
              <span class="custom-tree-node">
                <el-icon class="folder-icon">
                  <Folder v-if="!node.expanded || !data.children || data.children.length === 0" />
                  <FolderOpened v-else />
                </el-icon>
                <span class="node-name">{{ node.label }}</span>
                <span class="node-badge">{{ data.totalPostCount || 0 }}</span>
              </span>
            </template>
          </el-tree>
          <el-empty v-else-if="!treeLoading" description="暂无分类" :image-size="60" />
        </div>
      </div>

      <div class="category-posts-card" v-loading="articlesLoading">
        <template v-if="selectedCategory">
          <div class="posts-header">
            <h2 class="category-title">{{ selectedCategory.name }}</h2>
            <p class="category-desc">{{ selectedCategory.description || '暂无描述' }}</p>
          </div>

          <div class="posts-list" v-if="articles.length > 0">
            <PostCard v-for="post in articles" :key="post.id" :post="post" />
          </div>
          <el-empty v-else description="该分类下暂无文章" :image-size="80" />

          <div class="pagination-wrapper" v-if="total > pageSize">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              @current-change="handlePageChange"
            />
          </div>
        </template>
        <div v-else class="empty-hint">
          <el-icon :size="48"><FolderOpened /></el-icon>
          <p>请在左侧选择一个分类</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { categoryApi } from '@/api/category'
import { postApi } from '@/api/post'
import PostCard from '@/components/post/PostCard.vue'
import { Folder, FolderOpened } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const categoryTree = ref([])
const treeData = ref([])
const treeLoading = ref(false)
const articlesLoading = ref(false)
const selectedCategoryId = ref(null)
const selectedCategory = ref(null)
const articles = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const treeRef = ref(null)

const treeProps = {
  children: 'children',
  label: 'name'
}

const totalCategoryCount = computed(() => {
  let count = 0
  function countNodes(nodes) {
    for (const node of nodes) {
      count++
      if (node.children) {
        countNodes(node.children)
      }
    }
  }
  countNodes(categoryTree.value)
  return count
})

async function fetchCategories() {
  treeLoading.value = true
  try {
    const res = await categoryApi.getTree()
    categoryTree.value = res.data || []
    treeData.value = buildTreeData(categoryTree.value)
  } catch (e) {
    console.error('获取分类失败', e)
  } finally {
    treeLoading.value = false
  }
}

function buildTreeData(nodes) {
  return nodes.map(node => ({
    id: node.id,
    name: node.name,
    description: node.description,
    totalPostCount: node.totalPostCount,
    postCount: node.postCount,
    children: node.children && node.children.length > 0 ? buildTreeData(node.children) : undefined
  }))
}

async function handleNodeClick(data) {
  selectedCategoryId.value = data.id
  selectedCategory.value = {
    id: data.id,
    name: data.name,
    description: data.description
  }
  currentPage.value = 1
  await fetchArticles(data.id)
}

async function fetchArticles(categoryId) {
  articlesLoading.value = true
  try {
    const res = await postApi.getList({
      current: currentPage.value,
      size: pageSize.value,
      categoryId: categoryId
    })
    articles.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('获取文章失败', e)
  } finally {
    articlesLoading.value = false
  }
}

function findPathToNode(nodes, targetId, path = []) {
  for (const node of nodes) {
    if (node.id === targetId) {
      return [...path, node.id]
    }
    if (node.children) {
      const result = findPathToNode(node.children, targetId, [...path, node.id])
      if (result) return result
    }
  }
  return null
}

async function expandPathToNode(targetId) {
  if (!treeRef.value || !treeData.value || treeData.value.length === 0) {
    return
  }
  const path = findPathToNode(treeData.value, targetId)
  if (path && path.length > 0) {
    for (const nodeId of path) {
      await nextTick()
      const node = treeRef.value.getNode(nodeId)
      if (node) {
        node.expanded = true
      }
    }
  }
}

async function initFromRoute() {
  const categoryId = route.query.categoryId
  if (!categoryId) {
    return
  }
  
  const targetId = parseInt(categoryId)
  selectedCategoryId.value = targetId
  
  try {
    const res = await categoryApi.getAncestors(categoryId)
    const ancestors = res.data || []
    
    if (ancestors.length > 0) {
      selectedCategory.value = ancestors[ancestors.length - 1]
    } else {
      const categoryRes = await categoryApi.getDetail(categoryId)
      selectedCategory.value = categoryRes.data
    }
  } catch (e) {
    console.error('获取分类信息失败', e)
    try {
      const categoryRes = await categoryApi.getDetail(categoryId)
      selectedCategory.value = categoryRes.data
    } catch (e2) {
      console.error('获取分类详情失败', e2)
    }
  }
  
  await nextTick()
  await expandPathToNode(targetId)
  await nextTick()
  
  if (treeRef.value) {
    treeRef.value.setCurrentKey(targetId)
  }
  
  currentPage.value = 1
  await fetchArticles(targetId)
}

function handlePageChange(page) {
  currentPage.value = page
  if (selectedCategoryId.value) {
    fetchArticles(selectedCategoryId.value)
  }
}

onMounted(async () => {
  await fetchCategories()
  await initFromRoute()
})

watch(() => route.query.categoryId, async (newCategoryId, oldCategoryId) => {
  if (newCategoryId && newCategoryId !== oldCategoryId) {
    if (treeData.value.length === 0) {
      await fetchCategories()
    }
    await initFromRoute()
  }
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

.category-layout {
  display: flex;
  gap: 24px;
  min-height: 500px;
}

.category-tree-card {
  width: 280px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
}

.tree-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.tree-content {
  padding: 12px;
  max-height: calc(100vh - 280px);
  overflow-y: auto;

  :deep(.el-tree) {
    background: transparent;

    .el-tree-node__content {
      height: 36px;
      border-radius: 6px;
      padding: 0 8px;
      margin-bottom: 2px;

      &:hover {
        background: var(--el-fill-color-light);
      }
    }

    .el-tree-node.is-current > .el-tree-node__content {
      background: var(--el-color-primary-light-9);

      .node-name {
        color: var(--el-color-primary);
        font-weight: 500;
      }
    }

    .el-tree-node__children {
      .el-tree-node__content {
        padding-left: 8px !important;
      }
    }
  }
}

.custom-tree-node {
  display: flex;
  align-items: center;
  flex: 1;
  font-size: 14px;
}

.folder-icon {
  color: var(--el-color-primary);
  margin-right: 8px;
  font-size: 16px;
}

.node-name {
  flex: 1;
  color: var(--el-text-color-primary);
}

.node-badge {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color);
  padding: 2px 8px;
  border-radius: 10px;
}

.category-posts-card {
  flex: 1;
  min-width: 0;
  border-radius: 8px;
  padding: 24px;
}

.posts-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.category-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
}

.category-desc {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: var(--el-text-color-secondary);

  .el-icon {
    margin-bottom: 16px;
    opacity: 0.5;
  }

  p {
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  .category-layout {
    flex-direction: column;
  }

  .category-tree-card {
    width: 100%;
  }

  .tree-content {
    max-height: 300px;
  }
}
</style>
