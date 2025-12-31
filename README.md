# 个人博客系统

一个基于 Vue 3 + Spring Boot 的现代化博客系统，包含前台展示、后台管理和后端服务三个子项目。

## 📚 项目简介

本项目是一个功能完善的全栈博客系统，支持文章发布、分类管理、标签管理、评论系统、文件上传等功能。采用前后端分离架构，具有良好的代码结构和用户体验。

## 🚀 技术栈

### 前端技术
- **框架**: Vue 3 + Vite
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP客户端**: Axios
- **富文本编辑器**: WangEditor (后台)
- **Markdown渲染**: markdown-it (前台)
- **代码高亮**: highlight.js

### 后端技术
- **框架**: Spring Boot 3.1.5
- **JDK版本**: Java 17
- **ORM框架**: MyBatis Plus 3.5.5
- **数据库**: MySQL 8.x
- **对象存储**: AWS S3 (兼容 MinIO)
- **工具类**: Lombok, Commons Lang3, Fastjson2

## 📁 项目结构

```
my-blog/
├── blog-backend/          # 后端服务
├── blog-web/             # 前台展示
├── blog-admin/           # 后台管理
└── README.md
```

## ✨ 主要功能

### 前台功能 (blog-web)
- 📝 文章列表展示与分页
- 🔍 文章搜索与筛选
- 📂 分类与标签导航
- 💬 评论功能
- 🌙 深色模式切换
- 📱 响应式设计
- 🔐 用户登录注册
- 👤 个人资料管理

### 后台功能 (blog-admin)
- ✍️ 文章管理（新增、编辑、删除、批量删除）
- 📁 分类管理
- 🏷️ 标签管理（支持级联分类）
- 💬 评论审核
- 👥 用户管理
- 📊 数据统计
- 🖼️ 文件管理（上传、下载、批量删除）
- ⚙️ 系统设置

### 后端功能 (blog-backend)
- 🔐 JWT身份认证
- 🔑 密码加密存储
- 📝 登录日志记录
- 🎨 标签颜色自定义
- 📊 访问统计
- 🗄️ S3对象存储

## 🛠️ 快速开始

### 环境要求

- Node.js 16+
- JDK 17+
- MySQL 8.0+
- AWS S3

### 后端部署

1. 克隆项目
```bash
git clone <repository-url>
cd my-blog/blog-backend
```

2. 配置数据库
- 创建MySQL数据库
- 导入SQL脚本（如有）

3. 修改配置文件
```yaml
# application.yml 或 application-local.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog
    username: your_username
    password: your_password
```

4. 启动服务
```bash
mvn clean install
mvn spring-boot:run
# 或使用 Docker
docker-compose up -d
```

### 前台部署

```bash
cd blog-web
npm install
npm run dev          # 开发环境
npm run build        # 生产构建
```

### 后台部署

```bash
cd blog-admin
npm install
npm run dev          # 开发环境
npm run build        # 生产构建
```

## 🌐 体验地址
- 前台地址: http://111.229.61.213
- 后台地址: http://111.229.61.213/admin


## 🌐 访问地址
- 前台地址: http://localhost:3000
- 后台地址: http://localhost:3001/admin
- 后端API: http://localhost:8080
- Swagger文档: http://localhost:8080/swagger-ui.html
- API文档JSON: http://localhost:8080/v3/api-docs

## 📝 开发配置

### 本地开发环境

后端使用 `application-local.yml` 配置文件，启动时添加参数：
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

前端开发时自动使用 Vite 代理转发 API 请求。

### 生产环境

使用 Nginx 反向代理，配置参考：
```nginx
location /api {
    proxy_pass http://localhost:1717;
}

location /admin {
    alias /path/to/blog-admin/dist;
}

location / {
    root /path/to/blog-web/dist;
}
```

## 🐳 Docker 部署

后端提供 Docker 支持：

```bash
cd blog-backend
docker build -t blog-backend .
docker run -p 1717:1717 blog-backend
```

或使用 docker-compose：
```bash
docker-compose up -d
```

## 📄 License

MIT License

## 👨‍💻 作者

YuPan

## 🙏 致谢

感谢所有开源项目的贡献者们！
