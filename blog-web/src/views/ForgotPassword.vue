<template>
  <div class="forgot-password-page">
    <div class="forgot-password-container">
      <div class="forgot-password-card">
        <div class="forgot-password-header">
          <h2>找回密码</h2>
          <p>请输入您的邮箱地址以找回密码</p>
        </div>

        <!-- 步骤1：输入邮箱并获取验证码 -->
        <el-form
          v-if="step === 1"
          ref="emailFormRef"
          :model="forgotForm"
          :rules="emailRules"
          size="large"
          @submit.prevent="handleSendCode"
        >
          <el-form-item prop="email">
            <el-input
              v-model="forgotForm.email"
              placeholder="请输入邮箱地址"
              :prefix-icon="Message"
              clearable
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              style="width: 100%"
              :loading="sendLoading"
              :disabled="!forgotForm.email"
              @click="handleSendCode"
            >
              获取验证码
            </el-button>
          </el-form-item>

          <el-form-item>
            <div class="back-to-login">
              <el-button type="primary" link @click="router.push('/login')">
                返回登录
              </el-button>
            </div>
          </el-form-item>
        </el-form>

        <!-- 步骤2：输入验证码和新密码 -->
        <el-form
          v-if="step === 2"
          ref="resetFormRef"
          :model="forgotForm"
          :rules="resetRules"
          size="large"
          @submit.prevent="handleResetPassword"
        >
          <p class="step-info">验证码已发送至 {{ forgotForm.email }}</p>

          <el-form-item prop="code">
            <el-input
              v-model="forgotForm.code"
              placeholder="请输入验证码"
              clearable
            />
          </el-form-item>

          <el-form-item prop="newPassword">
            <el-input
              v-model="forgotForm.newPassword"
              type="password"
              placeholder="请输入新密码（至少6位）"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="forgotForm.confirmPassword"
              type="password"
              placeholder="请确认新密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              style="width: 100%"
              :loading="resetLoading"
              :disabled="!forgotForm.code || !forgotForm.newPassword || !forgotForm.confirmPassword"
              @click="handleResetPassword"
            >
              重置密码
            </el-button>
          </el-form-item>

          <el-form-item>
            <div class="back-to-login">
              <el-button type="primary" link @click="goBackToStep1">
                返回
              </el-button>
            </div>
          </el-form-item>
        </el-form>

        <!-- 步骤3：成功提示 -->
        <div v-if="step === 3" class="success-message">
          <el-icon class="success-icon"><CircleCheckFilled /></el-icon>
          <h3>密码重置成功！</h3>
          <p>您的密码已成功重置，请使用新密码登录</p>
          <el-button type="primary" @click="router.push('/login')">
            返回登录
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Lock, Message, CircleCheckFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const emailFormRef = ref(null)
const resetFormRef = ref(null)
const step = ref(1) // 1: 输入邮箱, 2: 输入验证码和新密码, 3: 成功
const sendLoading = ref(false)
const resetLoading = ref(false)

const forgotForm = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

// 验证密码一致性
const validatePasswordConsistency = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请确认密码'))
  } else if (value !== forgotForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const emailRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const resetRules = {
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePasswordConsistency, trigger: 'blur' }
  ]
}

// 第一步：发送验证码
const handleSendCode = async () => {
  try {
    await emailFormRef.value.validate()
    sendLoading.value = true

    const res = await request.post('/user/forgot-password/send-code', {
      email: forgotForm.email
    })

    if (res.code === 200) {
      ElMessage.success('验证码已发送到您的邮箱')
      step.value = 2
    } else {
      ElMessage.error(res.message || '发送验证码失败')
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
  } finally {
    sendLoading.value = false
  }
}

// 第二步：重置密码
const handleResetPassword = async () => {
  try {
    await resetFormRef.value.validate()
    resetLoading.value = true

    const res = await request.post('/user/forgot-password/reset', {
      email: forgotForm.email,
      code: forgotForm.code,
      newPassword: forgotForm.newPassword
    })

    if (res.code === 200) {
      ElMessage.success('密码重置成功！')
      step.value = 3
    } else {
      ElMessage.error(res.message || '密码重置失败')
    }
  } catch (error) {
    console.error('密码重置失败:', error)
  } finally {
    resetLoading.value = false
  }
}

// 返回第一步
const goBackToStep1 = () => {
  step.value = 1
  forgotForm.code = ''
  forgotForm.newPassword = ''
  forgotForm.confirmPassword = ''
}
</script>

<style scoped>
.forgot-password-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.forgot-password-container {
  width: 100%;
  max-width: 450px;
}

.forgot-password-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.forgot-password-header {
  text-align: center;
  margin-bottom: 30px;
}

.forgot-password-header h2 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 10px;
}

.forgot-password-header p {
  font-size: 14px;
  color: #909399;
}

.step-info {
  text-align: center;
  color: #606266;
  margin-bottom: 20px;
  font-size: 14px;
}

.back-to-login {
  width: 100%;
  text-align: center;
  color: #606266;
  font-size: 14px;
}

.success-message {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  font-size: 60px;
  color: #67c23a;
  margin-bottom: 20px;
}

.success-message h3 {
  font-size: 20px;
  color: #303133;
  margin: 15px 0;
}

.success-message p {
  color: #909399;
  margin-bottom: 20px;
  font-size: 14px;
}

@media (max-width: 768px) {
  .forgot-password-card {
    padding: 30px 20px;
  }

  .forgot-password-header h2 {
    font-size: 24px;
  }
}
</style>
