<template>
  <div class="home-page">
    <div class="home-layout">
      <main class="home-main">
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
                  <span class="meta-item" v-if="post.categoryName">
                    <el-icon><Folder /></el-icon>
                    {{ post.categoryName }}
                  </span>
                  <span class="meta-item" v-if="post.tags && post.tags.length > 0">
                    <el-icon><PriceTag /></el-icon>
                    <span class="tags-container">
                      <span v-for="(tag, index) in post.tags" :key="tag.id" class="tag-item">
                        {{ tag.name }}{{ index < post.tags.length - 1 ? ',' : '' }}
                      </span>
                    </span>
                  </span>
                  <span class="meta-item">
                    <el-icon><View /></el-icon>
                    {{ post.viewCount || 0 }}
                  </span>
                </div>
              </div>
              <div class="post-cover" v-if="post.coverImage">
                <el-image :src="post.coverImage" fit="cover" lazy />
              </div>
            </article>
          </template>
          <el-empty v-else description="暂无文章" />
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
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { postApi } from '@/api/post'
import { categoryApi } from '@/api/category'
import { tagApi } from '@/api/tag'
import { userApi } from '@/api/user'
import { formatDate } from '@/utils/format'
import { Calendar, Folder, View, PriceTag, Message } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const posts = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function fetchPosts() {
  loading.value = true
  try {
    const [postRes, categoryRes, tagRes] = await Promise.all([
      postApi.getList({ current: currentPage.value, size: pageSize.value }),
      categoryApi.getAll(),
      tagApi.getAll()
    ])
    posts.value = postRes.data.records || []
    total.value = postRes.data.total || 0
  } catch (e) {
    console.error('获取文章列表失败', e)
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchPosts()
}

onMounted(() => {
  fetchPosts()
})
</script>

<style lang="scss" scoped>
.home-page {
  margin: 0 auto;
  min-height: calc(100vh - 180px);
}

.home-layout {
  display: flex;
  gap: 24px;
}

.home-main {
  flex: 1;
  min-width: 0;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.post-list {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  align-content: flex-start;
}

.post-card {
  display: flex;
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  width: calc(50% - 8px);

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
}

@media (max-width: 768px) {
  .post-card {
    width: 100%;
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

.post-cover {
  width: 160px;
  height: 100px;
  margin-left: 20px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;

  .el-image {
    width: 100%;
    height: 100%;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 768px) {
  .post-cover {
    display: none;
  }
}
</style>
