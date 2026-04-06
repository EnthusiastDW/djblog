<template>
  <div class="admin-categories">
    <div class="page-header">
      <h2 class="page-title">分类管理</h2>
      <el-button type="primary" @click="handleAdd(null)">
        <el-icon><Plus /></el-icon>
        新增顶级分类
      </el-button>
    </div>

    <el-card>
      <el-table
        :data="categoryTree"
        v-loading="loading"
        row-key="id"
        style="width: 100%"
      >
        <el-table-column prop="name" label="名称" min-width="200">
          <template #default="{ row }">
            <span class="category-name-cell">
              {{ row.name }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="250">
          <template #default="{ row }">
            <span class="text-ellipsis">{{ row.description || '暂无描述' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="文章数" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.postCount || 0 }}</el-tag>
            <span v-if="row.totalPostCount !== row.postCount && row.totalPostCount" class="total-count">
              / {{ row.totalPostCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleAddChild(row)">
              <el-icon><Plus /></el-icon>
              子分类
            </el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm
              :title="`确定删除分类「${row.name}」吗？删除前请确保没有子分类和关联文章。`"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="父级分类" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentCategoryOptions"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="无（顶级分类）"
            check-strictly
            clearable
            :render-after-expand="false"
            filterable
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述（可选）"
          />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { categoryApi } from '@/api/category'
import { Plus, Folder } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const categoryTree = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const dialogTitle = ref('新增分类')

const form = reactive({
  id: null,
  name: '',
  description: '',
  parentId: null
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

/**
 * 计算父级分类选项（编辑时排除自身及其后代）
 */
const parentCategoryOptions = computed(() => {
  if (!form.id) {
    return categoryTree.value
  }
  // 编辑时，需要排除自身及其后代
  const excludeIds = new Set()
  collectIds(categoryTree.value, form.id, excludeIds)
  return filterTree(categoryTree.value, excludeIds)
})

computed(() => {
  const result = []

  function flatten(nodes, level = 0) {
    for (const node of nodes) {
      result.push({
        ...node,
        _level: level
      })
      if (node.children && node.children.length > 0) {
        flatten(node.children, level + 1)
      }
    }
  }

  flatten(categoryTree.value)
  return result
});

/**
 * 递归收集目标节点及其所有子节点ID
 */
function collectIds(nodes, targetId, idSet) {
  for (const node of nodes) {
    if (node.id === targetId) {
      idSet.add(node.id)
      collectAllChildIds(node.children || [], idSet)
      return true
    }
    if (node.children && collectIds(node.children, targetId, idSet)) {
      return true
    }
  }
  return false
}

function collectAllChildIds(children, idSet) {
  for (const child of children) {
    idSet.add(child.id)
    if (child.children) {
      collectAllChildIds(child.children, idSet)
    }
  }
}

/**
 * 过滤树，排除指定ID的节点
 */
function filterTree(nodes, excludeIds) {
  return nodes
    .filter(node => !excludeIds.has(node.id))
    .map(node => ({
      ...node,
      children: node.children ? filterTree(node.children, excludeIds) : undefined
    }))
}

async function fetchCategories() {
  loading.value = true
  try {
    const res = await categoryApi.getTreeForAdmin()
    categoryTree.value = res.data || []
  } catch (e) {
    console.error('获取分类列表失败', e)
  } finally {
    loading.value = false
  }
}

function handleAdd(parentId) {
  dialogTitle.value = '新增顶级分类'
  form.id = null
  form.name = ''
  form.description = ''
  form.parentId = parentId || null
  dialogVisible.value = true
}

function handleAddChild(row) {
  dialogTitle.value = `在「${row.name}」下添加子分类`
  form.id = null
  form.name = ''
  form.description = ''
  form.parentId = row.id
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑分类'
  form.id = row.id
  form.name = row.name
  form.description = row.description || ''
  form.parentId = row.parentId || null
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (form.id) {
      await categoryApi.update(form)
      ElMessage.success('更新成功')
    } else {
      await categoryApi.create(form)
      ElMessage.success('创建成功')
    }
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
    ElMessage.error(e.response?.data?.message || '删除失败')
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

  .total-count {
    color: var(--el-text-color-placeholder);
    font-size: 12px;
    margin-left: 4px;
  }
}
</style>
