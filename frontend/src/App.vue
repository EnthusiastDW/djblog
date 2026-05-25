<script setup>
import { ref, onMounted, watch } from 'vue'
import { userApi } from '@/api/user'
import { useAppStore } from '@/stores/app'

const blogUser = ref(null)
const appStore = useAppStore()

// 获取博主信息
async function fetchBlogUser() {
  try {
    const res = await userApi.getFirstUser()
    if (res.data?.records?.length > 0) {
      blogUser.value = res.data.records[0]
    }
  } catch (e) {
    console.error('获取博主信息失败', e)
  }
}

// 更新浏览器标题
function updateTitle() {
  if (blogUser.value) {
    document.title = `${blogUser.value.username}的博客`
  } else {
    document.title = '首页'
  }
}

onMounted(() => {
  fetchBlogUser()
})

// 监听博主信息变化，更新标题
watch(blogUser, () => {
  updateTitle()
}, { immediate: true })
</script>

<template>
  <router-view />
</template>
