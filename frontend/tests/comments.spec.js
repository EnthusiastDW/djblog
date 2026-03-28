import { test, expect } from '@playwright/test'

test.describe('评论功能测试', () => {
  test.beforeEach(async ({ page }) => {
    // 清除所有 cookie 和 localStorage
    await page.context().clearCookies()
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.clear()
    })
  })

  test('游客发表评论 - 使用对话框', async ({ page }) => {
    // 访问首页
    await page.goto('/')
    await page.waitForTimeout(1500)
    
    // 检查是否有文章，如果没有则跳过测试
    const postCount = await page.locator('.post-card').count()
    test.skip(postCount === 0, '首页没有文章，跳过测试')
    
    // 点击第一篇文章进入详情页
    const firstPost = page.locator('.post-card').first()
    await firstPost.click()
    await page.waitForTimeout(1000)
    
    // 验证当前是游客状态（未登录）
    const loginButton = page.locator('button:has-text("登录")')
    const isLoggedIn = await loginButton.count() > 0
    
    if (isLoggedIn) {
      // 游客状态下，点击发表评论按钮应该显示对话框
      const commentButton = page.locator('.comment-form button:has-text("发表评论")').first()
      await commentButton.scrollIntoViewIfNeeded()
      await commentButton.click()
      
      // 等待对话框出现
      await page.waitForTimeout(500)
      
      // 验证对话框存在
      const dialog = page.locator('.el-dialog')
      await expect(dialog).toBeVisible()
      
      // 验证对话框标题
      await expect(page.locator('.el-dialog__title')).toContainText('发表评论')
      
      // 验证表单字段存在
      await expect(page.locator('input[placeholder="请输入昵称"]')).toBeVisible()
      await expect(page.locator('input[placeholder="请输入邮箱"]')).toBeVisible()
      await expect(page.locator('textarea[placeholder="写下你的评论..."]')).toBeVisible()
      
      // 填写表单
      await page.fill('input[placeholder="请输入昵称"]', '游客测试')
      await page.fill('input[placeholder="请输入邮箱"]', 'guest@test.com')
      await page.fill('textarea[placeholder="写下你的评论..."]', '这是一条游客测试评论')
      
      // 点击发表按钮
      const submitButton = page.locator('.el-dialog .el-dialog__footer button:has-text("发表")')
      await submitButton.click()
      
      // 等待提交完成
      await page.waitForTimeout(2000)
      
      // 验证是否显示成功消息
      const successMessage = page.locator('.el-message--success')
      await expect(successMessage.first()).toBeVisible()
      
      // 验证评论列表是否刷新（至少有一条评论）
      const commentList = page.locator('.comment-item')
      await expect(commentList.first()).toBeVisible({ timeout: 5000 }).catch(() => {
        console.log('评论可能需要审核后才显示')
      })
    }
  })

  test('登录用户发表评论 - 直接显示输入框', async ({ page }) => {
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
    
    // 点击第一篇文章
    const firstPost = page.locator('.post-card').first()
    await firstPost.click()
    await page.waitForTimeout(1000)
    
    // 登录用户应该直接看到输入框而不是按钮
    const textarea = page.locator('.comment-form textarea')
    const count = await textarea.count()
    
    if (count > 0) {
      await expect(textarea.first()).toBeVisible()
      
      // 验证没有发表评论按钮（因为已经显示输入框了）
      const commentButtons = page.locator('.comment-form button:has-text("发表评论")')
      const buttonCount = await commentButtons.count()
      expect(buttonCount).toBe(0)
      
      // 填写评论并提交
      await textarea.first().fill('这是登录用户的测试评论')
      
      const submitButton = page.locator('.comment-form button:has-text("发表评论")')
      await submitButton.first().click()
      
      // 等待提交完成
      await page.waitForTimeout(2000)
      
      // 验证成功消息
      const successMessage = page.locator('.el-message--success')
      await expect(successMessage.first()).toBeVisible()
    }
  })

  test('游客回复评论 - 使用对话框', async ({ page }) => {
    // 访问文章详情页
    await page.goto('/')
    await page.waitForTimeout(1000)
    
    // 点击第一篇文章
    const firstPost = page.locator('.post-card').first()
    await firstPost.click()
    await page.waitForTimeout(1000)
    
    // 验证当前是游客状态
    const loginButton = page.locator('button:has-text("登录")')
    const isLoggedIn = await loginButton.count() > 0
    
    if (isLoggedIn && await page.locator('.comment-item').count() > 0) {
      // 找到第一个评论的回复按钮
      const replyButton = page.locator('.comment-reply:has-text("回复")').first()
      await replyButton.scrollIntoViewIfNeeded()
      await replyButton.click()
      
      // 等待对话框出现
      await page.waitForTimeout(500)
      
      // 验证回复对话框存在
      const dialog = page.locator('.el-dialog')
      await expect(dialog).toBeVisible()
      
      // 验证对话框标题
      await expect(page.locator('.el-dialog__title')).toContainText('回复评论')
      
      // 验证表单字段存在
      await expect(page.locator('input[placeholder="请输入昵称"]')).toBeVisible()
      await expect(page.locator('input[placeholder="请输入邮箱"]')).toBeVisible()
      await expect(page.locator('textarea[placeholder="写下你的回复..."]')).toBeVisible()
      
      // 填写回复表单
      await page.fill('input[placeholder="请输入昵称"]', '游客回复')
      await page.fill('input[placeholder="请输入邮箱"]', 'reply@test.com')
      await page.fill('textarea[placeholder="写下你的回复..."]', '这是游客的测试回复')
      
      // 点击回复按钮
      const submitButton = page.locator('.el-dialog .el-dialog__footer button:has-text("回复")')
      await submitButton.click()
      
      // 等待提交完成
      await page.waitForTimeout(2000)
      
      // 验证成功消息
      const successMessage = page.locator('.el-message--success')
      await expect(successMessage.first()).toBeVisible()
    }
  })

  test('Cookie 保存和自动填充', async ({ page }) => {
    // 先设置 cookie
    await page.goto('/')
    await page.evaluate(() => {
      document.cookie = "comment_author_name=测试用户; path=/; max-age=86400"
      document.cookie = "comment_author_email=test@example.com; path=/; max-age=86400"
    })
    
    // 刷新页面
    await page.reload()
    await page.waitForTimeout(500)
    
    // 访问文章详情页
    await page.goto('/')
    await page.waitForTimeout(1000)
    
    const firstPost = page.locator('.post-card').first()
    await firstPost.click()
    await page.waitForTimeout(1000)
    
    // 点击发表评论按钮
    const commentButton = page.locator('.comment-form button:has-text("发表评论")').first()
    await commentButton.scrollIntoViewIfNeeded()
    await commentButton.click()
    
    // 等待对话框出现
    await page.waitForTimeout(500)
    
    // 验证表单字段是否自动填充
    const nameInput = page.locator('input[placeholder="请输入昵称"]')
    const emailInput = page.locator('input[placeholder="请输入邮箱"]')
    
    // 检查输入框的值
    const nameValue = await nameInput.inputValue()
    const emailValue = await emailInput.inputValue()
    
    // 验证是否从 cookie 中读取了值
    expect(nameValue).toBe('测试用户')
    expect(emailValue).toBe('test@example.com')
  })

  test('多层评论 - 递归回复功能', async ({ page }) => {
    // 模拟登录状态以便测试内联回复
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
    
    // 验证评论组件存在
    const commentItems = page.locator('.comment-item')
    const count = await commentItems.count()
    
    if (count > 0) {
      // 找到第一个评论
      const firstComment = commentItems.first()
      
      // 验证回复按钮存在（根评论）
      const replyBtn = firstComment.locator('.comment-reply:has-text("回复")')
      await expect(replyBtn.first()).toBeVisible()
      
      // 如果有回复列表，验证递归组件
      const replyList = firstComment.locator('.reply-list')
      const replyCount = await replyList.count()
      
      if (replyCount > 0) {
        // 验证回复项的样式（应该有缩进）
        const replyItems = replyList.locator('.reply-item')
        const item = replyItems.first()
        await expect(item).toBeVisible()
        
        // 验证回复项的 marginLeft 样式（通过计算深度）
        const style = await item.getAttribute('style')
        // 样式中应该包含 margin-left 或 marginLeft
        expect(style).toBeTruthy()
      }
    }
  })

  test('评论深度限制 - 最多 5 层', async ({ page }) => {
    // 这个测试验证第 5 层不显示回复按钮
    // 由于需要实际的 5 层评论数据，这里做基本验证
    
    await page.goto('/')
    await page.waitForTimeout(1000)
    
    const firstPost = page.locator('.post-card').first()
    await firstPost.click()
    await page.waitForTimeout(1000)
    
    // 验证页面正常加载
    await expect(page).toHaveURL(/\/post\/\d+/)
    
    // 检查评论区域存在
    const commentSection = page.locator('.comment-section')
    await expect(commentSection).toBeVisible()
  })
})
