<template>
  <div class="post-detail" v-loading="loading">
    <template v-if="post">
      <div class="article-layout">
        <article class="article">
          <div class="article-main">
            <header class="article-header">
              <h1 class="article-title">{{ post.title }}</h1>
              <div class="article-meta">
                <span class="meta-item">
                  <el-icon><Calendar /></el-icon>
                  {{ formatDate(post.createdAt) }}
                </span>
                <span class="meta-item" v-if="post.author">
                  <router-link :to="`/user/${post.author.id}`" class="author-link">
                    <el-avatar :size="20" :src="post.author.avatarUrl">
                      {{ post.author.username?.charAt(0) }}
                    </el-avatar>
                    {{ post.author.username }}
                  </router-link>
                </span>
                <span class="meta-item" v-if="post.categoryName">
                  <el-icon><Folder /></el-icon>
                  {{ post.categoryName }}
                </span>
                <span class="meta-item">
                  <el-icon><View /></el-icon>
                  {{ post.viewCount || 0 }} 次阅读
                </span>
              </div>
              <div class="article-tags" v-if="post.tags && post.tags.length > 0">
                <router-link
                  v-for="tag in post.tags"
                  :key="tag.id"
                  :to="`/tag/${tag.id}`"
                  class="tag-item"
                >
                  #{{ tag.name }}
                </router-link>
              </div>
            </header>

            <div class="article-cover" v-if="post.coverImage">
              <el-image :src="post.coverImage" fit="cover" />
            </div>
          </div>

          <div class="article-content" v-html="renderedContent"></div>
        </article>

        <!-- 目录侧边栏 - 使用渲染后的HTML内容以便正确解析标题 -->
        <toc-sidebar 
          :content="renderedContent || ''" 
          class="toc-sidebar-wrapper"
        />
      </div>

      <section class="comment-section">
        <h3 class="section-title">评论 ({{ comments.length }})</h3>
        
        <!-- 评论表单 -->
        <div class="comment-form">
          <template v-if="userStore.isLoggedIn">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              placeholder="写下你的评论..."
            />
            <el-button type="primary" @click="submitComment" :loading="submitting">
              发表评论
            </el-button>
          </template>
          <template v-else>
            <el-button type="primary" @click="showCommentDialog = true">
              发表评论
            </el-button>
          </template>
        </div>

        <!-- 匿名评论对话框 -->
        <el-dialog
          v-model="showCommentDialog"
          title="发表评论"
          width="500px"
          :close-on-click-modal="false"
        >
          <el-form :model="anonymousForm" :rules="rules" ref="formRef">
            <el-form-item label="昵称" prop="authorName">
              <el-input v-model="anonymousForm.authorName" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="邮箱" prop="authorEmail">
              <el-input v-model="anonymousForm.authorEmail" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="评论内容" prop="content">
              <el-input
                v-model="anonymousForm.content"
                type="textarea"
                :rows="4"
                placeholder="写下你的评论..."
              />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="showCommentDialog = false">取消</el-button>
            <el-button type="primary" @click="submitAnonymousComment" :loading="submitting">
              发表
            </el-button>
          </template>
        </el-dialog>

        <!-- 匿名回复对话框 -->
        <el-dialog
          v-model="showReplyDialog"
          title="回复评论"
          width="500px"
          :close-on-click-modal="false"
        >
          <el-form :model="anonymousReplyForm" :rules="rules" ref="replyFormRef">
            <el-form-item label="昵称" prop="authorName">
              <el-input v-model="anonymousReplyForm.authorName" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="邮箱" prop="authorEmail">
              <el-input v-model="anonymousReplyForm.authorEmail" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="回复内容" prop="content">
              <el-input
                v-model="anonymousReplyForm.content"
                type="textarea"
                :rows="3"
                placeholder="写下你的回复..."
              />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="showReplyDialog = false">取消</el-button>
            <el-button type="primary" @click="submitAnonymousReply(currentReplyComment)" :loading="submitting">
              回复
            </el-button>
          </template>
        </el-dialog>

        <div class="comment-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-avatar">
              <el-avatar :size="40">{{ comment.authorName?.charAt(0) || 'U' }}</el-avatar>
            </div>
            <div class="comment-content">
              <div class="comment-header">
                <span class="comment-author">{{ comment.authorName || '匿名用户' }}</span>
                <span class="comment-time">{{ fromNow(comment.createdAt) }}</span>
                <span class="comment-reply" @click="handleReply(comment)">回复</span>
              </div>
              <p class="comment-text">{{ comment.content }}</p>
              
              <!-- 根评论的回复按钮（匿名用户的回复也使用对话框） -->
              <div v-if="replyingTo === comment.id && userStore.isLoggedIn" class="reply-form">
                <el-input
                  v-model="replyContent"
                  type="textarea"
                  :rows="2"
                  placeholder="写下你的回复..."
                />
                <div class="reply-actions">
                  <el-button size="small" @click="replyingTo = null; replyContent = ''">取消</el-button>
                  <el-button size="small" type="primary" @click="submitRootReply(comment)" :loading="submitting">
                    回复
                  </el-button>
                </div>
              </div>
              
              <!-- 回复列表（递归组件） -->
              <div v-if="comment.replies && comment.replies.length > 0" class="reply-list">
                <CommentReply 
                  v-for="reply in comment.replies" 
                  :key="reply.id" 
                  :reply="reply" 
                  :depth="1"
                  :post-id="post.id"
                  @reply="handleReply"
                />
              </div>
            </div>
          </div>
          <el-empty v-if="comments.length === 0" description="暂无评论" />
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { postApi } from '@/api/post'
import { commentApi } from '@/api/comment'
import { formatDate, fromNow } from '@/utils/format'
import { Calendar, Folder, View } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import anchor from 'markdown-it-anchor'
import hljs from 'highlight.js'
import { setCookie, getCookie } from '@/utils/cookie'
import { ElMessage } from 'element-plus'
import CommentReply from '@/components/CommentReply.vue'
import TocSidebar from '@/components/TocSidebar.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const post = ref(null)
const comments = ref([])
const loading = ref(false)
const commentContent = ref('')
const submitting = ref(false)
const replyingTo = ref(null)
const replyContent = ref('')
const formRef = ref(null)
const replyFormRef = ref(null)
const showCommentDialog = ref(false)
const showReplyDialog = ref(false)
const currentReplyComment = ref(null)

