import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('../layouts/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '数据统计', icon: 'DataAnalysis', requiresAdmin: true }
      },
      {
        path: '/articles',
        name: 'Articles',
        component: () => import('../views/Articles/Index.vue'),
        meta: { title: '文章管理', icon: 'Document', requiresAdmin: true }
      },
      {
        path: '/articles/audit',
        name: 'ArticleAudit',
        component: () => import('../views/Articles/Audit.vue'),
        meta: { title: '文章审核', icon: 'Check', requiresAdmin: true }
      },
      {
        path: '/articles/edit/:id',
        name: 'ArticleEdit',
        component: () => import('../views/Articles/Edit.vue'),
        meta: { title: '编辑文章', hidden: true, requiresAdmin: true }
      },
      {
        path: '/categories',
        name: 'Categories',
        component: () => import('../views/Categories.vue'),
        meta: { title: '分类管理', icon: 'Folder', requiresAdmin: true }
      },
      {
        path: '/tags',
        name: 'Tags',
        component: () => import('../views/Tags.vue'),
        meta: { title: '标签管理', icon: 'PriceTag', requiresAdmin: true }
      },
      {
        path: '/comments',
        name: 'Comments',
        component: () => import('../views/Comments.vue'),
        meta: { title: '评论管理', icon: 'ChatDotRound', requiresAdmin: true }
      },
      {
        path: '/users',
        name: 'Users',
        component: () => import('../views/Users.vue'),
        meta: { title: '用户管理', icon: 'User', requiresAdmin: true }
      },
      {
        path: '/uploads',
        name: 'Uploads',
        component: () => import('../views/Uploads.vue'),
        meta: { title: '文件管理', icon: 'Picture', requiresAdmin: true }
      },
      {
        path: '/settings',
        name: 'Settings',
        component: () => import('../views/Settings.vue'),
        meta: { title: '系统设置', icon: 'Setting', requiresAdmin: true }
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心', icon: 'User', hidden: true, requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory('/admin'),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token
  const userInfo = userStore.userInfo

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - blog-admin` : 'blog-admin'

  // 检查是否需要认证
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } 
  // 检查是否在登录页且已登录，如果是则重定向到首页
  else if (to.path === '/login' && token) {
    next('/')
  } 
  // 检查是否需要管理员权限
  else if (to.matched.some(record => record.meta.requiresAdmin)) {
    if (token && userInfo && (userInfo.username === 'admin' || userInfo.role === 1)) {
      next()
    } else {
      ElMessage.error('您没有权限访问该页面')
      next('/login')
    }
  } 
  // 其他情况允许访问
  else {
    next()
  }
})

export default router