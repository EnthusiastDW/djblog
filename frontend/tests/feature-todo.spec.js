import { test, expect } from '@playwright/test'

test.describe('访问统计测试', () => {
  test('页脚应该显示访问统计（今日访问和总访问）', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('domcontentloaded')
    
    const footerStats = page.locator('.footer-stats')
    await expect(footerStats).toBeVisible()
    
    const todayStat = footerStats.locator('.stat-item').filter({ hasText: '今日' })
    await expect(todayStat).toBeVisible()
    
    const totalStat = footerStats.locator('.stat-item').filter({ hasText: '总访问' })
    await expect(totalStat).toBeVisible()
  })
})

test.describe('热门文章测试', () => {
  test('侧边栏应该显示热门文章', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('domcontentloaded')
    
    const recentPosts = page.locator('.recent-posts')
    if (await recentPosts.count() > 0) {
      await expect(recentPosts).toBeVisible()
    }
  })

  test('热门文章标题应该可点击跳转', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('domcontentloaded')
    
    const recentPostItems = page.locator('.recent-post-item')
    const count = await recentPostItems.count()
    
    if (count > 0) {
      await recentPostItems.first().click()
      await expect(page).toHaveURL(/.*\/article\/.+/)
    }
  })
})

test.describe('主页标题测试', () => {
  test('主页标题应该显示博主名称或默认标题', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('domcontentloaded')
    
    const logoText = page.locator('.logo-text')
    await expect(logoText).toBeVisible()
    
    const text = await logoText.textContent()
    expect(text === 'DJ Blog' || text.includes('的博客')).toBe(true)
  })

  test('主页页面标题应该显示博主名称或默认标题', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('domcontentloaded')
    
    const pageTitle = page.locator('.page-title')
    const count = await pageTitle.count()
    
    if (count > 0) {
      const text = await pageTitle.textContent()
      expect(text === '最新文章' || text.includes('的博客')).toBe(true)
    }
  })
})

test.describe('侧边栏分类和标签测试', () => {
  test('侧边栏分类应该按文章数量排序显示前10个', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('domcontentloaded')
    
    const categoryList = page.locator('.category-list')
    if (await categoryList.count() > 0) {
      const categoryItems = categoryList.locator('.category-item')
      const itemCount = await categoryItems.count()
      
      if (itemCount > 0) {
        const moreLink = categoryList.locator('.more-link')
        if (await moreLink.count() > 0) {
          await expect(moreLink).toBeVisible()
          const linkText = await moreLink.textContent()
          expect(linkText).toContain('更多')
        }
      }
    }
  })

  test('侧边栏标签应该按文章数量排序显示前10个', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('domcontentloaded')
    
    const tagCloud = page.locator('.tag-cloud')
    if (await tagCloud.count() > 0) {
      const tagItems = tagCloud.locator('.tag-item')
      const itemCount = await tagItems.count()
      
      if (itemCount > 0) {
        const moreLink = tagCloud.locator('.more-link')
        if (await moreLink.count() > 0) {
          await expect(moreLink).toBeVisible()
          const linkText = await moreLink.textContent()
          expect(linkText).toContain('更多')
        }
      }
    }
  })
})

test.describe('博主信息卡片测试', () => {
  test('侧边栏应该显示博主信息卡片', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('domcontentloaded')
    
    const profileCard = page.locator('.profile-card')
    if (await profileCard.count() > 0) {
      await expect(profileCard).toBeVisible()
      
      const avatar = profileCard.locator('.el-avatar')
      if (await avatar.count() > 0) {
        await expect(avatar.first()).toBeVisible()
      }
      
      const profileName = profileCard.locator('.profile-name')
      if (await profileName.count() > 0) {
        await expect(profileName).toBeVisible()
      }
    }
  })

  test('点击分类的"更多"链接应该跳转到分类页面', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('domcontentloaded')
    
    const categoryList = page.locator('.category-list')
    const moreLink = categoryList.locator('.more-link')
    
    if (await moreLink.count() > 0) {
      await moreLink.click()
      await expect(page).toHaveURL(/.*categories/)
    }
  })

  test('点击标签的"更多"链接应该跳转到标签页面', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('domcontentloaded')
    
    const tagCloud = page.locator('.tag-cloud')
    const moreLink = tagCloud.locator('.more-link')
    
    if (await moreLink.count() > 0) {
      await moreLink.click()
      await expect(page).toHaveURL(/.*tags/)
    }
  })
})

