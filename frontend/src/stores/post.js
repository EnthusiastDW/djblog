import { defineStore } from 'pinia'
import { ref } from 'vue'
import { postApi } from '@/api/post'

export const usePostStore = defineStore('post', () => {
  const posts = ref([])
  const currentPost = ref(null)
  const archives = ref([])
  const total = ref(0)
  const loading = ref(false)

  async function fetchPosts(params = {}) {
    loading.value = true
    try {
      const res = await postApi.getList(params)
      posts.value = res.data.records
      total.value = res.data.total
      return res
    } finally {
      loading.value = false
    }
  }

  async function fetchPostDetail(id) {
    loading.value = true
    try {
      const res = await postApi.getDetailWithContent(id)
      currentPost.value = res.data
      return res
    } finally {
      loading.value = false
    }
  }

  async function fetchArchives() {
    const res = await postApi.getArchives()
    archives.value = res.data
    return res
  }

  async function searchPosts(keyword, params = {}) {
    loading.value = true
    try {
      const res = await postApi.search(keyword, params)
      posts.value = res.data.records
      total.value = res.data.total
      return res
    } finally {
      loading.value = false
    }
  }

  function clearCurrentPost() {
    currentPost.value = null
  }

  return {
    posts,
    currentPost,
    archives,
    total,
    loading,
    fetchPosts,
    fetchPostDetail,
    fetchArchives,
    searchPosts,
    clearCurrentPost
  }
})
