<template>
  <div class="login-container">
    <div class="login-box">
      <h2 class="login-title">博客后台管理系统</h2>
      <el-tabs v-model="loginType" class="login-tabs">
        <el-tab-pane label="账号登录" name="account">
          <el-form :model="loginForm" :rules="rules" ref="accountFormRef" class="login-form">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                size="large"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <!-- 账号登陆验证码 -->
            <el-form-item prop="captcha">
              <div class="capture-input-wrapper">
                <el-input
                  v-model="loginForm.captcha"
                  placeholder="请输入验证码"
                  size="large"
                  @keyup.enter="handleLogin"
                  class="capture-input"
                />
                <el-button
                  type="primary"
                  size="large"
                  @click="getImageCaptcha"
                  :loading="accountCaptchaLoading"
                  :disabled="!isValidUsername(loginForm.username) || !isValidPassword(loginForm.password)"
                  class="send-btn"
                >
                  <img v-if="accountCaptchaImage" :src="accountCaptchaImage" alt="验证码" style="height: 100%; width: 100%;" />
                  <span v-else>获取验证码</span>
                </el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                :disabled="!isValidUsername(loginForm.username) || !isValidPassword(loginForm.password) || !loginForm.captcha"
                @click="handleLogin"
                class="login-btn"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="邮箱登录" name="email">
          <el-form :model="loginForm" :rules="rules" ref="emailFormRef" class="login-form">
            <el-form-item prop="email">
              <el-input
                v-model="loginForm.email"
                placeholder="请输入邮箱"
                prefix-icon="Message"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="emailCaptcha">
              <div class="capture-input-wrapper">
                <el-input
                    v-model="loginForm.emailCaptcha"
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
                  :disabled="!isValidEmail(loginForm.email) || captchaCountdown > 0"
                  class="email-send-btn"
                >
                  {{ captchaCountdown > 0 ? `${captchaCountdown}s` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                :disabled="!isValidEmail(loginForm.email) || !loginForm.emailCaptcha"
                @click="handleLogin"
                class="login-btn"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { login, sendEmailCaptcha } from '../api/user'
import { adminLogin } from '../api/user'

const router = useRouter()
const userStore = useUserStore()
const accountFormRef = ref(null)
const emailFormRef = ref(null)
const loading = ref(false)
const loginType = ref('account')
const captchaLoading = ref(false)
const captchaCountdown = ref(0)
const accountCaptchaLoading = ref(false)
const accountCaptchaImage = ref(null)
const accountCaptchaId = ref(null)

const loginForm = reactive({
  username: '',
  password: '',
  email: '',
  emailCaptcha: '',
  captcha: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  emailCaptcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

// 验证邮箱格式
const isValidEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

// 验证用户名和密码长度
const isValidUsername = (username) => {
  return username && username.length > 0
}

const isValidPassword = (password) => {
  return password && password.length >= 6
}

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
    if (!loginForm.email) {
      ElMessage.error('请先输入邮箱')
      return
    }
    captchaLoading.value = true
    const res = await sendEmailCaptcha(loginForm.email)
    if (res.code === 200) {
      ElMessage.success('验证码已发送到邮箱')
      startCountdown()
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error('发送验证码失败')
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
    console.log('点击登陆按钮')
    console.log('登陆类型:', loginType.value)
    console.log('登陆表单:', loginForm)
    
    // 根据登陆类型选择对应的表单验证
    const formRef = loginType.value === 'account' ? accountFormRef : emailFormRef
    await formRef.value.validate()
    loading.value = true
    
    let loginData
    if (loginType.value === 'account') {
      // 账号登录（带图形验证码）
      loginData = {
        username: loginForm.username,
        password: loginForm.password,
        captchaId: accountCaptchaId.value,
        captcha: loginForm.captcha
      }
    } else {
      // 邮箱登录
      loginData = {
        email: loginForm.email,
        emailCaptcha: loginForm.emailCaptcha
      }
    }
    
    const res = await adminLogin(loginData)
    const { data } = res
    
    // 使用后端返回的 token 和 userInfo
    userStore.setToken(data.token)
    userStore.setUserInfo(data.userInfo)
    
    console.log('登录成功，用户信息:', data.userInfo)
    
    // 显示上次登录信息
    const userInfo = data.userInfo
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
      console.error('完整错误对象:', error)
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
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 24px;
  color: #333;
  font-weight: 600;
}

.login-form {
  margin-top: 20px;
}

.login-btn {
  width: 100%;
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

.send-btn {
  width: 120px;
  flex-shrink: 0;
  padding: 0;
  overflow: hidden;
  border: none;
  background: transparent !important;
}

.send-btn img {
  width: 100%;
  height: 100%;
  display: block;
  cursor: pointer;
}

.email-send-btn {
  width: 120px;
  flex-shrink: 0;
}

.login-tabs {
  :deep(.el-tabs__content) {
    padding: 0 !important;
  }
}
</style>
