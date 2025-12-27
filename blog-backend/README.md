# Blog Backend - 博客后端服务

基于 Spring Boot 3.1.5 的博客系统后端服务，提供完整的 RESTful API。

## 🚀 技术栈

- **Spring Boot**: 3.1.5
- **JDK**: 17
- **MyBatis Plus**: 3.5.5
- **MySQL**: 8.x
- **Redis**: 缓存和会话管理
- **MongoDB**: 文档存储
- **AWS S3 SDK**: 对象存储（兼容MinIO）
- **Lombok**: 简化代码
- **Fastjson2**: JSON处理

## 📁 项目结构

```
blog-backend/
├── src/main/java/com/panyu/mybolg/
│   ├── config/           # 配置类
│   ├── controller/       # 控制器
│   ├── entity/          # 实体类
│   ├── vo/              # 视图对象
│   ├── mapper/          # MyBatis Mapper
│   ├── service/         # 服务层
│   ├── util/            # 工具类
│   └── interceptor/     # 拦截器
├── src/main/resources/
│   ├── application.yml           # 主配置文件
│   ├── application-local.yml    # 本地开发配置
│   └── mapper/                  # MyBatis XML
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## ✨ 核心功能

### 用户管理

- ✅ 用户注册/登录
- ✅ JWT Token 认证
- ✅ 密码加密存储（MD5）
- ✅ 登录历史记录
- ✅ 用户信息管理

### 文章管理

- ✅ 文章CRUD操作
- ✅ 文章分类
- ✅ 标签管理（支持级联分类）
- ✅ 富文本/Markdown支持
- ✅ 文章搜索与过滤
- ✅ 浏览量统计

### 评论系统

- ✅ 评论发布
- ✅ 评论审核
- ✅ 评论回复
- ✅ 评论状态管理

### 文件管理

- ✅ 文件上传（S3/MinIO）
- ✅ 图片封面上传
- ✅ 文件列表管理
- ✅ 文件类型过滤

### 系统功能

- ✅ 数据统计
- ✅ Redis缓存
- ✅ MongoDB日志存储
- ✅ 跨域支持

## 🛠️ 快速开始

### 环境准备

确保已安装以下环境：

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+
- MongoDB 4.0+
- MinIO（可选）

### 配置文件

#### 1. 数据库配置

创建MySQL数据库：

```sql
CREATE
DATABASE blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 2. 修改配置文件

编辑 `application.yml` 或 `application-local.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password

    mongodb:
      uri: mongodb://root:your_password@localhost:27017/blog?authSource=admin

# S3/MinIO 配置
rustfs:
  endpoint: http://localhost:9000
  access-key: your_access_key
  secret-key: your_secret_key
  bucket-name: blog

server:
  port: 1717
```

### 启动应用

#### 方式一：Maven 运行

```bash
# 安装依赖
mvn clean install

# 开发环境（使用 application-local.yml）
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 生产环境
mvn spring-boot:run
```

#### 方式二：打包运行

```bash
mvn clean package
java -jar target/blog-backend-0.0.1-SNAPSHOT.jar
```

#### 方式三：Docker 运行

```bash
# 构建镜像
docker build -t blog-backend .

# 运行容器
docker run -p 1717:1717 blog-backend

# 或使用 docker-compose
docker-compose up -d
```

## 📡 API 文档

### 基础URL

```
http://localhost:1717
```

### 主要接口

#### 用户接口

- `POST /user/register` - 用户注册
- `POST /user/login` - 用户登录
- `GET /user/info` - 获取用户信息
- `PUT /user/update` - 更新用户信息

#### 文章接口

- `GET /article/list` - 文章列表
- `GET /article/{id}` - 文章详情
- `POST /article/create` - 创建文章
- `PUT /article/update` - 更新文章
- `DELETE /article/{id}` - 删除文章

#### 分类标签

- `GET /category/list` - 分类列表
- `GET /tag/list` - 标签列表

#### 评论接口

- `GET /comment/list` - 评论列表
- `POST /comment/create` - 发表评论
- `PUT /comment/audit` - 审核评论

#### 文件上传

- `POST /upload/cover` - 上传封面
- `POST /upload/file` - 上传文件

## 🔧 开发说明

### 多环境配置

- `application.yml` - 生产环境配置
- `application-local.yml` - 本地开发配置

使用 `-Dspring-boot.run.profiles=local` 切换环境。

### 数据库迁移

请在首次运行前执行数据库初始化脚本（如提供）。

### 密码加密

用户密码使用 MD5 加密存储，盐值为用户ID。

### Token 认证

使用自定义 JWT Token，有效期可在配置中调整。

## 🐛 常见问题

### 1. 端口冲突

修改 `application.yml` 中的 `server.port`

### 2. 数据库连接失败

检查 MySQL 服务状态和配置文件中的连接信息

### 3. Redis 连接失败

确认 Redis 服务已启动，检查密码配置

### 4. 文件上传失败

检查 MinIO/S3 配置和 Bucket 权限

## 📝 License

MIT License

## 👨‍💻 作者

YuPan
