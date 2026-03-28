import { test, expect } from '@playwright/test'

test.describe('文章编辑页面测试', () => {
  test.beforeEach(async ({ page }) => {
    // 每个测试前清除本地存储并模拟登录状态
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.clear()
      localStorage.setItem('token', 'fake-token')
      localStorage.setItem('user', JSON.stringify({
        id: 1,
        username: 'testuser',
        nickname: '测试用户',
        role: 'USER'
      }))
    })
  })

  test('文章编辑页面基本结构', async ({ page }) => {
    // 导航到文章编辑页面
    await page.goto('/admin/posts/write')
    
    // 等待页面加载
    await page.waitForTimeout(2000)
    
    // 验证页面标题
    await expect(page).toHaveTitle(/写文章/)
    
    // 验证基本表单元素存在
    await expect(page.locator('input').first()).toBeVisible()
    await expect(page.locator('textarea').first()).toBeVisible()
    await expect(page.locator('button').first()).toBeVisible()
  })

  test('分类和标签选择器存在', async ({ page }) => {
    // 导航到文章编辑页面
    await page.goto('/admin/posts/write')
    
    // 等待页面加载
    await page.waitForTimeout(2000)
    
    // 验证分类和标签选择器存在
    await expect(page.locator('div.el-select').first()).toBeVisible()
  })

  test('表单提交按钮存在', async ({ page }) => {
    // 导航到文章编辑页面
    await page.goto('/admin/posts/write')
    
    // 等待页面加载
    await page.waitForTimeout(2000)
    
    // 验证保存草稿和发布按钮存在
    await expect(page.locator('button:has-text("保存草稿")')).toBeVisible()
    await expect(page.locator('button:has-text("发布")')).toBeVisible()
  })

  test('Markdown编辑器存在', async ({ page }) => {
    // 导航到文章编辑页面
    await page.goto('/admin/posts/write')
    
    // 等待页面加载
    await page.waitForTimeout(2000)
    
    // 验证编辑器容器存在
    await expect(page.locator('.editor-container')).toBeVisible()
  })


})
