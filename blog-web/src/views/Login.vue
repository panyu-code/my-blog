<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <h2>用户登录</h2>
          <p>欢迎回来！请登录您的账户</p>
        </div>

        <el-tabs v-model="loginType" class="login-tabs">
          <!-- 账号密码登陆 -->
          <el-tab-pane label="账号登陆" name="account">
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

              <!-- 图形验证码 -->
              <el-form-item prop="captcha">
                <div class="capture-input-wrapper">
                  <el-input
                    v-model="loginForm.captcha"
                    placeholder="请输入验证码"
                    clearable
                    class="capture-input"
                  />
                  <el-button
                    type="primary"
                    @click="getImageCaptcha"
                    :loading="accountCaptchaLoading"
                    :disabled="!canGetAccountCaptcha"
                    class="image-captcha-btn"
                  >
                    <img v-if="accountCaptchaImage" :src="accountCaptchaImage" alt="验证码" />
                    <span v-else>获取验证码</span>
                  </el-button>
                </div>
              </el-form-item>

              <el-form-item>
                <div class="remember-forgot">
                  <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
                  <el-button type="primary" link @click="router.push('/forgot-password')">忘记密码？</el-button>
                </div>
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  style="width: 100%"
                  :loading="loading"
                  :disabled="!canAccountLogin"
                  @click="handleLogin"
                >
                  登录
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- 邮箱验证码登陆 -->
          <el-tab-pane label="邮箱登陆" name="email">
            <el-form
              ref="emailLoginFormRef"
              :model="emailLoginForm"
              :rules="emailRules"
              size="large"
              @submit.prevent="handleLogin"
            >
              <el-form-item prop="email">
                <el-input
                  v-model="emailLoginForm.email"
                  placeholder="请输入邮箱"
                  :prefix-icon="Message"
                  clearable
                />
              </el-form-item>

              <el-form-item prop="emailCaptcha">
                <div class="capture-input-wrapper">
                  <el-input
                    v-model="emailLoginForm.emailCaptcha"
                    placeholder="请输入验证码"
                    size="large"
                    @keyup.enter="handleLogin"
                    class="capture-input"
                  />
                  <el-button
                    type="primary"
                    size="large"
                    @click="sendCaptcha"
                    :loading="captchaLoading"
                    :disabled="!canGetEmailCaptcha || captchaCountdown > 0"
                    class="send-btn"
                  >
                    {{ captchaCountdown > 0 ? `${captchaCountdown}s` : '获取验证码' }}
                  </el-button>
                </div>
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  style="width: 100%"
                  :loading="loading"
                  :disabled="!canEmailLogin"
                  @click="handleLogin"
                >
                  登录
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>

        <el-form ref="loginFormRef" style="margin-top: 20px">
          <el-form-item>
            <div class="register-link">
              还没有账号？
              <el-button type="primary" link @click="router.push('/register')">
                立即注册
              </el-button>
            </div>
          </el-form-item>
        </el-form>

<!--        <el-divider>其他登录方式</el-divider>-->

<!--        <div class="social-login">-->
<!--          <el-button circle>-->
<!--            <el-icon :size="20"><MessageBox /></el-icon>-->
<!--          </el-button>-->
<!--          <el-button circle>-->
<!--            <el-icon :size="20"><ChatDotRound /></el-icon>-->
<!--          </el-button>-->
<!--          <el-button circle>-->
<!--            <el-icon :size="20"><Connection /></el-icon>-->
<!--          </el-button>-->
<!--        </div>-->
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, MessageBox, ChatDotRound, Connection, Message } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { sendEmailCaptcha } from '../api/user'
import crypto from '../utils/crypto'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const emailLoginFormRef = ref(null)
const loading = ref(false)
const loginType = ref('account')
const captchaLoading = ref(false)
const captchaCountdown = ref(0)
const accountCaptchaLoading = ref(false)
const accountCaptchaImage = ref(null)
const accountCaptchaId = ref(null)
// 表单校验是否通过
const isAccountFormValid = ref(false)
const isEmailFormValid = ref(false)

// 从 localStorage 读取记住的用户信息
const savedUsername = localStorage.getItem('rememberedUsername') || ''
// 使用加密工具解密密码
const savedEncryptedPassword = localStorage.getItem('rememberedEncryptedPassword') || ''
const savedPassword = savedEncryptedPassword ? crypto.decrypt(savedEncryptedPassword) : ''
const isRemembered = localStorage.getItem('rememberMe') === 'true'

const loginForm = reactive({
  username: savedUsername,
  password: savedPassword,
  remember: isRemembered,
  captcha: ''
})

