import hljs from 'highlight.js'

/**
 * 代码语法高亮
 */
export function highlightCode(str, lang) {
  const language = lang || 'plaintext'
  const trimmedStr = str.trim()
  
  let highlighted
  try {
    if (lang && hljs.getLanguage(lang)) {
      highlighted = hljs.highlight(trimmedStr, { language: lang }).value
    } else {
      highlighted = hljs.highlightAuto(trimmedStr).value
    }
  } catch (e) {
    console.warn(`Highlight error for language "${lang}":`, e)
    highlighted = trimmedStr.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  }
  
  return highlighted
}

/**
 * 创建带复制按钮的代码高亮包装器
 */
export function createHighlightWithWrapper() {
  return (str, lang) => {
    const language = lang || 'plaintext'
    const highlighted = highlightCode(str, lang)
    
    return `<div class="code-block-wrapper"><div class="code-block-header"><span class="code-block-language">${language}</span><button class="copy-btn" type="button" title="Copy code" onclick="copyCodeToClipboard(this)"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg></button></div><pre><code class="hljs language-${language}">${highlighted}</code></pre></div>`
  }
}

/**
 * 创建简单的代码高亮（不带复制按钮）
 */
export function createHighlightSimple() {
  return (str, lang) => {
    const language = lang || 'plaintext'
    const highlighted = highlightCode(str, lang)
    
    return `<pre><code class="hljs language-${language}">${highlighted}</code></pre>`
  }
}

/**
 * 高亮文本中的关键词
 * @param text 原始文本
 * @param keyword 要高亮的关键词
 * @returns 包含高亮标签的HTML字符串
 */
export function highlightKeyword(text, keyword) {
  if (!text || !keyword) {
    return text || ''
  }
  
  // 转义特殊字符
  const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  
  // 使用正则表达式进行不区分大小写的替换
  const regex = new RegExp(`(${escapedKeyword})`, 'gi')
  
  return text.replace(regex, '<mark class="highlight">$1</mark>')
}

/**
 * 将HTML字符串安全地渲染到DOM
 * @param html HTML字符串
 * @returns 安全的HTML对象（用于Vue的v-html）
 */
export function safeHtml(html) {
  return html
}

// 全局复制代码函数
if (typeof window !== 'undefined') {
  window.copyCodeToClipboard = function(button) {
    const wrapper = button.closest('.code-block-wrapper')
    const code = wrapper.querySelector('code').textContent
    
    navigator.clipboard.writeText(code).then(() => {
      button.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>'
      button.title = 'Copied!'
      
      setTimeout(() => {
        button.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg>'
        button.title = 'Copy code'
      }, 2000)
    }).catch(() => {
      console.error('Failed to copy code')
    })
  }
}
