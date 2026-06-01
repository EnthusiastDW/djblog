<template>
  <div class="admin-posts">
    <div class="page-header">
      <h2 class="page-title">文章管理</h2>
      <el-button type="primary" @click="router.push('/admin/posts/write')">
        <el-icon><Plus /></el-icon>
        写文章
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="关键词">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索标题"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 120px;">
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已下架" value="UNPUBLISHED" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filters.categoryId" placeholder="全部分类" clearable style="width: 150px;">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="文章列表" name="list">
          <el-table :data="posts" v-loading="loading" style="width: 100%">
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="categoryName" label="分类" width="120" />
            <el-table-column label="标签" min-width="150">
              <template #default="{ row }">
                <el-tag v-for="tag in row.tags" :key="tag.id" size="small" style="margin-right: 4px;">
                  {{ tag.name }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="同步" width="220">
              <template #default="{ row }">
                <div class="sync-indicators">
                  <template v-for="p in platformList" :key="p.code">
                    <!-- 已同步且有外部链接：显示为超链接 -->
                    <a
                      v-if="getSyncBadgeClass(row, p.code) === 'synced' && row.syncUrls?.[p.code]"
                      :href="row.syncUrls[p.code]"
                      target="_blank"
                      class="sync-badge synced-link"
                      :title="`已同步到${p.label}`"
                    >
                      {{ p.short }}
                    </a>
                    <!-- 同步中：禁用 -->
                    <span
                      v-else-if="isSyncing(row, p.code)"
                      class="sync-badge syncing disabled"
                    >
                      ...
                    </span>
                    <!-- 已同步但无外部链接（兜底）：显示为不可点击的已同步标识 -->
                    <span
                      v-else-if="getSyncBadgeClass(row, p.code) === 'synced'"
                      class="sync-badge synced"
                      title="已同步"
                    >
                      {{ p.short }}
                    </span>
                    <!-- 未同步：可点击触发同步 -->
                    <span
                      v-else
                      class="sync-badge not-synced"
                      :title="`点击同步到${p.label}`"
                      @click.stop="handlePlatformSync(row, p.code)"
                    >
                      {{ p.short }}
                    </span>
                  </template>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="viewCount" label="浏览" width="80" />
            <el-table-column prop="createdAt" label="创建时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEdit(row)">
                  编辑
                </el-button>
                <el-button type="primary" link @click="handleView(row)">
                  查看
                </el-button>
                <el-popconfirm title="确定删除该文章吗？" @confirm="handleDelete(row)">
                  <template #reference>
                    <el-button type="danger" link>删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              layout="total, prev, pager, next"
              @current-change="handlePageChange"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="回收站" name="trash">
          <el-table :data="deletedPosts" v-loading="trashLoading" style="width: 100%">
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="categoryName" label="分类" width="120" />
            <el-table-column prop="updatedAt" label="删除时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.updatedAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleRestore(row)">
                  恢复
                </el-button>
                <el-popconfirm title="彻底删除后无法恢复，确定要删除吗？" @confirm="handlePermanentDelete(row)">
                  <template #reference>
                    <el-button type="danger" link>彻底删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="trashCurrentPage"
              :page-size="pageSize"
              :total="trashTotal"
              layout="total, prev, pager, next"
              @current-change="handleTrashPageChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { postApi } from '@/api/post'
import { categoryApi } from '@/api/category'
import { formatDate } from '@/utils/format'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

const activeTab = ref('list')
const posts = ref([])
const deletedPosts = ref([])
const categories = ref([])
const loading = ref(false)
const trashLoading = ref(false)
const currentPage = ref(1)
const trashCurrentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const trashTotal = ref(0)

const filters = reactive({
  keyword: '',
  status: '',
  categoryId: null
})

// 平台同步相关
const platformList = [
  { code: 'CNBLOG', short: '博', label: '博客园' }
]
const syncingMap = ref({})

function isSyncing(row, platformCode) {
  return !!syncingMap.value[`${row.id}:${platformCode}`]
}

function getSyncBadgeClass(row, platformCode) {
  if (isSyncing(row, platformCode)) return 'syncing'
  if (!row.syncPlatforms) return 'not-synced'
  const synced = row.syncPlatforms.split(',').includes(platformCode)
  return synced ? 'synced' : 'not-synced'
}

function getSyncTitle(row, platformCode) {
  const platform = platformList.find(p => p.code === platformCode)
  if (isSyncing(row, platformCode)) return `正在同步到${platform.label}...`
  if (!row.syncPlatforms) return `点击同步到${platform.label}`
  const synced = row.syncPlatforms.split(',').includes(platformCode)
  if (synced) return `已同步到${platform.label}`
  return `点击同步到${platform.label}`
}

async function handlePlatformSync(row, platformCode) {
  const key = `${row.id}:${platformCode}`
  if (syncingMap.value[key]) return
  syncingMap.value[key] = true
  try {
    const res = await postApi.syncToPlatform(row.id, platformCode)
    if (res.data.success) {
      ElMessage.success(`同步到${platformList.find(p => p.code === platformCode).label}成功`)
      fetchPosts()
    } else {
      ElMessage.error(`同步失败: ${res.data.errorMessage || '未知错误'}`)
    }
  } catch (e) {
    ElMessage.error('同步请求失败')
  } finally {
    syncingMap.value[key] = false
  }
}

async function fetchCategories() {
  try {
    const res = await categoryApi.getList({ size: 100 })
    categories.value = res.data.records || []
  } catch (e) {
    console.error('获取分类失败', e)
  }
}

async function fetchPosts() {
  loading.value = true
  try {
    let res
    if (filters.keyword) {
      res = await postApi.search({
        keyword: filters.keyword,
        current: currentPage.value,
        size: pageSize.value,
        status: filters.status
      })
    } else {
      res = await postApi.getAdminList({
        current: currentPage.value,
        size: pageSize.value,
        status: filters.status || undefined,
        categoryId: filters.categoryId || undefined
      })
    }
    posts.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('获取文章列表失败', e)
  } finally {
    loading.value = false
  }
}

async function fetchDeletedPosts() {
  trashLoading.value = true
  try {
    const res = await postApi.getDeletedList({
      current: trashCurrentPage.value,
      size: pageSize.value
    })
    deletedPosts.value = res.data.records || []
    trashTotal.value = res.data.total || 0
  } catch (e) {
    console.error('获取已删除文章列表失败', e)
  } finally {
    trashLoading.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchPosts()
}

function handleTrashPageChange(page) {
  trashCurrentPage.value = page
  fetchDeletedPosts()
}

function handleSearch() {
  currentPage.value = 1
  fetchPosts()
}

function handleReset() {
  filters.keyword = ''
  filters.status = ''
  filters.categoryId = null
  currentPage.value = 1
  fetchPosts()
}

function handleTabChange(tab) {
  if (tab === 'trash') {
    fetchDeletedPosts()
  } else {
    fetchPosts()
  }
}

function handleEdit(row) {
  router.push(`/admin/posts/edit/${row.id}`)
}

function handleView(row) {
  window.open(`/post/${row.id}`, '_blank')
}

async function handleDelete(row) {
  try {
    await postApi.delete([row.id])
    ElMessage.success('删除成功')
    fetchPosts()
  } catch (e) {
    console.error('删除失败', e)
  }
}

async function handleRestore(row) {
  try {
    await postApi.restore([row.id])
    ElMessage.success('恢复成功')
    fetchDeletedPosts()
  } catch (e) {
    console.error('恢复失败', e)
  }
}

async function handlePermanentDelete(row) {
  try {
    await postApi.permanentDelete([row.id])
    ElMessage.success('彻底删除成功')
    fetchDeletedPosts()
  } catch (e) {
    console.error('彻底删除失败', e)
  }
}

function getStatusType(status) {
  const statusMap = {
    'PUBLISHED': 'success',
    'DRAFT': 'info',
    'UNPUBLISHED': 'warning',
    'DELETED': 'danger'
  }
  return statusMap[status] || 'info'
}

function getStatusText(status) {
  const statusMap = {
    'PUBLISHED': '已发布',
    'DRAFT': '草稿',
    'UNPUBLISHED': '已下架',
    'DELETED': '已删除'
  }
  return statusMap[status] || '未知'
}

onMounted(() => {
  fetchCategories()
  fetchPosts()
})
</script>

<style lang="scss" scoped>
.admin-posts {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .filter-card {
    margin-bottom: 16px;
  }

  .filter-form {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .sync-indicators {
    display: flex;
    gap: 4px;
    align-items: center;

    .sync-badge {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 22px;
      height: 22px;
      border-radius: 4px;
      font-size: 11px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;

      &.syncing {
        background: var(--el-color-warning-light-9);
        color: var(--el-color-warning);
        border: 1px solid var(--el-color-warning-light-5);
      }

      &.synced {
        background: var(--el-color-success-light-8);
        color: var(--el-color-success);
        border: 1px solid var(--el-color-success-light-5);
      }

      &.synced-link {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 22px;
        height: 22px;
        border-radius: 4px;
        font-size: 11px;
        font-weight: 600;
        text-decoration: none;
        background: var(--el-color-success-light-8);
        color: var(--el-color-success);
        border: 1px solid var(--el-color-success-light-5);
        transition: all 0.2s;

        &:hover {
          background: var(--el-color-success-light-7);
          text-decoration: underline;
        }
      }

      &.not-synced {
        background: var(--el-color-info-light-9);
        color: var(--el-color-info);
        border: 1px solid var(--el-color-info-light-7);

        &:hover {
          background: var(--el-color-primary-light-9);
          color: var(--el-color-primary);
          border-color: var(--el-color-primary-light-5);
        }
      }

      &.disabled {
        opacity: 0.5;
        cursor: not-allowed;
        pointer-events: none;
      }
    }
  }
}
</style>
