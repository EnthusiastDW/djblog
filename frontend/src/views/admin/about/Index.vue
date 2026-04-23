<template>
  <div class="admin-about">
    <div class="page-header">
      <h2 class="page-title">关于我配置</h2>
      <el-button type="primary" @click="handleSave" :loading="saving">
        保存
      </el-button>
    </div>

    <el-card>
      <MarkdownEditor v-model="aboutContent" mode="split" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/api/user'
import { ElMessage } from 'element-plus'
import MarkdownEditor from '@/components/MarkdownEditor.vue'

const aboutContent = ref('')
const saving = ref(false)

async function fetchAboutContent() {
  try {
    const res = await userApi.getAbout()
    aboutContent.value = res.data || ''
  } catch (e) {
    console.error('获取关于内容失败', e)
    ElMessage.error('获取内容失败')
  }
}

async function handleSave() {
  saving.value = true
  try {
    await userApi.updateAbout({ aboutContent: aboutContent.value })
    ElMessage.success('保存成功')
  } catch (e) {
    console.error('保存失败', e)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchAboutContent()
})
</script>

<style lang="scss" scoped>
.admin-about {
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
    margin: 0;
  }
}
</style>
