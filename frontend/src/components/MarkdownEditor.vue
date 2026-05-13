<template>
  <div class="markdown-editor-wrapper">
    <!-- 编辑模式或双栏模式 -->
    <div v-if="mode === 'edit' || mode === 'split'" class="editor-container">
      <div class="editor-toolbar">
        <el-button-group>
          <el-button size="small" @click="insertMarkdown('**', '**')"><b>B</b></el-button>
          <el-button size="small" @click="insertMarkdown('*', '*')"><i>I</i></el-button>
          <el-button size="small" @click="insertMarkdown('# ', '')">H1</el-button>
          <el-button size="small" @click="insertMarkdown('## ', '')">H2</el-button>
          <el-button size="small" @click="insertMarkdown('### ', '')">H3</el-button>
          <el-button size="small" @click="insertMarkdown('- ', '')">列表</el-button>
          <el-button size="small" @click="insertMarkdown('```\n', '\n```')">代码块</el-button>
          <el-button size="small" @click="insertMarkdown('[', '](url)')">链接</el-button>
          <el-button size="small" @click="insertMarkdown('![alt](', ')')">图片</el-button>
          <el-button size="small" @click="insertMarkdown('> ', '')">引用</el-button>
        </el-button-group>
      </div>
      <div class="editor-main" :class="{ 'split-mode': mode === 'split' }">
        <el-input
          ref="textareaRef"
          :model-value="modelValue"
          type="textarea"
          :rows="20"
          placeholder="请输入内容（支持Markdown格式）"
          class="editor-textarea"
          @update:model-value="handleChange"
        />
        <div v-if="mode === 'split'" class="editor-preview markdown-content" v-html="renderedContent"></div>
      </div>
    </div>
    
    <!-- 预览模式 -->
    <div v-else-if="mode === 'preview'" class="preview-container markdown-content" v-html="renderedContent"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useAppStore } from '@/stores/app'
import MarkdownIt from 'markdown-it'
import anchor from 'markdown-it-anchor'
import { createHighlightWithWrapper, setupInlineCodeCopy } from '@/utils/highlight'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  mode: {
    type: String,
    default: 'edit', // 'edit' | 'preview' | 'split'
    validator: (value) => ['edit', 'preview', 'split'].includes(value)
  }
})

const emit = defineEmits(['update:modelValue'])

const appStore = useAppStore()
const textareaRef = ref(null)

// 动态主题映射
const themeMap = {
  dark: 'atom-one-dark',
  light: 'atom-one-light'
}

// 动态加载 highlight.js 主题
function loadHighlightTheme(theme) {
  const themeName = themeMap[theme] || 'atom-one-dark'
  document.querySelectorAll('link[data-highlight-theme]').forEach(el => el.remove())
  
  const link = document.createElement('link')
  link.rel = 'stylesheet'
  link.href = `https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.11.1/styles/${themeName}.min.css`
  link.setAttribute('data-highlight-theme', 'true')
  document.head.appendChild(link)
}

// 配置 Markdown 渲染器（与文章详情页保持一致）
const md = new MarkdownIt({
  highlight: createHighlightWithWrapper()
}).use(anchor, {
  slugify: (str) => str.trim().toLowerCase().replace(/\s+/g, '-').replace(/[^\w\u4e00-\u9fa5-]/g, ''),
  permalink: false
})

// 配置行内代码复制按钮
setupInlineCodeCopy(md)

// 渲染Markdown内容
const renderedContent = computed(() => {
  if (!props.modelValue) return ''
  return md.render(props.modelValue)
})

function handleChange(value) {
  emit('update:modelValue', value)
}

function insertMarkdown(prefix, suffix) {
  const textarea = textareaRef.value?.$el?.querySelector('textarea')
  if (!textarea) return
  
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = props.modelValue.substring(start, end)
  const newText = prefix + selectedText + suffix
  
  const newValue = props.modelValue.substring(0, start) + newText + props.modelValue.substring(end)
  emit('update:modelValue', newValue)
  
  // 重新设置光标位置
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + prefix.length, start + prefix.length + selectedText.length)
  }, 0)
}

// 组件挂载时加载主题
onMounted(() => {
  loadHighlightTheme(appStore.theme)
})

// 监听主题变化
watch(() => appStore.theme, (newTheme) => {
  loadHighlightTheme(newTheme)
})
</script>

<style lang="scss" scoped>
.markdown-editor-wrapper {
  width: 100%;
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
  
  &.split-mode {
    height: 600px;
  }
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
}

.preview-container {
  min-height: 200px;
  padding: 20px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  background: var(--el-bg-color);
}
</style>

<style lang="scss">
// 非 scoped 样式，用于 v-html 渲染的 Markdown 内容
@import '@/assets/styles/_markdown.scss';

.markdown-content {
  @include markdown-content;
  @include code-theme-dark;
}

html.light .markdown-content {
  @include code-theme-light;
}
</style>
