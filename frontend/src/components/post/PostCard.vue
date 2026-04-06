<template>
  <article class="post-card" @click="handleClick">
    <div class="post-content">
      <h2 class="post-title">{{ post.title }}</h2>
      <p class="post-summary text-ellipsis-2">{{ post.summary || '暂无摘要' }}</p>
      <div class="post-meta">
        <span class="meta-item" v-if="post.authorName">
          <el-icon><User /></el-icon>
          {{ post.authorName }}
        </span>
        <span class="meta-item">
          <el-icon><Calendar /></el-icon>
          {{ formatDate(post.createdAt, 'YYYY-MM-DD') }}
        </span>
        <span class="meta-item" v-if="post.categoryName">
          <el-icon><Folder /></el-icon>
          {{ post.categoryName }}
        </span>
        <span class="meta-item" v-if="post.tags && post.tags.length > 0">
          <el-icon><PriceTag /></el-icon>
          <span class="tags-container">
            <span v-for="(tag, index) in post.tags" :key="tag.id" class="tag-item">
              {{ tag.name }}{{ index < post.tags.length - 1 ? ',' : '' }}
            </span>
          </span>
        </span>
        <span class="meta-item">
          <el-icon><View /></el-icon>
          {{ post.viewCount || 0 }}
        </span>
      </div>
    </div>
    <div class="post-cover" v-if="post.coverImage">
      <el-image :src="post.coverImage" fit="cover" lazy />
    </div>
  </article>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { formatDate } from '@/utils/format'
import { Calendar, Folder, View, PriceTag, User } from '@element-plus/icons-vue'

const props = defineProps({
  post: {
    type: Object,
    required: true
  }
})

const router = useRouter()

function handleClick() {
  router.push(`/article/${props.post.slug}`)
}
</script>

<style lang="scss" scoped>
.post-card {
  display: flex;
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  width: 100%;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
}

.post-content {
  flex: 1;
  min-width: 0;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
  line-height: 1.4;

  &:hover {
    color: var(--el-color-primary);
  }
}

.post-summary {
  color: var(--el-text-color-regular);
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
}

.post-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.tags-container {
  display: inline;
}

.post-cover {
  width: 160px;
  height: 100px;
  margin-left: 20px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;

  .el-image {
    width: 100%;
    height: 100%;
  }
}

@media (max-width: 768px) {
  .post-cover {
    display: none;
  }
}
</style>
