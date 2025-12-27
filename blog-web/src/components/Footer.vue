<template>
  <footer class="footer">
    <div class="footer-content">
      <div class="footer-section">
        <h3>关于博客</h3>
        <p>记录技术，分享生活</p>
        <p>专注于前端、后端开发</p>
      </div>

      <div class="footer-section">
        <h3>友情链接</h3>
        <div class="links">
          <a href="#" target="_blank">Vue.js</a>
          <a href="#" target="_blank">Element Plus</a>
          <a href="#" target="_blank">GitHub</a>
        </div>
      </div>

      <div class="footer-section">
        <h3>联系方式</h3>
        <p>Email: blog@example.com</p>
        <div class="social-links">
          <el-icon :size="20"><Message /></el-icon>
          <el-icon :size="20"><ChatDotRound /></el-icon>
          <el-icon :size="20"><Location /></el-icon>
        </div>
      </div>
    </div>

    <div class="footer-bottom">
      <p>&copy; 2024 我的博客. All rights reserved.</p>
      <p>Powered by Vue 3 + Vite</p>
    </div>
  </footer>
</template>

<script setup>
import { Message, ChatDotRound, Location } from '@element-plus/icons-vue'
</script>

<style scoped>
.footer {
  background: #2c3e50;
  color: #ecf0f1;
  margin-top: 50px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
}

.footer-section h3 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #409eff;
}

.footer-section p {
  margin: 8px 0;
  font-size: 14px;
  color: #bdc3c7;
}

.links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.links a {
  color: #bdc3c7;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.links a:hover {
  color: #409eff;
}

.social-links {
  display: flex;
  gap: 15px;
  margin-top: 10px;
}

.social-links .el-icon {
  cursor: pointer;
  transition: color 0.3s;
}

.social-links .el-icon:hover {
  color: #409eff;
}

.footer-bottom {
  border-top: 1px solid #34495e;
  padding: 20px;
  text-align: center;
}

.footer-bottom p {
  margin: 5px 0;
  font-size: 14px;
  color: #95a5a6;
}

@media (max-width: 768px) {
  .footer-content {
    grid-template-columns: 1fr;
    padding: 30px 15px;
  }
}
</style>
