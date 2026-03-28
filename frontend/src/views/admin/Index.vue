<template>
  <div class="admin-dashboard">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff;">
              <el-icon :size="24"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ stats.postCount }}</p>
              <p class="stat-label">文章总数</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a;">
              <el-icon :size="24"><Folder /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ stats.categoryCount }}</p>
              <p class="stat-label">分类数量</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c;">
              <el-icon :size="24"><PriceTag /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ stats.tagCount }}</p>
              <p class="stat-label">标签数量</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c;">
              <el-icon :size="24"><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ stats.commentCount }}</p>
              <p class="stat-label">评论数量</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #909399;">
              <el-icon :size="24"><View /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ stats.viewCount }}</p>
              <p class="stat-label">阅读次数</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :lg="16">
        <el-card>
          <template #header>
            <span>最近文章</span>
          </template>
          <el-table :data="recentPosts" style="width: 100%">
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'">
                  {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="发布时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="router.push('/admin/posts/write')">
              <el-icon><Edit /></el-icon>
              写文章
            </el-button>
            <el-button @click="router.push('/admin/categories')">
              <el-icon><Folder /></el-icon>
              管理分类
            </el-button>
            <el-button @click="router.push('/admin/tags')">
              <el-icon><PriceTag /></el-icon>
              管理标签
            </el-button>
            <el-button @click="router.push('/admin/comments')">
              <el-icon><ChatDotRound /></el-icon>
              管理评论
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { postApi } from '@/api/post'
import { categoryApi } from '@/api/category'
import { tagApi } from '@/api/tag'
import { commentApi } from '@/api/comment'
import { formatDate } from '@/utils/format'
import { Document, Folder, PriceTag, ChatDotRound, Edit, View } from '@element-plus/icons-vue'

const router = useRouter()

const stats = ref({
  postCount: 0,
  categoryCount: 0,
  tagCount: 0,
  commentCount: 0,
  viewCount: 0
})

const recentPosts = ref([])

async function fetchStats() {
  try {
    const [postRes, categoryRes, tagRes, commentRes, viewCountRes] = await Promise.all([
      postApi.getList({ size: 1 }),
      categoryApi.getList({ size: 1 }),
      tagApi.getList({ size: 1 }),
      commentApi.getList({ size: 1 }),
      postApi.getTotalViewCount()
    ])
    
    stats.value.postCount = postRes.data.total || 0
    stats.value.categoryCount = categoryRes.data.total || 0
    stats.value.tagCount = tagRes.data.total || 0
    stats.value.commentCount = commentRes.data.total || 0
    stats.value.viewCount = viewCountRes.data || 0
  } catch (e) {
    console.error('获取统计数据失败', e)
  }
}

async function fetchRecentPosts() {
  try {
    const res = await postApi.getList({ size: 5 })
    recentPosts.value = res.data.records || []
  } catch (e) {
    console.error('获取最近文章失败', e)
  }
}

onMounted(() => {
  fetchStats()
  fetchRecentPosts()
})
</script>

<style lang="scss" scoped>
.admin-dashboard {
  .stat-card {
    margin-bottom: 20px;
  }

  .stat-content {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }

  .stat-info {
    flex: 1;
  }

  .stat-value {
    font-size: 28px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    margin-bottom: 4px;
  }

  .stat-label {
    font-size: 14px;
    color: var(--el-text-color-secondary);
  }

  .quick-actions {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .el-button {
      justify-content: flex-start;
    }
  }
}
</style>
