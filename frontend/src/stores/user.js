import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const token = ref(localStorage.getItem('token') || '')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  async function login(loginData) {
    const res = await authApi.login(loginData)
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.user))
    return res
  }

  async function register(registerData) {
    const res = await authApi.register(registerData)
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.user))
    return res
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch (e) {
      // ignore
    }
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  function updateUserInfo(userInfo) {
    user.value = { ...user.value, ...userInfo }
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  return {
    user,
    token,
    isLoggedIn,
    isAdmin,
    login,
    register,
    logout,
    updateUserInfo
  }
})
