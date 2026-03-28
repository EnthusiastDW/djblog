<template>
  <div class="comment-reply" :style="{ marginLeft: depth * 20 + 'px' }">
    <div class="reply-item">
      <div class="reply-avatar">
        <el-avatar :size="32">{{ reply.authorName?.charAt(0) || 'U' }}</el-avatar>
      </div>
      <div class="reply-content">
        <div class="reply-header">
          <span class="reply-author">{{ reply.authorName || '匿名用户' }}</span>
          <span class="reply-time">{{ fromNow(reply.createdAt) }}</span>
          <!-- 最大深度为 5 层，避免嵌套过深 -->
          <span 
            v-if="depth < 5" 
            class="reply-reply-btn" 
            @click="handleReplyClick"
          >
            回复
          </span>
        </div>
        <p class="reply-text">{{ reply.content }}</p>
        
        <!-- 登录用户的回复输入框 -->
        <div v-if="showReplyInput && userStore.isLoggedIn" class="inline-reply-form">
          <el-input
            v-model="replyContent"
            type="textarea"
            :rows="2"
            placeholder="写下你的回复..."
          />
          <div class="reply-actions">
            <el-button size="small" @click="cancelReply">取消</el-button>
            <el-button size="small" type="primary" @click="submitInlineReply" :loading="submitting">
              回复
            </el-button>
          </div>
        </div>
        
        <!-- 递归显示子回复 -->
        <div v-if="reply.replies && reply.replies.length > 0" class="sub-replies">
          <CommentReply 
            v-for="subReply in reply.replies" 
            :key="subReply.id" 
            :reply="subReply" 
            :depth="depth + 1"
            :post-id="postId"
            @reply="handleSubReply"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { commentApi } from '@/api/comment'
import { fromNow } from '@/utils/format'
import { ElMessage } from 'element-plus'

const props = defineProps({
  reply: {
    type: Object,
    required: true
  },
  depth: {
    type: Number,
    default: 0
  },
  postId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['reply'])

const userStore = useUserStore()

const replyContent = ref('')
const submitting = ref(false)
const showReplyInput = ref(false)

// 处理子回复点击
function handleSubReply(reply) {
  emit('reply', reply)
}

// 显示回复输入框
function showInlineReply() {
  if (userStore.isLoggedIn) {
    showReplyInput.value = true
  } else {
    emit('reply', props.reply)
  }
}

// 点击回复按钮
function handleReplyClick() {
  showInlineReply()
}

// 取消回复
function cancelReply() {
  showReplyInput.value = false
  replyContent.value = ''
}

// 提交内联回复（仅登录用户）
async function submitInlineReply() {
  if (!replyContent.value.trim()) return
  
  submitting.value = true
  try {
    await commentApi.create({
      postId: props.postId,
      content: replyContent.value.trim(),
      parentId: props.reply.id,
      userId: userStore.user?.id
    })
    replyContent.value = ''
    showReplyInput.value = false
    ElMessage.success('回复发表成功')
    emit('reply', { refresh: true })
  } catch (e) {
    console.error('发表回复失败', e)
    ElMessage.error('发表回复失败')
  } finally {
    submitting.value = false
  }
}

// 暴露方法给父组件
defineExpose({
  showInlineReply
})
</script>

<style lang="scss" scoped>
.comment-reply {
  margin-top: 12px;
}

.reply-item {
  display: flex;
  gap: 8px;
  padding: 8px 0;
}

.reply-avatar {
  flex-shrink: 0;
}

.reply-content {
  flex: 1;
  background: var(--el-fill-color-light);
  padding: 8px 12px;
  border-radius: 8px;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
  font-size: 13px;
}

.reply-author {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.reply-time {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.reply-reply-btn {
  color: var(--el-color-primary);
  cursor: pointer;
  font-size: 12px;
  margin-left: auto;
  
  &:hover {
    text-decoration: underline;
  }
}

.reply-text {
  color: var(--el-text-color-regular);
  line-height: 1.6;
  font-size: 14px;
  margin: 0;
}

.inline-reply-form {
  margin-top: 8px;
  
  .el-textarea {
    margin-bottom: 8px;
  }
  
  .reply-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}

.sub-replies {
  margin-top: 8px;
}
</style>
