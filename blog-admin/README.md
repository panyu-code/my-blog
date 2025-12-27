# Blog Admin - 博客后台管理系统

基于 Vue 3 + Vite 的博客管理后台，提供完整的内容管理功能。

## 🚀 技术栈

- **Vue 3**: 使用 Composition API
- **Vite**: 快速的构建工具
- **Element Plus**: UI组件库
- **Vue Router**: 路由管理
- **Pinia**: 状态管理
- **Axios**: HTTP客户端
- **WangEditor**: 富文本编辑器
- **ECharts**: 数据可视化

## 📁 项目结构

```
blog-admin/
├── src/
│   ├── api/              # API接口
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   │   └── FileUpload.vue  # 文件上传组件
│   ├── layouts/          # 布局组件
│   │   └── MainLayout.vue  # 主布局
│   ├── router/           # 路由配置
│   ├── stores/           # Pinia状态管理
│   ├── utils/            # 工具函数
│   ├── views/            # 页面组件
│   │   ├── Dashboard.vue     # 仪表盘
│   │   ├── Articles/         # 文章管理
│   │   ├── Categories.vue    # 分类管理
│   │   ├── Tags.vue         # 标签管理
│   │   ├── Comments.vue     # 评论管理
│   │   ├── Users.vue        # 用户管理
│   │   ├── Uploads.vue      # 文件管理
│   │   ├── Settings.vue     # 系统设置
│   │   ├── Profile.vue      # 个人中心
│   │   └── Login.vue        # 登录页
│   ├── App.vue
│   └── main.js
├── public/
├── index.html
├── vite.config.js
└── package.json
```

## ✨ 主要功能

### 📊 数据统计
- 文章、用户、评论统计
- 访问量趋势图表
- 实时数据概览
- 可视化图表展示

### ✍️ 内容管理

#### 文章管理
- 文章列表（支持搜索、筛选、分页）
- 新增/编辑文章（富文本编辑器）
- 文章分类和标签设置
- 文章状态管理（草稿/已发布）
- 批量删除功能
- 封面图上传

#### 分类管理
- 分类CRUD操作
- 分类下标签管理
- 文章数量统计
- 批量删除

#### 标签管理
- 标签CRUD操作
- 标签颜色自定义
- 分类级联管理
- 文章数量统计
- 批量删除

### 💬 互动管理

#### 评论管理
- 评论列表查看
- 评论审核（通过/拒绝）
- 评论删除
- 批量删除
- 状态筛选

### 👥 用户管理
- 用户列表
- 用户CRUD操作
- 角色分配（管理员/编辑/普通用户）
- 用户状态管理
- 批量删除

### 🖼️ 文件管理
- 文件上传（图片、文档等）
- 文件预览
- 文件下载
- 复制文件链接
- 批量删除
- 文件类型筛选

### ⚙️ 系统设置
- 网站基本信息
- 系统参数配置
- 个人资料修改
- 密码修改
- 登录历史查看

## 🛠️ 快速开始

### 环境要求

- Node.js 16+
- npm 或 yarn

### 安装依赖

```bash
npm install
# 或
yarn install
```

### 开发运行

```bash
npm run dev
```

访问 http://localhost:3001/admin

### 生产构建

```bash
npm run build
```

构建产物位于 `dist/` 目录。

### 预览构建

```bash
npm run preview
```

## ⚙️ 配置说明

### 路由配置

基础路径：`/admin`

主要路由：
- `/admin/login` - 登录页
- `/admin/dashboard` - 仪表盘
- `/admin/articles` - 文章管理
- `/admin/categories` - 分类管理
- `/admin/tags` - 标签管理
- `/admin/comments` - 评论管理
- `/admin/users` - 用户管理
- `/admin/uploads` - 文件管理
- `/admin/settings` - 系统设置
- `/admin/profile` - 个人中心

### Vite 配置

开发环境代理（`vite.config.js`）：

```javascript
export default defineConfig({
  base: '/admin/',
  server: {
    port: 3001,
    proxy: {
      '/api': {
        target: 'http://localhost:1717',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
```

### API 配置

API 基础地址在 `src/utils/request.js` 中配置：

```javascript
const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
```

## 🎨 界面特性

### 布局设计
- 侧边栏导航
- 顶部工具栏
- 面包屑导航
- 响应式布局

### 交互体验
- 加载状态提示
- 操作确认弹窗
- 成功/失败消息提示
- 表格多选操作
- 批量删除确认

### 富文本编辑
- WangEditor 富文本编辑器
- 支持图片上传
- 代码块插入
- 表格编辑
- 链接插入

## 🔐 权限管理

### 路由守卫

登录验证：未登录用户自动跳转至登录页

```javascript
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.path !== '/admin/login' && !userStore.token) {
    next('/admin/login')
  } else {
    next()
  }
})
```

### Token 认证

使用 JWT Token，存储在 localStorage，请求时自动添加到 Header。

## 📊 数据可视化

使用 ECharts 展示统计数据：

- 文章发布趋势图
- 访问量统计
- 用户增长曲线
- 评论活跃度

## 🔧 开发指南

### 状态管理

使用 Pinia 管理全局状态：

```javascript
// stores/user.js
export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    token: null
  }),
  actions: {
    setUserInfo(info) {
      this.userInfo = info
    }
  }
})
```

### 组件开发

推荐使用 Composition API：

```vue
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const tableData = ref([])
const loading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    // 获取数据
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>
```

### 表格批量操作

```vue
<template>
  <el-button 
    v-if="selectedIds.length > 0"
    type="danger"
    @click="handleBatchDelete"
  >
    批量删除 ({{ selectedIds.length }})
  </el-button>
  
  <el-table 
    :data="tableData"
    @selection-change="handleSelectionChange"
  >
    <el-table-column type="selection" width="55" />
    <!-- 其他列 -->
  </el-table>
</template>

<script setup>
const selectedIds = ref([])

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleBatchDelete = async () => {
  // 批量删除逻辑
}
</script>
```

## 🚀 部署

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name admin.your-domain.com;
    
    location /admin {
        alias /path/to/blog-admin/dist;
        try_files $uri $uri/ /admin/index.html;
    }
    
    location /api {
        proxy_pass http://localhost:1717;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 构建优化

生产构建时已开启：
- 代码压缩
- Tree Shaking
- 资源分包
- Gzip 压缩（需服务器配置）

## 🐛 常见问题

### 1. 登录后立即退出
检查 Token 是否正确存储，后端 Token 验证逻辑

### 2. 富文本编辑器图片上传失败
检查上传接口配置和文件大小限制

### 3. 批量操作不生效
确认已正确绑定 `@selection-change` 事件

### 4. 路由刷新 404
确保 Nginx 配置了正确的 try_files 规则

## 📝 默认账号

首次使用可使用以下账号登录（如已创建）：

- 用户名：admin
- 密码：（请查看数据库或注册新账号）

## 📝 License

MIT License

## 👨‍💻 作者

YuPan
