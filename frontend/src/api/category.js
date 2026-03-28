import api from './index'

export const categoryApi = {
  // 获取所有分类（带文章数量）
  getAll() {
    return api.get('/category/all')
  },

  // 分页查询分类列表
  getList(params = {}) {
    return api.get('/category', { params })
  },

  // 获取分类详情
  getDetail(id) {
    return api.get(`/category/${id}`)
  },

  // 创建分类
  create(data) {
    return api.post('/category', data)
  },

  // 更新分类
  update(data) {
    return api.put('/category', data)
  },

  // 删除分类
  delete(id) {
    return api.delete(`/category/${id}`)
  }
}
