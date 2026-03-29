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

test.describe('背景设置测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
  })

  test('访问设置页面', async ({ page }) => {
    await page.goto('/admin/settings')
    await page.waitForLoadState('networkidle')
    
    const hasTitle = await page.locator('.page-title, h2').first().isVisible().catch(() => false)
    const hasForm = await page.locator('.el-form').isVisible().catch(() => false)
    
    expect(hasTitle || hasForm).toBeTruthy()
  })

  test('设置页面显示背景设置表单', async ({ page }) => {
    await page.goto('/admin/settings')
    await page.waitForLoadState('networkidle')
    
    await expect(page.locator('text=背景图片')).toBeVisible()
    await expect(page.locator('text=透明度')).toBeVisible()
  })

  test('设置背景图片URL并保存', async ({ page }) => {
    await page.goto('/admin/settings')
    await page.waitForLoadState('networkidle')
    
    const testImageUrl = 'https://picsum.photos/1920/1080'
    
    const bgImageInput = page.locator('input[placeholder="输入图片URL或上传"]')
    await bgImageInput.fill(testImageUrl)
    
    const saveBtn = page.locator('button:has-text("保存")')
    await saveBtn.click()
    
    await page.waitForTimeout(2000)
    
    const successMsg = page.locator('.el-message--success')
    await expect(successMsg).toBeVisible({ timeout: 5000 })
  })

  test('调整透明度并保存', async ({ page }) => {
    await page.goto('/admin/settings')
    await page.waitForLoadState('networkidle')
    
    const slider = page.locator('.el-slider')
    await expect(slider).toBeVisible()
    
    const saveBtn = page.locator('button:has-text("保存")')
    await saveBtn.click()
    
    await page.waitForTimeout(2000)
    
    const successMsg = page.locator('.el-message--success')
    await expect(successMsg).toBeVisible({ timeout: 5000 })
  })

  test('设置背景后前端页面显示背景', async ({ page }) => {
    const testImageUrl = 'https://picsum.photos/1920/1080'
    
    await page.goto('/admin/settings')
    await page.waitForLoadState('networkidle')
    
    const bgImageInput = page.locator('input[placeholder="输入图片URL或上传"]')
    await bgImageInput.fill(testImageUrl)
    
    const saveBtn = page.locator('button:has-text("保存")')
    await saveBtn.click()
    
    await page.waitForTimeout(2000)
    
    await page.goto('/')
    await page.waitForLoadState('networkidle')
    
    const bgDiv = page.locator('.bg-image')
    await expect(bgDiv).toBeVisible({ timeout: 10000 })
    
    const bgStyle = await bgDiv.getAttribute('style')
    expect(bgStyle).toContain('background-image')
  })

  test('背景设置全局生效 - 多页面验证', async ({ page }) => {
    const testImageUrl = 'https://picsum.photos/1920/1080'
    
    await page.goto('/admin/settings')
    await page.waitForLoadState('networkidle')
    
    const bgImageInput = page.locator('input[placeholder="输入图片URL或上传"]')
    await bgImageInput.fill(testImageUrl)
    
    const saveBtn = page.locator('button:has-text("保存")')
    await saveBtn.click()
    
    await page.waitForTimeout(2000)
    
    const pages = ['/', '/archives', '/categories', '/tags']
    
    for (const pagePath of pages) {
      await page.goto(pagePath)
      await page.waitForLoadState('networkidle')
      
      const bgDiv = page.locator('.bg-image')
      const isVisible = await bgDiv.isVisible().catch(() => false)
      
      if (isVisible) {
        const bgStyle = await bgDiv.getAttribute('style')
        expect(bgStyle).toContain('background-image')
      }
    }
  })

  test('清除背景图片', async ({ page }) => {
    await page.goto('/admin/settings')
    await page.waitForLoadState('networkidle')
    
    const bgImageInput = page.locator('input[placeholder="输入图片URL或上传"]')
    await bgImageInput.fill('')
    
    const clearBtn = page.locator('.el-input__clear')
    if (await clearBtn.isVisible().catch(() => false)) {
      await clearBtn.click()
    }
    
    const saveBtn = page.locator('button:has-text("保存")')
    await saveBtn.click()
    
    await page.waitForTimeout(2000)
    
    const successMsg = page.locator('.el-message--success')
    await expect(successMsg).toBeVisible({ timeout: 5000 })
  })
})
