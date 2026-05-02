<template>
  <header class="app-header">
    <div class="header-container">
      <div class="header-left">
        <!-- 移动端博主头像 -->
        <el-avatar 
          v-if="blogUser && isMobile" 
          :size="32" 
          :src="blogUser.avatarUrl"
          class="mobile-blogger-avatar"
          @click="handleToggleLeftSidebar"
        >
          {{ blogUser.username?.charAt(0) || '博' }}
        </el-avatar>
        <router-link to="/" class="logo">
          <span class="logo-text">{{ logoText }}</span>
        </router-link>
      </div>
      <div class="header-right">
        <div class="search-box" ref="searchBoxRef">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索文章..."
            :prefix-icon="Search"
            clearable
            @input="handleInput"
            @keyup.enter="handleSearch"
            @focus="showSuggestions = true"
          />
          <!-- 搜索建议下拉列表 -->
          <div 
            v-if="showSuggestions && suggestions.length > 0" 
            class="suggestions-dropdown"
            @scroll="handleScroll"
          >
            <div 
              v-for="post in suggestions" 
              :key="post.id"
              class="suggestion-item"
              @click.prevent="handleSelectSuggestion(post)"
            >
              <div class="suggestion-title" v-html="highlightTitle(post.title)"></div>
              <div v-if="post.matchedContent" class="suggestion-content" v-html="highlightContent(post.matchedContent)"></div>
              <div class="suggestion-meta">
                <span v-if="post.categoryName" class="category-tag">{{ post.categoryName }}</span>
                <span class="date">{{ formatDate(post.createdAt, 'YYYY-MM-DD') }}</span>
              </div>
            </div>
            <!-- 加载更多提示 -->
            <div v-if="loadingMore" class="loading-more">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>加载中...</span>
            </div>
            <div v-else-if="hasMore && suggestions.length > 0" class="load-more-hint">
              滚动加载更多
            </div>
          </div>
        </div>
        <el-icon class="theme-btn" @click="toggleTheme">
            <Sunny v-if="appStore.theme === 'dark'" />
            <Moon v-else />
        </el-icon>
        <nav class="nav-menu">
          <router-link to="/archives" class="nav-item" :class="{ active: route.path.startsWith('/archives') }">
            归档
          </router-link>
          <router-link to="/categories" class="nav-item" :class="{ active: route.path.startsWith('/categories') }">
            分类
          </router-link>
          <router-link to="/tags" class="nav-item" :class="{ active: route.path.startsWith('/tags') }">
            标签
          </router-link>
          <router-link to="/about" class="nav-item" :class="{ active: route.path === '/about' }">
            关于
          </router-link>
        </nav>

        <template v-if="userStore.isLoggedIn">
          <el-dropdown>
            <span class="user-dropdown">
              <el-avatar :size="32" :src="userStore.user?.avatar">
                {{ userStore.user?.username?.charAt(0) || 'U' }}
              </el-avatar>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item @click="router.push('/admin')">后台管理</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <el-icon class="mobile-menu-btn" @click="handleToggleSidebar">
          <Fold />
        </el-icon>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMediaQuery } from '@vueuse/core'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { userApi } from '@/api/user'
import { postApi } from '@/api/post'
import { Search, Sunny, Moon, Fold, Loading } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/format'
import { highlightKeyword } from '@/utils/highlight'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()
const isMobile = useMediaQuery('(max-width: 992px)')

function handleToggleSidebar() {
  appStore.toggleSidebar()
}

function handleToggleLeftSidebar() {
  appStore.toggleLeftSidebar()
}

const blogUser = ref(null)
const searchKeyword = ref('')
const suggestions = ref([])
const showSuggestions = ref(false)
const searchBoxRef = ref(null)
let searchTimer = null
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)
const loadingMore = ref(false)
const hasMore = ref(true)

const logoText = computed(() => {
  if (blogUser.value) {
    return `${blogUser.value.username}的博客`
  }
  return 'DJ Blog'
})

function toggleTheme() {
  appStore.toggleTheme()
}

function handleInput() {
  // 清除之前的定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  // 如果输入为空，隐藏建议
  if (!searchKeyword.value.trim()) {
    suggestions.value = []
    showSuggestions.value = false
    return
  }
  
  // 延迟500ms后执行搜索（防抖）
  searchTimer = setTimeout(() => {
    // 重置分页
    currentPage.value = 1
    suggestions.value = []
    hasMore.value = true
    fetchSuggestions()
  }, 500)
}

