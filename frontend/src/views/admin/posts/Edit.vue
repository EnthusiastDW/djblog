<template>
  <div class="admin-post-edit" :class="{ 'is-fullscreen': fullscreen }">
    <div class="page-header">
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2 class="page-title">{{ isEdit ? '编辑文章' : '写文章' }}</h2>
      <div class="header-actions">
        <!-- 自动保存设置 -->
        <div class="auto-save-settings">
          <el-switch
            v-model="autoSaveEnabled"
            active-text="自动保存"
            @change="handleAutoSaveToggle"
          />
          <el-select
            v-model="autoSaveInterval"
            size="small"
            style="width: 100px; margin-left: 8px;"
            :disabled="!autoSaveEnabled"
            @change="handleIntervalChange"
          >
            <el-option label="1分钟" :value="60" />
            <el-option label="2分钟" :value="120" />
            <el-option label="5分钟" :value="300" />
            <el-option label="10分钟" :value="600" />
          </el-select>
          <span v-if="lastSaveTime" class="last-save-time">
            上次保存: {{ formatLastSaveTime }}
          </span>
        </div>
        <!-- 同步平台选择 -->
        <div class="sync-platforms" v-if="form.id">
          <span class="sync-label">同步到：</span>
          <el-checkbox-group v-model="form.syncPlatforms" size="small">
            <el-checkbox
              v-for="p in platformList"
              :key="p.code"
              :label="p.code"
              :disabled="p.disabled"
            >
              {{ p.label }}
            </el-checkbox>
          </el-checkbox-group>
        </div>
        <el-button @click="handleSaveDraft" :loading="saving">保存草稿</el-button>
        <el-button type="primary" @click="handlePublish" :loading="publishing">发布</el-button>
      </div>
    </div>

    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" class="edit-form">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入文章标题" />
      </el-form-item>

      <el-form-item label="Slug" prop="slug" v-show="!fullscreen">
        <el-input v-model="form.slug" placeholder="URL友好别名，如 my-awesome-post">
          <template #append>
            <el-button
              :loading="aiGenerating"
              :disabled="!form.title || !form.content"
              @click="handleGenerateSlugAndSummary"
            >
              <el-icon><MagicStick /></el-icon>
              AI生成
            </el-button>
          </template>
        </el-input>
        <div class="slug-hint">发布后将作为文章链接的一部分，建议使用英文或拼音，发布后不建议修改</div>
      </el-form-item>
      
      <el-form-item label="摘要" prop="summary" v-show="!fullscreen">
        <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="请输入文章摘要" />
      </el-form-item>

      <!-- 分类、标签、封面紧凑排列 -->
      <div class="meta-row" v-show="!fullscreen">
        <el-form-item label="分类" prop="categoryId" class="meta-item">
          <div class="category-select-wrapper">
            <el-tree-select
              v-model="form.categoryId"
              :data="categoryTree"
              :props="{ label: 'name', value: 'id', children: 'children' }"
              placeholder="请选择分类"
              check-strictly
              clearable
              filterable
              :render-after-expand="false"
              :loading="categoryLoading"
              style="width: 100%;"
            />
            <el-button type="primary" link class="add-category-btn" @click="handleAddCategory">
              <el-icon><Plus /></el-icon>
              新增
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="标签" prop="tagIds" class="meta-item">
          <el-select
            v-model="form.tagIds"
            multiple
            placeholder="选择或输入新标签"
            filterable
            allow-create
            default-first-option
            :loading="tagLoading"
            style="width: 100%;"
            @change="handleTagChange"
          >
            <el-option
              v-for="tag in tags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="封面" class="meta-item">
          <el-input v-model="form.coverImage" placeholder="封面图片链接" />
        </el-form-item>
      </div>

      <el-form-item label="内容" prop="content" class="editor-form-item">
        <div class="editor-container">
          <div class="editor-toolbar">
            <el-button-group>
              <el-button size="small" @click="insertMarkdown('**', '**')">B</el-button>
              <el-button size="small" @click="insertMarkdown('*', '*')">I</el-button>
              <el-button size="small" @click="insertMarkdown('# ', '')">H1</el-button>
              <el-button size="small" @click="insertMarkdown('## ', '')">H2</el-button>
              <el-button size="small" @click="insertMarkdown('```\n', '\n```')">Code</el-button>
              <el-button size="small" @click="insertMarkdown('[', '](url)')">Link</el-button>
              <el-button size="small" @click="insertMarkdown('![alt](', ')')">Image</el-button>
            </el-button-group>
            <el-button size="small" :icon="FullScreen" @click="toggleFullscreen" class="fullscreen-btn">
              {{ fullscreen ? '退出全屏' : '全屏' }}
            </el-button>
          </div>
          <div class="editor-main">
            <el-input
              ref="editorRef"
              v-model="form.content"
              type="textarea"
              placeholder="请输入文章内容（支持Markdown格式）"
              class="editor-textarea"
            />
            <div class="editor-preview" ref="previewRef" v-html="renderedContent" @scroll="onPreviewScroll"></div>
          </div>
        </div>
      </el-form-item>
    </el-form>

    <!-- 新增分类对话框 -->
    <CategoryCreateDialog
      v-model="categoryDialogVisible"
      :category-tree="categoryTree"
      @success="handleCategoryCreated"
    />

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { postApi } from '@/api/post'
import { categoryApi } from '@/api/category'
import { tagApi } from '@/api/tag'
import { ArrowLeft, MagicStick, Setting, Plus, FullScreen } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import MarkdownIt from 'markdown-it'
import { createHighlightWithWrapper, setupInlineCodeCopy } from '@/utils/highlight'
import { mermaidPlugin, initializeMermaid, renderMermaid } from '@/utils/mermaid-plugin'
import CategoryCreateDialog from '@/components/CategoryCreateDialog.vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const formRef = ref(null)
const editorRef = ref(null)
const previewRef = ref(null)
let syncingScroll = false
let leaveConfirmed = false
const saving = ref(false)
const publishing = ref(false)
const categories = ref([])
const categoryTree = ref([])
const tags = ref([])
const categoryLoading = ref(false)
const tagLoading = ref(false)
const aiGenerating = ref(false)
const categoryDialogVisible = ref(false)

