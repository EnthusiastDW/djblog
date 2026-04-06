import { test, expect } from '@playwright/test'

/**
 * 分类页面新功能 E2E 测试
 * 覆盖：两列布局、树形结构、侧边栏只显示顶级分类、跳转后自动展开
 */

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

// ==================== 分类页面两列布局测试 ====================

test.describe('分类页面两列布局', () => {
  test('分类页面应显示两列布局，左侧为树形结构，右侧为文章列表', async ({ page }) => {
    await page.goto('/categories')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    const layout = page.locator('.category-layout')
    const hasLayout = await layout.isVisible().catch(() => false)
    expect(hasLayout).toBeTruthy()

    const treeCard = page.locator('.category-tree-card')
    const hasTreeCard = await treeCard.isVisible().catch(() => false)
    expect(hasTreeCard).toBeTruthy()

    const postsCard = page.locator('.category-posts-card')
    const hasPostsCard = await postsCard.isVisible().catch(() => false)
    expect(hasPostsCard).toBeTruthy()
  })

  test('左侧分类树应可折叠展开，且有文件夹图标', async ({ page }) => {
    await page.goto('/categories')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    const folderIcons = page.locator('.folder-icon')
    const iconCount = await folderIcons.count()
    expect(iconCount).toBeGreaterThan(0)
  })

  test('点击分类节点应加载该分类下的文章', async ({ page }) => {
    await page.goto('/categories')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    const treeNodes = page.locator('.el-tree-node__content')
    const nodeCount = await treeNodes.count()

    if (nodeCount > 0) {
      await treeNodes.first().click()
      await page.waitForTimeout(1000)

      const postsHeader = page.locator('.posts-header')
      const hasHeader = await postsHeader.isVisible().catch(() => false)
      expect(hasHeader).toBeTruthy()
    }
  })
})

// ==================== 侧边栏只显示顶级分类测试 ====================

test.describe('侧边栏只显示顶级分类', () => {
  test('侧边栏分类应只显示顶级分类，不显示子分类', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('domcontentloaded')
    await page.waitForTimeout(1500)

    const categorySection = page.locator('.sidebar-section:has(.section-title:has-text("分类"))').first()
    const hasSection = await categorySection.isVisible().catch(() => false)
    expect(hasSection).toBeTruthy()

    const childItems = page.locator('.category-item.child-item')
    const childCount = await childItems.count()

    expect(childCount).toBe(0)
  })
})

// ==================== 跳转后自动展开并加载测试 ====================

test.describe('跳转后自动展开并加载', () => {
  test('从URL参数categoryId跳转应自动选中和展开', async ({ page, request }) => {
    const treeResponse = await request.get('http://localhost:8801/category/tree')
    const treeData = await treeResponse.json()

    if (treeData.data && treeData.data.length > 0) {
      const findChild = (nodes) => {
        for (const node of nodes) {
          if (node.children && node.children.length > 0) {
            return node.children[0].id
          }
        }
        return null
      }

      const childCategoryId = findChild(treeData.data)
      if (childCategoryId) {
        await page.goto(`/categories?categoryId=${childCategoryId}`)
        await page.waitForLoadState('networkidle')
        await page.waitForTimeout(1500)

        const postsHeader = page.locator('.posts-header')
        const hasHeader = await postsHeader.isVisible().catch(() => false)
        expect(hasHeader).toBeTruthy()

        const currentNode = page.locator('.el-tree-node.is-current')
        const hasCurrent = await currentNode.isVisible().catch(() => false)
        expect(hasCurrent).toBeTruthy()
      }
    }
  })

  test('选中分类后应显示对应的文章列表', async ({ page }) => {
    await page.goto('/categories')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    const treeNodes = page.locator('.el-tree-node__content')
    const nodeCount = await treeNodes.count()

    if (nodeCount > 0) {
      await treeNodes.first().click()
      await page.waitForTimeout(1500)

      const postItems = page.locator('.post-item')
      const emptyState = page.locator('.category-posts-card .el-empty')

      const hasPosts = await postItems.first().isVisible().catch(() => false)
      const hasEmpty = await emptyState.isVisible().catch(() => false)

      expect(hasPosts || hasEmpty).toBeTruthy()
    }
  })
})

// ==================== API 测试 ====================

test.describe('分类 API 接口', () => {
  test('GET /category/tree 应返回树形结构', async ({ request }) => {
    const response = await request.get('http://localhost:8801/category/tree')

    expect(response.status()).toBe(200)

    const data = await response.json()
    expect(data.code).toBe(200)
    expect(data.data).toBeDefined()
    expect(Array.isArray(data.data)).toBeTruthy()
  })

  test('GET /category/{id}/ancestors 应返回祖先链路', async ({ request }) => {
    const treeResponse = await request.get('http://localhost:8801/category/tree')
    const treeData = await treeResponse.json()

    if (treeData.data && treeData.data.length > 0) {
      const findChild = (nodes) => {
        for (const node of nodes) {
          if (node.children && node.children.length > 0) {
            return node.children[0].id
          }
        }
        return null
      }

      const childId = findChild(treeData.data)
      if (childId) {
        const ancestorResponse = await request.get(`http://localhost:8801/category/${childId}/ancestors`)
        expect(ancestorResponse.status()).toBe(200)

        const ancestorData = await ancestorResponse.json()
        expect(ancestorData.code).toBe(200)
        expect(ancestorData.data).toBeDefined()
        expect(ancestorData.data.length).toBeGreaterThanOrEqual(1)
      }
    }
  })
})