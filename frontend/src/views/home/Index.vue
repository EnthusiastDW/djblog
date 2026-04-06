<template>
  <div class="home-page">
    <div class="home-layout">
      <main class="home-main">
        <div class="post-list" v-loading="loading">
          <template v-if="posts.length > 0">
            <PostCard v-for="post in posts" :key="post.id" :post="post" />
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
import { ref, onMounted } from 'vue'
import { postApi } from '@/api/post'
import PostCard from '@/components/post/PostCard.vue'

const posts = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function fetchPosts() {
  loading.value = true
  try {
    const res = await postApi.getList({ current: currentPage.value, size: pageSize.value })
    posts.value = res.data.records || []
    total.value = res.data.total || 0
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