async function fetchSuggestions(isLoadMore = false) {
  try {
    if (isLoadMore) {
      loadingMore.value = true
    }
    
    const res = await postApi.search({
      keyword: searchKeyword.value.trim(),
      current: currentPage.value,
      size: pageSize.value
    })
    
    const newRecords = res.data.records || []
    total.value = res.data.total || 0
    
    if (isLoadMore) {
      // 追加新数据
      suggestions.value = [...suggestions.value, ...newRecords]
    } else {
      // 替换数据
      suggestions.value = newRecords
    }
    
    // 判断是否还有更多数据
    hasMore.value = suggestions.value.length < total.value
    showSuggestions.value = true
  } catch (e) {
    console.error('获取搜索建议失败', e)
  } finally {
    loadingMore.value = false
  }
}

function highlightTitle(title) {
  return highlightKeyword(title, searchKeyword.value)
}

function highlightContent(content) {
  // 如果内容过长，可以在此处进行二次截取
  // 目前后端已经返回合适的长度（前后各30字符），直接使用即可
  if (!content) return ''
  return highlightKeyword(content, searchKeyword.value)
}

function handleSelectSuggestion(post) {
  // 点击建议项，直接跳转到文章详情页
  showSuggestions.value = false
  // 不清除搜索关键词，保留用户输入
  
  // 使用 Vue Router 进行 SPA 跳转，不刷新页面
  router.push(`/article/${post.slug}`)
}

function handleSearch() {
  if (searchKeyword.value.trim()) {
    // 按回车键时，触发一次搜索以显示更多结果
    showSuggestions.value = true
    currentPage.value = 1
    suggestions.value = []
    hasMore.value = true
    fetchSuggestions()
  }
}

// 滚动加载
function handleScroll(event) {
  const { scrollTop, scrollHeight, clientHeight } = event.target
  
  // 当滚动到底部附近时加载更多
  if (scrollTop + clientHeight >= scrollHeight - 10 && hasMore.value && !loadingMore.value) {
    loadMore()
  }
}

async function loadMore() {
  if (!hasMore.value || loadingMore.value) return
  
  currentPage.value++
  await fetchSuggestions(true)
}

// 点击外部关闭建议列表
function handleClickOutside(event) {
  // 检查是否点击在搜索框内部（包括下拉列表）
  if (searchBoxRef.value && !searchBoxRef.value.contains(event.target)) {
    showSuggestions.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

async function handleLogout() {
  await userStore.logout()
  router.push('/')
}

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
})

onMounted(async () => {
  try {
    const res = await userApi.getFirstUser()
    if (res.data?.records?.length > 0) {
      blogUser.value = res.data.records[0]
    }
  } catch (e) {
    console.error('获取博主信息失败', e)
  }
})
</script>

<style lang="scss" scoped>
.app-header {
  height: 100%;
}

.header-container {
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.mobile-blogger-avatar {
  cursor: pointer;
  transition: transform 0.3s;
  
  &:hover {
    transform: scale(1.1);
  }
}

.logo {
  text-decoration: none;
}

.logo-text {
  font-size: 24px;
  font-weight: bold;
  color: var(--el-color-primary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-menu {
  display: flex;
  gap: 24px;
}

.nav-item {
  color: var(--el-text-color-regular);
  text-decoration: none;
  font-size: 15px;
  transition: color 0.3s;

  &:hover,
  &.active {
    color: var(--el-color-primary);
  }
}

.mobile-menu-btn {
  display: none;
  font-size: 24px;
  cursor: pointer;
  color: var(--el-text-color-regular);
  transition: color 0.3s;

  &:hover {
    color: var(--el-color-primary);
  }
}

.search-box {
  width: 200px;
  position: relative;
}

.suggestions-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 8px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  max-height: 400px;
  overflow-y: auto;
  z-index: 1000;
  min-width: 280px;
}

.suggestion-item {
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 1px solid var(--el-border-color-lighter);
  
  &:last-child {
    border-bottom: none;
  }
  
  &:hover {
    background: var(--el-fill-color-light);
  }
}

.suggestion-title {
  font-size: 14px;
  color: var(--el-text-color-primary);
  margin-bottom: 6px;
  line-height: 1.5;
  font-weight: 500;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.suggestion-content {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  
  :deep(mark.highlight) {
    background-color: var(--el-color-primary-light-7);
    color: var(--el-color-primary);
    padding: 1px 3px;
    border-radius: 2px;
    font-weight: 500;
  }
}

.suggestion-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.category-tag {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
}

.loading-more,
.load-more-hint {
  padding: 12px 16px;
  text-align: center;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.load-more-hint {
  cursor: pointer;
  
  &:hover {
    color: var(--el-color-primary);
  }
}

.theme-btn {
  font-size: 20px;
  cursor: pointer;
  color: var(--el-text-color-regular);

  &:hover {
    color: var(--el-color-primary);
  }
}

.user-dropdown {
  cursor: pointer;
}

@media (max-width: 992px) {
  .mobile-menu-btn {
    display: block;
  }

  .nav-menu {
    display: none;
  }

  .search-box {
    width: 150px;
  }
  
  .suggestions-dropdown {
    min-width: 240px;
  }
}
</style>
