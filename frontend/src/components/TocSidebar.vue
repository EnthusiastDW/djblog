<template>
  <!-- 使用 Teleport 将按钮传送到 body，确保不受父元素影响 -->
  <Teleport to="body">
    <button 
      class="toc-toggle-btn" 
      @click="toggleToc" 
      v-show="isMobile && titles.length > 0"
      :title="showToc ? '关闭目录' : '打开目录'"
    >
      <el-icon><List /></el-icon>
    </button>
  </Teleport>

  <div class="toc-container">
    <!-- 桌面端目录侧边栏 -->
    <aside 
      class="toc-sidebar" 
      v-show="!isMobile"
    >
      <div class="toc-sidebar-inner">
        <div class="toc-header">
          <div class="toc-title">目录</div>
        </div>
        <nav class="toc-nav">
          <ul class="toc-list">
            <li 
              v-for="title in titles" 
              :key="title.id"
              :class="['toc-item', `level-${title.level}`]"
              :style="{ paddingLeft: (title.level - 1) * 16 + 'px' }"
            >
              <a 
                :href="`#${title.id}`"
                :class="{ active: activeId === title.id }"
                @click="handleClick($event, title.id)"
              >
                {{ title.text }}
              </a>
            </li>
          </ul>
        </nav>
      </div>
    </aside>
  </div>

  <!-- 使用 Teleport 将移动端目录面板传送到 body -->
  <Teleport to="body">
    <!-- 遮罩层（移动端） -->
    <div class="toc-overlay" :class="{ 'is-visible': showToc && isMobile }" @click="showToc = false"></div>
    
    <aside 
      v-if="isMobile"
      class="toc-sidebar toc-sidebar-mobile" 
      :class="{ 'is-expanded': showToc }"
    >
      <div class="toc-sidebar-inner">
        <div class="toc-header">
          <div class="toc-title">目录</div>
          <button class="toc-close-btn" @click="showToc = false">
            <el-icon><Close /></el-icon>
          </button>
        </div>
        <nav class="toc-nav">
          <ul class="toc-list">
            <li 
              v-for="title in titles" 
              :key="title.id"
              :class="['toc-item', `level-${title.level}`]"
              :style="{ paddingLeft: (title.level - 1) * 16 + 'px' }"
            >
              <a 
                :href="`#${title.id}`"
                :class="{ active: activeId === title.id }"
                @click="handleClick($event, title.id)"
              >
                {{ title.text }}
              </a>
            </li>
          </ul>
        </nav>
      </div>
    </aside>
  </Teleport>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { Menu, Close, List } from '@element-plus/icons-vue'

const props = defineProps({
  content: {
    type: String,
    default: ''
  }
})

const titles = ref([])
const activeId = ref(null)
const showToc = ref(true) // 桌面端默认展开
const isMobile = ref(false)
const isScrolled = ref(false)

// 解析 HTML 内容中的标题
function parseTitles() {
  const parser = new DOMParser()
  const doc = parser.parseFromString(props.content || '', 'text/html')
  const headings = doc.querySelectorAll('h1, h2, h3, h4, h5, h6')
  
  titles.value = Array.from(headings).map((heading, index) => {
    const level = parseInt(heading.tagName.charAt(1))
    const text = heading.textContent.trim()
    const id = heading.id || `heading-${index}`
    
    return {
      id,
      level,
      text
    }
  }).filter(t => t.text)
}

// 处理点击事件 - 修复锚点跳转
function handleClick(event, id) {
  event.preventDefault()
  
  // 先关闭移动端目录面板
  if (isMobile.value) {
    showToc.value = false
  }
  
  // 等待 DOM 更新后滚动到目标位置
  nextTick(() => {
    const element = document.getElementById(id)
    if (element) {
      const offset = 80 // 考虑固定 header 的高度
      const top = element.getBoundingClientRect().top + window.scrollY - offset
      window.scrollTo({ top, behavior: 'smooth' })
      activeId.value = id
      
      // 更新 URL hash 但不触发滚动
      history.pushState(null, '', `#${id}`)
    }
  })
}

// 监听滚动，高亮当前标题
function handleScroll() {
  if (titles.value.length === 0) return
  
  const scrollPosition = window.scrollY + 100
  
  // 找到第一个在滚动位置之前的标题
  for (let i = 0; i < titles.value.length; i++) {
    const element = document.getElementById(titles.value[i].id)
    if (element) {
      const elementTop = element.offsetTop
      if (scrollPosition >= elementTop) {
        activeId.value = titles.value[i].id
        return
      }
    }
  }
  
  // 如果滚动位置在第一个标题之前，选中第一个
  if (titles.value.length > 0) {
    activeId.value = titles.value[0].id
  }
  
  // 移动端滚动时显示切换按钮
  if (isMobile.value) {
    isScrolled.value = window.scrollY > 100
  }
}

