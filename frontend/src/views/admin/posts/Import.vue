<template>
  <div class="import-posts-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>批量导入文章</h2>
        </div>
      </template>

      <el-alert
        title="使用说明"
        type="info"
        :closable="false"
        style="margin-bottom: 20px;"
      >
        <p>1. 请先使用扫描工具生成 JSON 文件（路径：backend/scripts/scan_external_posts.py）</p>
        <p>2. JSON 文件格式应包含：title, category, tags, content, summary 等字段</p>
        <p>3. 系统会自动创建不存在的分类和标签</p>
        <p>4. 如果 slug 重复，系统会自动添加后缀</p>
      </el-alert>

      <el-form label-width="120px">
        <el-form-item label="选择文件">
          <input
            type="file"
            ref="fileInput"
            accept=".json"
            @change="handleFileChange"
            style="display: none;"
          />
          <el-button @click="$refs.fileInput.click()">选择 JSON 文件</el-button>
          <span v-if="selectedFile" style="margin-left: 10px;">
            {{ selectedFile.name }}
          </span>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="handleImport"
            :loading="importing"
            :disabled="!selectedFile"
          >
            开始导入
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 导入结果 -->
      <el-divider v-if="importResult" />

      <el-result
        v-if="importResult"
        :icon="importResult.successCount > 0 ? 'success' : 'error'"
        :title="importResult.successCount > 0 ? '导入完成' : '导入失败'"
      >
        <template #extra>
          <div class="result-info">
            <p>成功：{{ importResult.successCount }} 篇</p>
            <p>失败：{{ importResult.failCount }} 篇</p>
          </div>
          
          <el-collapse v-if="importResult.errors && importResult.errors.length > 0">
            <el-collapse-item title="查看错误详情">
              <ul class="error-list">
                <li v-for="(error, index) in importResult.errors" :key="index">
                  {{ error }}
                </li>
              </ul>
            </el-collapse-item>
          </el-collapse>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { postApi } from '@/api/post'

const selectedFile = ref(null)
const importing = ref(false)
const importResult = ref(null)

function handleFileChange(event) {
  const file = event.target.files[0]
  if (file) {
    selectedFile.value = file
    importResult.value = null
  }
}

async function handleImport() {
  if (!selectedFile.value) {
    ElMessage.warning('请选择要导入的文件')
    return
  }

  importing.value = true
  importResult.value = null

  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)

    const res = await postApi.import(formData)
    importResult.value = res.data

    if (res.data.successCount > 0) {
      ElMessage.success(`成功导入 ${res.data.successCount} 篇文章`)
    }

    if (res.data.failCount > 0) {
      ElMessage.warning(`有 ${res.data.failCount} 篇文章导入失败，请查看错误详情`)
    }

    // 清空文件选择
    selectedFile.value = null
    document.querySelector('input[type="file"]').value = ''

  } catch (error) {
    console.error('导入失败', error)
    ElMessage.error('导入失败：' + (error.message || '未知错误'))
  } finally {
    importing.value = false
  }
}
</script>

<style lang="scss" scoped>
.import-posts-page {
  max-width: 800px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  h2 {
    margin: 0;
    font-size: 18px;
  }
}

.result-info {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-bottom: 16px;

  p {
    margin: 0;
    font-size: 16px;
  }
}

.error-list {
  margin: 0;
  padding-left: 20px;

  li {
    margin-bottom: 8px;
    color: var(--el-color-danger);
    font-size: 13px;
  }
}
</style>
