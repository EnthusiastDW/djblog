import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/FrontLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/Index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'post/:id',
        name: 'PostDetail',
        component: () => import('@/views/post/Detail.vue'),
        meta: { title: '文章详情' }
      },
      {
        path: 'article/:slug',
        name: 'ArticleBySlug',
        component: () => import('@/views/post/Detail.vue'),
        meta: { title: '文章详情' }
      },
      {
        path: 'archives',
        name: 'Archives',
        component: () => import('@/views/archive/Index.vue'),
        meta: { title: '归档' }
      },
      {
        path: 'archives/:yearMonth',
        name: 'ArchivePosts',
        component: () => import('@/views/archive/Posts.vue'),
        meta: { title: '归档文章' }
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/category/Index.vue'),
        meta: { title: '分类' }
      },
      {
        path: 'category/:id',
        name: 'CategoryPosts',
        component: () => import('@/views/category/Posts.vue'),
        meta: { title: '分类文章' }
      },
      {
        path: 'tags',
        name: 'Tags',
        component: () => import('@/views/tag/Index.vue'),
        meta: { title: '标签' }
      },
      {
        path: 'tag/:id',
        name: 'TagPosts',
        component: () => import('@/views/tag/Posts.vue'),
        meta: { title: '标签文章' }
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/post/Search.vue'),
        meta: { title: '搜索结果' }
      },
      {
        path: 'user/:id',
        name: 'UserProfile',
        component: () => import('@/views/user/Index.vue'),
        meta: { title: '用户主页' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/Index.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Index.vue'),
        meta: { title: '后台管理' }
      },
      {
        path: 'posts',
        name: 'AdminPosts',
        component: () => import('@/views/admin/posts/Index.vue'),
        meta: { title: '文章管理' }
      },
      {
        path: 'posts/write',
        name: 'AdminPostWrite',
        component: () => import('@/views/admin/posts/Edit.vue'),
        meta: { title: '写文章' }
      },
      {
        path: 'posts/edit/:id',
        name: 'AdminPostEdit',
        component: () => import('@/views/admin/posts/Edit.vue'),
        meta: { title: '编辑文章' }
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/categories/Index.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'tags',
        name: 'AdminTags',
        component: () => import('@/views/admin/tags/Index.vue'),
        meta: { title: '标签管理' }
      },
      {
        path: 'comments',
        name: 'AdminComments',
        component: () => import('@/views/admin/comments/Index.vue'),
        meta: { title: '评论管理' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/users/Index.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('@/views/admin/settings/Index.vue'),
        meta: { title: '全局设置' }
      },
      {
        path: 'posts/import',
        name: 'AdminPostsImport',
        component: () => import('@/views/admin/posts/Import.vue'),
        meta: { title: '文章导入' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - DJ Blog` : 'DJ Blog'

  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
  }

  next()
})

export default router
