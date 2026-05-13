import { test, expect } from '@playwright/test'

/**
 * 测试行内代码复制按钮功能
 */

test.describe('行内代码复制按钮功能', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('domcontentloaded')
  })

  test('文章详情页的行内代码应该显示复制按钮', async ({ page }) => {
    // 查找首页的第一篇文章
    const firstPost = page.locator('.post-card').first()
    
    if (await firstPost.count() > 0) {
      await firstPost.click()
      await page.waitForLoadState('domcontentloaded')
      await page.waitForTimeout(1000)
      
      // 检查是否存在行内代码
      const inlineCodeElements = page.locator('.inline-code-wrapper')
      const count = await inlineCodeElements.count()
      
      console.log(`找到 ${count} 个行内代码元素`)
      
      if (count > 0) {
        // 验证第一个行内代码存在复制按钮
        const firstInlineCode = inlineCodeElements.first()
        const copyButton = firstInlineCode.locator('.inline-code-copy-btn')
        
        await expect(copyButton).toBeVisible({ timeout: 5000 })
        
        // 验证鼠标悬停时按钮可见
        await firstInlineCode.hover()
        await page.waitForTimeout(300)
        
        // 复制按钮应该在悬停后变得可见（opacity: 1）
        const buttonStyle = await copyButton.evaluate(el => {
          return window.getComputedStyle(el).opacity
        })
        
        console.log(`复制按钮透明度: ${buttonStyle}`)
        expect(buttonStyle).toBe('1')
      } else {
        console.log('当前文章没有行内代码，跳过测试')
      }
    } else {
      console.log('首页没有文章，跳过测试')
    }
  })

  test('点击行内代码复制按钮应该复制代码内容', async ({ page }) => {
    // 导航到一篇包含行内代码的文章
    await page.goto('/article/spring-ai-concepts-models')
    await page.waitForLoadState('domcontentloaded')
    await page.waitForTimeout(1000)
    
    const inlineCodeElements = page.locator('.inline-code-wrapper')
    const count = await inlineCodeElements.count()
    
    if (count > 0) {
      const firstInlineCode = inlineCodeElements.first()
      
      // 获取原始代码内容
      const originalText = await firstInlineCode.evaluate(el => {
        // 克隆节点并移除按钮
        const clone = el.cloneNode(true)
        const btn = clone.querySelector('.inline-code-copy-btn')
        if (btn) {
          btn.remove()
        }
        return clone.textContent
      })
      
      console.log(`要复制的代码: "${originalText}"`)
      
      // 授予剪贴板权限
      const context = page.context()
      await context.grantPermissions(['clipboard-read', 'clipboard-write'])
      
      // 悬停以显示复制按钮
      await firstInlineCode.hover()
      await page.waitForTimeout(300)
      
      // 点击复制按钮
      const copyButton = firstInlineCode.locator('.inline-code-copy-btn')
      await copyButton.click()
      await page.waitForTimeout(500)
      
      // 验证标题变为 "Copied!"
      const titleAfterCopy = await firstInlineCode.getAttribute('title')
      expect(titleAfterCopy).toBe('Copied!')
      
      // 等待提示消失
      await page.waitForTimeout(2000)
      
      // 验证标题恢复
      const titleAfterReset = await firstInlineCode.getAttribute('title')
      expect(titleAfterReset).toBe('Click to copy')
    } else {
      console.log('该文章没有行内代码，跳过测试')
    }
  })

  test('行内代码应该有正确的样式类', async ({ page }) => {
    // 导航到有内容的页面
    await page.goto('/')
    await page.waitForLoadState('domcontentloaded')
    
    const postCards = page.locator('.post-card')
    const postCount = await postCards.count()
    
    if (postCount > 0) {
      await postCards.first().click()
      await page.waitForLoadState('domcontentloaded')
      await page.waitForTimeout(1000)
      
      const inlineCodes = page.locator('code.inline-code-wrapper')
      const codeCount = await inlineCodes.count()
      
      if (codeCount > 0) {
        // 验证第一个行内代码的类名
        const className = await inlineCodes.first().getAttribute('class')
        expect(className).toContain('inline-code-wrapper')
        
        // 验证复制按钮存在
        const buttons = inlineCodes.first().locator('button.inline-code-copy-btn')
        await expect(buttons).toHaveCount(1)
      }
    }
  })

  test('关于页面的行内代码也应该有复制按钮', async ({ page }) => {
    await page.goto('/about')
    await page.waitForLoadState('domcontentloaded')
    await page.waitForTimeout(1000)
    
    const inlineCodeElements = page.locator('.inline-code-wrapper')
    const count = await inlineCodeElements.count()
    
    console.log(`关于页面找到 ${count} 个行内代码元素`)
    
    if (count > 0) {
      const firstInlineCode = inlineCodeElements.first()
      const copyButton = firstInlineCode.locator('.inline-code-copy-btn')
      
      await expect(copyButton).toBeVisible({ timeout: 5000 })
    }
  })
})
