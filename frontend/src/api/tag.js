import api from './index'

export const tagApi = {
  // 获取所有标签（带文章数量）
  getAll() {
    return api.get('/tag/all')
  },

  // 分页查询标签列表
  getList(params = {}) {
    return api.get('/tag', { params })
  },

  // 获取标签详情
  getDetail(id) {
    return api.get(`/tag/${id}`)
  },

  // 创建标签
  create(data) {
    return api.post('/tag', data)
  },

  // 更新标签
  update(data) {
    return api.put('/tag', data)
  },

  // 删除标签
  delete(id) {
    return api.delete(`/tag/${id}`)
  }
}
