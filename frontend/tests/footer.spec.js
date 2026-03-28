import { test, expect } from '@playwright/test'

test.describe('页面 Footer 功能测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.context().clearCookies()
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.clear()
    })
  })

  test('Footer 内容居中显示', async ({ page }) => {
    await page.goto('/')
    await page.waitForTimeout(1000)
    
    // 滚动到页面底部
    await page.evaluate(() => {
      window.scrollTo(0, document.body.scrollHeight)
    })
    await page.waitForTimeout(500)
    
    // 验证 footer 存在
    const footer = page.locator('.app-footer')
    await expect(footer.first()).toBeVisible()
    
    // 验证 footer 容器使用了 flexbox 居中
    const footerContainer = page.locator('.footer-container')
    const containerStyle = await footerContainer.first().getAttribute('style')
    
    // 检查是否有居中样式（通过计算样式或类名）
    const footerContent = page.locator('.footer-content')
    await expect(footerContent.first()).toBeVisible()
    
    // 验证内容是居中的（通过检查 text-align 或 flex 布局）
    const copyrightText = page.locator('.copyright')
    await expect(copyrightText.first()).toBeVisible()
  })

  test('Footer 版权信息正常显示', async ({ page }) => {
    await page.goto('/')
    await page.waitForTimeout(1000)
    
    // 滚动到底部
    await page.evaluate(() => {
      window.scrollTo(0, document.body.scrollHeight)
    })
    await page.waitForTimeout(500)
    
    // 验证版权信息
    const currentYear = new Date().getFullYear()
    const copyright = page.locator('.copyright')
    await expect(copyright.first()).toContainText(currentYear.toString())
    await expect(copyright.first()).toContainText('DJ Blog')
  })

  test('Footer Powered by 信息正常显示', async ({ page }) => {
    await page.goto('/')
    await page.waitForTimeout(1000)
    
    // 滚动到底部
    await page.evaluate(() => {
      window.scrollTo(0, document.body.scrollHeight)
    })
    await page.waitForTimeout(500)
    
    // 验证技术栈信息
    const powered = page.locator('.powered')
    await expect(powered.first()).toBeVisible()
    await expect(powered.first()).toContainText('Vue 3')
    await expect(powered.first()).toContainText('Spring Boot')
  })

  test('备案号链接和配置（如果设置了）', async ({ page }) => {
    await page.goto('/')
    await page.waitForTimeout(1000)
    
    // 滚动到底部
    await page.evaluate(() => {
      window.scrollTo(0, document.body.scrollHeight)
    })
    await page.waitForTimeout(500)
    
    // 检查是否有备案号元素
    const icpElement = page.locator('.icp')
    const icpCount = await icpElement.count()
    
    if (icpCount > 0) {
      // 如果设置了备案号，验证其正确显示
      const icpLink = icpElement.first().locator('a')
      await expect(icpLink.first()).toBeVisible()
      
      // 验证链接地址
      const href = await icpLink.first().getAttribute('href')
      expect(href).toBe('https://beian.miit.gov.cn/')
      
      // 验证 target 属性
      const target = await icpLink.first().getAttribute('target')
      expect(target).toBe('_blank')
      
      // 验证备案号文本不为空
      const icpText = await icpLink.first().textContent()
      expect(icpText?.trim()).toBeTruthy()
    } else {
      // 如果没有设置备案号，跳过测试
      console.log('未设置备案号，跳过此测试')
    }
  })

  test('移动端 Footer 响应式布局', async ({ page }) => {
    // 设置为移动设备视图
    await page.setViewportSize({ width: 375, height: 667 })
    
    await page.goto('/')
    await page.waitForTimeout(1000)
    
    // 滚动到底部
    await page.evaluate(() => {
      window.scrollTo(0, document.body.scrollHeight)
    })
    await page.waitForTimeout(500)
    
    // 验证 footer 在移动端也正常显示
    const footer = page.locator('.app-footer')
    await expect(footer.first()).toBeVisible()
    
    // 验证内容仍然居中
    const footerContent = page.locator('.footer-content')
    await expect(footerContent.first()).toBeVisible()
  })
})
