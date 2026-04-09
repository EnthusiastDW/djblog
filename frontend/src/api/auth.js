import api from './index'

export const authApi = {
  // 用户登录
  login(data) {
    return api.post('/auth/login', data)
  },

  // 用户注册
  register(data) {
    return api.post('/auth/register', data)
  },

  // 用户登出
  logout() {
    return api.post('/auth/logout')
  },

  // 检查系统是否有用户
  hasUser() {
    return api.get('/auth/has-user')
  },

  // 验证初始密码
  verifyInitialPassword(password) {
    return api.post('/auth/verify-initial-password', { password })
  }
}