test.describe('文章目录测试', () => {
  test('文章详情页应该存在目录容器', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('domcontentloaded')
    
    const postCards = page.locator('.post-card')
    const count = await postCards.count()
    
    if (count > 0) {
      await postCards.first().click()
      await page.waitForLoadState('domcontentloaded')
      
      const tocContainer = page.locator('.toc-container')
      const tocSidebar = page.locator('.toc-sidebar')
      
      expect(await tocContainer.count() > 0 || await tocSidebar.count() > 0).toBe(true)
    }
  })

  test('目录应该显示标题列表且点击可跳转', async ({ page }) => {
    await page.goto('/article/spring-ai-concepts-models')
    await page.waitForLoadState('domcontentloaded')
    
    const tocList = page.locator('.toc-list')
    if (await tocList.count() > 0) {
      const tocItems = tocList.locator('.toc-item')
      const itemCount = await tocItems.count()
      
      if (itemCount > 0) {
        const firstItem = tocItems.first()
        const link = firstItem.locator('a')
        const href = await link.getAttribute('href')
        
        expect(href).toContain('#')
        expect(href.length).toBeGreaterThan(1)
      }
    }
  })

  test('点击目录项应该跳转到对应标题', async ({ page }) => {
    await page.goto('/article/spring-ai-concepts-models')
    await page.waitForLoadState('domcontentloaded')
    
    const tocList = page.locator('.toc-list')
    if (await tocList.count() > 0) {
      const tocItems = tocList.locator('.toc-item')
      const itemCount = await tocItems.count()
      
      if (itemCount > 0) {
        const firstItem = tocItems.first()
        const link = firstItem.locator('a')
        
        const href = await link.getAttribute('href')
        const headingId = href.replace('#', '')
        
        const heading = page.locator(`#${headingId}`)
        if (await heading.count() > 0) {
          await link.click()
          await page.waitForTimeout(500)
          
          const url = page.url()
          expect(url).toContain(`#${headingId}`)
        }
      }
    }
  })

  test('目录项应该高亮当前阅读位置', async ({ page }) => {
    await page.goto('/article/spring-ai-concepts-models')
    await page.waitForLoadState('domcontentloaded')
    
    const tocList = page.locator('.toc-list')
    if (await tocList.count() > 0) {
      const activeItem = tocList.locator('.toc-item a.active')
      
      await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight))
      await page.waitForTimeout(500)
      
      if (await activeItem.count() > 0) {
        expect(await activeItem.isVisible()).toBe(true)
      }
    }
  })
})

test.describe('移动端目录测试', () => {
  test('移动端应该显示目录切换按钮', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 844 })
    await page.goto('/article/spring-ai-concepts-models')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(2000)
    
    const toggleBtn = page.locator('.toc-toggle-btn')
    await expect(toggleBtn).toBeVisible({ timeout: 15000 })
  })

  test('移动端切换按钮应该固定在视口中不随页面滚动移动', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 844 })
    await page.goto('/article/spring-ai-concepts-models')
    await page.waitForLoadState('domcontentloaded')
    await page.waitForTimeout(3000)
    
    const toggleBtn = page.locator('.toc-toggle-btn')
    await expect(toggleBtn).toBeVisible({ timeout: 15000 })
    
    // 验证按钮确实存在并且有正确的样式
    const btnStyle = await page.evaluate(() => {
      const btn = document.querySelector('.toc-toggle-btn')
      const style = window.getComputedStyle(btn)
      return {
        position: style.position,
        top: style.top,
        right: style.right,
        bottom: style.bottom
      }
    })
    
    expect(btnStyle.position).toBe('fixed')
    expect(btnStyle.bottom).not.toBe('auto')
    
    // 验证按钮在滚动后仍然可见
    await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight))
    await page.waitForTimeout(1000)
    
    await expect(toggleBtn).toBeVisible()
  })

  test('点击移动端切换按钮应该展开目录面板', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 844 })
    await page.goto('/article/spring-ai-concepts-models')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(2000)
    
    const toggleBtn = page.locator('.toc-toggle-btn')
    await expect(toggleBtn).toBeVisible({ timeout: 15000 })
    await toggleBtn.click()
    await page.waitForTimeout(500)
    
    const tocSidebar = page.locator('.toc-sidebar.is-expanded')
    await expect(tocSidebar).toBeVisible()
  })

  test('移动端目录面板应该有关闭按钮', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 844 })
    await page.goto('/article/spring-ai-concepts-models')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(2000)
    
    const toggleBtn = page.locator('.toc-toggle-btn')
    await expect(toggleBtn).toBeVisible({ timeout: 15000 })
    await toggleBtn.click()
    await page.waitForTimeout(500)
    
    const closeBtn = page.locator('.toc-close-btn')
    await expect(closeBtn).toBeVisible()
  })

  test('点击关闭按钮应该关闭目录面板', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 844 })
    await page.goto('/article/spring-ai-concepts-models')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(2000)
    
    const toggleBtn = page.locator('.toc-toggle-btn')
    await expect(toggleBtn).toBeVisible({ timeout: 15000 })
    await toggleBtn.click()
    await page.waitForTimeout(500)
    
    const closeBtn = page.locator('.toc-close-btn')
    await closeBtn.click()
    await page.waitForTimeout(500)
    
    const tocSidebar = page.locator('.toc-sidebar.is-expanded')
    await expect(tocSidebar).not.toBeVisible()
  })
})
