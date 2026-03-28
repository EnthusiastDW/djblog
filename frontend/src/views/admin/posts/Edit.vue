<template>
  <div class="admin-post-edit">
    <div class="page-header">
      <el-button @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2 class="page-title">{{ isEdit ? '编辑文章' : '写文章' }}</h2>
      <div class="header-actions">
        <el-button @click="handleSaveDraft" :loading="saving">保存草稿</el-button>
        <el-button type="primary" @click="handlePublish" :loading="publishing">发布</el-button>
      </div>
    </div>

    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入文章标题" />
      </el-form-item>
      
      <el-form-item label="摘要" prop="summary">
        <div class="summary-input">
          <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="请输入文章摘要" />
          <el-button type="primary" link :loading="summaryLoading" @click="handleGenerateSummary">
            <el-icon><MagicStick /></el-icon>
            AI生成
          </el-button>
        </div>
      </el-form-item>

      <!-- 分类选择 -->
      <el-form-item label="分类" prop="categoryId">
        <el-select
          v-model="form.categoryId"
          placeholder="请选择分类或输入新分类"
          filterable
          allow-create
          default-first-option
          :loading="categoryLoading"
          style="width: 100%;"
          @change="handleCategoryChange"
        >
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>
      </el-form-item>

      <!-- 标签选择 -->
      <el-form-item label="标签" prop="tagIds">
        <el-select
          v-model="form.tagIds"
          multiple
          placeholder="请选择标签或输入新标签"
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

      <el-form-item label="封面">
        <el-input v-model="form.coverImage" placeholder="请输入封面图片链接" />
      </el-form-item>

      <el-form-item label="内容" prop="content">
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
          </div>
          <div class="editor-main">
            <el-input
              ref="editorRef"
              v-model="form.content"
              type="textarea"
              :rows="20"
              placeholder="请输入文章内容（支持Markdown格式）"
              class="editor-textarea"
            />
            <div class="editor-preview" v-html="renderedContent"></div>
          </div>
        </div>
      </el-form-item>
    </el-form>


  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { postApi } from '@/api/post'
import { categoryApi } from '@/api/category'
import { tagApi } from '@/api/tag'
import { ArrowLeft, MagicStick } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

const route = useRoute()
const router = useRouter()

const formRef = ref(null)
const editorRef = ref(null)
const saving = ref(false)
const publishing = ref(false)
const categories = ref([])
const tags = ref([])
const categoryLoading = ref(false)
const tagLoading = ref(false)
const summaryLoading = ref(false)

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  id: null,
  title: '',
  summary: '',
  content: '',
  categoryId: null,
  tagIds: [],
  coverImage: ''
})

const rules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }]
}

const md = new MarkdownIt({
  highlight: (str, lang) => {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(str, { language: lang }).value
      } catch (e) {
        console.error(e)
      }
    }
    return ''
  }
})

const renderedContent = computed(() => {
  if (!form.content) return ''
  return md.render(form.content)
})

async function fetchCategories() {
  try {
    const res = await categoryApi.getList({ size: 100 })
    categories.value = res.data.records || []
  } catch (e) {
    console.error('获取分类失败', e)
  }
}

async function fetchTags() {
  try {
    const res = await tagApi.getList({ size: 100 })
    tags.value = res.data.records || []
  } catch (e) {
    console.error('获取标签失败', e)
  }
}



async function handleCategoryChange(value) {
  // 检查是否是新创建的分类（字符串）
  if (typeof value === 'string') {
    const categoryName = value.trim()
    if (!categoryName) return

    categoryLoading.value = true
    try {
      // 检查是否已存在相同名称的分类
      const existingCategory = categories.value.find(c => c.name === categoryName)
      if (existingCategory) {
        form.categoryId = existingCategory.id
        ElMessage.info('该分类已存在')
        return
      }

      // 创建新分类
      const res = await categoryApi.create({ name: categoryName })
      const newId = res.data
      categories.value.push({ id: newId, name: categoryName })
      form.categoryId = newId
      ElMessage.success('分类创建成功')
    } catch (e) {
      console.error('创建分类失败', e)
      ElMessage.error(e.response?.data?.message || '创建分类失败')
      form.categoryId = null
    } finally {
      categoryLoading.value = false
    }
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

async function handleGenerateSummary() {
  if (!form.content) {
    ElMessage.warning('请先输入文章内容')
    return
  }
  summaryLoading.value = true
  try {
    const res = await postApi.generateSummary(form.title, form.content, 200)
    form.summary = res.data
    ElMessage.success('摘要生成成功')
  } catch (e) {
    console.error('生成摘要失败', e)
    ElMessage.error('生成摘要失败')
  } finally {
    summaryLoading.value = false
  }
}

async function fetchPost() {
  if (!route.params.id) return
  
  try {
    const res = await postApi.getDetailWithContent(route.params.id)
    const post = res.data
    form.id = post.id
    form.title = post.title
    form.summary = post.summary || ''
    form.content = post.content || ''
    form.categoryId = post.categoryId
    form.tagIds = post.tags?.map(t => t.id) || []
    form.coverImage = post.coverImage || ''
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
    await postApi.saveDraft(form)
    ElMessage.success('保存成功')
    router.push('/admin/posts')
  } catch (e) {
    console.error('保存失败', e)
  } finally {
    saving.value = false
  }
}

async function handlePublish() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  publishing.value = true
  try {
    // 无论新建还是编辑，都使用 publish 方法确保状态为已发布
    await postApi.publish(form)
    ElMessage.success('发布成功')
    router.push('/admin/posts')
  } catch (e) {
    console.error('发布失败', e)
  } finally {
    publishing.value = false
  }
}

onMounted(() => {
  fetchCategories()
  fetchTags()
  if (isEdit.value) {
    fetchPost()
  }
})
</script>

<style lang="scss" scoped>
.admin-post-edit {
  .page-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 20px;
  }

  .page-title {
    flex: 1;
    font-size: 20px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .header-actions {
    display: flex;
    gap: 8px;
  }

  .editor-container {
    width: 100%;
    border: 1px solid var(--el-border-color);
    border-radius: 4px;
    overflow: hidden;
  }

  .editor-toolbar {
    padding: 8px;
    background: var(--el-fill-color-light);
    border-bottom: 1px solid var(--el-border-color);
  }

  .editor-main {
    display: flex;
    height: 500px;
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
    line-height: 1.8;

    :deep(h1), :deep(h2), :deep(h3), :deep(h4) {
      margin: 16px 0 8px;
      font-weight: 600;
    }

    :deep(pre) {
      background: #1e1e1e;
      padding: 12px;
      border-radius: 4px;
      overflow-x: auto;

      code {
        color: #d4d4d4;
        font-size: 13px;
      }
    }

    :deep(code) {
      background: var(--el-fill-color-light);
      padding: 2px 6px;
      border-radius: 4px;
      font-size: 13px;
    }

    :deep(blockquote) {
      border-left: 3px solid var(--el-color-primary);
      padding-left: 12px;
      margin: 8px 0;
      color: var(--el-text-color-secondary);
    }
  }
}
</style>
