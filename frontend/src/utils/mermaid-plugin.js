import mermaid from 'mermaid'

let currentTheme = null

/**
 * 根据当前主题初始化/更新 Mermaid 配置
 * @param {'light'|'dark'} theme
 */
export function initializeMermaid(theme = 'light') {
  if (currentTheme === theme) return
  currentTheme = theme

  mermaid.initialize({
    startOnLoad: false,
    theme: theme === 'dark' ? 'dark' : 'default',
    securityLevel: 'loose',
  })
}

/**
 * 渲染页面中所有未处理的 .mermaid 元素
 */
export async function renderMermaid() {
  const elements = document.querySelectorAll('.mermaid')
  if (elements.length === 0) return

  // 移除 data-processed 标记，确保 mermaid 重新渲染
  elements.forEach(el => el.removeAttribute('data-processed'))

  try {
    await mermaid.run({ querySelector: '.mermaid' })
  } catch (e) {
    console.warn('Mermaid 渲染失败:', e)
  }
}

/**
 * markdown-it 插件：支持 Mermaid 流程图
 *
 * 将 ```mermaid 代码块渲染为 <div class="mermaid">，
 * 由前端在 DOM 更新后调用 renderMermaid() 渲染为 SVG。
 *
 * @param {import('markdown-it')} md
 */
export function mermaidPlugin(md) {
  const defaultFence = md.renderer.rules.fence

  md.renderer.rules.fence = (tokens, idx, options, env, self) => {
    const token = tokens[idx]
    const info = token.info ? token.info.trim() : ''
    const lang = info.split(/\s+/)[0]

    if (lang === 'mermaid') {
      return `<div class="mermaid">${token.content}</div>`
    }

    return defaultFence
      ? defaultFence(tokens, idx, options, env, self)
      : self.renderToken(tokens, idx, options)
  }
}
