<template>
  <div class="profile-page">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <el-icon :size="20"><User /></el-icon>
          <span>个人中心</span>
        </div>
      </template>

      <div class="profile-content">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <el-avatar :size="120" :src="userStore.userInfo?.avatar">
              {{ userStore.userInfo?.username?.charAt(0) }}
            </el-avatar>
            <div class="avatar-overlay">
              <el-upload
                :action="uploadUrl"
                :headers="uploadHeaders"
                :data="uploadData"
                :show-file-list="false"
                :before-upload="beforeUpload"
                :on-success="handleAvatarSuccess"
                :on-error="handleUploadError"
                :auto-upload="true"
                accept="image/*"
              >
                <template #default>
                  <div class="upload-trigger">
                    <el-icon :size="20"><Camera /></el-icon>
                    <span>更换头像</span>
                  </div>
                </template>
              </el-upload>
            </div>
          </div>
          <div class="user-meta">
            <h2>{{ userStore.userInfo?.username }}</h2>
            <p class="email">{{ userStore.userInfo?.email }}</p>
            <el-tag v-if="userStore.userInfo?.role === 1" type="danger">管理员</el-tag>
            <el-tag v-else type="success">普通用户</el-tag>
          </div>
        </div>

        <el-divider />

        <!-- 基本信息 -->
        <div class="info-section">
          <h3>基本信息</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户名">
              {{ userStore.userInfo?.username }}
            </el-descriptions-item>
            <el-descriptions-item label="昵称">
              {{ userStore.userInfo?.nickname || '未设置' }}
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              {{ userStore.userInfo?.email }}
            </el-descriptions-item>
            <el-descriptions-item label="角色">
              {{ userStore.userInfo?.role === 1 ? '管理员' : '普通用户' }}
            </el-descriptions-item>
            <el-descriptions-item label="最近登录时间">
              {{ formatDate(userStore.userInfo?.lastLoginTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="最近登录IP">
              {{ userStore.userInfo?.lastLoginIp || '未知' }}
            </el-descriptions-item>
            <el-descriptions-item label="注册时间">
              {{ formatDate(userStore.userInfo?.createTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <el-divider />

        <!-- 操作按钮 -->
        <div class="action-section">
          <el-button type="primary" @click="showEditDialog = true">
            <el-icon><Edit /></el-icon>
            编辑资料
          </el-button>
          <el-button type="warning" @click="showPasswordDialog = true">
            <el-icon><Lock /></el-icon>
            修改密码
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 编辑资料对话框 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑资料"
      width="500px"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="80px"
      >
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email"  disabled placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleUpdateProfile">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="修改密码"
      width="500px"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleUpdatePassword">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { User, Edit, Lock, Camera } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { updateUserInfo } from '../api/user'
import { changePassword } from '../api/user'
import dayjs from 'dayjs'

const userStore = useUserStore()
const loading = ref(false)
const showEditDialog = ref(false)
const showPasswordDialog = ref(false)
const editFormRef = ref(null)
const passwordFormRef = ref(null)

// 头像上传配置
const uploadUrl = '/api/upload/image'
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${userStore.token || ''}`
}))
const uploadData = reactive({
  bizDir: 'avatar'
})

const editForm = reactive({
  nickname: '',
  email: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const editRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ]
}

const beforeUpload = (file) => {
  // 检查文件类型
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  
  // 检查文件大小（限制为5MB）
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  
  return true
}

const handleAvatarSuccess = async (response) => {
  if (response.code === 200) {
    const imageUrl = response.data.url
    
    // 更新用户头像
    try {
      await updateUserInfo({
        avatar: imageUrl
      })
      
      // 更新本地存储
      userStore.userInfo.avatar = imageUrl
      localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
      
      ElMessage.success('头像更新成功')
    } catch (error) {
      console.error('更新头像失败:', error)
      ElMessage.error('更新头像失败')
    }
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('上传失败，请重试')
}

const formatDate = (date) => {
  if (!date) return '未知'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const handleUpdateProfile = async () => {
  try {
    await editFormRef.value.validate()
    loading.value = true
    
    // 调用更新用户信息接口
    await updateUserInfo(editForm)
    
    // 更新本地存储
    userStore.userInfo.nickname = editForm.nickname
    userStore.userInfo.email = editForm.email
    localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
    
    ElMessage.success('更新成功')
    showEditDialog.value = false
  } catch (error) {
    console.error('更新失败:', error)
  } finally {
    loading.value = false
  }
}

const handleUpdatePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    loading.value = true
    
    // 核寸两次密码是否相等
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      ElMessage.error('两次密码不一致')
      return
    }
    
    // 调用修改密码接口
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    
    ElMessage.success('密码修改成功，请重新登录')
    
    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    showPasswordDialog.value = false
    
    // 需要重新登录
    setTimeout(() => {
      userStore.logout()
      window.location.href = '/login'
    }, 1000)
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error(error.message || '修改密码失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  editForm.nickname = userStore.userInfo?.nickname || ''
  editForm.email = userStore.userInfo?.email || ''
})
</script>

<style scoped>
.profile-page {
  max-width: 900px;
  margin: 0 auto;
}

.profile-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.profile-content {
  padding: 20px;
}

.avatar-wrapper {
  position: relative;
  width: fit-content;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  cursor: pointer;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: white;
  font-size: 12px;
}

.avatar-overlay :deep(.el-upload) {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-overlay :deep(.el-upload__input) {
  display: none;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 30px;
  padding: 20px;
}

.user-meta h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #303133;
}

.user-meta .email {
  margin: 0 0 10px 0;
  color: #909399;
  font-size: 14px;
}

.info-section {
  margin: 20px 0;
}

.info-section h3 {
  margin-bottom: 15px;
  font-size: 16px;
  color: #303133;
}

.action-section {
  display: flex;
  gap: 15px;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .avatar-section {
    flex-direction: column;
    text-align: center;
  }

  .action-section {
    flex-direction: column;
  }

  .action-section .el-button {
    width: 100%;
  }
}
</style>
