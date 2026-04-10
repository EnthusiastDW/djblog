<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="父级分类" prop="parentId">
        <el-tree-select
          v-model="form.parentId"
          :data="categoryTree"
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
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { categoryApi } from '@/api/category'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  categoryTree: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const dialogTitle = ref('新增分类')

const form = reactive({
  name: '',
  description: '',
  parentId: null
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    resetForm()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

function resetForm() {
  form.name = ''
  form.description = ''
  form.parentId = null
  dialogTitle.value = '新增分类'
  
  // 清除验证状态
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

function handleClose() {
  visible.value = false
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await categoryApi.create(form)
    ElMessage.success('创建成功')
    visible.value = false
    emit('success')
  } catch (e) {
    console.error('创建分类失败', e)
    ElMessage.error(e.response?.data?.message || '创建分类失败')
  } finally {
    submitting.value = false
  }
}
</script>