// 动态主题映射
const themeMap = {
  dark: 'github-dark',
  light: 'github'
}

// 动态加载 highlight.js 主题
function loadHighlightTheme(theme) {
  const themeName = themeMap[theme] || 'github-dark'
  // 移除之前的主题
  document.querySelectorAll('link[data-highlight-theme]').forEach(el => el.remove())
  
  const link = document.createElement('link')
  link.rel = 'stylesheet'
  link.href = `https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.11.1/styles/${themeName}.min.css`
  link.setAttribute('data-highlight-theme', 'true')
  document.head.appendChild(link)
}

const md = new MarkdownIt({
  highlight: (str, lang) => {
    const language = lang || 'plaintext'
    // 移除代码前后的空行
    const trimmedStr = str.trim()
    const highlighted = hljs.highlight(trimmedStr, { language }).value
    
    return `<div class="code-block-wrapper"><div class="code-block-header"><span class="code-block-language">${language}</span><button class="copy-btn" title="复制代码" onclick="copyCode(this)"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg></button></div><pre><code class="hljs language-${language}">${highlighted}</code></pre></div>`
  }
}).use(anchor, {
  slugify: (str) => str.trim().toLowerCase().replace(/\s+/g, '-').replace(/[^\w\u4e00-\u9fa5-]/g, ''),
  permalink: false
})