const emailLoginForm = reactive({
  email: '',
  emailCaptcha: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名/邮箱', trigger: 'blur' },
    { min: 3, message: '用户名至少3个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

// 邮箱验证规则
const emailRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  emailCaptcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

// 计算属性：验证表单有效性
const canGetAccountCaptcha = computed(() => {
  return (
    loginForm.username && loginForm.username.length >= 3 &&
    loginForm.password && loginForm.password.length >= 6
  )
})

const canAccountLogin = computed(() => {
  return (
    loginForm.username && loginForm.username.length >= 3 &&
    loginForm.password && loginForm.password.length >= 6 &&
    loginForm.captcha && loginForm.captcha.length > 0
  )
})

const canGetEmailCaptcha = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailLoginForm.email && emailRegex.test(emailLoginForm.email)
})

const canEmailLogin = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return (
    emailLoginForm.email && emailRegex.test(emailLoginForm.email) &&
    emailLoginForm.emailCaptcha && emailLoginForm.emailCaptcha.length > 0
  )
})

// 获取图形验证码
const getImageCaptcha = async () => {
  try {
    accountCaptchaLoading.value = true
    const response = await fetch('/api/captcha/image')
    const data = await response.json()
    if (data.code === 200) {
      accountCaptchaImage.value = data.data.image
      accountCaptchaId.value = data.data.captchaId
      // 清空之前的验证码输入
      loginForm.captcha = ''
    } else {
      ElMessage.error('获取验证码失败')
    }
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败')
  } finally {
    accountCaptchaLoading.value = false
  }
}

// 发送邮箱验证码
const sendCaptcha = async () => {
  try {
    if (!emailLoginForm.email) {
      ElMessage.error('请先输入邮箱')
      return
    }
    captchaLoading.value = true
    const res = await sendEmailCaptcha(emailLoginForm.email)
    if (res.code === 200) {
      ElMessage.success('验证码已发送到邮箱')
      startCountdown()
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
  } finally {
    captchaLoading.value = false
  }
}

// 倒计时
const startCountdown = () => {
  captchaCountdown.value = 60
  const timer = setInterval(() => {
    captchaCountdown.value--
    if (captchaCountdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const handleLogin = async () => {
  try {
    loading.value = true
    
    let loginData
    let formRef
    
    if (loginType.value === 'account') {
      // 账号登录（带图形验证码）
      await loginFormRef.value.validate()
      formRef = loginFormRef
      
      // 处理"记住我"功能 - 安全存储密码
      if (loginForm.remember) {
        localStorage.setItem('rememberedUsername', loginForm.username)
        // 加密后存储密码，而不是明文存储
        const encryptedPassword = crypto.encrypt(loginForm.password)
        localStorage.setItem('rememberedEncryptedPassword', encryptedPassword)
        localStorage.setItem('rememberMe', 'true')
      } else {
        localStorage.removeItem('rememberedUsername')
        localStorage.removeItem('rememberedEncryptedPassword')  // 删除加密密码
        localStorage.removeItem('rememberMe')
      }
      
      loginData = {
        username: loginForm.username,
        password: loginForm.password,
        captchaId: accountCaptchaId.value,
        captcha: loginForm.captcha
      }
    } else {
      // 邮箱登录
      await emailLoginFormRef.value.validate()
      formRef = emailLoginFormRef
      
      loginData = {
        email: emailLoginForm.email,
        emailCaptcha: emailLoginForm.emailCaptcha
      }
    }
    
    const result = await userStore.loginAction(loginData)
    
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
      console.log('错误对象:', error)
      console.log('error.response:', error?.response)
      console.log('error.message:', error?.message)
      let errorMsg = '登录失败，请重试'
      // 处理不同类型的错误对象
      if (error && typeof error === 'object') {
        if (error.response?.data?.message) {
          errorMsg = error.response.data.message
        } else if (error.message) {
          errorMsg = error.message
        }
      } else if (typeof error === 'string') {
        errorMsg = error
      }
      console.error('登录失败:', errorMsg)
      // 如果验证码已过期，自动刷新验证码
      if (errorMsg.includes('验证码已过期')) {
        ElMessage.warning('验证码已过期，已为您刷新')
        getImageCaptcha()
      }
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 初始化时获取一张图形验证码
  getImageCaptcha()
  console.log('登录页加载，记住我状态:', loginForm.remember)
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 450px;
}

.login-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
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

.capture-input-wrapper {
  display: flex;
  gap: 10px;
  align-items: center;
  width: 100%;
}

.capture-input {
  flex: 1;
}

.image-captcha-btn {
  width: 120px;
  height: 40px;
  padding: 0;
  overflow: hidden;
  border: none;
  background: transparent !important;
}

.image-captcha-btn img {
  width: 100%;
  height: 100%;
  display: block;
  cursor: pointer;
}

.send-btn {
  width: 120px;
  flex-shrink: 0;
}

.login-tabs {
  :deep(.el-tabs__content) {
    padding: 0 !important;
  }
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
