import { test, expect } from '@playwright/test'

test.describe('登录流程测试', () => {
  test.beforeEach(async ({ page }) => {
    // 每个测试前清除本地存储
    await page.goto('/')
    await page.evaluate(() => localStorage.clear())
  })

  test('登录页面完整显示', async ({ page }) => {
    await page.goto('/login')
    
    // 验证页面元素
    await expect(page.locator('.login-title')).toHaveText('DJ Blog')
    await expect(page.locator('.login-subtitle')).toHaveText('登录您的账户')
    await expect(page.locator('input[placeholder="用户名"]')).toBeVisible()
    await expect(page.locator('input[placeholder="密码"]')).toBeVisible()
    await expect(page.locator('button:has-text("登录")')).toBeEnabled()
    await expect(page.locator('a:has-text("立即注册")')).toBeVisible()
  })

  test('点击注册链接跳转到注册页面', async ({ page }) => {
    await page.goto('/login')
    
    await page.click('a:has-text("立即注册")')
    await expect(page).toHaveURL(/.*register/)
  })

  test('登录表单验证 - 空用户名', async ({ page }) => {
    await page.goto('/login')
    
    // 只填写密码
    await page.fill('input[placeholder="密码"]', '123456')
    await page.click('button:has-text("登录")')
    
    // 应该显示用户名错误提示
    await expect(page.locator('.el-form-item__error:has-text("请输入用户名")')).toBeVisible()
  })

  test('登录表单验证 - 空密码', async ({ page }) => {
    await page.goto('/login')
    
    // 只填写用户名
    await page.fill('input[placeholder="用户名"]', 'testuser')
    await page.click('button:has-text("登录")')
    
    // 应该显示密码错误提示
    await expect(page.locator('.el-form-item__error:has-text("请输入密码")')).toBeVisible()
  })

  test('登录表单验证 - 密码过短', async ({ page }) => {
    await page.goto('/login')
    
    await page.fill('input[placeholder="用户名"]', 'testuser')
    await page.fill('input[placeholder="密码"]', '123')
    await page.click('button:has-text("登录")')
    
    // 应该显示密码长度错误提示
    await expect(page.locator('.el-form-item__error:has-text("密码长度")')).toBeVisible()
  })

  test('成功登录流程（模拟）', async ({ page }) => {
    await page.goto('/login')
    
    // 填写表单
    await page.fill('input[placeholder="用户名"]', 'testuser')
    await page.fill('input[placeholder="密码"]', '123456')
    
    // 监听 API 请求（需要后端运行）
    // 点击登录
    await page.click('button:has-text("登录")')
    
    // 如果后端未运行，会显示错误消息
    // 这里只是验证按钮点击行为
  })
})

test.describe('注册流程测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
    await page.evaluate(() => localStorage.clear())
  })

  test('注册页面完整显示', async ({ page }) => {
    await page.goto('/register')
    
    // 验证页面元素
    await expect(page.locator('.register-title')).toHaveText('DJ Blog')
    await expect(page.locator('.register-subtitle')).toHaveText('创建新账户')
    await expect(page.locator('input[placeholder="用户名"]')).toBeVisible()
    await expect(page.locator('input[placeholder="邮箱"]')).toBeVisible()
    await expect(page.locator('input[placeholder="密码"]')).toBeVisible()
    await expect(page.locator('button:has-text("注册")')).toBeEnabled()
    await expect(page.locator('a:has-text("立即登录")')).toBeVisible()
  })

  test('点击登录链接跳转到登录页面', async ({ page }) => {
    await page.goto('/register')
    
    await page.click('a:has-text("立即登录")')
    await expect(page).toHaveURL(/.*login/)
  })

  test('注册表单验证 - 必填项检查', async ({ page }) => {
    await page.goto('/register')
    
    // 直接点击注册按钮
    await page.click('button:has-text("注册")')
    
    // 等待验证错误出现
    await page.waitForTimeout(100)
    
    // 应该显示错误提示 - 使用 first() 选择第一个错误
    await expect(page.locator('.el-form-item__error').first()).toBeVisible()
  })

  test('注册表单验证 - 邮箱格式', async ({ page }) => {
    await page.goto('/register')
    
    await page.fill('input[placeholder="用户名"]', 'testuser')
    await page.fill('input[placeholder="邮箱"]', 'invalid-email')
    await page.fill('input[placeholder="密码"]', '123456')
    await page.locator('input[placeholder="密码"]').press('Tab')
    
    // 应该显示邮箱格式错误
    await expect(page.locator('.el-form-item__error:has-text("邮箱")')).toBeVisible()
  })

  test('注册表单验证 - 密码确认', async ({ page }) => {
    await page.goto('/register')
    
    await page.fill('input[placeholder="用户名"]', 'testuser')
    await page.fill('input[placeholder="邮箱"]', 'test@example.com')
    await page.fill('input[placeholder="密码"]', '123456')
    await page.fill('input[placeholder="确认密码"]', '654321')
    await page.click('button:has-text("注册")')
    
    // 应该显示密码不一致错误
    await expect(page.locator('.el-form-item__error:has-text("密码不一致")')).toBeVisible()
  })
})

test.describe('认证状态测试', () => {
  test('未登录状态 - 首页显示登录按钮', async ({ page }) => {
    await page.goto('/')
    await page.evaluate(() => localStorage.clear())
    await page.reload()
    
    // 应该显示登录按钮
    await expect(page.locator('button:has-text("登录")')).toBeVisible()
  })

  test('未登录状态 - 访问后台重定向到登录页', async ({ page }) => {
    await page.goto('/')
    await page.evaluate(() => localStorage.clear())
    
    // 尝试访问后台
    await page.goto('/admin')
    
    // 应该重定向到登录页面
    await expect(page).toHaveURL(/.*login/)
  })

  test('模拟已登录状态', async ({ page }) => {
    // 模拟登录状态
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.setItem('token', 'fake-token')
      localStorage.setItem('user', JSON.stringify({
        id: 1,
        username: 'testuser',
        nickname: '测试用户',
        role: 'USER'
      }))
    })
    
    await page.reload()
    
    // 应该显示用户头像
    await expect(page.locator('.user-dropdown')).toBeVisible()
  })
})
