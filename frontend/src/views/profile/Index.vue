<template>
  <div class="profile-page">
    <div class="profile-header">
      <el-avatar :size="80" :src="userStore.user?.avatar">
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
          <el-input v-model="form.username" disabled />
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
  bio: ''
})

onMounted(() => {
  if (userStore.user) {
    form.username = userStore.user.username || ''
    form.email = userStore.user.email || ''
    form.avatarUrl = userStore.user.avatarUrl || ''
    form.contactInfo = userStore.user.contactInfo || ''
    form.bio = userStore.user.bio || ''
  }
})

async function handleUpdate() {
  loading.value = true
  try {
    await userApi.update({
      id: userStore.user.id,
      email: form.email,
      avatar: form.avatarUrl,
      contactInfo: form.contactInfo,
      bio: form.bio
    })
    userStore.updateUserInfo({
      email: form.email,
      avatarUrl: form.avatarUrl,
      contactInfo: form.contactInfo,
      bio: form.bio
    })
    ElMessage.success('保存成功')
  } catch (e) {
    console.error('保存失败', e)
  } finally {
    loading.value = false
  }
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
</style>
