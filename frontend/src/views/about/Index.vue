<template>
  <div class="about-page" v-loading="loading">
    <div class="about-container">
      <h1 class="page-title">关于我</h1>
      
      <div v-if="aboutContent" class="about-content markdown-content" v-html="renderedContent"></div>
      
      <el-empty v-else description="暂无内容" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useAppStore } from '@/stores/app'
import MarkdownIt from 'markdown-it'
import anchor from 'markdown-it-anchor'
import { createHighlightWithWrapper, setupInlineCodeCopy } from '@/utils/highlight'
import { userApi } from '@/api/user'

const appStore = useAppStore()
const aboutContent = ref('')
const loading = ref(false)

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

const renderedContent = computed(() => {
  if (!aboutContent.value) return ''
  return md.render(aboutContent.value)
})

async function fetchAboutContent() {
  loading.value = true
  try {
    const res = await userApi.getAbout()
    aboutContent.value = res.data || ''
  } catch (e) {
    console.error('获取关于内容失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchAboutContent()
  loadHighlightTheme(appStore.theme)
})

// 监听主题变化
watch(() => appStore.theme, (newTheme) => {
  loadHighlightTheme(newTheme)
})
</script>

<style lang="scss" scoped>
.about-page {
  max-width: 900px;
  margin: 0 auto;
}

.about-container {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 40px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  margin-bottom: 32px;
  text-align: center;
}

.about-content {
  min-height: 200px;
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
