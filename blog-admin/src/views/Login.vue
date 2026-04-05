<template>
  <div class="login-container">
    <!-- 六边形网格背景 -->
    <div class="hexagon-grid"></div>
    <!-- 环形装饰 -->
    <div class="ring-decoration"></div>
    <div class="ring-decoration"></div>
    
    <!-- 代码雨 -->
    <div class="code-rain">01001</div>
    <div class="code-rain">11010</div>
    <div class="code-rain">00110</div>
    <div class="code-rain">10101</div>
    <div class="code-rain">01101</div>
    
    <!-- 数据流 -->
    <div class="data-stream"></div>
    <div class="data-stream"></div>
    <div class="data-stream"></div>
    <div class="data-stream"></div>
    
    <!-- 光晕背景 -->
    <div class="light-orb"></div>
    <div class="light-orb-center"></div>
    
    <!-- 星空背景 -->
    <div class="stars"></div>
    
    <!-- 流星效果 -->
    <div class="meteor"></div>
    <div class="meteor"></div>
    <div class="meteor"></div>
    
    <!-- 浮动粒子 -->
    <div class="particle"></div>
    <div class="particle"></div>
    <div class="particle"></div>
    <div class="particle"></div>
    
    <!-- 几何装饰元素 -->
    <div class="geometric-shape"></div>
    <div class="geometric-shape"></div>
    
    <div class="login-box">
      <!-- 边框流动光效 -->
      <div class="border-glow"></div>
      
      <!-- 顶部装饰线 -->
      <div class="top-accent"></div>
      
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
              <div class="remember-forgot">
                <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
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
import crypto from '../utils/crypto'

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

// 从 localStorage 读取记住的用户信息
const savedUsername = localStorage.getItem('admin_rememberedUsername') || ''
// 使用加密工具解密密码
const savedEncryptedPassword = localStorage.getItem('admin_rememberedEncryptedPassword') || ''
const savedPassword = savedEncryptedPassword ? crypto.decrypt(savedEncryptedPassword) : ''
const isRemembered = localStorage.getItem('admin_rememberMe') === 'true'

const loginForm = reactive({
  username: savedUsername,
  password: savedPassword,
  email: '',
  emailCaptcha: '',
  captcha: '',
  remember: isRemembered  // 添加记住我字段
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
        password: crypto.encrypt(loginForm.password), // 加密密码后再发送
        captchaId: accountCaptchaId.value,
        captcha: loginForm.captcha
      }
      
      // 处理"记住我"功能 - 安全存储密码
      if (loginForm.remember) {
        localStorage.setItem('admin_rememberedUsername', loginForm.username)
        // 加密后存储密码，而不是明文存储
        const encryptedPassword = crypto.encrypt(loginForm.password)
        localStorage.setItem('admin_rememberedEncryptedPassword', encryptedPassword)
        localStorage.setItem('admin_rememberMe', 'true')
      } else {
        localStorage.removeItem('admin_rememberedUsername')
        localStorage.removeItem('admin_rememberedEncryptedPassword')  // 删除加密密码
        localStorage.removeItem('admin_rememberMe')
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
  background: linear-gradient(135deg, #2D384A 0%, #3a3a3a 50%, #4D5D72 100%);
  position: relative;
  overflow: hidden;
}

/* 动态光晕背景 */
.login-container::before {
  content: '';
  position: absolute;
  width: 800px;
  height: 800px;
  background: radial-gradient(circle, rgba(255, 138, 128, 0.15) 0%, transparent 70%);
  top: -300px;
  right: -200px;
  animation: pulse 8s ease-in-out infinite;
  filter: blur(60px);
}

/* 第二个光晕 */
.light-orb {
  position: absolute;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(255, 209, 128, 0.12) 0%, transparent 70%);
  bottom: -200px;
  left: -150px;
  animation: pulse 10s ease-in-out infinite reverse;
  filter: blur(50px);
}

/* 第三个光晕 - 中心位置 */
.light-orb-center {
  position: absolute;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.05) 0%, transparent 70%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: pulse 12s ease-in-out infinite;
  filter: blur(40px);
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.8;
  }
}

