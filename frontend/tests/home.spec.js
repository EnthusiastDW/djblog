import { test, expect } from '@playwright/test'

test.describe('首页测试', () => {
  test('首页应该正常加载', async ({ page }) => {
    await page.goto('/')
    
    // 检查页面标题
    await expect(page).toHaveTitle(/DJ Blog/)
    
    // 检查导航栏存在
    await expect(page.locator('.app-header')).toBeVisible()
  })

  test('导航栏功能测试', async ({ page }) => {
    await page.goto('/')
    
    // 点击归档链接
    await page.click('text=归档')
    await expect(page).toHaveURL(/.*archives/)
    
    // 点击分类链接
    await page.click('text=分类')
    await expect(page).toHaveURL(/.*categories/)
    
    // 点击标签链接
    await page.click('text=标签')
    await expect(page).toHaveURL(/.*tags/)
  })

  test('搜索功能测试', async ({ page }) => {
    await page.goto('/')
    
    // 输入搜索关键词
    await page.fill('.search-box input', '测试文章')
    await page.press('.search-box input', 'Enter')
    
    // 验证跳转到搜索页面
    await expect(page).toHaveURL(/.*search\?keyword=/)
  })
})

test.describe('登录注册测试', () => {
  test('登录页面应该正常显示', async ({ page }) => {
    await page.goto('/login')
    
    // 检查登录表单元素
    await expect(page.locator('input[placeholder="用户名"]')).toBeVisible()
    await expect(page.locator('input[placeholder="密码"]')).toBeVisible()
    await expect(page.locator('button:has-text("登录")')).toBeVisible()
  })

  test('注册页面应该正常显示', async ({ page }) => {
    await page.goto('/register')
    
    await expect(page.locator('input[placeholder="用户名"]')).toBeVisible()
    await expect(page.locator('input[placeholder="邮箱"]')).toBeVisible()
    await expect(page.locator('input[placeholder="密码"]')).toBeVisible()
    await expect(page.locator('button:has-text("注册")')).toBeVisible()
  })

  test('登录表单验证', async ({ page }) => {
    await page.goto('/login')
    
    // 点击登录按钮，不填写表单
    await page.click('button:has-text("登录")')
    
    // 应该显示验证错误 - 使用 first() 选择第一个错误
    await expect(page.locator('.el-form-item__error').first()).toBeVisible()
  })
})

test.describe('后台管理测试', () => {
  test('未登录访问后台应重定向到登录页', async ({ page }) => {
    await page.goto('/admin')
    
    // 应该重定向到登录页面
    await expect(page).toHaveURL(/.*login/)
  })
})

test.describe('分类页面测试', () => {
  test('分类页面应该正常加载', async ({ page }) => {
    await page.goto('/categories')
    
    // 检查页面标题
    await expect(page.locator('h1:has-text("分类")')).toBeVisible()
  })
})

test.describe('标签页面测试', () => {
  test('标签页面应该正常加载', async ({ page }) => {
    await page.goto('/tags')
    
    // 检查页面标题
    await expect(page.locator('h1:has-text("标签")')).toBeVisible()
  })
})

test.describe('归档页面测试', () => {
  test('归档页面应该正常加载', async ({ page }) => {
    await page.goto('/archives')
    
    // 检查页面标题
    await expect(page.locator('h1:has-text("文章归档")')).toBeVisible()
  })
})

test.describe('主题切换测试', () => {
  test('点击主题切换按钮', async ({ page }) => {
    await page.goto('/')
    
    // 获取初始主题
    const htmlElement = page.locator('html')
    const initialTheme = await htmlElement.getAttribute('class')
    
    // 点击主题切换按钮
    await page.click('.theme-btn')
    
    // 验证主题已切换
    const newTheme = await htmlElement.getAttribute('class')
    expect(newTheme).not.toBe(initialTheme)
  })
})
