<template>
  <div class="not-found">
    <div class="content">
      <h1>404</h1>
      <h2>页面未找到</h2>
      <p>抱歉，您访问的页面不存在</p>
      <el-button type="primary" size="large" @click="router.push('/')">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.not-found {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.content {
  text-align: center;
}

h1 {
  font-size: 120px;
  color: #409eff;
  margin: 0;
  font-weight: bold;
}

h2 {
  font-size: 32px;
  color: #303133;
  margin: 20px 0;
}

p {
  font-size: 16px;
  color: #909399;
  margin-bottom: 30px;
}

@media (max-width: 768px) {
  h1 {
    font-size: 80px;
  }

  h2 {
    font-size: 24px;
  }
}
</style>