/* 星空背景点 */
.stars {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(2px 2px at 20px 30px, rgba(255, 255, 255, 0.8), transparent),
    radial-gradient(2px 2px at 40px 70px, rgba(255, 255, 255, 0.6), transparent),
    radial-gradient(1px 1px at 90px 40px, rgba(255, 255, 255, 0.9), transparent),
    radial-gradient(2px 2px at 160px 120px, rgba(255, 255, 255, 0.7), transparent),
    radial-gradient(1px 1px at 230px 80px, rgba(255, 255, 255, 0.8), transparent),
    radial-gradient(2px 2px at 300px 150px, rgba(255, 255, 255, 0.6), transparent),
    radial-gradient(1px 1px at 400px 60px, rgba(255, 255, 255, 0.9), transparent),
    radial-gradient(2px 2px at 500px 200px, rgba(255, 255, 255, 0.7), transparent);
  background-size: 550px 250px;
  animation: twinkle 4s ease-in-out infinite alternate;
  opacity: 0.6;
}

/* 流星效果 */
.meteor {
  position: absolute;
  width: 2px;
  height: 2px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.1), 0 0 0 8px rgba(255, 255, 255, 0.1), 0 0 20px rgba(255, 255, 255, 1);
  animation: meteor 3s linear infinite;
  opacity: 0;
}

.meteor::before {
  content: '';
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 200px;
  height: 1px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 1), transparent);
  right: 0;
}

.meteor:nth-child(1) {
  top: 20%;
  left: 80%;
  animation-delay: 0s;
  animation-duration: 3s;
}

.meteor:nth-child(2) {
  top: 40%;
  left: 90%;
  animation-delay: 2s;
  animation-duration: 4s;
}

.meteor:nth-child(3) {
  top: 60%;
  left: 85%;
  animation-delay: 4s;
  animation-duration: 3.5s;
}

@keyframes meteor {
  0% {
    transform: rotate(-45deg) translateX(0);
    opacity: 1;
  }
  70% {
    opacity: 1;
  }
  100% {
    transform: rotate(-45deg) translateX(-1000px);
    opacity: 0;
  }
}

/* 几何图形装饰 */
.login-container::after {
  content: '';
  position: absolute;
  width: 600px;
  height: 600px;
  border: 2px solid rgba(255, 255, 255, 0.06);
  border-radius: 50%;
  top: -200px;
  right: -200px;
  animation: rotate 30s linear infinite;
}

/* 额外的装饰圆圈 */
.geometric-shape {
  position: absolute;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 50%;
  animation: rotate 25s linear infinite reverse;
}

.geometric-shape:nth-child(1) {
  width: 400px;
  height: 400px;
  bottom: -150px;
  left: -150px;
  animation-duration: 35s;
}

.geometric-shape:nth-child(2) {
  width: 250px;
  height: 250px;
  top: 50%;
  left: 10%;
  border-color: rgba(255, 255, 255, 0.05);
  animation-duration: 20s;
}

/* 浮动粒子 */
.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: rgba(255, 138, 128, 0.6);
  border-radius: 50%;
  animation: float-particle 15s infinite;
  box-shadow: 0 0 10px rgba(255, 138, 128, 0.8);
}

