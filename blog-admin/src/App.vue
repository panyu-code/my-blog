<template>
  <div id="app" :class="{ 'dark': isDark }">
    <router-view />
  </div>
</template>

<script setup>
import { useDark } from '@vueuse/core'

// 暗黑模式
const isDark = useDark({
  storageKey: 'admin-theme',
  valueDark: 'dark',
  valueLight: 'light',
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  transition: background-color 0.3s ease, color 0.3s ease, border-color 0.3s ease;
}

html,
body {
  height: 100%;
  width: 100%;
  overflow: hidden;
}

#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

/* 暗黑模式样式 */
#app.dark {
  background: #1a1a1a;
  color: #e0e0e0;
}

/* 暗黑模式下的主容器 */
.dark .main-content {
  background-color: #1a1a1a !important;
}

/* 暗黑模式下的卡片 */
.dark .el-card {
  background-color: #2a2a2a !important;
  border-color: #3a3a3a !important;
  color: #e0e0e0 !important;
}

/* 暗黑模式下的表格 */
.dark .el-table {
  background-color: #2a2a2a !important;
  color: #e0e0e0 !important;
}

.dark .el-table th,
.dark .el-table tr {
  background-color: #2a2a2a !important;
  color: #e0e0e0 !important;
}

.dark .el-table td {
  border-color: #3a3a3a !important;
}

.dark .el-table__body tr:hover > td {
  background-color: #3a3a3a !important;
}

/* 暗黑模式下的 Header */
.dark .header {
  background-color: #2a2a2a !important;
  border-bottom-color: #3a3a3a !important;
}

.dark .user-info:hover {
  background-color: #3a3a3a !important;
}

.dark .username {
  color: #e0e0e0 !important;
}

/* 暗黑模式下的面包屑 */
.dark .el-breadcrumb__inner {
  color: #e0e0e0 !important;
}

/* 暗黑模式下的对话框 */
.dark .el-dialog {
  background-color: #2a2a2a !important;
}

.dark .el-dialog__header {
  border-bottom-color: #3a3a3a !important;
}

.dark .el-dialog__title {
  color: #e0e0e0 !important;
}

/* 暗黑模式下的表单 */
.dark .el-form-item__label {
  color: #e0e0e0 !important;
}

/* 暗黑模式下的分页 */
.dark .el-pagination {
  color: #e0e0e0 !important;
}

.dark .el-pagination button {
  background-color: #2a2a2a !important;
  color: #e0e0e0 !important;
}

/* 暗黑模式下的空状态 */
.dark .el-empty__description {
  color: #909399 !important;
}

/* 暗黑模式下的统计卡片 */
.dark .stat-card {
  background: linear-gradient(135deg, #2d4a5f 0%, #1e3648 100%) !important;
}

/* 暗黑模式下的登录页 */
.dark .login-container {
  background: linear-gradient(135deg, #2d4a5f 0%, #1e3648 100%) !important;
}

.dark .login-box {
  background: #2a2a2a !important;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5) !important;
}

.dark .login-title {
  color: #e0e0e0 !important;
}
</style>
