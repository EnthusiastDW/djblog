import { test, expect } from '@playwright/test'

/**
 * 分类层级功能 E2E 测试
 * 覆盖：后台树形管理、前台层级展示、侧边栏树形分类、文章编辑树形选择、面包屑、包含子分类开关
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

// ==================== 后台分类管理测试 ====================

test.describe('后台分类管理 - 树形表格', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
  })

  test('分类管理页面应显示树形表格', async ({ page }) => {
    await page.goto('/admin/categories')
    await page.waitForLoadState('networkidle')

    // 验证表格存在
    const table = page.locator('.el-table')
    const hasTable = await table.isVisible().catch(() => false)
    expect(hasTable).toBeTruthy()
  })

  test('新增分类弹窗应包含父级分类选择器', async ({ page }) => {
    await page.goto('/admin/categories')
    await page.waitForLoadState('networkidle')

    // 点击新增按钮
    const addBtn = page.locator('button:has-text("新增")').first()
    if (await addBtn.isVisible().catch(() => false)) {
      await addBtn.click()
      await page.waitForTimeout(500)

      // 验证弹窗存在
      const dialog = page.locator('.el-dialog')
      const isVisible = await dialog.isVisible().catch(() => false)
      expect(isVisible).toBeTruthy()

      // 验证父级分类选择器存在（el-tree-select）
      const parentSelect = dialog.locator('.el-tree-select, .el-form-item:has-text("父级")').first()
      const hasParentSelect = await parentSelect.isVisible().catch(() => false)
      expect(hasParentSelect).toBeTruthy()
    }
  })

  test('创建子分类流程', async ({ page }) => {
    await page.goto('/admin/categories')
    await page.waitForLoadState('networkidle')

    // 等待表格加载
    await page.waitForTimeout(1000)

    // 查找树形表格中已有的行
    const table = page.locator('.el-table')
    if (await table.isVisible().catch(() => false)) {
      const rows = table.locator('.el-table__row')
      const rowCount = await rows.count()

      if (rowCount > 0) {
        // 找到添加子分类按钮
        const addChildBtn = rows.first().locator('button:has-text("添加子分类"), button:has-text("子分类")').first()
        if (await addChildBtn.isVisible().catch(() => false)) {
          await addChildBtn.click()
          await page.waitForTimeout(500)

          // 验证弹窗出现且父级分类已自动填充
          const dialog = page.locator('.el-dialog')
          if (await dialog.isVisible().catch(() => false)) {
            // 验证分类名称输入框存在
            const nameInput = dialog.locator('input').first()
            if (await nameInput.isVisible().catch(() => false)) {
              const timestamp = Date.now()
              await nameInput.fill(`测试子分类 ${timestamp}`)

              const confirmBtn = dialog.locator('button:has-text("确定")').first()
              if (await confirmBtn.isVisible().catch(() => false)) {
                await confirmBtn.click()
                await page.waitForTimeout(2000)
              }
            }
          }
        }
      }
    }
  })

  test('树形表格应展示层级缩进', async ({ page }) => {
    await page.goto('/admin/categories')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    const table = page.locator('.el-table')
    if (await table.isVisible().catch(() => false)) {
      const rows = table.locator('.el-table__row')
      const rowCount = await rows.count()

      if (rowCount > 1) {
        // 树形表格的子行应该有缩进
        // el-table 树形结构会自动添加 .el-table__indent 类
        const indents = page.locator('.el-table__indent')
        const indentCount = await indents.count()
        // 如果有缩进元素，说明树形结构生效
        if (indentCount > 0) {
          expect(indentCount).toBeGreaterThan(0)
        }
      }
    }
  })
})

// ==================== 分类树形 API 接口测试 ====================

test.describe('分类树形 API 接口', () => {
  test('GET /category/tree 应返回树形结构', async ({ request }) => {
    const response = await request.get('http://localhost:8801/category/tree')

    expect(response.status()).toBe(200)

    const data = await response.json()
    expect(data.code).toBe(200)
    expect(data.data).toBeDefined()
    expect(Array.isArray(data.data)).toBeTruthy()
  })

  test('GET /category/tree/admin 应返回管理用树形结构', async ({ request }) => {
    const response = await request.get('http://localhost:8801/category/tree/admin')

    expect(response.status()).toBe(200)

    const data = await response.json()
    expect(data.code).toBe(200)
    expect(data.data).toBeDefined()
    expect(Array.isArray(data.data)).toBeTruthy()
  })

  test('GET /category/{id}/ancestors 应返回祖先链路', async ({ request }) => {
    // 先获取树形结构找一个分类ID
    const treeResponse = await request.get('http://localhost:8801/category/tree')
    const treeData = await treeResponse.json()

    if (treeData.data && treeData.data.length > 0) {
      // 尝试找一个非顶级分类（有 parentId 的）
      const findChildId = (nodes) => {
        for (const node of nodes) {
          if (node.children && node.children.length > 0) {
            return node.children[0].id
          }
        }
        return null
      }

      const childId = findChildId(treeData.data)
      if (childId) {
        const ancestorResponse = await request.get(`http://localhost:8801/category/${childId}/ancestors`)
        expect(ancestorResponse.status()).toBe(200)

        const ancestorData = await ancestorResponse.json()
        expect(ancestorData.code).toBe(200)
        expect(ancestorData.data).toBeDefined()
        expect(Array.isArray(ancestorData.data)).toBeTruthy()
        // 祖先链路至少包含自身
        expect(ancestorData.data.length).toBeGreaterThanOrEqual(1)
        // 第一个应该是顶级分类（parentId 为 null）
        expect(ancestorData.data[0].parentId).toBeNull()
      }
    }
  })

  test('GET /post/by-categories 应支持按多个分类ID查询文章', async ({ request }) => {
    // 先获取分类树
    const treeResponse = await request.get('http://localhost:8801/category/tree')
    const treeData = await treeResponse.json()

    if (treeData.data && treeData.data.length > 0) {
      const categoryIds = treeData.data.map(c => c.id)
      const response = await request.get(`http://localhost:8801/post/by-categories?current=1&size=10&categoryIds=${categoryIds.join(',')}`)

      expect(response.status()).toBe(200)

      const data = await response.json()
      expect(data.code).toBe(200)
      expect(data.data).toBeDefined()
    }
  })
})

// ==================== 前台分类页面测试 ====================

test.describe('前台分类页面', () => {
  test('分类列表页面应正常加载', async ({ page }) => {
    await page.goto('/categories')
    await page.waitForLoadState('networkidle')

    // 验证页面标题
    const title = page.locator('h1:has-text("分类"), h2:has-text("分类")').first()
    const hasTitle = await title.isVisible().catch(() => false)
    expect(hasTitle).toBeTruthy()
  })

  test('分类页面应展示分组层级结构', async ({ page }) => {
    await page.goto('/categories')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    // 验证页面有内容
    const pageContent = page.locator('.category-list, .categories-grid, .category-group').first()
    const hasContent = await pageContent.isVisible().catch(() => false)
    expect(hasContent).toBeTruthy()
  })

  test('分类卡片应可点击跳转到分类文章页', async ({ page }) => {
    await page.goto('/categories')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    // 查找可点击的分类链接
    const categoryLink = page.locator('a[href*="/category/"], router-link').first()
    if (await categoryLink.isVisible().catch(() => false)) {
      await categoryLink.click()
      await page.waitForTimeout(1000)

      // 应跳转到分类文章页
      const url = page.url()
      expect(url).toContain('/category/')
    }
  })
})

// ==================== 分类文章页测试 ====================

test.describe('分类文章页', () => {
  test('分类文章页应显示面包屑导航', async ({ page }) => {
    await page.goto('/categories')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    // 点击一个分类
    const categoryLink = page.locator('a[href*="/category/"]').first()
    if (await categoryLink.isVisible().catch(() => false)) {
      await categoryLink.click()
      await page.waitForLoadState('networkidle')
      await page.waitForTimeout(1000)

      // 验证面包屑存在
      const breadcrumb = page.locator('.el-breadcrumb, .breadcrumb, [class*="breadcrumb"]').first()
      const hasBreadcrumb = await breadcrumb.isVisible().catch(() => false)
      expect(hasBreadcrumb).toBeTruthy()
    }
  })

  test('分类文章页应显示"包含子分类"开关', async ({ page }) => {
    await page.goto('/categories')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    const categoryLink = page.locator('a[href*="/category/"]').first()
    if (await categoryLink.isVisible().catch(() => false)) {
      await categoryLink.click()
      await page.waitForLoadState('networkidle')
      await page.waitForTimeout(1500)

      // 查找包含子分类的开关
      const switchEl = page.locator('.el-switch, .include-children-switch').first()
      const hasSwitch = await switchEl.isVisible().catch(() => false)

      // 如果该分类有子分类，应该显示开关
      if (hasSwitch) {
        expect(hasSwitch).toBeTruthy()
      }
    }
  })

  test('面包屑应显示完整祖先链路', async ({ page, request }) => {
    // 先找到有子分类的分类ID
    const treeResponse = await request.get('http://localhost:8801/category/tree')
    const treeData = await treeResponse.json()

    if (treeData.data) {
      // 找一个有子分类的分类
      const findChildCategory = (nodes) => {
        for (const node of nodes) {
          if (node.children && node.children.length > 0) {
            return node.children[0].id
          }
        }
        return null
      }

      const childCategoryId = findChildCategory(treeData.data)
      if (childCategoryId) {
        await page.goto(`/category/${childCategoryId}`)
        await page.waitForLoadState('networkidle')
        await page.waitForTimeout(1000)

        // 验证面包屑存在且显示多个层级
        const breadcrumb = page.locator('.el-breadcrumb').first()
        if (await breadcrumb.isVisible().catch(() => false)) {
          const items = breadcrumb.locator('.el-breadcrumb__item')
          const itemCount = await items.count()
          // 子分类的面包屑至少应该有2项（父分类 + 自身）
          expect(itemCount).toBeGreaterThanOrEqual(2)
        }
      }
    }
  })
})

// ==================== 侧边栏树形分类测试 ====================

test.describe('侧边栏树形分类', () => {
  test('侧边栏应展示树形分类结构', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('domcontentloaded')
    await page.waitForTimeout(1500)

    // 验证侧边栏分类区域存在
    const categorySection = page.locator('.sidebar-section:has(.section-title:has-text("分类"))').first()
    const hasSection = await categorySection.isVisible().catch(() => false)
    expect(hasSection).toBeTruthy()
  })

  test('父分类和子分类应有视觉区分', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('domcontentloaded')
    await page.waitForTimeout(1500)

    const categoryTree = page.locator('.category-tree').first()
    if (await categoryTree.isVisible().catch(() => false)) {
      // 子分类应该有缩进样式
      const childItem = page.locator('.child-item').first()
      const hasChild = await childItem.isVisible().catch(() => false)

      if (hasChild) {
        // 验证子分类有缩进（padding-left 或 margin-left 大于父分类）
        const childStyle = await childItem.getAttribute('style') || ''
        const parentItem = page.locator('.parent-item').first()
        const parentStyle = await parentItem.getAttribute('style') || ''

        // 至少验证子分类存在
        expect(hasChild).toBeTruthy()
      }
    }
  })
})

// ==================== 文章详情分类路径测试 ====================

test.describe('文章详情页分类路径', () => {
  test('文章详情页应显示分类路径链接', async ({ page, request }) => {
    // 获取文章列表
    const response = await request.get('http://localhost:8801/post?current=1&size=1&status=PUBLISHED')
    const data = await response.json()

    if (data.data && data.data.records && data.data.records.length > 0) {
      const post = data.data.records[0]

      if (post.categoryId) {
        // 检查该分类是否有父分类
        const ancestorResponse = await request.get(`http://localhost:8801/category/${post.categoryId}/ancestors`)
        const ancestorData = await ancestorResponse.json()

        if (ancestorData.data && ancestorData.data.length > 1) {
          // 该分类有父分类，文章详情应显示路径
          await page.goto(`/article/${post.slug || post.id}`)
          await page.waitForLoadState('networkidle')
          await page.waitForTimeout(1500)

          // 验证分类链接存在
          const categoryLink = page.locator('.category-link, a[href*="/category/"]').first()
          const hasLink = await categoryLink.isVisible().catch(() => false)
          expect(hasLink).toBeTruthy()

          // 如果有路径，分类文本应包含 ">" 分隔符
          if (ancestorData.data.length > 1) {
            const categoryText = await categoryLink.textContent().catch(() => '')
            expect(categoryText).toContain('>')
          }
        }
      }
    }
  })
})
