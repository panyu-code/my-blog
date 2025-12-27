<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <h2>用户登录</h2>
          <p>欢迎回来！请登录您的账户</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          size="large"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名/邮箱"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <div class="remember-forgot">
              <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
              <el-button type="primary" link>忘记密码？</el-button>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              style="width: 100%"
              :loading="loading"
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>

          <el-form-item>
            <div class="register-link">
              还没有账号？
              <el-button type="primary" link @click="router.push('/register')">
                立即注册
              </el-button>
            </div>
          </el-form-item>
        </el-form>

        <el-divider>其他登录方式</el-divider>

        <div class="social-login">
          <el-button circle>
            <el-icon :size="20"><MessageBox /></el-icon>
          </el-button>
          <el-button circle>
            <el-icon :size="20"><ChatDotRound /></el-icon>
          </el-button>
          <el-button circle>
            <el-icon :size="20"><Connection /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, MessageBox, ChatDotRound, Connection } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

// 从 localStorage 读取记住的用户信息
const savedUsername = localStorage.getItem('rememberedUsername') || ''
const savedPassword = localStorage.getItem('rememberedPassword') || ''
const isRemembered = localStorage.getItem('rememberMe') === 'true'

const loginForm = reactive({
  username: savedUsername,
  password: savedPassword,
  remember: isRemembered
})

const rules = {
  username: [
    { required: true, message: '请输入用户名/邮箱', trigger: 'blur' },
    { min: 3, message: '用户名至少3个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    // 处理“记住我”功能
    if (loginForm.remember) {
      localStorage.setItem('rememberedUsername', loginForm.username)
      localStorage.setItem('rememberedPassword', loginForm.password)
      localStorage.setItem('rememberMe', 'true')
    } else {
      localStorage.removeItem('rememberedUsername')
      localStorage.removeItem('rememberedPassword')
      localStorage.removeItem('rememberMe')
    }
    
    const result = await userStore.loginAction({
      username: loginForm.username,
      password: loginForm.password
    })
    
    // 显示上次登录信息
    const userInfo = result.data.userInfo
    if (userInfo.lastLoginTime || userInfo.lastLoginIp) {
      const lastLoginMsg = `欢迎回来！${
        userInfo.lastLoginTime ? `上次登录时间：${userInfo.lastLoginTime}` : ''
      }${
        userInfo.lastLoginIp ? ` IP：${userInfo.lastLoginIp}` : ''
      }`
      ElMessage.success(lastLoginMsg)
    } else {
      ElMessage.success('登录成功')
    }
    
    router.push('/')
  } catch (error) {
    if (error !== false) {
      console.error('登录失败:', error)
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  console.log('登录页加载，记住我状态:', loginForm.remember)
})
</script>

<style scoped>
.login-page {
  min-height: calc(100vh - 64px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 450px;
}

.login-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 10px;
}

.login-header p {
  font-size: 14px;
  color: #909399;
}

.remember-forgot {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.register-link {
  width: 100%;
  text-align: center;
  color: #606266;
  font-size: 14px;
}

.social-login {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .login-card {
    padding: 30px 20px;
  }

  .login-header h2 {
    font-size: 24px;
  }
}
</style>
