<template>
  <div class="search-page">
    <div class="page-header">
      <h1 class="page-title">搜索: {{ keyword }}</h1>
      <p class="result-count">共找到 {{ total }} 篇文章</p>
    </div>

    <div class="post-list" v-loading="loading">
      <template v-if="posts.length > 0">
        <article
          v-for="post in posts"
          :key="post.id"
          class="post-card"
          @click="router.push(`/article/${post.slug}`)"
        >
          <div class="post-content">
            <h2 class="post-title">{{ post.title }}</h2>
            <p class="post-summary text-ellipsis-2">{{ post.summary || '暂无摘要' }}</p>
            <div class="post-meta">
              <span class="meta-item">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(post.createdAt, 'YYYY-MM-DD') }}
              </span>
            </div>
          </div>
        </article>
      </template>
      <el-empty v-else description="没有找到相关文章" />
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
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { postApi } from '@/api/post'
import { formatDate } from '@/utils/format'
import { Calendar } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const posts = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function searchPosts() {
  if (!keyword.value) return
  
  loading.value = true
  try {
    const res = await postApi.search({
      keyword: keyword.value,
      current: currentPage.value,
      size: pageSize.value
    })
    posts.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('搜索失败', e)
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  searchPosts()
}

watch(() => route.query.keyword, (newKeyword) => {
  if (newKeyword) {
    keyword.value = newKeyword
    currentPage.value = 1
    searchPosts()
  }
}, { immediate: true })
</script>

<style lang="scss" scoped>
.search-page {
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

.result-count {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
}

.post-content {
  flex: 1;
  min-width: 0;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
  line-height: 1.4;

  &:hover {
    color: var(--el-color-primary);
  }
}

.post-summary {
  color: var(--el-text-color-regular);
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
}

.post-meta {
  display: flex;
  gap: 16px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
