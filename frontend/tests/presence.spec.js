import { test, expect } from '@playwright/test'

test.describe('在线用户检测', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
    await page.waitForTimeout(2000)
  })

  test('单标签页显示在线人数', async ({ page }) => {
    const onlineCount = page.locator('.online-count')
    await expect(onlineCount).toBeVisible()
    
    const text = await onlineCount.textContent()
    expect(text).toMatch(/\d+ 人在线/)
  })

  test('多标签页去重 - 同一浏览器只算1人', async ({ context, page }) => {
    const page1 = await context.newPage()
    await page1.goto('/')
    await page1.waitForTimeout(2000)
    
    const text1 = await page1.locator('.online-count').textContent()
    expect(text1).toMatch(/1 人在线/)
    
    const page2 = await context.newPage()
    await page2.goto('/')
    await page2.waitForTimeout(2000)
    
    const text2 = await page2.locator('.online-count').textContent()
    expect(text2).toMatch(/1 人在线/)
    
    await page1.close()
    await page2.close()
  })

  test('不同浏览器上下文显示2人', async ({ browser }) => {
    const context1 = await browser.newContext()
    const context2 = await browser.newContext()
    
    const page1 = await context1.newPage()
    await page1.goto('/')
    await page1.waitForTimeout(2000)
    
    const page2 = await context2.newPage()
    await page2.goto('/')
    await page2.waitForTimeout(2000)
    
    const text = await page1.locator('.online-count').textContent()
    expect(text).toMatch(/2 人在线/)
    
    await context1.close()
    await context2.close()
  })
})
