import api from './index'

export const commentApi = {
  // 分页查询评论列表
  getList(params = {}) {
    return api.get('/comment', { params })
  },

  // 获取评论详情
  getDetail(id) {
    return api.get(`/comment/${id}`)
  },

  // 根据文章ID获取评论列表
  getByPostId(postId) {
    return api.get(`/comment/post/${postId}`)
  },

  // 根据文章ID分页获取评论
  getByPostIdPage(postId, params = {}) {
    return api.get(`/comment/post/${postId}/page`, { params })
  },

  // 发表评论
  create(data) {
    return api.post('/comment', data)
  },

  // 更新评论
  update(data) {
    return api.put('/comment', data)
  },

  // 删除评论
  delete(data) {
    return api.delete('/comment', { data })
  },

  // 审核评论
  audit(data) {
    return api.put('/comment/audit', data)
  },

  // 获取待审核评论
  getPending(params = {}) {
    return api.get('/comment/pending', { params })
  }
}