// 平台同步相关
const platformList = ref([])

// 自动保存相关
const autoSaveEnabled = ref(false)
const autoSaveInterval = ref(120) // 默认2分钟（秒）
const lastSaveTime = ref(null)
let autoSaveTimer = null

// 全屏编辑
const fullscreen = ref(false)
function toggleFullscreen() {
  fullscreen.value = !fullscreen.value
}

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  id: null,
  title: '',
  slug: '',
  summary: '',
  content: '',
  categoryId: null,
  tagIds: [],
  coverImage: '',
  syncPlatforms: []
})

// 表单快照，用于检测未保存的更改
const initialForm = ref(null)
function takeFormSnapshot() {
  initialForm.value = { ...form, tagIds: [...form.tagIds], syncPlatforms: [...form.syncPlatforms] }
}
function formMatchesSnapshot() {
  if (!initialForm.value) return true
  return initialForm.value.title === form.title
    && initialForm.value.slug === form.slug
    && initialForm.value.summary === form.summary
    && initialForm.value.content === form.content
    && initialForm.value.categoryId === form.categoryId
    && JSON.stringify(initialForm.value.tagIds) === JSON.stringify(form.tagIds)
    && initialForm.value.coverImage === form.coverImage
    && JSON.stringify(initialForm.value.syncPlatforms) === JSON.stringify(form.syncPlatforms)
}
const hasUnsavedChanges = computed(() => !formMatchesSnapshot())

// 编辑器与预览同步滚动
function getTextareaEl() {
  return editorRef.value?.$el?.querySelector('textarea')
}

