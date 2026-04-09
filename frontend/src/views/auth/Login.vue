<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <h1 class="login-title">DJ Blog</h1>
        <p class="login-subtitle">{{ pageTitle }}</p>
      </div>

      <!-- 初始密码验证表单 -->
      <el-form
        v-if="needsInitialSetup && !initialPasswordVerified"
        ref="initialFormRef"
        :model="initialForm"
        :rules="initialRules"
        class="login-form"
        @submit.prevent="handleVerifyInitialPassword"
      >
        <el-alert
          title="首次使用系统"
          type="info"
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <p>系统检测到无用户，请输入控制台生成的初始密码进行首次设置。</p>
          <p style="margin-top: 8px; font-size: 12px;">提示：初始密码在应用启动时输出到控制台日志中</p>
        </el-alert>

        <el-form-item prop="password">
          <el-input
            v-model="initialForm.password"
            type="password"
            placeholder="请输入初始密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleVerifyInitialPassword"
          >
            验证并继续
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 注册表单（初始密码验证通过后显示） -->
      <el-form
        v-else-if="needsInitialSetup && initialPasswordVerified"
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="login-form"
        @submit.prevent="handleRegister"
      >
        <el-alert
          title="创建管理员账户"
          type="success"
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <p>初始密码验证成功，请创建您的管理员账户。</p>
        </el-alert>

        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="邮箱"
            :prefix-icon="Message"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="确认密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleRegister"
          >
            完成注册
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 普通登录表单 -->
      <el-form
        v-else
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 状态管理
const loading = ref(false)
const needsInitialSetup = ref(false)
const initialPasswordVerified = ref(false)

// 表单引用
const formRef = ref(null)
const initialFormRef = ref(null)
const registerFormRef = ref(null)

// 普通登录表单
const form = reactive({
  username: '',
  password: ''
})

// 初始密码表单
const initialForm = reactive({
  password: ''
})

// 注册表单
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// 页面标题
const pageTitle = computed(() => {
  if (needsInitialSetup.value) {
    if (!initialPasswordVerified.value) {
      return '初始密码验证'
    }
    return '创建管理员账户'
  }
  return '登录您的账户'
})

// 验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const initialRules = {
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 检查系统是否有用户
async function checkSystemStatus() {
  try {
    const res = await authApi.hasUser()
    needsInitialSetup.value = res.data.needsInitialSetup
  } catch (e) {
    console.error('检查系统状态失败', e)
    ElMessage.error('检查系统状态失败')
  }
}

// 验证初始密码
async function handleVerifyInitialPassword() {
  const valid = await initialFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await authApi.verifyInitialPassword(initialForm.password)
    ElMessage.success('初始密码验证成功')
    initialPasswordVerified.value = true
  } catch (e) {
    console.error('初始密码验证失败', e)
  } finally {
    loading.value = false
  }
}

// 处理注册
async function handleRegister() {
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.register({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password
    })
    ElMessage.success('注册成功')
    router.push('/')
  } catch (e) {
    console.error('注册失败', e)
  } finally {
    loading.value = false
  }
}

// 处理登录
async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    console.error('登录失败', e)
  } finally {
    loading.value = false
  }
}

// 组件挂载时检查系统状态
onMounted(() => {
  checkSystemStatus()
})
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-bg-color-page);
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 400px;
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--el-color-primary);
  margin-bottom: 8px;
}

.login-subtitle {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.login-form {
  .el-form-item {
    margin-bottom: 24px;
  }
}

.login-btn {
  width: 100%;
}
</style>
