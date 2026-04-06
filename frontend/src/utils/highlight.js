import hljs from 'highlight.js'

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

export function createHighlightWithWrapper() {
  return (str, lang) => {
    const language = lang || 'plaintext'
    const highlighted = highlightCode(str, lang)
    
    return `<div class="code-block-wrapper"><div class="code-block-header"><span class="code-block-language">${language}</span><button class="copy-btn" type="button" title="Copy code" onclick="copyCodeToClipboard(this)"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg></button></div><pre><code class="hljs language-${language}">${highlighted}</code></pre></div>`
  }
}

export function createHighlightSimple() {
  return (str, lang) => {
    const language = lang || 'plaintext'
    const highlighted = highlightCode(str, lang)
    
    return `<pre><code class="hljs language-${language}">${highlighted}</code></pre>`
  }
}

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
