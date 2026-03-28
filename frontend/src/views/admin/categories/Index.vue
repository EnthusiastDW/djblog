<template>
  <div class="admin-categories">
    <div class="page-header">
      <h2 class="page-title">分类管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增分类
      </el-button>
    </div>

    <el-card>
      <el-table :data="categories" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该分类吗？" @confirm="handleDelete(row)">
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入分类描述" />
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
import { categoryApi } from '@/api/category'
import { formatDate } from '@/utils/format'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const categories = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  name: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

async function fetchCategories() {
  loading.value = true
  try {
    const res = await categoryApi.getList({
      current: currentPage.value,
      size: pageSize.value
    })
    categories.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('获取分类列表失败', e)
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchCategories()
}

function handleAdd() {
  isEdit.value = false
  form.id = null
  form.name = ''
  form.description = ''
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.description = row.description || ''
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await categoryApi.update(form)
    } else {
      await categoryApi.create(form)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    fetchCategories()
  } catch (e) {
    console.error('操作失败', e)
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await categoryApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchCategories()
  } catch (e) {
    console.error('删除失败', e)
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<style lang="scss" scoped>
.admin-categories {
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
