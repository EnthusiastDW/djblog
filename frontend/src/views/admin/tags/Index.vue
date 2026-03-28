<template>
  <div class="admin-tags">
    <div class="page-header">
      <h2 class="page-title">标签管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增标签
      </el-button>
    </div>

    <el-card>
      <el-table :data="tags" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确定删除该标签吗？" @confirm="handleDelete(row)">
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

    <el-dialog v-model="dialogVisible" title="新增标签" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { tagApi } from '@/api/tag'
import { formatDate } from '@/utils/format'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const tags = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  name: ''
})

const rules = {
  name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }]
}

async function fetchTags() {
  loading.value = true
  try {
    const res = await tagApi.getList({
      current: currentPage.value,
      size: pageSize.value
    })
    tags.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('获取标签列表失败', e)
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchTags()
}

function handleAdd() {
  form.name = ''
  dialogVisible.value = true
}



async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await tagApi.create(form)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    fetchTags()
  } catch (e) {
    console.error('操作失败', e)
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await tagApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchTags()
  } catch (e) {
    console.error('删除失败', e)
    ElMessage.error('删除失败，该标签可能有关联的文章')
  }
}

onMounted(() => {
  fetchTags()
})
</script>

<style lang="scss" scoped>
.admin-tags {
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

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
