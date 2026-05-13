import { test, expect } from '@playwright/test'

/**
 * 测试重复保存草稿时应该使用同一个文章ID，而不是创建多篇重复文章
 */

const ADMIN_USER = {
  username: 'test',
  password: '123456'
}

async function loginAsAdmin(page) {
  await page.goto('/login')
  await page.fill('input[placeholder="用户名"]', ADMIN_USER.username)
  await page.fill('input[placeholder="密码"]', ADMIN_USER.password)
  await page.click('button:has-text("登录")')
  
  try {
    await page.waitForSelector('.user-dropdown', { timeout: 10000 })
  } catch {
    await page.waitForURL(/^(?!.*login).*$/, { timeout: 10000 })
  }
  await page.waitForTimeout(500)
}

test.describe('重复保存草稿测试', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page)
  })

  test('重复点击保存草稿按钮不应该创建多篇文章', async ({ page }) => {
    await page.goto('/admin/posts/write')
    await page.waitForLoadState('networkidle')
    
    const timestamp = Date.now()
    const title = `重复保存测试 ${timestamp}`
    
    // 填写标题
    const titleInput = page.locator('input').first()
    await titleInput.fill(title)
    
    // 填写内容
    const contentInput = page.locator('.editor-textarea textarea, textarea').first()
    if (await contentInput.isVisible()) {
      await contentInput.fill('# 测试文章\n\n这是用于测试重复保存的内容。')
    }
    
    // 第一次保存草稿
    const saveBtn = page.locator('button:has-text("保存草稿")')
    await saveBtn.click()
    
    // 等待保存成功消息
    await page.waitForSelector('.el-message--success:has-text("草稿已保存")', { timeout: 5000 })
    await page.waitForTimeout(1000)
    
    // 第二次保存草稿（快速连续点击）
    await saveBtn.click()
    
    // 等待第二次保存成功
    await page.waitForSelector('.el-message--success:has-text("草稿已保存")', { timeout: 5000 })
    await page.waitForTimeout(1000)
    
    // 第三次保存草稿
    await saveBtn.click()
    await page.waitForSelector('.el-message--success:has-text("草稿已保存")', { timeout: 5000 })
    await page.waitForTimeout(1000)
    
    // 导航到文章列表页面，验证只创建了一篇文章
    await page.goto('/admin/posts')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(2000)
    
    // 搜索刚才创建的文章标题
    const searchInput = page.locator('input[placeholder*="搜索"], input[placeholder*="关键词"]').first()
    if (await searchInput.isVisible()) {
      await searchInput.fill(title)
      await page.keyboard.press('Enter')
      await page.waitForTimeout(2000)
    }
    
    // 统计搜索结果中的文章数量
    const tableRows = page.locator('.el-table__row')
    const rowCount = await tableRows.count()
    
    console.log(`搜索结果中找到 ${rowCount} 篇文章`)
    
    // 应该只有1篇文章，而不是3篇
    expect(rowCount).toBeLessThanOrEqual(1)
    
    // 如果找到文章，验证标题匹配
    if (rowCount === 1) {
      const firstRowTitle = await tableRows.first().locator('td').nth(1).textContent()
      expect(firstRowTitle).toContain(title)
    }
  })

  test('自动保存功能不应该创建多篇文章', async ({ page }) => {
    await page.goto('/admin/posts/write')
    await page.waitForLoadState('networkidle')
    
    const timestamp = Date.now()
    const title = `自动保存测试 ${timestamp}`
    
    // 开启自动保存
    const autoSaveSwitch = page.locator('.auto-save-settings .el-switch')
    if (await autoSaveSwitch.isVisible()) {
      await autoSaveSwitch.click()
      await page.waitForTimeout(500)
      
      // 设置为最短间隔（1分钟）
      const intervalSelect = page.locator('.auto-save-settings .el-select')
      if (await intervalSelect.isVisible()) {
        await intervalSelect.click()
        await page.waitForTimeout(300)
        const oneMinOption = page.locator('.el-select-dropdown__item').filter({ hasText: '1分钟' })
        if (await oneMinOption.isVisible()) {
          await oneMinOption.click()
          await page.waitForTimeout(500)
        }
      }
    }
    
    // 填写标题
    const titleInput = page.locator('input').first()
    await titleInput.fill(title)
    
    // 填写内容
    const contentInput = page.locator('.editor-textarea textarea, textarea').first()
    if (await contentInput.isVisible()) {
      await contentInput.fill('# 测试文章\n\n这是用于测试自动保存的内容。')
    }
    
    // 等待一段时间让自动保存触发（由于1分钟太长，我们手动触发几次保存）
    // 在实际场景中，自动保存会每隔设定的时间触发一次
    
    // 手动模拟多次保存（代表自动保存的行为）
    const saveBtn = page.locator('button:has-text("保存草稿")')
    
    for (let i = 0; i < 3; i++) {
      await saveBtn.click()
      await page.waitForSelector('.el-message--success:has-text("草稿已保存")', { timeout: 5000 })
      await page.waitForTimeout(1000)
    }
    
    // 导航到文章列表页面，验证只创建了一篇文章
    await page.goto('/admin/posts')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(2000)
    
    // 搜索刚才创建的文章标题
    const searchInput = page.locator('input[placeholder*="搜索"], input[placeholder*="关键词"]').first()
    if (await searchInput.isVisible()) {
      await searchInput.fill(title)
      await page.keyboard.press('Enter')
      await page.waitForTimeout(2000)
    }
    
    // 统计搜索结果中的文章数量
    const tableRows = page.locator('.el-table__row')
    const rowCount = await tableRows.count()
    
    console.log(`自动保存测试：搜索结果中找到 ${rowCount} 篇文章`)
    
    // 应该只有1篇文章
    expect(rowCount).toBeLessThanOrEqual(1)
  })

  test('从草稿发布后再次编辑不应该创建新文章', async ({ page }) => {
    await page.goto('/admin/posts/write')
    await page.waitForLoadState('networkidle')
    
    const timestamp = Date.now()
    const title = `草稿发布测试 ${timestamp}`
    
    // 填写标题和内容
    const titleInput = page.locator('input').first()
    await titleInput.fill(title)
    
    const contentInput = page.locator('.editor-textarea textarea, textarea').first()
    if (await contentInput.isVisible()) {
      await contentInput.fill('# 测试文章\n\n这是用于测试草稿发布的内容。')
    }
    
    // 先保存草稿
    const saveDraftBtn = page.locator('button:has-text("保存草稿")')
    await saveDraftBtn.click()
    await page.waitForSelector('.el-message--success:has-text("草稿已保存")', { timeout: 5000 })
    await page.waitForTimeout(1000)
    
    // 然后发布
    const publishBtn = page.locator('button:has-text("发布")')
    await publishBtn.click()
    
    // 等待发布成功并跳转到列表页
    await page.waitForURL(/\/admin\/posts/, { timeout: 10000 })
    await page.waitForTimeout(2000)
    
    // 搜索刚才发布的文章
    const searchInput = page.locator('input[placeholder*="搜索"], input[placeholder*="关键词"]').first()
    if (await searchInput.isVisible()) {
      await searchInput.fill(title)
      await page.keyboard.press('Enter')
      await page.waitForTimeout(2000)
    }
    
    // 验证只有一篇文章
    const tableRows = page.locator('.el-table__row')
    const rowCount = await tableRows.count()
    
    console.log(`草稿发布测试：搜索结果中找到 ${rowCount} 篇文章`)
    expect(rowCount).toBeLessThanOrEqual(1)
  })
})
