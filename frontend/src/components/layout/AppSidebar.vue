<template>
  <aside class="app-sidebar">
    <!-- 博主信息卡片 -->
    <div class="profile-card">
      <div class="profile-avatar">
        <el-avatar v-if="blogUser?.avatarUrl" :size="80" :src="blogUser.avatarUrl" />
        <el-avatar v-else :size="80">{{ blogUser?.username?.charAt(0) || '博' }}</el-avatar>
      </div>
      <h3 class="profile-name">{{ blogUser?.nickname || blogUser?.username || '博主' }}</h3>
      <p class="profile-bio">{{ blogUser?.bio || '这个人很懒，什么都没留下' }}</p>
      <div class="profile-contact-row" v-if="blogUser?.email || blogUser?.contactInfo">
        <a v-if="blogUser?.email" :href="'mailto:' + blogUser.email" class="contact-icon" title="发送邮件">
          <el-icon><Message /></el-icon>
        </a>
        <span class="contact-text" v-if="blogUser?.contactInfo">{{ blogUser.contactInfo }}</span>
      </div>
      <div class="profile-stats">
        <div class="stat-item">
          <span class="stat-value">{{ postCount }}</span>
          <span class="stat-label">文章</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ categoryCount }}</span>
          <span class="stat-label">分类</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ tagCount }}</span>
          <span class="stat-label">标签</span>
        </div>
      </div>
    </div>

    <div class="sidebar-section">
      <h3 class="section-title">分类</h3>
      <div class="category-list">
        <router-link
          v-for="category in displayCategories"
          :key="category.id"
          :to="`/category/${category.id}`"
          class="category-item"
        >
          <span class="category-name">{{ category.name }}</span>
          <span class="category-count">{{ category.postCount || 0 }}</span>
        </router-link>
        <router-link v-if="categoryCount > 10" to="/categories" class="more-link">
          更多...
        </router-link>
        <el-empty v-if="categories.length === 0" description="暂无分类" :image-size="60" />
      </div>
    </div>

    <div class="sidebar-section">
      <h3 class="section-title">热门标签</h3>
      <div class="tag-cloud">
        <router-link
          v-for="tag in displayTags"
          :key="tag.id"
          :to="`/tag/${tag.id}`"
          class="tag-item"
          :style="{ fontSize: getTagSize(tag.postCount) + 'px' }"
        >
          {{ tag.name }}
        </router-link>
        <router-link v-if="tagCount > 10" to="/tags" class="more-link">
          更多...
        </router-link>
        <el-empty v-if="tags.length === 0" description="暂无标签" :image-size="60" />
      </div>
    </div>

    <div class="sidebar-section">
      <h3 class="section-title">热门文章</h3>
      <div class="recent-posts">
        <router-link
          v-for="post in recentPosts"
          :key="post.id"
          :to="`/article/${post.slug}`"
          class="recent-post-item"
        >
          <span class="post-title text-ellipsis">{{ post.title }}</span>
          <span class="post-date">{{ formatDate(post.createdAt, 'MM-DD') }}</span>
        </router-link>
        <el-empty v-if="recentPosts.length === 0" description="暂无文章" :image-size="60" />
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { categoryApi } from '@/api/category'
import { tagApi } from '@/api/tag'
import { postApi } from '@/api/post'
import { userApi } from '@/api/user'
import { formatDate } from '@/utils/format'
import { Message } from '@element-plus/icons-vue'

const blogUser = ref(null)
const categories = ref([])
const tags = ref([])
const recentPosts = ref([])
const postCount = ref(0)
const categoryCount = ref(0)
const tagCount = ref(0)

const displayCategories = computed(() => {
  return categories.value.slice(0, 10)
})

const displayTags = computed(() => {
  return tags.value.slice(0, 10)
})

onMounted(async () => {
  try {
    const [userRes, categoryRes, tagRes, postListRes] = await Promise.all([
      userApi.getFirstUser(),
      categoryApi.getAll(),
      tagApi.getAll(),
      postApi.getList({ current: 1, size: 0 })
    ])
    if (userRes.data?.records?.length > 0) {
      blogUser.value = userRes.data.records[0]
    }
    categories.value = categoryRes.data || []
    categoryCount.value = categoryRes.data?.length || 0
    tags.value = (tagRes.data || []).slice(0, 10)
    tagCount.value = tagRes.data?.length || 0
    postCount.value = postListRes.data.total || 0
    
    // 单独获取热门文章用于展示
    const popularRes = await postApi.getPopular({ size: 10 })
    recentPosts.value = popularRes.data || []
  } catch (e) {
    console.error('加载侧边栏数据失败', e)
  }
})

function getTagSize(count) {
  const baseSize = 12
  const maxSize = 18
  const size = baseSize + Math.min(count || 0, 10)
  return Math.min(size, maxSize)
}
</script>

<style lang="scss" scoped>
.app-sidebar {
  padding-top: 0;
}

.profile-card {
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  margin-bottom: 24px;
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
  margin-bottom: 16px;
  line-height: 1.6;
}

.profile-contact-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 16px;
}

.contact-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
  text-decoration: none;
  transition: all 0.3s;

  &:hover {
    color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
  }
}

.contact-text {
  font-size: 13px;
  color: var(--el-text-color-secondary);
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

.sidebar-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-radius: 4px;
  color: var(--el-text-color-regular);
  text-decoration: none;
  transition: all 0.3s;

  &:hover {
    background: var(--el-fill-color-light);
    color: var(--el-color-primary);
  }
}

.category-count {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color);
  padding: 2px 8px;
  border-radius: 10px;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  color: var(--el-text-color-regular);
  text-decoration: none;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s;

  &:hover {
    color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
  }
}

.recent-posts {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recent-post-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  color: var(--el-text-color-regular);
  text-decoration: none;
  border-bottom: 1px dashed var(--el-border-color-lighter);
  transition: color 0.3s;
  cursor: pointer;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    color: var(--el-color-primary);
  }
}

.post-title {
  flex: 1;
  margin-right: 8px;
}

.post-date {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}

.more-link {
  display: block;
  text-align: center;
  padding: 8px;
  color: var(--el-color-primary);
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
  
  &:hover {
    color: var(--el-color-primary-light-3);
  }
}
</style>
