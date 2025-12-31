<script setup>
import { onMounted } from 'vue'
import { useUserStore } from './stores/user'
import { useDark } from '@vueuse/core'

const userStore = useUserStore()

// 暗黑模式
const isDark = useDark({
  storageKey: 'blog-theme',
  valueDark: 'dark',
  valueLight: 'light',
})

// 页面加载时的初始化逻辑
onMounted(() => {
  // 登录时已经保存了 userInfo 到 localStorage
  // 刷新页面时 store 会自动从 localStorage 恢复
  // 不需要再次请求后端
  console.log('当前登录状态:', userStore.isLogin)
  console.log('用户信息:', userStore.userInfo)
  console.log('当前主题模式:', isDark.value ? '暗黑' : '明亮')
})
</script>

<template>
  <div id="app" :class="{ 'dark': isDark }">
    <router-view />
  </div>
</template>

<style>
* {
  transition: background-color 0.3s ease, color 0.3s ease;
}

#app {
  min-height: 100vh;
}

/* 暗黑模式样式 */
#app.dark {
  background: rgba(26, 26, 26, 0.3);
  color: #e0e0e0;
}

/* 暗黑模式下的 Banner */
.dark .banner {
  background: linear-gradient(135deg, #2d4a5f 0%, #1e3648 100%) !important;
}

/* 暗黑模式下的卡片 */
.dark .el-card {
  background-color: #2a2a2a !important;
  border-color: #3a3a3a !important;
  color: #e0e0e0 !important;
}

/* 暗黑模式下的标签云 */
.dark .tags-section {
  background: #2a2a2a !important;
}

/* 暗黑模式下的文章卡片 hover */
.dark .article-card:hover {
  box-shadow: 0 4px 16px rgba(255, 255, 255, 0.1) !important;
}

/* 暗黑模式下的文章标题 */
.dark .article-title {
  color: #e0e0e0 !important;
}

/* 暗黑模式下的文章摘要 */
.dark .article-summary {
  color: #b0b0b0 !important;
}

/* 暗黑模式下的边框 */
.dark .article-footer {
  border-top-color: #3a3a3a !important;
}

/* 暗黑模式下的侧边栏 */
.dark .sidebar-card {
  background-color: #2a2a2a !important;
}

.dark .hot-article-item:hover {
  background: #3a3a3a !important;
}

.dark .category-item:hover {
  background: #3a3a3a !important;
}

/* 暗黑模式下的 Header */
.dark .header {
  background: #2a2a2a !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3) !important;
}

.dark .nav-item {
  color: #e0e0e0 !important;
}

.dark .nav-item:hover,
.dark .nav-item.router-link-active {
  color: #409eff !important;
}

/* 暗黑模式下的空状态 */
.dark .empty-state {
  color: #e0e0e0 !important;
}

/* 暗黑模式下的文章详情 */
.dark .article-container {
  background: #2a2a2a !important;
}

.dark .article-detail {
  background: #1a1a1a !important;
}

.dark .markdown-body {
  color: #e0e0e0 !important;
}

/* 暗黑模式下的评论区 */
.dark .comment-section {
  background: #2a2a2a !important;
}

/* 暗黑模式下的分类/标签页面 */
.dark .page-header {
  background: linear-gradient(135deg, #1e3a5f 0%, #2d5a7b 100%) !important;
}

.dark .category-page,
.dark .tag-page {
  background: #1a1a1a !important;
}

/* 暗黑模式下的搜索页面 */
.dark .search-page {
  background: #1a1a1a !important;
}

.dark .search-header {
  background: linear-gradient(135deg, #1e3a5f 0%, #2d5a7b 100%) !important;
}

.dark .search-result {
  background: #2a2a2a !important;
}

.dark .result-info {
  background: linear-gradient(135deg, #3a3a3a 0%, #2d3748 100%) !important;
  border-left-color: #409eff !important;
}

.dark .result-text {
  color: #e0e0e0 !important;
}

.dark .search-tips {
  background: #2a2a2a !important;
}

.dark .tips-title {
  color: #e0e0e0 !important;
}

.dark .tips-desc {
  color: #909399 !important;
}

.dark .keyword-header {
  color: #e0e0e0 !important;
}

/* 暗黑模式下的登录/注册页面 */
.dark .auth-container {
  background: #1a1a1a !important;
}

.dark .login-page,
.dark .register-page {
  background: #1a1a1a !important;
}

.dark .auth-card,
.dark .login-card,
.dark .register-card {
  background: #2a2a2a !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3) !important;
}

.dark .login-header h2,
.dark .register-header h2 {
  color: #e0e0e0 !important;
}

.dark .login-header p,
.dark .register-header p {
  color: #909399 !important;
}

.dark .login-link,
.dark .register-link {
  color: #e0e0e0 !important;
}
</style>
