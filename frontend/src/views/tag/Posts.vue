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
        <PostCard v-for="post in posts" :key="post.id" :post="post" />
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
import { useRoute } from 'vue-router'
import { tagApi } from '@/api/tag'
import { postApi } from '@/api/post'
import PostCard from '@/components/post/PostCard.vue'

const route = useRoute()

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

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
