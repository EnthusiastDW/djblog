<template>
  <header class="app-header">
    <div class="header-container">
      <div class="header-left">
        <router-link to="/" class="logo">
          <span class="logo-text">{{ logoText }}</span>
        </router-link>
        <nav class="nav-menu">
          <router-link to="/" class="nav-item" :class="{ active: route.path === '/' }">
            首页
          </router-link>
          <router-link to="/archives" class="nav-item" :class="{ active: route.path.startsWith('/archives') }">
            归档
          </router-link>
          <router-link to="/categories" class="nav-item" :class="{ active: route.path.startsWith('/categories') }">
            分类
          </router-link>
          <router-link to="/tags" class="nav-item" :class="{ active: route.path.startsWith('/tags') }">
            标签
          </router-link>
        </nav>
      </div>
      <div class="header-right">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索文章..."
            :prefix-icon="Search"
            clearable
            @keyup.enter="handleSearch"
          />
        </div>
        <el-icon class="theme-btn" @click="toggleTheme">
          <Sunny v-if="appStore.theme === 'dark'" />
          <Moon v-else />
        </el-icon>
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
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { userApi } from '@/api/user'
import { Search, Sunny, Moon, Fold } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

function handleToggleSidebar() {
  appStore.toggleSidebar()
}

const blogUser = ref(null)
const searchKeyword = ref('')

const logoText = computed(() => {
  if (blogUser.value) {
    return `${blogUser.value.username}的博客`
  }
  return 'DJ Blog'
})

function toggleTheme() {
  appStore.toggleTheme()
}

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: searchKeyword.value.trim() } })
  }
}

async function handleLogout() {
  await userStore.logout()
  router.push('/')
}

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

.logo {
  text-decoration: none;
}

.logo-text {
  font-size: 24px;
  font-weight: bold;
  color: var(--el-color-primary);
}

.nav-menu {
  display: flex;
  gap: 32px;
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

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
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
}
</style>
