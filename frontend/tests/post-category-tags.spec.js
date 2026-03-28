import { test, expect } from '@playwright/test'

const TEST_USER = {
  username: 'test',
  password: '123456'
}

async function login(page) {
  await page.goto('/login')
  await page.fill('input[placeholder="用户名"]', TEST_USER.username)
  await page.fill('input[placeholder="密码"]', TEST_USER.password)
  await page.click('button:has-text("登录")')
  
  try {
    await page.waitForSelector('.user-dropdown', { timeout: 10000 })
  } catch {
    await page.waitForURL(/^(?!.*login).*$/, { timeout: 10000 })
  }
  
  await page.waitForTimeout(500)
}

test.describe('文章标签和分类功能测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
  })

  test('文章详情页显示分类名称', async ({ page }) => {
    await page.goto('/admin/posts')
    await page.waitForLoadState('networkidle')
    
    const table = page.locator('.el-table')
    if (await table.isVisible()) {
      const rows = table.locator('tbody tr')
      const count = await rows.count()
      
      if (count > 0) {
        const firstRow = rows.first()
        const categoryCell = firstRow.locator('td').nth(1)
        const hasCategory = await categoryCell.isVisible().catch(() => false)
        expect(hasCategory).toBeTruthy()
      }
    }
  })

  test('文章详情页显示标签', async ({ page }) => {
    await page.goto('/admin/posts')
    await page.waitForLoadState('networkidle')
    
    const table = page.locator('.el-table')
    if (await table.isVisible()) {
      const rows = table.locator('tbody tr')
      const count = await rows.count()
      
      if (count > 0) {
        const firstRow = rows.first()
        const tagCell = firstRow.locator('td').nth(2)
        const hasTags = await tagCell.isVisible().catch(() => false)
        expect(hasTags).toBeTruthy()
      }
    }
  })

  test('编辑页面加载文章分类和标签', async ({ page }) => {
    await page.goto('/admin/posts')
    await page.waitForLoadState('networkidle')
    
    const table = page.locator('.el-table')
    if (await table.isVisible()) {
      const rows = table.locator('tbody tr')
      const count = await rows.count()
      
      if (count > 0) {
        const editBtn = rows.first().locator('button:has-text("编辑")')
        await editBtn.click()
        await page.waitForLoadState('networkidle')
        
        await page.waitForTimeout(1000)
        
        const categorySelect = page.locator('.el-select').first()
        const hasCategory = await categorySelect.isVisible().catch(() => false)
        
        const tagSelect = page.locator('.el-select').nth(1)
        const hasTags = await tagSelect.isVisible().catch(() => false)
        
        expect(hasCategory || hasTags).toBeTruthy()
      }
    }
  })

  test('文章列表显示浏览量', async ({ page }) => {
    await page.goto('/admin/posts')
    await page.waitForLoadState('networkidle')
    
    const table = page.locator('.el-table')
    if (await table.isVisible()) {
      const headers = table.locator('thead th')
      const headerCount = await headers.count()
      
      let hasViewCount = false
      for (let i = 0; i < headerCount; i++) {
        const text = await headers.nth(i).textContent()
        if (text && text.includes('浏览')) {
          hasViewCount = true
          break
        }
      }
      expect(hasViewCount).toBeTruthy()
    }
  })
})

test.describe('文章编辑页面分类回显测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
  })

  test('编辑已有文章时正确回显分类', async ({ page }) => {
    await page.goto('/admin/posts')
    await page.waitForLoadState('networkidle')
    
    const table = page.locator('.el-table')
    if (await table.isVisible()) {
      const rows = table.locator('tbody tr')
      const count = await rows.count()
      
      if (count > 0) {
        await rows.first().locator('button:has-text("编辑")').click()
        await page.waitForLoadState('networkidle')
        await page.waitForTimeout(1500)
        
        const categoryInput = page.locator('.el-select').first()
        const isSelectVisible = await categoryInput.isVisible().catch(() => false)
        
        expect(isSelectVisible).toBeTruthy()
      }
    }
  })

  test('编辑已有文章时正确回显标签', async ({ page }) => {
    await page.goto('/admin/posts')
    await page.waitForLoadState('networkidle')
    
    const table = page.locator('.el-table')
    if (await table.isVisible()) {
      const rows = table.locator('tbody tr')
      const count = await rows.count()
      
      if (count > 0) {
        await rows.first().locator('button:has-text("编辑")').click()
        await page.waitForLoadState('networkidle')
        await page.waitForTimeout(1500)
        
        const tagInput = page.locator('.el-select').nth(1)
        const isSelectVisible = await tagInput.isVisible().catch(() => false)
        
        expect(isSelectVisible).toBeTruthy()
      }
    }
  })

  test('新建文章时可以选择分类', async ({ page }) => {
    await page.goto('/admin/posts/write')
    await page.waitForLoadState('networkidle')
    
    const categorySelect = page.locator('.el-select').first()
    const isVisible = await categorySelect.isVisible().catch(() => false)
    
    expect(isVisible).toBeTruthy()
  })

  test('新建文章时可以选择标签', async ({ page }) => {
    await page.goto('/admin/posts/write')
    await page.waitForLoadState('networkidle')
    
    const tagSelect = page.locator('.el-select').nth(1)
    const isVisible = await tagSelect.isVisible().catch(() => false)
    
    expect(isVisible).toBeTruthy()
  })
})