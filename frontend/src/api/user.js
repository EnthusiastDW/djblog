import api from './index'

export const userApi = {
  // 分页查询用户列表
  getList(params = {}) {
    return api.get('/user', { params })
  },

  // 获取用户详情
  getDetail(id) {
    return api.get(`/user/${id}`)
  },

  // 获取公开用户资料
  getPublicProfile(id) {
    return api.get(`/user/${id}/public`)
  },

  getFirstUser() {
    return api.get('/user', { params: { current: 1, size: 1 } })
  },

  // 创建用户
  create(data) {
    return api.post('/user', data)
  },

  // 更新用户
  update(data) {
    return api.put('/user', data)
  },

  // 删除用户
  delete(data) {
    return api.delete('/user', { data })
  },

  // 获取关于我内容
  getAbout() {
    return api.get('/user/about')
  },

  // 更新关于我内容
  updateAbout(data) {
    return api.put('/user/about', data)
  }
}
