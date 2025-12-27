# Blog Web - 博客前台展示

基于 Vue 3 + Vite 的博客系统前台页面，为用户提供现代化的阅读体验。

## 🚀 技术栈

- **Vue 3**: 使用 Composition API
- **Vite**: 快速的构建工具
- **Element Plus**: UI组件库
- **Vue Router**: 路由管理
- **Pinia**: 状态管理
- **Axios**: HTTP客户端
- **markdown-it**: Markdown渲染
- **highlight.js**: 代码高亮

## 📁 项目结构

```
blog-web/
├── src/
│   ├── api/              # API接口
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   │   ├── Header.vue    # 顶部导航
│   │   └── Footer.vue    # 底部信息
│   ├── router/           # 路由配置
│   ├── stores/           # Pinia状态管理
│   ├── utils/            # 工具函数
│   ├── views/            # 页面组件
│   │   ├── Home.vue      # 首页
│   │   ├── ArticleDetail.vue  # 文章详情
│   │   ├── Category.vue  # 分类页
│   │   ├── Tag.vue       # 标签页
│   │   ├── Search.vue    # 搜索页
│   │   ├── Login.vue     # 登录页
│   │   └── Profile.vue   # 个人中心
│   ├── App.vue
│   └── main.js
├── public/
├── index.html
├── vite.config.js
└── package.json
```

## ✨ 主要功能

### 📝 文章阅读
- 文章列表展示（支持分页）
- 文章详情页（Markdown渲染）
- 代码高亮显示
- 文章分类和标签筛选
- 热门文章推荐
- 文章搜索功能

### 🎨 界面特性
- 响应式设计，适配移动端
- 深色模式切换
- 流畅的页面切换动画
- 优雅的加载状态
- 现代化的UI设计

### 👤 用户功能
- 用户注册登录
- 个人信息管理
- 密码修改
- 评论发表
- 登录历史查看

### 📂 内容导航
- 分类级联展示
- 标签云（使用自定义颜色）
- 面包屑导航
- 返回上一页按钮

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

访问 http://localhost:3000

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

### API 地址配置

开发环境使用 Vite 代理（`vite.config.js`）：

```javascript
export default defineConfig({
  server: {
    port: 3000,
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

生产环境通过 Nginx 反向代理配置。

### 路由配置

路由模式：HTML5 History 模式

主要路由：
- `/` - 首页
- `/article/:id` - 文章详情
- `/category/:id` - 分类页
- `/tag/:id` - 标签页
- `/search` - 搜索页
- `/login` - 登录页
- `/register` - 注册页
- `/profile` - 个人中心

## 🎨 主题定制

### 深色模式

系统支持深色模式，通过顶部导航栏切换。深色模式状态保存在 localStorage。

### 自定义样式

全局样式文件：`src/assets/style/global.css`

Element Plus 主题定制可在 `main.js` 中配置。

## 📱 响应式设计

断点设置：
- `xs`: < 768px （移动设备）
- `sm`: 768px - 992px （平板）
- `md`: 992px - 1200px （小屏笔记本）
- `lg`: 1200px+ （桌面显示器）

## 🔧 开发指南

### 状态管理

使用 Pinia 管理全局状态：

- `useUserStore` - 用户信息
- `useArticleStore` - 文章数据
- `useThemeStore` - 主题设置

### API 调用

所有 API 请求统一封装在 `src/api/` 目录：

```javascript
// 示例
import { getArticleList } from '@/api/article'

const fetchArticles = async () => {
  const res = await getArticleList({ pageNum: 1, pageSize: 10 })
  // 处理数据
}
```

### 组件开发

推荐使用 Vue 3 Composition API：

```vue
<script setup>
import { ref, onMounted } from 'vue'

const data = ref([])

onMounted(async () => {
  // 初始化数据
})
</script>
```

## 🚀 部署

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    root /path/to/blog-web/dist;
    index index.html;
    
    # 处理 Vue Router 的 History 模式
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API 代理
    location /api {
        proxy_pass http://localhost:1717;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 环境变量

可创建 `.env.production` 配置生产环境变量：

```
VITE_API_BASE_URL=/api
```

## 🐛 常见问题

### 1. API 请求 404
检查后端服务是否启动，以及代理配置是否正确

### 2. 路由刷新 404
确保服务器配置了 History 模式的重定向规则

### 3. 深色模式不生效
清除浏览器缓存和 localStorage

## 📝 License

MIT License

## 👨‍💻 作者

YuPan