onMounted(() => {
  parseTitles()
  
  // 立即检查移动端状态
  checkMobile()
  showToc.value = !isMobile.value
  
  // 延迟确保 DOM 已渲染
  setTimeout(() => {
    handleScroll()
  }, 100)
  window.addEventListener('scroll', handleScroll, { passive: true })
  window.addEventListener('resize', handleResize)
  
  // 监听 hash 变化
  window.addEventListener('hashchange', handleHashChange)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('hashchange', handleHashChange)
})

// 处理页面加载时的 hash
function handleHashChange() {
  const hash = window.location.hash.slice(1)
  if (hash) {
    const element = document.getElementById(hash)
    if (element) {
      activeId.value = hash
    }
  }
}

// 监听内容变化
watch(() => props.content, () => {
  parseTitles()
  nextTick(() => {
    handleScroll()
  })
}, { immediate: true })

// 检查是否为移动设备
function checkMobile() {
  const width = window.innerWidth
  isMobile.value = width <= 1200
}

// 切换设备类型时调整显示状态
function handleResize() {
  const wasMobile = isMobile.value
  checkMobile()
  // 切换设备类型时调整显示状态
  if (wasMobile !== isMobile.value) {
    showToc.value = !isMobile.value
  }
}

// 切换目录显示
function toggleToc() {
  showToc.value = !showToc.value
}
</script>

<style lang="scss" scoped>
.toc-container {
  position: relative;
}

.toc-sidebar {
  width: 240px;
  flex-shrink: 0;
  
  // 桌面端使用 sticky 定位
  position: sticky;
  top: 84px;
  max-height: calc(100vh - 100px);
  
  // 桌面端显示内容
  .toc-sidebar-inner {
    display: block;
    background: var(--el-bg-color);
    border-radius: 8px;
    padding: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    overflow-y: auto;
    transition: all 0.3s;
  }
}

// 移动端目录面板 - 使用 Teleport 传送到 body，完全独立
.toc-sidebar-mobile {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: 85%;
  max-width: 320px;
  z-index: 10002;
  max-height: 100vh;
  transform: translateX(100%);
  transition: transform 0.3s ease;
  display: none;
  
  &.is-expanded {
    transform: translateX(0);
    display: block;
    
    .toc-sidebar-inner {
      height: 100%;
      border-radius: 0;
      box-shadow: -2px 0 8px rgba(0, 0, 0, 0.15);
    }
  }

  .toc-sidebar-inner {
    display: block;
    background: var(--el-bg-color);
    padding: 16px;
  }
}

.toc-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 10001;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s;
  
  &.is-visible {
    opacity: 1;
    pointer-events: auto;
  }
}

.toc-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.toc-close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  padding: 0;
  background: transparent;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  color: var(--el-text-color-secondary);
  transition: all 0.2s;

  &:hover {
    background: var(--el-fill-color);
    color: var(--el-text-color-primary);
  }

  .el-icon {
    font-size: 18px;
  }
}

.toc-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0;
}

.toc-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.toc-item {
  margin-bottom: 8px;
  
  a {
    display: block;
    color: var(--el-text-color-regular);
    text-decoration: none;
    font-size: 14px;
    line-height: 1.6;
    padding: 4px 8px;
    border-radius: 4px;
    transition: all 0.3s;
    
    &:hover {
      color: var(--el-color-primary);
      background: var(--el-fill-color-light);
    }
    
    &.active {
      color: var(--el-color-primary);
      background: var(--el-color-primary-light-9);
      font-weight: 500;
    }
  }
}

.level-1 {
  a {
    font-size: 15px;
    font-weight: 500;
  }
}

.level-3, .level-4, .level-5, .level-6 {
  a {
    font-size: 13px;
  }
}

// 移动端按钮样式
.toc-toggle-btn {
  display: none;
  position: fixed;
  right: 16px;
  top: 100px;
  z-index: 10000;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  padding: 0;
  background: var(--el-color-primary);
  color: #fff;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  transition: all 0.3s;

  &:hover {
    background: var(--el-color-primary-light-3);
    transform: scale(1.05);
  }

  &:active {
    transform: scale(0.95);
  }

  .el-icon {
    font-size: 22px;
  }
}

// 移动端显示按钮
@media (max-width: 1200px) {
  .toc-toggle-btn {
    display: flex;
  }
}
</style>