// 复制代码函数
function copyCode(button) {
  const codeBlock = button.closest('.code-block-wrapper')
  const code = codeBlock.querySelector('code').textContent
  
  navigator.clipboard.writeText(code).then(() => {
    button.innerHTML = `
      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="20 6 9 17 4 12"></polyline>
      </svg>
    `
    button.title = '已复制'
    
    ElMessage.success('代码已复制到剪贴板')
    
    setTimeout(() => {
      button.innerHTML = `
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
          <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
        </svg>
      `
      button.title = '复制代码'
    }, 2000)
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

const anonymousReplyForm = reactive({
  authorName: getCookie('comment_author_name') || '',
  authorEmail: getCookie('comment_author_email') || '',
  content: ''
})

const rules = {
  authorName: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  authorEmail: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }],
  content: [{ required: true, message: '请输入评论内容', trigger: 'blur' }]
}

const renderedContent = computed(() => {
  if (!post.value?.content) return ''
  return md.render(post.value.content)
})

async function fetchPostDetail() {
  loading.value = true
  try {
    if (route.params.slug) {
      const res = await postApi.getBySlug(route.params.slug)
      post.value = res.data
    } else {
      const res = await postApi.getDetailWithContent(route.params.id)
      post.value = res.data
    }
    
    if (post.value?.id) {
      await fetchComments(post.value.id)
      postApi.incrementViewCount(post.value.id).catch(() => {})
    }
  } catch (e) {
    console.error('获取文章详情失败', e)
  } finally {
    loading.value = false
  }
}

async function fetchComments(postId) {
  try {
    const res = await commentApi.getByPostId(postId)
    comments.value = res.data || []
  } catch (e) {
    console.error('获取评论失败', e)
  }
}

function handleReply(comment) {
  // 如果是刷新评论列表的请求，不处理
  if (comment.refresh) {
    if (post.value?.id) {
      fetchComments(post.value.id)
    }
    return
  }
  
  if (userStore.isLoggedIn) {
    // 登录用户直接显示回复框
    replyingTo.value = comment.id
  } else {
    // 匿名用户显示对话框
    currentReplyComment.value = comment
    showReplyDialog.value = true
  }
}

async function submitComment() {
  if (!commentContent.value.trim()) return
  
  submitting.value = true
  try {
    await commentApi.create({
      postId: post.value.id,
      content: commentContent.value.trim(),
      userId: userStore.user?.id
    })
    commentContent.value = ''
    await fetchComments(post.value.id)
    ElMessage.success('评论发表成功')
  } catch (e) {
    console.error('发表评论失败', e)
    ElMessage.error('发表评论失败')
  } finally {
    submitting.value = false
  }
}

// 提交根评论的回复（登录用户）
async function submitRootReply(comment) {
  if (!replyContent.value.trim()) return
  
  submitting.value = true
  try {
    await commentApi.create({
      postId: post.value.id,
      content: replyContent.value.trim(),
      parentId: comment.id,
      userId: userStore.user?.id
    })
    replyContent.value = ''
    replyingTo.value = null
    await fetchComments(post.value.id)
    ElMessage.success('回复发表成功')
  } catch (e) {
    console.error('发表回复失败', e)
    ElMessage.error('发表回复失败')
  } finally {
    submitting.value = false
  }
}

async function submitAnonymousComment() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitting.value = true
  try {
    // 保存用户信息到 Cookie
    setCookie('comment_author_name', anonymousForm.authorName)
    setCookie('comment_author_email', anonymousForm.authorEmail)
    
    await commentApi.create({
      postId: post.value.id,
      content: anonymousForm.content.trim(),
      authorName: anonymousForm.authorName.trim(),
      authorEmail: anonymousForm.authorEmail.trim()
    })
    anonymousForm.authorName = ''
    anonymousForm.authorEmail = ''
    anonymousForm.content = ''
    showCommentDialog.value = false
    await fetchComments(post.value.id)
    ElMessage.success('评论发表成功')
  } catch (e) {
    console.error('发表评论失败', e)
  } finally {
    submitting.value = false
  }
}

async function submitReply(comment) {
  if (!replyContent.value.trim()) return
  
  submitting.value = true
  try {
    await commentApi.create({
      postId: post.value.id,
      content: replyContent.value.trim(),
      parentId: comment.id,
      userId: userStore.user?.id
    })
    replyContent.value = ''
    replyingTo.value = null
    await fetchComments(post.value.id)
    ElMessage.success('回复发表成功')
  } catch (e) {
    console.error('发表回复失败', e)
  } finally {
    submitting.value = false
  }
}

async function submitAnonymousReply(comment) {
  const valid = await replyFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitting.value = true
  try {
    // 保存用户信息到 Cookie
    setCookie('comment_author_name', anonymousReplyForm.authorName)
    setCookie('comment_author_email', anonymousReplyForm.authorEmail)
    
    await commentApi.create({
      postId: post.value.id,
      content: anonymousReplyForm.content.trim(),
      authorName: anonymousReplyForm.authorName.trim(),
      authorEmail: anonymousReplyForm.authorEmail.trim(),
      parentId: comment.id
    })
    anonymousReplyForm.authorName = ''
    anonymousReplyForm.authorEmail = ''
    anonymousReplyForm.content = ''
    showReplyDialog.value = false
    currentReplyComment.value = null
    await fetchComments(post.value.id)
    ElMessage.success('回复发表成功')
  } catch (e) {
    console.error('发表回复失败', e)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchPostDetail()
  // 加载当前主题的代码高亮样式
  loadHighlightTheme(appStore.theme)
})

// 监听主题变化，动态切换代码高亮主题
watch(() => appStore.theme, (newTheme) => {
  loadHighlightTheme(newTheme)
})
</script>

<style lang="scss" scoped>
.post-detail {
  max-width: 1200px;
  margin: 0 auto;
}

.article-layout {
  display: flex;
  gap: 24px;
}

.article {
  flex: 1;
  min-width: 0;
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 32px;
  margin-bottom: 24px;
}

.toc-sidebar-wrapper {
  width: 240px;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .article-layout {
    flex-direction: column;
  }
  
  .toc-sidebar-wrapper {
    width: 100%;
  }
}

