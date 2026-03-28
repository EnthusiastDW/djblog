<template>
  <div class="admin-comments">
    <div class="page-header">
      <h2 class="page-title">评论管理</h2>
    </div>

    <el-card>
      <el-table :data="comments" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="nickname" label="用户" width="120" />
        <el-table-column prop="content" label="内容" min-width="250">
          <template #default="{ row }">
            <div class="comment-content text-ellipsis-2">{{ row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'APPROVED' ? 'success' : row.status === 'PENDING' ? 'warning' : 'danger'">
              {{ row.status === 'APPROVED' ? '已通过' : row.status === 'PENDING' ? '待审核' : '已拒绝' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button type="success" link @click="handleAudit(row, 'APPROVED')">通过</el-button>
              <el-button type="warning" link @click="handleAudit(row, 'REJECTED')">拒绝</el-button>
            </template>
            <el-popconfirm title="确定删除该评论吗？" @confirm="handleDelete(row)">
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
import { ref, onMounted } from 'vue'
import { commentApi } from '@/api/comment'
import { formatDate } from '@/utils/format'
import { ElMessage } from 'element-plus'

const comments = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function fetchComments() {
  loading.value = true
  try {
    const res = await commentApi.getList({
      current: currentPage.value,
      size: pageSize.value
    })
    comments.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('获取评论列表失败', e)
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchComments()
}

async function handleAudit(row, status) {
  try {
    await commentApi.audit({ id: row.id, status })
    ElMessage.success('审核成功')
    fetchComments()
  } catch (e) {
    console.error('审核失败', e)
  }
}

async function handleDelete(row) {
  try {
    await commentApi.delete({ idList: [row.id] })
    ElMessage.success('删除成功')
    fetchComments()
  } catch (e) {
    console.error('删除失败', e)
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  fetchComments()
})
</script>

<style lang="scss" scoped>
.admin-comments {
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

  .comment-content {
    max-width: 300px;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
