<template>
  <div class="user-page">
    <div class="user-layout">
      <aside class="user-sidebar">
        <div class="profile-card">
          <div class="profile-avatar">
            <el-avatar :size="80">{{ user?.username?.charAt(0) || 'U' }}</el-avatar>
          </div>
          <h3 class="profile-name">{{ user?.username || '未知用户' }}</h3>
          <p class="profile-bio">{{ user?.bio || '这个人很懒，什么都没留下' }}</p>
          <div class="profile-stats">
            <div class="stat-item">
              <span class="stat-value">{{ postCount }}</span>
              <span class="stat-label">文章</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ totalViewCount }}</span>
              <span class="stat-label">阅读</span>
            </div>
          </div>
        </div>
      </aside>
      
      <main class="user-main">
        <div class="page-header">
          <h1 class="page-title">{{ user?.username }} 的文章</h1>
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
                  <span class="meta-item" v-if="post.categoryName">
                    <el-icon><Folder /></el-icon>
                    {{ post.categoryName }}
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
          <el-empty v-else description="该用户暂无文章" />
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
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { userApi } from '@/api/user'
import { postApi } from '@/api/post'
import { formatDate } from '@/utils/format'
import { Calendar, Folder, View } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const user = ref(null)
const posts = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const postCount = ref(0)
const totalViewCount = ref(0)

async function fetchUser() {
  try {
    const res = await userApi.getPublicProfile(route.params.id)
    user.value = res.data
  } catch (e) {
    console.error('获取用户信息失败', e)
  }
}

async function fetchPosts() {
  loading.value = true
  try {
    const res = await postApi.search({
      authorId: route.params.id,
      current: currentPage.value,
      size: pageSize.value
    })
    posts.value = res.data.records || []
    total.value = res.data.total || 0
    
    let totalViews = 0
    posts.value.forEach(p => {
      totalViews += p.viewCount || 0
    })
    totalViewCount.value = totalViews
    postCount.value = total.value
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

watch(() => route.params.id, () => {
  currentPage.value = 1
  fetchUser()
  fetchPosts()
}, { immediate: true })

onMounted(() => {
  fetchUser()
  fetchPosts()
})
</script>

<style lang="scss" scoped>
.user-page {
  max-width: 1200px;
}

.user-layout {
  display: flex;
  gap: 24px;
}

.user-sidebar {
  width: 280px;
  flex-shrink: 0;
}

.profile-card {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  position: sticky;
  top: 84px;
}

.profile-avatar {
  margin-bottom: 16px;
}

.profile-name {
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
}

.profile-bio {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-bottom: 20px;
  line-height: 1.6;
}

.profile-stats {
  display: flex;
  justify-content: space-around;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.user-main {
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
  flex-direction: column;
  gap: 16px;
}

.post-card {
  display: flex;
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

@media (max-width: 992px) {
  .user-sidebar {
    display: none;
  }
}

@media (max-width: 768px) {
  .post-cover {
    display: none;
  }
}
</style>