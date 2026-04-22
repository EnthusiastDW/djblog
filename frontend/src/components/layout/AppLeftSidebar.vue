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
      <div class="profile-contact-row" v-if="blogUser?.email || blogUser?.contactInfo || blogUser?.wechatQrCode">
        <a v-if="blogUser?.email" :href="'mailto:' + blogUser.email" class="contact-icon" title="发送邮件">
          <el-icon><Message /></el-icon>
        </a>
        <div v-if="blogUser?.wechatQrCode" class="wechat-icon-wrapper">
          <div class="contact-icon wechat-icon" title="微信">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="16" height="16">
              <path d="M8.5,2C4.9,2,2,4.5,2,7.5c0,1.8,1,3.4,2.6,4.4C4.5,12.6,4.4,13,4.3,13.4c-0.1,0.4-0.4,1.4-0.4,1.4s1.3-0.7,1.9-1 c0.1,0,0.2,0,0.3,0.1c0.8,0.2,1.6,0.4,2.5,0.4c0.3,0,0.6,0,0.9-0.1c-0.1-0.3-0.1-0.6-0.1-0.9c0-3,2.7-5.5,6-5.5c0.3,0,0.6,0,0.9,0.1 C16.2,4.5,12.7,2,8.5,2z M6.5,6.5c-0.6,0-1-0.4-1-1s0.4-1,1-1s1,0.4,1,1S7.1,6.5,6.5,6.5z M10.5,6.5c-0.6,0-1-0.4-1-1s0.4-1,1-1 s1,0.4,1,1S11.1,6.5,10.5,6.5z"/>
              <path d="M17.5,9c-2.8,0-5,1.9-5,4.3c0,1.3,0.7,2.5,1.8,3.3c-0.1,0.3-0.2,0.9-0.2,0.9s0.9-0.5,1.3-0.7 c0.1,0,0.1,0,0.2,0.1c0.6,0.2,1.2,0.3,1.9,0.3c0.2,0,0.4,0,0.6-0.1c-0.1-0.2-0.1-0.4-0.1-0.7c0-2.3,2.2-4.2,5-4.2c0.2,0,0.4,0,0.6,0.1 C23.2,10.4,20.6,9,17.5,9z M16,13c-0.4,0-0.8-0.3-0.8-0.8s0.3-0.8,0.8-0.8s0.8,0.3,0.8,0.8S16.4,13,16,13z M19,13 c-0.4,0-0.8-0.3-0.8-0.8s0.3-0.8,0.8-0.8s0.8,0.3,0.8,0.8S19.4,13,19,13z"/>
            </svg>
          </div>
          <div class="qrcode-tooltip">
            <img :src="blogUser.wechatQrCode" alt="微信二维码" class="qrcode-img" />
          </div>
        </div>
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
  white-space: pre-wrap;
  word-break: break-word;
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

.wechat-icon-wrapper {
  position: relative;
  display: inline-block;
  flex-shrink: 0;
  
  &:hover .qrcode-tooltip {
    opacity: 1;
    visibility: visible;
    transform: translateX(-50%) translateY(0);
  }
}

.wechat-icon {
  cursor: pointer;
}

.qrcode-tooltip {
  position: absolute;
  top: calc(100% + 10px);
  left: 50%;
  transform: translateX(-50%) translateY(-10px);
  padding: 8px;
  background: var(--el-bg-color);
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
  z-index: 1000;
  pointer-events: none;
  width: max-content;
  max-width: 250px;
  
  &::after {
    content: '';
    position: absolute;
    bottom: 100%;
    left: 50%;
    transform: translateX(-50%);
    border: 8px solid transparent;
    border-bottom-color: var(--el-bg-color);
  }
}

.qrcode-img {
  width: 100%;
  height: auto;
  aspect-ratio: 1 / 1;
  border-radius: 4px;
  display: block;
  object-fit: contain;
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
