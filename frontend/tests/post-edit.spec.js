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

  test('分类树形选择器存在', async ({ page }) => {
    // 导航到文章编辑页面
    await page.goto('/admin/posts/write')
    
    // 等待页面加载
    await page.waitForTimeout(2000)
    
    // 验证分类树形选择器存在（el-tree-select 或 el-select）
    const treeSelect = page.locator('.el-tree-select').first()
    const fallbackSelect = page.locator('.el-select').first()
    const hasTreeSelect = await treeSelect.isVisible().catch(() => false)
    const hasSelect = await fallbackSelect.isVisible().catch(() => false)
    expect(hasTreeSelect || hasSelect).toBeTruthy()
  })

  test('分类选择器旁边应有管理分类按钮', async ({ page }) => {
    // 导航到文章编辑页面
    await page.goto('/admin/posts/write')
    
    // 等待页面加载
    await page.waitForTimeout(2000)
    
    // 验证管理分类按钮存在
    const manageBtn = page.locator('button:has-text("管理分类")').first()
    const hasManageBtn = await manageBtn.isVisible().catch(() => false)
    expect(hasManageBtn).toBeTruthy()
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
