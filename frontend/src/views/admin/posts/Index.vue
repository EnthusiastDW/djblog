<template>
  <div class="admin-posts">
    <div class="page-header">
      <h2 class="page-title">文章管理</h2>
      <el-button type="primary" @click="router.push('/admin/posts/write')">
        <el-icon><Plus /></el-icon>
        写文章
      </el-button>
    </div>

    <!-- 搜索筛选区 -->
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

const posts = ref([])
const categories = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filters = reactive({
  keyword: '',
  status: '',
  categoryId: null
})

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

function handlePageChange(page) {
  currentPage.value = page
  fetchPosts()
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

function getStatusType(status) {
  const statusMap = {
    'PUBLISHED': 'success',
    'DRAFT': 'info',
    'UNPUBLISHED': 'warning'
  }
  return statusMap[status] || 'info'
}

function getStatusText(status) {
  const statusMap = {
    'PUBLISHED': '已发布',
    'DRAFT': '草稿',
    'UNPUBLISHED': '已下架'
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
}
</style>
