<template>
  <div class="tag-posts-page">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/tags' }">标签</el-breadcrumb-item>
        <el-breadcrumb-item>#{{ tag?.name }}</el-breadcrumb-item>
      </el-breadcrumb>
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
      <el-empty v-else description="该标签下暂无文章" />
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
import { useRoute, useRouter } from 'vue-router'
import { tagApi } from '@/api/tag'
import { postApi } from '@/api/post'
import { formatDate } from '@/utils/format'
import { Calendar } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const tag = ref(null)
const posts = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function fetchTag() {
  try {
    const res = await tagApi.getDetail(route.params.id)
    tag.value = res.data
  } catch (e) {
    console.error('获取标签失败', e)
  }
}

async function fetchPosts() {
  loading.value = true
  try {
    const res = await postApi.search({
      tagId: route.params.id,
      current: currentPage.value,
      size: pageSize.value
    })
    posts.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('获取文章失败', e)
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchPosts()
}

watch(() => route.params.id, () => {
  currentPage.value = 1
  fetchTag()
  fetchPosts()
}, { immediate: true })
</script>

<style lang="scss" scoped>
.tag-posts-page {
  max-width: 900px;
}

.page-header {
  margin-bottom: 16px;
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

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;

  &:hover {
    color: var(--el-color-primary);
  }
}

.post-summary {
  color: var(--el-text-color-regular);
  font-size: 14px;
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
