import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { title: '首页', requiresAuth: false }
      },
      {
        path: '/article/:id',
        name: 'ArticleDetail',
        component: () => import('../views/ArticleDetail.vue'),
        meta: { title: '文章详情', requiresAuth: true }
      },
      {
        path: '/search',
        name: 'Search',
        component: () => import('../views/Search.vue'),
        meta: { title: '搜索', requiresAuth: true }
      },
      {
        path: '/category/:id',
        name: 'Category',
        component: () => import('../views/Category.vue'),
        meta: { title: '分类', requiresAuth: true }
      },
      {
        path: '/tag/:id',
        name: 'Tag',
        component: () => import('../views/Tag.vue'),
        meta: { title: '标签', requiresAuth: true }
      },
      {
        path: '/about',
        name: 'About',
        component: () => import('../views/About.vue'),
        meta: { title: '关于', requiresAuth: false }
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: '/publish',
        name: 'Publish',
        component: () => import('../views/Publish.vue'),
        meta: { title: '发布文章', requiresAuth: true }
      },
      {
        path: '/publish/:id',
        name: 'EditArticle',
        component: () => import('../views/Publish.vue'),
        meta: { title: '编辑文章', requiresAuth: true }
      },
      {
        path: '/my-articles',
        name: 'MyArticles',
        component: () => import('../views/MyArticles.vue'),
        meta: { title: '我的文章', requiresAuth: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('../views/ForgotPassword.vue'),
    meta: { title: '找回密码', requiresAuth: false }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: { title: '404' }
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
  document.title = to.meta.title ? `${to.meta.title} - blog-web` : 'blog-web'
  
  // 检查是否需要登录
  if (to.meta.requiresAuth !== false) {
    const userStore = useUserStore()
    
    if (!userStore.isLogin) {
      ElMessage.warning('请先登录')
      next('/login')
      return
    }
  }
  
  next()
})

export default router
