<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-card">
        <div class="register-header">
          <h2>用户注册</h2>
          <p>创建您的账户，开始您的博客之旅</p>
        </div>

        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="rules"
          size="large"
          @submit.prevent="handleRegister"
        >
          <!-- 用户名 -->
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <!-- 邮箱 -->
          <el-form-item prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱"
              :prefix-icon="Message"
              clearable
            />
          </el-form-item>

          <!-- 密码 -->
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <!-- 确认密码 -->
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请确认密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <!-- 邮箱验证码 - 放在最后 -->
          <el-form-item prop="emailCaptcha">
            <div class="capture-wrapper">
              <el-input
                  v-model="registerForm.emailCaptcha"
                  placeholder="请输入验证码"
                  clearable
                  class="captcha-input"
              />
              <el-button
                  type="primary"
                  @click="sendCaptcha"
                  :loading="captchaLoading"
                  :disabled="captchaCountdown > 0 || !registerForm.email"
                  class="send-btn"
              >
                {{ captchaCountdown > 0 ? `${captchaCountdown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <!-- 用户协议 -->
          <el-form-item prop="agree">
            <el-checkbox v-model="registerForm.agree">
              我已阅读并同意
              <el-button type="primary" link>《用户协议》</el-button>
              和
              <el-button type="primary" link>《隐私政策》</el-button>
            </el-checkbox>
          </el-form-item>
          <!-- 注册按钮 -->
          <el-form-item>
            <el-button
              type="primary"
              style="width: 100%"
              :loading="loading"
              @click="handleRegister"
            >
              注册
            </el-button>
          </el-form-item>

          <!-- 登录链接 -->
          <el-form-item>
            <div class="login-link">
              已有账号？
              <el-button type="primary" link native-type="button" @click="() => router.push('/login')">
                立即登录
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const userStore = useUserStore()

const registerFormRef = ref(null)
const loading = ref(false)
const captchaLoading = ref(false)
const captchaCountdown = ref(0)

const registerForm = reactive({
  username: '',
  email: '',
  emailCaptcha: '',
  password: '',
  confirmPassword: '',
  agree: false
})

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (registerForm.confirmPassword !== '') {
      registerFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const validateAgree = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请阅读并同意用户协议'))
  } else {
    callback()
  }
}

// 异步校验用户名一性
const validateUsername = async (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入用户名'))
    return
  }
  
  try {
    const res = await request.get('/user/check-username', {
      params: { username: value }
    })
    // 检查exists是否为true（用户名已存在）
    if (res && res.data && res.data.exists === true) {
      // 用户名已存在，不能注册
      callback(new Error('用户名已存在'))
    } else {
      // 用户名不存在，可以注册
      callback()
    }
  } catch (error) {
    console.error('校验用户名失败:', error)
    // 校验失败也注册（便于继续填写）
    callback()
  }
}

// 异步校验邮箱一性
const validateEmail = async (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入邮箱'))
    return
  }
  
  try {
    const res = await request.get('/user/check-email', {
      params: { email: value }
    })
    // 检查exists是否为true（邮箱已存在）
    if (res && res.data && res.data.exists === true) {
      // 邮箱已存在，不能注册
      callback(new Error('邮箱已存在'))
    } else {
      // 邮箱不存在，可以注册
      callback()
    }
  } catch (error) {
    console.error('校验邮箱失败:', error)
    // 校验失败也注册（便于继续填写）
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' },
    { validator: validateUsername, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
    { validator: validateEmail, trigger: 'blur' }
  ],
  emailCaptcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ],
  agree: [
    { validator: validateAgree, trigger: 'change' }
  ]
}

// 发送验证码
const sendCaptcha = async () => {
  try {
    if (!registerForm.email) {
      ElMessage.error('请先输入邮箱')
      return
    }
    
    // 验证邮箱格式
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(registerForm.email)) {
      ElMessage.error('请输入正确的邮箱地址')
      return
    }
    
    captchaLoading.value = true
    const response = await fetch('/api/captcha/send', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ email: registerForm.email })
    })
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('验证码已发送到邮箱')
      startCountdown()
    } else {
      ElMessage.error(data.message || '发送失败')
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

const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    loading.value = true

    await userStore.registerAction({
      username: registerForm.username,
      email: registerForm.email,
      emailCaptcha: registerForm.emailCaptcha,
      password: registerForm.password
    })

    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    if (error !== false) {
      console.error('注册失败:', error)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4a5f8f 0%, #2c3e50 100%);
  padding: 20px;
}

.register-container {
  width: 100%;
  max-width: 450px;
}

.register-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 10px;
}

.register-header p {
  font-size: 14px;
  color: #909399;
}

.login-link {
  width: 100%;
  text-align: center;
  color: #606266;
  font-size: 14px;
}

.capture-wrapper {
  display: flex;
  gap: 10px;
  align-items: center;
  width: 100%;
}

.captcha-input {
  flex: 1;
}

.send-btn {
  width: 120px;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .register-card {
    padding: 30px 20px;
  }

  .register-header h2 {
    font-size: 24px;
  }
}
</style>
