import api from './index'

export const postApi = {
  // 分页查询文章列表
  getList(params = {}) {
    return api.get('/post', { params })
  },

  // 管理后台获取所有文章列表（包含草稿）
  getAdminList(params = {}) {
    return api.get('/post/admin/list', { params })
  },

  // 查询文章详情
  getDetail(id) {
    return api.get(`/post/${id}`)
  },

  // 查询文章详情（含内容）
  getDetailWithContent(id) {
    return api.get(`/post/${id}/detail`)
  },

  // 根据 slug 查询文章详情
  getBySlug(slug) {
    return api.get(`/post/slug/${slug}`)
  },

  // 搜索文章
  search(params = {}) {
    return api.get('/post/search', { params })
  },

  // 获取文章归档
  getArchives() {
    return api.get('/post/archives')
  },

  // 根据年月获取文章
  getPostsByArchive(yearMonth, params = {}) {
    return api.get(`/post/archives/${yearMonth}`, { params })
  },

  // 获取草稿列表
  getDrafts(params = {}) {
    return api.get('/post/drafts', { params })
  },

  // 保存草稿
  saveDraft(data) {
    return api.post('/post/draft', data)
  },

  // 发布文章
  publish(data) {
    return api.post('/post/publish', data)
  },

  // 创建文章
  create(data) {
    return api.post('/post', data)
  },

  // 更新文章
  update(data) {
    return api.put('/post', data)
  },

  // 删除文章
  delete(idList) {
    return api.delete('/post', { params: { idList: idList.join(',') } })
  },

  // AI生成摘要
  generateSummary(title, content, maxLength = 200) {
    return api.post('/post/summary/generate', { title, content, maxLength })
  },

  // 获取总阅读量
  getTotalViewCount() {
    return api.get('/post/stats/views')
  },

  // 增加阅读量
  incrementViewCount(id) {
    return api.post(`/post/${id}/view`)
  }
}