.particle:nth-child(1) {
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.particle:nth-child(2) {
  top: 60%;
  left: 80%;
  animation-delay: 3s;
  background: rgba(255, 209, 128, 0.6);
  box-shadow: 0 0 10px rgba(255, 209, 128, 0.8);
}

.particle:nth-child(3) {
  top: 80%;
  left: 30%;
  animation-delay: 6s;
}

.particle:nth-child(4) {
  top: 30%;
  left: 70%;
  animation-delay: 9s;
  background: rgba(255, 209, 128, 0.6);
  box-shadow: 0 0 10px rgba(255, 209, 128, 0.8);
}

@keyframes float-particle {
  0%, 100% {
    transform: translate(0, 0) scale(1);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  50% {
    transform: translate(100px, -100px) scale(1.5);
  }
}

/* 六边形网格背景 */
.hexagon-grid {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(30deg, rgba(255, 255, 255, 0.03) 12%, transparent 12.5%, transparent 87%, rgba(255, 255, 255, 0.03) 87.5%, rgba(255, 255, 255, 0.03)),
    linear-gradient(150deg, rgba(255, 255, 255, 0.03) 12%, transparent 12.5%, transparent 87%, rgba(255, 255, 255, 0.03) 87.5%, rgba(255, 255, 255, 0.03)),
    linear-gradient(30deg, rgba(255, 255, 255, 0.03) 12%, transparent 12.5%, transparent 87%, rgba(255, 255, 255, 0.03) 87.5%, rgba(255, 255, 255, 0.03)),
    linear-gradient(150deg, rgba(255, 255, 255, 0.03) 12%, transparent 12.5%, transparent 87%, rgba(255, 255, 255, 0.03) 87.5%, rgba(255, 255, 255, 0.03)),
    linear-gradient(60deg, rgba(255, 255, 255, 0.02) 25%, transparent 25.5%, transparent 75%, rgba(255, 255, 255, 0.02) 75%, rgba(255, 255, 255, 0.02)),
    linear-gradient(60deg, rgba(255, 255, 255, 0.02) 25%, transparent 25.5%, transparent 75%, rgba(255, 255, 255, 0.02) 75%, rgba(255, 255, 255, 0.02));
  background-size: 80px 140px;
  background-position: 0 0, 0 0, 40px 70px, 40px 70px, 0 0, 40px 70px;
  opacity: 0.3;
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% {
    background-position: 0 0, 0 0, 40px 70px, 40px 70px, 0 0, 40px 70px;
  }
  100% {
    background-position: 80px 140px, 80px 140px, 120px 210px, 120px 210px, 80px 140px, 120px 210px;
  }
}

@keyframes scan {
  0% {
    top: 0%;
  }
  100% {
    top: 100%;
  }
}

/* 数据流效果 */
.data-stream {
  position: absolute;
  width: 1px;
  height: 100px;
  background: linear-gradient(to bottom, transparent, rgba(255, 209, 128, 0.6), transparent);
  animation: dataFlow 5s linear infinite;
  opacity: 0.4;
}

.data-stream:nth-child(1) {
  left: 15%;
  animation-delay: 0s;
}

.data-stream:nth-child(2) {
  left: 35%;
  animation-delay: 1.5s;
}

.data-stream:nth-child(3) {
  left: 55%;
  animation-delay: 3s;
}

.data-stream:nth-child(4) {
  left: 75%;
  animation-delay: 4.5s;
}

@keyframes dataFlow {
  0% {
    top: -100px;
    opacity: 0;
  }
  10% {
    opacity: 0.4;
  }
  90% {
    opacity: 0.4;
  }
  100% {
    top: 100%;
    opacity: 0;
  }
}

@keyframes lineExpand {
  0%, 100% {
    opacity: 0.2;
    transform: scaleX(0.5);
  }
  50% {
    opacity: 0.6;
    transform: scaleX(1);
  }
}

@keyframes badgeFloat {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-10px) rotate(2deg);
  }
}

/* 环形进度装饰 */
.ring-decoration {
  position: absolute;
  width: 80px;
  height: 80px;
  border: 3px solid rgba(255, 138, 128, 0.2);
  border-top-color: rgba(255, 138, 128, 0.6);
  border-radius: 50%;
  animation: ringRotate 10s linear infinite;
}

.ring-decoration::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 50px;
  height: 50px;
  border: 2px solid rgba(255, 209, 128, 0.2);
  border-bottom-color: rgba(255, 209, 128, 0.6);
  border-radius: 50%;
  animation: ringRotate 7s linear infinite reverse;
}

.ring-decoration:nth-child(1) {
  top: 15%;
  right: 12%;
}

.ring-decoration:nth-child(2) {
  bottom: 18%;
  left: 8%;
  animation-duration: 12s;
}

@keyframes ringRotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 代码雨效果 */
.code-rain {
  position: absolute;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  color: rgba(255, 138, 128, 0.3);
  writing-mode: vertical-rl;
  text-orientation: upright;
  letter-spacing: 8px;
  animation: codeFall 8s linear infinite;
  opacity: 0;
  pointer-events: none;
}

