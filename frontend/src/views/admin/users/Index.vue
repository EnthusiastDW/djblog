<template>
  <div class="admin-users">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
    </div>

    <el-card>
      <el-table :data="users" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'">
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该用户吗？" @confirm="handleDelete(row)">
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

    <el-dialog v-model="dialogVisible" title="编辑用户" width="500px">
      <el-form :model="form" ref="formRef" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width: 100%;">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
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
import { userApi } from '@/api/user'
import { formatDate } from '@/utils/format'
import { ElMessage } from 'element-plus'

const users = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  id: null,
  username: '',
  email: '',
  role: 'USER'
})

async function fetchUsers() {
  loading.value = true
  try {
    const res = await userApi.getList({
      current: currentPage.value,
      size: pageSize.value
    })
    users.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('获取用户列表失败', e)
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchUsers()
}

function handleEdit(row) {
  form.id = row.id
  form.username = row.username
  form.email = row.email || ''
  form.role = row.role || 'USER'
  dialogVisible.value = true
}

async function handleSubmit() {
  submitting.value = true
  try {
    await userApi.update(form)
    ElMessage.success('更新成功')
    dialogVisible.value = false
    fetchUsers()
  } catch (e) {
    console.error('更新失败', e)
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await userApi.delete({ id: row.id })
    ElMessage.success('删除成功')
    fetchUsers()
  } catch (e) {
    console.error('删除失败', e)
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style lang="scss" scoped>
.admin-users {
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
