<template>
  <div class="settings-page">
    <h2 class="page-title">全局设置</h2>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="背景设置" name="bg">
        <el-card>
          <template #header>
            <span>背景设置</span>
          </template>

          <el-form label-width="100px">
            <el-form-item label="轮播间隔">
              <el-slider
                v-model="bgCarouselInterval"
                :min="3"
                :max="30"
                :step="1"
                show-stops
                :marks="{ 3: '3s', 5: '5s', 10: '10s', 15: '15s', 20: '20s', 30: '30s' }"
              />
              <span class="slider-value">{{ bgCarouselInterval }} 秒</span>
            </el-form-item>

            <el-form-item label="透明度">
              <el-slider v-model="bgOpacity" :min="0" :max="1" :step="0.1" show-stops />
              <span class="slider-value">{{ bgOpacity }}</span>
            </el-form-item>

            <el-divider content-position="left">背景图片列表</el-divider>

            <el-form-item label-width="0">
              <div class="image-list">
                <div
                  v-for="(img, idx) in bgImageList"
                  :key="idx"
                  class="image-item"
                >
                  <div class="image-preview">
                    <el-image :src="img" fit="cover" />
                  </div>
                  <div class="image-info">
                    <el-tag v-if="idx === 0" type="success" size="small">首图</el-tag>
                    <span class="image-index">#{{ idx + 1 }}</span>
                  </div>
                  <div class="image-actions">
                    <el-button-group size="small">
                      <el-button
                        :disabled="idx === 0"
                        @click="moveImage(idx, -1)"
                        :icon="ArrowUp"
                        title="上移"
                      />
                      <el-button
                        :disabled="idx === bgImageList.length - 1"
                        @click="moveImage(idx, 1)"
                        :icon="ArrowDown"
                        title="下移"
                      />
                    </el-button-group>
                    <el-button
                      type="danger"
                      size="small"
                      :icon="Delete"
                      circle
                      @click="removeImage(idx)"
                      title="删除"
                    />
                  </div>
                </div>

                <div class="image-item image-item-add" @click="handleAddImage">
                  <el-icon :size="32"><Plus /></el-icon>
                  <span>添加图片</span>
                </div>
              </div>

              <div v-if="bgImageList.length >= 2" class="carousel-hint">
                <el-alert type="info" :closable="false" show-icon>
                  已配置 {{ bgImageList.length }} 张图片，轮播已自动激活，每 {{ bgCarouselInterval }} 秒切换一次
                </el-alert>
              </div>
              <div v-else-if="bgImageList.length === 1" class="carousel-hint">
                <el-alert type="warning" :closable="false" show-icon>
                  仅配置了 1 张图片，轮播未激活。添加至少 2 张图片可开启轮播
                </el-alert>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

    </el-tabs>

    <!-- 添加图片对话框 -->
    <el-dialog v-model="addDialogVisible" title="添加背景图片" width="500px">
      <el-tabs v-model="addMode">
        <el-tab-pane label="上传图片" name="upload">
          <div class="upload-area" @click="triggerUpload">
            <el-icon :size="48"><UploadFilled /></el-icon>
            <p>点击上传图片</p>
            <p class="upload-hint">支持 JPG/PNG/WebP，将转为 Base64 存储</p>
          </div>
          <div v-if="uploadPreview" class="upload-preview">
            <el-image :src="uploadPreview" fit="contain" style="max-height: 200px" />
          </div>
        </el-tab-pane>
        <el-tab-pane label="输入URL" name="url">
          <el-input v-model="newImageUrl" placeholder="请输入图片 URL" clearable />
          <div v-if="newImageUrl" class="upload-preview" style="margin-top: 12px">
            <el-image :src="newImageUrl" fit="contain" style="max-height: 200px" />
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="confirmAddImage"
          :disabled="!canConfirmAdd"
        >确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowUp, ArrowDown, Delete, Plus, UploadFilled } from '@element-plus/icons-vue'
import api from '@/api/index'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const activeTab = ref('bg')
const saving = ref(false)

// 基础字段
const bgOpacity = ref(0.3)
const bgCarouselInterval = ref(5)

// 多图列表
const bgImageList = ref([])
// 兼容旧版单图（读取时用，保存时统一走 bgImageList）
const bgImage = ref('')

// 添加图片对话框
const addDialogVisible = ref(false)
const addMode = ref('upload')
const newImageUrl = ref('')
const uploadPreview = ref('')
let pendingFileData = ''

