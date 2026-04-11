import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const api = axios.create({
  baseURL: '/api',
  timeout: 60000, // 60秒超时
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 清除本地存储的 token 和用户信息
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          
          // 判断当前是否在公开页面（文章详情、首页等）
          const currentPath = window.location.pathname
          const isPublicPage = currentPath.startsWith('/post/') || 
                              currentPath === '/' || 
                              currentPath.startsWith('/categories') ||
                              currentPath.startsWith('/tags') ||
                              currentPath.startsWith('/archive') ||
                              currentPath.startsWith('/search') ||
                              currentPath.startsWith('/user/')
          
          if (isPublicPage) {
            // 公开页面只显示提示，不强制跳转
            ElMessage.warning(error.response.data?.message || '请先登录')
          } else {
            // 管理页面或其他需要认证的页面，强制跳转到登录页
            ElMessage.error('登录已过期，请重新登录')
            router.push({ path: '/login', query: { redirect: currentPath } })
          }
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default api