.code-rain:nth-child(1) {
  left: 5%;
  animation-delay: 0s;
}

.code-rain:nth-child(2) {
  left: 25%;
  animation-delay: 2s;
}

.code-rain:nth-child(3) {
  left: 45%;
  animation-delay: 4s;
}

.code-rain:nth-child(4) {
  left: 65%;
  animation-delay: 6s;
}

.code-rain:nth-child(5) {
  left: 85%;
  animation-delay: 1s;
}

@keyframes codeFall {
  0% {
    top: -100%;
    opacity: 0;
  }
  10% {
    opacity: 0.3;
  }
  90% {
    opacity: 0.3;
  }
  100% {
    top: 100%;
    opacity: 0;
  }
}

@keyframes twinkle {
  0% {
    opacity: 0.4;
  }
  100% {
    opacity: 0.8;
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.login-box {
  width: 440px;
  padding: 50px;
  background: rgba(35, 35, 35, 0.93);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 
    0 25px 80px rgba(0, 0, 0, 0.6),
    0 0 0 1px rgba(255, 255, 255, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.08);
  position: relative;
  z-index: 1;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

/* 卡片光泽扫过效果 */
.login-box::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent 30%,
    rgba(255, 255, 255, 0.03) 50%,
    transparent 70%
  );
  transform: rotate(45deg);
  animation: shine 6s ease-in-out infinite;
  pointer-events: none;
}

@keyframes shine {
  0% {
    transform: translateX(-100%) translateY(-100%) rotate(45deg);
  }
  100% {
    transform: translateX(100%) translateY(100%) rotate(45deg);
  }
}

/* 卡片边框流动光效 */
.border-glow {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, #ff8a80, #ffd180, #ff8a80, #ffd180);
  background-size: 400% 400%;
  border-radius: 22px;
  z-index: -1;
  animation: borderFlow 8s ease infinite;
  opacity: 0;
  transition: opacity 0.3s;
  filter: blur(8px);
}

.login-box:hover .border-glow {
  opacity: 0.6;
}

@keyframes borderFlow {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.login-box:hover {
  transform: translateY(-8px);
  box-shadow: 
    0 30px 90px rgba(0, 0, 0, 0.7),
    0 0 0 1px rgba(255, 255, 255, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.12),
    0 0 40px rgba(255, 138, 128, 0.1);
}

/* 卡片顶部装饰线 */
.top-accent {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60%;
  height: 3px;
  background: linear-gradient(90deg, transparent, #ff8a80, #ffd180, transparent);
  border-radius: 0 0 2px 2px;
  box-shadow: 0 0 10px rgba(255, 138, 128, 0.5);
  animation: glow 3s ease-in-out infinite alternate;
}

@keyframes glow {
  0% {
    box-shadow: 0 0 10px rgba(255, 138, 128, 0.3);
  }
  100% {
    box-shadow: 0 0 20px rgba(255, 138, 128, 0.6), 0 0 30px rgba(255, 209, 128, 0.4);
  }
}

.login-title {
  text-align: center;
  margin-bottom: 40px;
  font-size: 28px;
  color: #ffffff;
  font-weight: 700;
  letter-spacing: 3px;
  text-transform: uppercase;
  position: relative;
  text-shadow: 0 0 20px rgba(255, 255, 255, 0.25);
  animation: titleFloat 3s ease-in-out infinite;
}

@keyframes titleFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-3px);
  }
}

.login-title::after {
  content: '';
  position: absolute;
  bottom: -12px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #ff8a80, #ffd180, transparent);
  box-shadow: 0 0 10px rgba(255, 138, 128, 0.5);
  animation: lineExpand 3s ease-in-out infinite;
}

@keyframes lineExpand {
  0%, 100% {
    width: 80px;
    opacity: 0.8;
  }
  50% {
    width: 100px;
    opacity: 1;
  }
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

/* 添加记住我和忘记密码的样式 */
.remember-forgot {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>