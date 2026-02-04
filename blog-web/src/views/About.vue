<template>
  <div class="about-page">
    <el-card class="about-card">
      <h1>关于MyBlog</h1>
      <div class="content">
        <el-divider />
        <h2>📝 MyBlog简介</h2>
        <p>这是一个基于 Vue 3 + Vite + Element Plus 构建的现代化博客系统。</p>
        
        <el-divider />
        <h2>✨ 主要功能</h2>
        <ul>
          <li>📱 响应式设计，完美适配各种设备</li>
          <li>📝 Markdown 文章编辑与展示</li>
          <li>🔍 全文搜索功能</li>
          <li>🏷️ 文章分类和标签</li>
          <li>💬 评论系统</li>
          <li>👤 用户登录注册</li>
          <li>⚡ 代码高亮显示</li>
        </ul>

        <el-divider />
        <h2>🛠️ 技术栈</h2>
        <div class="tech-stack">
          <el-tag type="success">Vue 3</el-tag>
          <el-tag type="primary">Vite</el-tag>
          <el-tag type="warning">Element Plus</el-tag>
          <el-tag type="danger">Vue Router</el-tag>
          <el-tag type="info">Pinia</el-tag>
          <el-tag>Axios</el-tag>
          <el-tag type="success">Markdown-it</el-tag>
          <el-tag type="primary">Highlight.js</el-tag>
        </div>

        <el-divider />
        <h2>📧 联系方式</h2>
        <p>如有任何问题或建议，欢迎联系我们：</p>
        <p>📮 Email: panyu_lucky9527@163.com</p>
        <p>🌐 Website: myblog.wiki</p>

        <el-divider />
        <div class="footer-info">
          <p>© 2025 MyBlog. All rights reserved.</p>
          <p>Powered by Vue 3 + Vite</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped>
.about-page {
  max-width: 900px;
  margin: 0 auto;
}

.about-card {
  padding: 20px;
}

h1 {
  font-size: 32px;
  color: #303133;
  text-align: center;
}

h2 {
  font-size: 24px;
  color: #409eff;
  margin: 20px 0 15px;
}

.content {
  font-size: 16px;
  line-height: 1.8;
  color: #606266;
}

ul {
  padding-left: 20px;
}

li {
  margin: 10px 0;
}

.tech-stack {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 15px 0;
}

.footer-info {
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.footer-info p {
  margin: 5px 0;
}

@media (max-width: 768px) {
  h1 {
    font-size: 24px;
  }

  h2 {
    font-size: 20px;
  }

  .content {
    font-size: 14px;
  }
}
</style>
