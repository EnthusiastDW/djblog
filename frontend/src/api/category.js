import api from './index'

export const categoryApi = {
  // 获取所有分类（带文章数量，平铺列表）
  getAll() {
    return api.get('/category/all')
  },

  // 获取树形分类（前台用，带文章数量）
  getTree() {
    return api.get('/category/tree')
  },

  // 获取树形分类（后台管理用）
  getTreeForAdmin() {
    return api.get('/category/tree/admin')
  },

  // 获取分类祖先链路（面包屑用）
  getAncestors(id) {
    return api.get(`/category/${id}/ancestors`)
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
