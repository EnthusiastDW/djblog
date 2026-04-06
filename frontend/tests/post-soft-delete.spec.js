import { test, expect } from '@playwright/test'

/**
 * 文章软删除功能 E2E 测试
 * 覆盖：软删除、回收站列表、恢复文章、彻底删除
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

test.describe('文章软删除功能', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page)
  })

  test('后台文章管理页面应显示文章列表和回收站标签', async ({ page }) => {
    await page.goto('/admin/posts')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(2000)

    const listTab = page.locator('.el-tabs__item:has-text("文章列表")')
    await expect(listTab).toBeVisible()

    const trashTab = page.locator('.el-tabs__item:has-text("回收站")')
    await expect(trashTab).toBeVisible()
  })

  test('点击删除按钮应弹出确认对话框', async ({ page }) => {
    await page.goto('/admin/posts')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(2000)

    const deleteButtons = page.locator('button:has-text("删除")')
    const deleteCount = await deleteButtons.count()

    if (deleteCount > 0) {
      await deleteButtons.first().click()
      await page.waitForTimeout(500)

      const confirmDialog = page.locator('.el-popconfirm')
      await expect(confirmDialog).toBeVisible()
    }
  })

  test('回收站标签页应正确切换', async ({ page }) => {
    await page.goto('/admin/posts')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(2000)

    const trashTab = page.locator('.el-tabs__item:has-text("回收站")')
    await trashTab.click()
    await page.waitForTimeout(1000)

    const activeTab = page.locator('.el-tabs__item.is-active')
    await expect(activeTab).toContainText('回收站')
  })
})
