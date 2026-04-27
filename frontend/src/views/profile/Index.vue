<template>
  <div class="profile-page">
    <div class="profile-header">
      <el-avatar :size="80" :src="userStore.user?.avatarUrl">
        {{ userStore.user?.username?.charAt(0) || 'U' }}
      </el-avatar>
      <div class="profile-info">
        <h1 class="profile-name">{{ userStore.user?.username || '用户' }}</h1>
        <p class="profile-email">{{ userStore.user?.email }}</p>
      </div>
    </div>

    <el-card class="profile-card">
      <template #header>
        <span>个人信息</span>
      </template>
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="头像URL">
          <el-input v-model="form.avatarUrl" placeholder="输入头像图片URL" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="form.contactInfo" placeholder="输入联系方式" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="form.bio" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="微信二维码">
          <div class="qrcode-upload">
            <el-input v-model="form.wechatQrCode" placeholder="输入微信二维码图片URL" />
            <el-button @click="handleUploadQrCode" style="margin-left: 10px;">上传</el-button>
          </div>
          <div v-if="form.wechatQrCode" class="qrcode-preview">
            <el-image :src="form.wechatQrCode" fit="contain" class="preview-image" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleUpdate" :loading="loading">
            保存修改
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  avatarUrl: '',
  contactInfo: '',
  bio: '',
  wechatQrCode: ''
})

onMounted(() => {
  if (userStore.user) {
    form.username = userStore.user.username || ''
    form.email = userStore.user.email || ''
    form.avatarUrl = userStore.user.avatarUrl || ''
    form.contactInfo = userStore.user.contactInfo || ''
    form.bio = userStore.user.bio || ''
    form.wechatQrCode = userStore.user.wechatQrCode || ''
  }
})

async function handleUpdate() {
  loading.value = true
  try {
    const res = await userApi.update({
      id: userStore.user.id,
      username: form.username,
      email: form.email,
      avatarUrl: form.avatarUrl,
      contactInfo: form.contactInfo,
      bio: form.bio,
      wechatQrCode: form.wechatQrCode
    })
    
    // 如果返回了新 token，说明用户名被修改了，需要更新 token
    if (res.data.token) {
      userStore.$patch({
        token: res.data.token,
        user: res.data.user
      })
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('user', JSON.stringify(res.data.user))
      ElMessage.success('保存成功，请重新登录')
      // 延迟后刷新页面以应用新 token
      setTimeout(() => {
        window.location.reload()
      }, 1000)
    } else {
      // 没有新 token，只更新用户信息
      userStore.updateUserInfo({
        username: form.username,
        email: form.email,
        avatarUrl: form.avatarUrl,
        contactInfo: form.contactInfo,
        bio: form.bio,
        wechatQrCode: form.wechatQrCode
      })
      ElMessage.success('保存成功')
      // 刷新页面数据
      window.location.reload()
    }
  } catch (e) {
    console.error('保存失败', e)
  } finally {
    loading.value = false
  }
}

function handleUploadQrCode() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = (e) => {
    const file = e.target.files[0]
    if (file) {
      const reader = new FileReader()
      reader.onload = (res) => {
        form.wechatQrCode = res.target.result
      }
      reader.readAsDataURL(file)
    }
  }
  input.click()
}
</script>

<style lang="scss" scoped>
.profile-page {
  max-width: 600px;
  margin: 0 auto;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 32px;
  margin-bottom: 24px;
}

.profile-info {
  flex: 1;
}

.profile-name {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
}

.profile-email {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.profile-card {
  background: var(--el-bg-color);
}

.qrcode-upload {
  display: flex;
  align-items: center;
}

.qrcode-preview {
  margin-top: 12px;
  width: 200px;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
}

.preview-image {
  width: 100%;
  height: 100%;
}
</style>