function syncScroll(source, target) {
  if (!source || !target || syncingScroll) return
  syncingScroll = true
  const ratio = source.scrollTop / (source.scrollHeight - source.clientHeight)
  target.scrollTop = ratio * (target.scrollHeight - target.clientHeight)
  syncingScroll = false
}

function onTextareaScroll() {
  const ta = getTextareaEl()
  syncScroll(ta, previewRef.value)
}

function onPreviewScroll() {
  syncScroll(previewRef.value, getTextareaEl())
}

async function confirmLeave() {
  if (!hasUnsavedChanges.value) return true
  try {
    await ElMessageBox.confirm('当前内容尚未保存，确定要离开吗？', '未保存的更改', {
      confirmButtonText: '离开',
      cancelButtonText: '取消',
      type: 'warning'
    })
    return true
  } catch {
    return false
  }
}

function handleBack() {
  confirmLeave().then(ok => {
    if (ok) {
      leaveConfirmed = true
      router.back()
    }
  })
}

const rules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  slug: [
    { required: true, message: '请输入 Slug', trigger: 'blur' },
    { pattern: /^[a-z0-9]+(?:-[a-z0-9]+)*$/, message: 'Slug 格式错误：仅支持小写字母、数字和连字符', trigger: 'blur' }
  ],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }]
}

const themeMap = {
  dark: 'atom-one-dark',
  light: 'atom-one-light'
}

function loadHighlightTheme(theme) {
  const themeName = themeMap[theme] || 'atom-one-dark'
  document.querySelectorAll('link[data-highlight-theme]').forEach(el => el.remove())
  
  const link = document.createElement('link')
  link.rel = 'stylesheet'
  link.href = `https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.11.1/styles/${themeName}.min.css`
  link.setAttribute('data-highlight-theme', 'true')
  document.head.appendChild(link)
}

const md = new MarkdownIt({
  highlight: createHighlightWithWrapper()
}).use(mermaidPlugin)

// 配置行内代码复制按钮
setupInlineCodeCopy(md)

const renderedContent = computed(() => {
  if (!form.content) return ''
  return md.render(form.content)
})

// 内容变化后触发 Mermaid 渲染图表
watch(renderedContent, async () => {
  await nextTick()
  renderMermaid()
}, { immediate: true })

// 格式化上次保存时间
const formatLastSaveTime = computed(() => {
  if (!lastSaveTime.value) return ''
  // 显示精确的保存时间：HH:MM:SS
  return lastSaveTime.value.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
})

async function fetchCategories() {
  categoryLoading.value = true
  try {
    const res = await categoryApi.getTree()
    categoryTree.value = res.data || []
  } catch (e) {
    console.error('获取分类失败', e)
  } finally {
    categoryLoading.value = false
  }
}

async function fetchTags() {
  tagLoading.value = true
  try {
    const res = await tagApi.getAll()
    tags.value = res.data || []
  } catch (e) {
    console.error('获取标签失败', e)
  } finally {
    tagLoading.value = false
  }
}



async function handleTagChange(values) {
  // 处理标签变化，检查是否有新创建的标签
  const newTags = values.filter(value => typeof value === 'string')
  if (newTags.length === 0) return

  tagLoading.value = true
  try {
    for (const tagName of newTags) {
      const trimmedName = tagName.trim()
      if (!trimmedName) continue

      // 检查是否已存在相同名称的标签
      const existingTag = tags.value.find(t => t.name === trimmedName)
      if (existingTag) {
        // 替换为已存在的标签ID
        const index = form.tagIds.indexOf(tagName)
        if (index > -1) {
          form.tagIds.splice(index, 1, existingTag.id)
        }
        ElMessage.info(`标签 "${trimmedName}" 已存在`)
        continue
      }

      // 创建新标签
      const res = await tagApi.create({ name: trimmedName })
      const newId = res.data
      tags.value.push({ id: newId, name: trimmedName })
      
      // 替换表单中的标签名为ID
      const index = form.tagIds.indexOf(tagName)
      if (index > -1) {
        form.tagIds.splice(index, 1, newId)
      }
    }
    if (newTags.length > 0) {
      ElMessage.success('标签创建成功')
    }
  } catch (e) {
    console.error('创建标签失败', e)
    ElMessage.error(e.response?.data?.message || '创建标签失败')
  } finally {
    tagLoading.value = false
  }
}

