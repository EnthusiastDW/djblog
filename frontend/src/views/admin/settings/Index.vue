<template>
  <div class="settings-page">
    <h2 class="page-title">全局设置</h2>
    
    <el-card>
      <template #header>
        <span>背景设置</span>
      </template>
      
      <el-form label-width="100px">
        <el-form-item label="背景图片">
          <el-input v-model="bgImage" placeholder="输入图片URL或上传" clearable>
            <template #append>
              <el-button @click="handleUpload">上传</el-button>
            </template>
          </el-input>
          <div v-if="bgImage" class="preview-box">
            <el-image :src="bgImage" fit="cover" />
          </div>
        </el-form-item>
        
        <el-form-item label="透明度">
          <el-slider v-model="bgOpacity" :min="0" :max="1" :step="0.1" show-stops />
          <span class="opacity-value">{{ bgOpacity }}</span>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api/index'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const bgImage = ref('')
const bgOpacity = ref(0.3)
const saving = ref(false)

async function fetchSettings() {
  try {
    const res = await api.get('/setting')
    if (res.data) {
      bgImage.value = res.data.bgImage || ''
      bgOpacity.value = parseFloat(res.data.bgOpacity) || 0.3
    }
  } catch (e) {
    console.error('获取设置失败', e)
  }
}

async function handleSave() {
  saving.value = true
  try {
    await api.put('/setting', {
      bgImage: bgImage.value,
      bgOpacity: bgOpacity.value.toString()
    })
    ElMessage.success('保存成功')
    appStore.setBackground(bgImage.value, bgOpacity.value)
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

function handleUpload() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = (e) => {
    const file = e.target.files[0]
    if (file) {
      const reader = new FileReader()
      reader.onload = (res) => {
        bgImage.value = res.target.result
      }
      reader.readAsDataURL(file)
    }
  }
  input.click()
}

onMounted(() => {
  fetchSettings()
})
</script>

<style lang="scss" scoped>
.settings-page {
  max-width: 600px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
}

.preview-box {
  margin-top: 12px;
  width: 200px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  
  .el-image {
    width: 100%;
    height: 100%;
  }
}

.opacity-value {
  margin-left: 12px;
  color: var(--el-text-color-secondary);
}
</style>