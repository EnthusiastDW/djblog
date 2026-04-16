<template>
  <aside class="app-left-sidebar">
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
      <div class="wechat-qrcode-section" v-if="blogUser?.wechatQrCode">
        <el-image 
          :src="blogUser.wechatQrCode" 
          fit="contain"
          class="qrcode-image"
          :preview-src-list="[blogUser.wechatQrCode]"
        />
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

    <!-- 广告位 -->
    <div class="ad-section" v-if="advertisement">
      <a :href="advertisement.link" target="_blank" class="ad-link">
        <el-image :src="advertisement.image" fit="cover" class="ad-image" />
      </a>
    </div>
  </aside>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/api/user'
import { postApi } from '@/api/post'
import { Message } from '@element-plus/icons-vue'

const blogUser = ref(null)
const postCount = ref(0)
const categoryCount = ref(0)
const tagCount = ref(0)
const advertisement = ref(null)

onMounted(async () => {
  try {
    const [userRes, postListRes] = await Promise.all([
      userApi.getFirstUser(),
      postApi.getList({ current: 1, size: 0 })
    ])
    console.log('用户数据:', userRes.data)
    if (userRes.data?.records?.length > 0) {
      blogUser.value = userRes.data.records[0]
      console.log('博主信息:', blogUser.value)
      console.log('微信二维码URL:', blogUser.value.wechatQrCode)
    }
    postCount.value = postListRes.data.total || 0
    
    // 获取分类和标签数量
    const categoryRes = await import('@/api/category')
    const tagRes = await import('@/api/tag')
    const categories = await categoryRes.categoryApi.getTree()
    const tags = await tagRes.tagApi.getAll()
    
    let totalCount = 0
    function countNodes(nodes) {
      for (const node of nodes) {
        totalCount++
        if (node.children) countNodes(node.children)
      }
    }
    countNodes(categories.data || [])
    categoryCount.value = totalCount
    tagCount.value = tags.data?.length || 0
  } catch (e) {
    console.error('加载左侧边栏数据失败', e)
  }
})
</script>

<style lang="scss" scoped>
.app-left-sidebar {
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

.wechat-qrcode-section {
  margin-top: 16px;
}

.qrcode-image {
  width: 220px;
  height: 220px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;
  
  &:hover {
    transform: scale(1.05);
  }
}

.ad-section {
  margin-bottom: 24px;
  border-radius: 8px;
  overflow: hidden;
}

.ad-link {
  display: block;
  text-decoration: none;
}

.ad-image {
  width: 100%;
  height: auto;
  display: block;
  transition: opacity 0.3s;
  
  &:hover {
    opacity: 0.9;
  }
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
</style>