const canConfirmAdd = computed(() => {
  if (addMode.value === 'url') return !!newImageUrl.value.trim()
  return !!pendingFileData
})

async function fetchSettings() {
  try {
    const res = await api.get('/setting')
    if (res.data) {
      bgImage.value = res.data.bgImage || ''
      bgOpacity.value = res.data.bgOpacity !== undefined && res.data.bgOpacity !== null
        ? parseFloat(res.data.bgOpacity) : 0.3
      bgCarouselInterval.value = res.data.bgCarouselInterval !== undefined && res.data.bgCarouselInterval !== null
        ? parseInt(res.data.bgCarouselInterval) : 5

      // 解析多图列表
      if (res.data.bgImages) {
        try {
          const images = typeof res.data.bgImages === 'string'
            ? JSON.parse(res.data.bgImages)
            : res.data.bgImages
          bgImageList.value = Array.isArray(images) ? images.filter(Boolean) : []
        } catch { bgImageList.value = [] }
      }

      // 兼容：如果有多图列表但旧 bgImage 也在列表里，去重
      if (bgImageList.value.length === 0 && bgImage.value) {
        bgImageList.value = [bgImage.value]
      }
    }
  } catch (e) {
    console.error('获取设置失败', e)
  }
}

async function handleSave() {
  saving.value = true
  try {
    // 过滤空值
    const validImages = bgImageList.value.filter(img => img && img.trim())
    const payload = {
      bgImage: validImages.length > 0 ? validImages[0] : '',
      bgOpacity: bgOpacity.value.toString(),
      bgImages: JSON.stringify(validImages),
      bgCarouselInterval: bgCarouselInterval.value.toString()
    }
    await api.put('/setting', payload)
    ElMessage.success('保存成功')
    appStore.setBackground(
      validImages.length > 0 ? validImages[0] : '',
      bgOpacity.value,
      validImages,
      bgCarouselInterval.value
    )
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// === 图片列表操作 ===

function moveImage(idx, direction) {
  const newIdx = idx + direction
  if (newIdx < 0 || newIdx >= bgImageList.value.length) return
  const arr = [...bgImageList.value]
  ;[arr[idx], arr[newIdx]] = [arr[newIdx], arr[idx]]
  bgImageList.value = arr
}

function removeImage(idx) {
  bgImageList.value.splice(idx, 1)
}

function handleAddImage() {
  addMode.value = 'upload'
  newImageUrl.value = ''
  uploadPreview.value = ''
  pendingFileData = ''
  addDialogVisible.value = true
}

function confirmAddImage() {
  if (addMode.value === 'url' && newImageUrl.value.trim()) {
    bgImageList.value.push(newImageUrl.value.trim())
  } else if (addMode.value === 'upload' && pendingFileData) {
    bgImageList.value.push(pendingFileData)
  }
  addDialogVisible.value = false
  newImageUrl.value = ''
  uploadPreview.value = ''
  pendingFileData = ''
}

function triggerUpload() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = (e) => {
    const file = e.target.files[0]
    if (file) {
      const reader = new FileReader()
      reader.onload = (res) => {
        pendingFileData = res.target.result
        uploadPreview.value = res.target.result
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
  max-width: 800px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
}

.slider-value {
  margin-left: 12px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

// ===== 图片列表 =====

.image-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.image-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  background: var(--el-bg-color);
  transition: border-color 0.2s;

  &:hover {
    border-color: var(--el-color-primary);
  }
}

.image-item-add {
  justify-content: center;
  padding: 20px;
  border-style: dashed;
  cursor: pointer;
  color: var(--el-text-color-secondary);
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  transition: all 0.2s;

  &:hover {
    color: var(--el-color-primary);
    border-color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
  }
}

.image-preview {
  width: 80px;
  height: 50px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
  border: 1px solid var(--el-border-color-lighter);

  .el-image {
    width: 100%;
    height: 100%;
  }
}

.image-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.image-index {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.image-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.carousel-hint {
  margin-top: 12px;
}

// ===== 上传对话框 =====

.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  border: 2px dashed var(--el-border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
  }

  p {
    margin: 8px 0 0;
    color: var(--el-text-color-regular);
  }
}

.upload-hint {
  font-size: 12px;
  color: var(--el-text-color-secondary) !important;
}

.upload-preview {
  margin-top: 16px;
  text-align: center;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  overflow: hidden;
}
</style>