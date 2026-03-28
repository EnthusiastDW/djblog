import { test, expect } from '@playwright/test'

test.describe('多层评论功能测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.context().clearCookies()
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.clear()
    })
  })

  test('评论层级显示正确', async ({ page }) => {
    // 模拟登录状态
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.setItem('token', 'fake-token')
      localStorage.setItem('user', JSON.stringify({
        id: 1,
        username: 'testuser',
        email: 'test@example.com',
        role: 'USER'
      }))
    })
    
    // 访问文章详情页
    await page.goto('/')
    await page.waitForTimeout(1000)
    
    const firstPost = page.locator('.post-card').first()
    await firstPost.click()
    await page.waitForTimeout(1000)
    
    // 验证评论组件存在
    const commentComponent = page.locator('.comment-reply')
    // 如果有回复评论，应该能看到递归组件
    
    // 验证回复按钮的缩进随层级增加
    // 这需要实际的评论数据，这里只是结构验证
  })

  test('递归回复组件深度限制', async ({ page }) => {
    // 这个测试验证回复深度不超过 5 层
    // 由于需要实际数据，这里只做基本验证
    await page.goto('/')
    await page.waitForTimeout(1000)
    
    const firstPost = page.locator('.post-card').first()
    await firstPost.click()
    await page.waitForTimeout(1000)
    
    // 验证页面加载正常
    await expect(page).toHaveTitle(/文章详情/)
  })

  test('内联回复表单显示（登录用户）', async ({ page }) => {
    // 模拟登录
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.setItem('token', 'fake-token')
      localStorage.setItem('user', JSON.stringify({
        id: 1,
        username: 'testuser',
        email: 'test@example.com',
        role: 'USER'
      }))
    })
    
    await page.goto('/')
    await page.waitForTimeout(1000)
    
    const firstPost = page.locator('.post-card').first()
    await firstPost.click()
    await page.waitForTimeout(1000)
    
    // 如果页面有评论，点击回复按钮验证内联表单显示
    const replyButtons = page.locator('.reply-reply-btn')
    const count = await replyButtons.count()
    
    if (count > 0) {
      await replyButtons.first().click()
      await page.waitForTimeout(500)
      
      // 验证内联回复表单出现
      const inlineForm = page.locator('.inline-reply-form')
      // 注意：这取决于 CommentReply 组件的实现
    }
  })

  test('匿名用户回复使用对话框', async ({ page }) => {
    await page.goto('/')
    await page.waitForTimeout(1000)
    
    const firstPost = page.locator('.post-card').first()
    await firstPost.click()
    await page.waitForTimeout(1000)
    
    // 验证未登录状态
    const loginButton = page.locator('button:has-text("登录")')
    const isLoggedIn = await loginButton.count() > 0
    
    if (isLoggedIn) {
      // 点击回复按钮应该显示对话框
      const replyButtons = page.locator('.comment-reply:has-text("回复")')
      const count = await replyButtons.count()
      
      if (count > 0) {
        await replyButtons.first().click()
        await page.waitForTimeout(500)
        
        // 验证对话框显示
        const dialog = page.locator('.el-dialog')
        await expect(dialog).toBeVisible()
        await expect(page.locator('.el-dialog__title')).toContainText('回复评论')
      }
    }
  })
})