async function handleGenerateSlugAndSummary() {
  if (!form.title) {
    ElMessage.warning('请先输入文章标题')
    return
  }
  if (!form.content) {
    ElMessage.warning('请先输入文章内容')
    return
  }
  aiGenerating.value = true
  try {
    const res = await postApi.generateSlugAndSummary(form.title, form.content)
    if (res.data) {
      if (res.data.slug) form.slug = res.data.slug
      if (res.data.summary) form.summary = res.data.summary
      ElMessage.success(
        [res.data.slug && 'Slug', res.data.summary && '摘要'].filter(Boolean).join('+') + ' 生成成功'
      )
    }
  } catch (e) {
    console.error('AI 生成失败', e)
    ElMessage.error('AI 生成失败，请重试')
  } finally {
    aiGenerating.value = false
  }
}

async function fetchPost() {
  if (!route.params.id) return
  
  try {
    const res = await postApi.getDetailWithContent(route.params.id)
    const post = res.data
    form.id = post.id
    form.title = post.title
    form.slug = post.slug || ''
    form.summary = post.summary || ''
    form.content = post.content || ''
    form.categoryId = post.categoryId
    form.tagIds = post.tags?.map(t => t.id) || []
    form.coverImage = post.coverImage || ''
    takeFormSnapshot()
  } catch (e) {
    console.error('获取文章失败', e)
  }
}

function insertMarkdown(prefix, suffix) {
  const textarea = editorRef.value?.$el?.querySelector('textarea')
  if (!textarea) return
  
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = form.content.substring(start, end)
  const newText = prefix + selectedText + suffix
  
  form.content = form.content.substring(0, start) + newText + form.content.substring(end)
  
  // 重新设置光标位置
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + prefix.length, start + prefix.length + selectedText.length)
  }, 0)
}

async function handleSaveDraft() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const res = await postApi.saveDraft(form)
    // 如果是新建文章，后端返回文章ID，需要更新到表单中
    if (res.data && !form.id) {
      form.id = res.data
      console.log('[Save Draft] New draft created with ID:', form.id)
    }
    // 更新同步平台列表（新建草稿后让同步选项可见）
    await fetchSyncStatus()
    lastSaveTime.value = new Date()
    takeFormSnapshot()
    ElMessage.success('草稿已保存')
    // 不跳转页面，保持在编辑页
  } catch (e) {
    console.error('保存失败', e)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 自动保存（静默保存，不显示成功消息）
async function autoSaveDraft() {
  // 如果没有标题或内容，不自动保存
  if (!form.title && !form.content) return
  
  try {
    const res = await postApi.saveDraft(form)
    // 如果是新建文章，后端返回文章ID，需要更新到表单中
    if (res.data && !form.id) {
      form.id = res.data
      console.log('[Auto Save] New draft created with ID:', form.id)
    }
    // 更新同步平台列表（新建草稿后让同步选项可见）
    await fetchSyncStatus()
    lastSaveTime.value = new Date()
    console.log('[Auto Save] Draft saved at', lastSaveTime.value)
  } catch (e) {
    console.error('[Auto Save] Failed to save draft', e)
  }
}

// 启动自动保存定时器
function startAutoSaveTimer() {
  stopAutoSaveTimer() // 先清除之前的定时器
  if (autoSaveEnabled.value && autoSaveInterval.value > 0) {
    autoSaveTimer = setInterval(() => {
      autoSaveDraft()
    }, autoSaveInterval.value * 1000)
    console.log(`[Auto Save] Timer started, interval: ${autoSaveInterval.value}s`)
  }
}

// 停止自动保存定时器
function stopAutoSaveTimer() {
  if (autoSaveTimer) {
    clearInterval(autoSaveTimer)
    autoSaveTimer = null
    console.log('[Auto Save] Timer stopped')
  }
}

// 切换自动保存
function handleAutoSaveToggle(enabled) {
  if (enabled) {
    startAutoSaveTimer()
    ElMessage.success('自动保存已开启')
  } else {
    stopAutoSaveTimer()
    ElMessage.info('自动保存已关闭')
  }
}

// 更改自动保存间隔
function handleIntervalChange() {
  if (autoSaveEnabled.value) {
    startAutoSaveTimer() // 重启定时器以应用新间隔
    ElMessage.success(`自动保存间隔已设置为${autoSaveInterval.value / 60}分钟`)
  }
}

async function handlePublish() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  publishing.value = true
  try {
    // 无论新建还是编辑，都使用 publish 方法确保状态为已发布
    const res = await postApi.publish(form)
    // 如果是新建文章，后端返回文章ID，需要更新到表单中
    if (res.data && !form.id) {
      form.id = res.data
      console.log('[Publish] New article published with ID:', form.id)
    }
    takeFormSnapshot()
    ElMessage.success('发布成功')
    router.push('/admin/posts')
  } catch (e) {
    console.error('发布失败', e)
  } finally {
    publishing.value = false
  }
}