.article {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 32px;
  margin-bottom: 24px;
}

.article-main {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
}

.article-header {
  flex: 1;
  min-width: 0;
}

.article-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  line-height: 1.4;
  margin-bottom: 16px;
}

.article-meta {
  display: flex;
  gap: 20px;
  color: var(--el-text-color-secondary);
  font-size: 14px;
  margin-bottom: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.author-link {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--el-text-color-secondary);
  text-decoration: none;
  
  &:hover {
    color: var(--el-color-primary);
  }
}

.article-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag-item {
  color: var(--el-color-primary);
  font-size: 14px;
  text-decoration: none;

  &:hover {
    text-decoration: underline;
  }
}

.article-cover {
  width: 200px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;

  .el-image {
    width: 100%;
    height: 150px;
  }
}

.article-content {
  line-height: 1.8;
  color: var(--el-text-color-primary);

  :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
    margin: 24px 0 16px;
    font-weight: 600;
    line-height: 1.4;
  }

  :deep(h1) { font-size: 28px; }
  :deep(h2) { font-size: 24px; }
  :deep(h3) { font-size: 20px; }
  :deep(h4) { font-size: 18px; }

  :deep(p) {
    margin-bottom: 16px;
  }

  :deep(pre) {
    margin: 16px 0;
    border-radius: 8px;
    overflow: hidden;
  }

  :deep(.code-block-wrapper) {
    position: relative;
    border-radius: 8px;
    overflow: hidden;
    
    pre {
      margin: 0;
      padding: 16px 16px;
      background: #1e1e1e;
      overflow-x: auto;
      border-radius: 0;
      line-height: 1.5;
      
      code {
        color: #d4d4d4;
        font-size: 14px;
        font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
        white-space: pre-wrap;
        word-wrap: break-word;
        word-break: break-all;
        background: transparent;
        padding: 0;
        display: block;
      }
    }
  }

  :deep(.code-block-header) {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 16px;
    background: #2d2d2d;
    border-bottom: 1px solid #404040;
    border-radius: 0;
    
    .code-block-language {
      font-size: 12px;
      color: #9cdcfe;
      font-weight: 600;
      text-transform: uppercase;
    }
    
    .copy-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 32px;
      height: 32px;
      padding: 0;
      border: none;
      background: transparent;
      color: #9cdcfe;
      cursor: pointer;
      border-radius: 4px;
      transition: all 0.3s;
      
      &:hover {
        background: rgba(79, 195, 247, 0.1);
        color: #4fc3f7;
      }
      
      svg {
        width: 16px;
        height: 16px;
      }
    }
  }

  :deep(code) {
    background: var(--el-fill-color-light);
    border-radius: 4px;
    font-size: 14px;
  }

  :deep(blockquote) {
    border-left: 4px solid var(--el-color-primary);
    padding-left: 16px;
    margin: 16px 0;
    color: var(--el-text-color-secondary);
  }

  :deep(ul), :deep(ol) {
    padding-left: 24px;
    margin-bottom: 16px;
  }

  :deep(li) {
    margin-bottom: 8px;
  }

  :deep(img) {
    max-width: 100%;
    border-radius: 8px;
    margin: 16px 0;
  }

  :deep(a) {
    color: var(--el-color-primary);
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: 16px 0;

    th, td {
      border: 1px solid var(--el-border-color);
      padding: 8px 12px;
      text-align: left;
    }

    th {
      background: var(--el-fill-color-light);
      font-weight: 600;
    }
  }
}

.comment-section {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 20px;
}

.comment-form {
  margin-bottom: 24px;

  .el-textarea {
    margin-bottom: 12px;
  }
}

.login-tip {
  margin-bottom: 24px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.comment-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.comment-text {
  color: var(--el-text-color-regular);
  line-height: 1.6;
}

.comment-reply {
  color: var(--el-color-primary);
  font-size: 14px;
  cursor: pointer;
  margin-left: auto;
  
  &:hover {
    text-decoration: underline;
  }
}

.reply-form {
  margin-top: 12px;
  padding: 12px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  
  .el-textarea {
    margin-bottom: 8px;
  }
  
  .reply-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}

.reply-list {
  margin-top: 12px;
  margin-left: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.reply-item {
  display: flex;
  gap: 8px;
}

.reply-content {
  flex: 1;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.reply-author {
  font-weight: 500;
  color: var(--el-text-color-primary);
  font-size: 14px;
}

.reply-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.reply-text {
  color: var(--el-text-color-regular);
  line-height: 1.6;
  font-size: 14px;
}
</style>