function handleAddCategory() {
  categoryDialogVisible.value = true
}

function handleCategoryCreated() {
  // 重新加载分类树
  fetchCategories()
}

async function fetchSyncStatus() {
  if (!form.id) return
  try {
    const res = await postApi.getSyncStatus(form.id)
    const records = res.data || []
    const statusMap = {}
    records.forEach(r => { statusMap[r.platformCode] = r })

    const basePlatforms = [
      { code: 'CNBLOG', label: '博客园' }
    ]
    platformList.value = basePlatforms.map(p => ({
      ...p,
      disabled: statusMap[p.code]?.syncStatus === 'SUCCESS',
      syncStatus: statusMap[p.code]?.syncStatus
    }))
  } catch (e) {
    console.error('获取同步状态失败', e)
  }
}

// 监听路由参数变化，处理 Edit.vue 组件复用（/write ↔ /edit/:id）
watch(() => route.params.id, async (newId) => {
  // 重置表单
  form.id = null
  form.title = ''
  form.slug = ''
  form.summary = ''
  form.content = ''
  form.categoryId = null
  form.tagIds = []
  form.coverImage = ''
  form.syncPlatforms = []

  if (newId) {
    await fetchPost()
    await fetchSyncStatus()
  }
}, { immediate: true })

onMounted(async () => {
  fetchCategories()
  fetchTags()
  initializeMermaid(appStore.theme)
  loadHighlightTheme(appStore.theme)
  // 启动自动保存
  startAutoSaveTimer()
  // 浏览器刷新/关闭时提示
  window.addEventListener('beforeunload', handleBeforeUnload)
  takeFormSnapshot()
  // 编辑器与预览同步滚动
  nextTick(() => {
    const ta = getTextareaEl()
    if (ta) {
      ta.addEventListener('scroll', onTextareaScroll)
    }
  })
})

// 组件卸载时清除定时器
onUnmounted(() => {
  stopAutoSaveTimer()
  window.removeEventListener('beforeunload', handleBeforeUnload)
  const ta = getTextareaEl()
  if (ta) {
    ta.removeEventListener('scroll', onTextareaScroll)
  }
})

// 路由离开前检查未保存的更改（侧边栏切换、路由跳转等）
onBeforeRouteLeave(async (to, from, next) => {
  // 如果已通过 handleBack 确认，直接放行
  if (leaveConfirmed) {
    leaveConfirmed = false
    next()
    return
  }
  if (await confirmLeave()) {
    next()
  } else {
    next(false)
  }
})

function handleBeforeUnload(e) {
  if (hasUnsavedChanges.value) {
    e.preventDefault()
    e.returnValue = ''
  }
}

watch(() => appStore.theme, async (newTheme) => {
  loadHighlightTheme(newTheme)
  initializeMermaid(newTheme)
  await nextTick()
  renderMermaid()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/_markdown.scss';

.admin-post-edit {
  // 使用负边距抵消父容器的 padding，让 header 占满宽度
  margin: -20px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 100px); // 填充父容器高度，让编辑器占更多空间

  .page-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin: -20px -20px 20px -20px; // 负边距抵消容器 padding
    padding: 16px 20px;
    background: var(--el-bg-color);
    border-radius: 0; // 移除圆角，紧贴边缘
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    position: sticky;
    top: -20px; // 调整为负值，紧贴顶部
    z-index: 100;
    transition: box-shadow 0.3s;
    flex-shrink: 0;

    // 滚动时增强阴影效果
    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
  }

  .page-title {
    flex: 1;
    font-size: 20px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .auto-save-settings {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .last-save-time {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    white-space: nowrap;
  }

  .category-select-wrapper {
    display: flex;
    align-items: center;
    gap: 8px;
    width: 100%;

    .el-tree-select {
      flex: 1;
    }

    .add-category-btn {
      flex-shrink: 0;
      white-space: nowrap;
    }
  }

  .edit-form {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .meta-row {
    display: flex;
    gap: 16px;

    .meta-item {
      flex: 1;
      :deep(.el-form-item__content) {
        width: 100%;
      }
    }
  }

  .slug-hint {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-top: 4px;
    line-height: 1.4;
  }

  .editor-form-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    :deep(.el-form-item__content) {
      flex: 1;
      overflow: hidden;
    }
  }

  .editor-container {
    width: 100%;
    border: 1px solid var(--el-border-color);
    border-radius: 4px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    height: 100%;
  }

  .editor-toolbar {
    padding: 8px;
    background: var(--el-fill-color-light);
    border-bottom: 1px solid var(--el-border-color);
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-shrink: 0;

    .fullscreen-btn {
      margin-left: auto;
    }
  }

  .editor-main {
    display: flex;
    flex: 1;
    min-height: 0; // flexbox min-height 重置
  }

  .editor-textarea {
    flex: 1;
    
    :deep(.el-textarea__inner) {
      height: 100%;
      border: none;
      border-radius: 0;
      resize: none;
      font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
      font-size: 14px;
      line-height: 1.6;
    }
  }

  .editor-preview {
    flex: 1;
    padding: 16px;
    border-left: 1px solid var(--el-border-color);
    overflow-y: auto;
    @include markdown-content;
    @include code-theme-dark;
  }

  html.light .editor-preview {
    @include code-theme-light;
  }

  .sync-platforms {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 0 8px;
    border-right: 1px solid var(--el-border-color);
    border-left: 1px solid var(--el-border-color);

    .sync-label {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      white-space: nowrap;
    }
  }

  // 全屏模式：遮盖整个视图，只显示标题栏和编辑器
  &.is-fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 1000;
    background: var(--el-bg-color);
    padding: 20px;
    margin: 0;
    height: 100vh;

    .page-header {
      margin: -20px -20px 16px -20px;
      top: 0;
    }

    .editor-main {
      .editor-textarea {
        flex: 1;
      }
      .editor-preview {
        flex: 1;
      }
    }
  }
}
</style>

<style lang="scss">
// 非 scoped 样式，用于 v-html 渲染的 Markdown 预览内容
@import '@/assets/styles/_markdown.scss';

.editor-preview {
  @include markdown-content;
  @include code-theme-dark;
}

html.light .editor-preview {
  @include code-theme-light;
}
</style>
