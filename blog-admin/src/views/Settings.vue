<template>
  <div class="settings-page">
    <el-card>
      <template #header>
        <span class="header-title">系统设置</span>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="站点设置" name="site">
          <el-form :model="siteForm" :rules="siteRules" ref="siteFormRef" label-width="120px" style="max-width: 600px;">
            <el-form-item label="站点名称" prop="siteName">
              <el-input v-model="siteForm.siteName" placeholder="请输入站点名称" />
            </el-form-item>
            <el-form-item label="站点描述" prop="siteDescription">
              <el-input
                v-model="siteForm.siteDescription"
                type="textarea"
                :rows="3"
                placeholder="请输入站点描述"
              />
            </el-form-item>
            <el-form-item label="站点关键词">
              <el-input v-model="siteForm.siteKeywords" placeholder="请输入站点关键词，用逗号分隔" />
            </el-form-item>
            <el-form-item label="站点Logo">
              <el-upload
                class="logo-uploader"
                :show-file-list="false"
                :before-upload="beforeUpload"
              >
                <img v-if="siteForm.siteLogo" :src="siteForm.siteLogo" class="logo" />
                <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            <el-form-item label="ICP备案号">
              <el-input v-model="siteForm.icp" placeholder="请输入ICP备案号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveSite">保存</el-button>
              <el-button @click="handleResetSite">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="评论设置" name="comment">
          <el-form :model="commentForm" label-width="120px" style="max-width: 600px;">
            <el-form-item label="是否开启评论">
              <el-switch v-model="commentForm.enabled" />
            </el-form-item>
            <el-form-item label="评论审核">
              <el-switch v-model="commentForm.needApproval" />
              <span class="form-tip">开启后，评论需要管理员审核后才能显示</span>
            </el-form-item>
            <el-form-item label="游客评论">
              <el-switch v-model="commentForm.allowGuest" />
              <span class="form-tip">是否允许未登录用户评论</span>
            </el-form-item>
            <el-form-item label="敏感词过滤">
              <el-input
                v-model="commentForm.sensitiveWords"
                type="textarea"
                :rows="3"
                placeholder="请输入敏感词，用逗号分隔"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveComment">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="邮件设置" name="email">
          <el-form :model="emailForm" label-width="120px" style="max-width: 600px;">
            <el-form-item label="SMTP服务器">
              <el-input v-model="emailForm.smtpHost" placeholder="例如：smtp.gmail.com" />
            </el-form-item>
            <el-form-item label="SMTP端口">
              <el-input v-model="emailForm.smtpPort" placeholder="例如：587" />
            </el-form-item>
            <el-form-item label="发件人邮箱">
              <el-input v-model="emailForm.fromEmail" placeholder="请输入发件人邮箱" />
            </el-form-item>
            <el-form-item label="邮箱密码">
              <el-input v-model="emailForm.password" type="password" placeholder="请输入邮箱密码或授权码" show-password />
            </el-form-item>
            <el-form-item label="启用SSL">
              <el-switch v-model="emailForm.ssl" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveEmail">保存</el-button>
              <el-button @click="handleTestEmail">发送测试邮件</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="存储设置" name="storage">
          <el-form :model="storageForm" label-width="120px" style="max-width: 600px;">
            <el-form-item label="存储方式">
              <el-radio-group v-model="storageForm.type">
                <el-radio label="local">本地存储</el-radio>
                <el-radio label="oss">阿里云OSS</el-radio>
                <el-radio label="cos">腾讯云COS</el-radio>
              </el-radio-group>
            </el-form-item>
            <template v-if="storageForm.type === 'oss'">
              <el-form-item label="AccessKey ID">
                <el-input v-model="storageForm.accessKeyId" placeholder="请输入AccessKey ID" />
              </el-form-item>
              <el-form-item label="AccessKey Secret">
                <el-input v-model="storageForm.accessKeySecret" type="password" placeholder="请输入AccessKey Secret" show-password />
              </el-form-item>
              <el-form-item label="Bucket名称">
                <el-input v-model="storageForm.bucket" placeholder="请输入Bucket名称" />
              </el-form-item>
              <el-form-item label="区域">
                <el-input v-model="storageForm.region" placeholder="例如：oss-cn-hangzhou" />
              </el-form-item>
            </template>
            <el-form-item>
              <el-button type="primary" @click="handleSaveStorage">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('site')
const siteFormRef = ref(null)

// 从 localStorage 加载配置
const loadSettings = () => {
  const savedSite = localStorage.getItem('blog-settings-site')
  const savedComment = localStorage.getItem('blog-settings-comment')
  const savedEmail = localStorage.getItem('blog-settings-email')
  const savedStorage = localStorage.getItem('blog-settings-storage')
  
  if (savedSite) {
    Object.assign(siteForm, JSON.parse(savedSite))
  }
  if (savedComment) {
    Object.assign(commentForm, JSON.parse(savedComment))
  }
  if (savedEmail) {
    Object.assign(emailForm, JSON.parse(savedEmail))
  }
  if (savedStorage) {
    Object.assign(storageForm, JSON.parse(savedStorage))
  }
}

const siteForm = reactive({
  siteName: '我的博客',
  siteDescription: '分享技术、记录生活',
  siteKeywords: 'Vue,JavaScript,前端开发',
  siteLogo: '',
  icp: ''
})

const siteRules = {
  siteName: [
    { required: true, message: '请输入站点名称', trigger: 'blur' }
  ],
  siteDescription: [
    { required: true, message: '请输入站点描述', trigger: 'blur' }
  ]
}

const commentForm = reactive({
  enabled: true,
  needApproval: true,
  allowGuest: false,
  sensitiveWords: '广告,垃圾'
})

const emailForm = reactive({
  smtpHost: '',
  smtpPort: '587',
  fromEmail: '',
  password: '',
  ssl: true
})

const storageForm = reactive({
  type: 'local',
  accessKeyId: '',
  accessKeySecret: '',
  bucket: '',
  region: ''
})

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  
  siteForm.siteLogo = URL.createObjectURL(file)
  return false
}

const handleSaveSite = async () => {
  try {
    await siteFormRef.value.validate()
    // 保存到 localStorage
    localStorage.setItem('blog-settings-site', JSON.stringify(siteForm))
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('请检查表单填写')
  }
}

const handleResetSite = () => {
  siteFormRef.value.resetFields()
  // 清除 localStorage
  localStorage.removeItem('blog-settings-site')
  ElMessage.success('重置成功')
}

const handleSaveComment = () => {
  // 保存到 localStorage
  localStorage.setItem('blog-settings-comment', JSON.stringify(commentForm))
  ElMessage.success('保存成功')
}

const handleSaveEmail = () => {
  // 保存到 localStorage
  localStorage.setItem('blog-settings-email', JSON.stringify(emailForm))
  ElMessage.success('保存成功')
}

const handleTestEmail = () => {
  if (!emailForm.smtpHost || !emailForm.fromEmail) {
    ElMessage.warning('请先配置邮箱信息')
    return
  }
  ElMessage.success('测试邮件已发送')
}

const handleSaveStorage = () => {
  // 保存到 localStorage
  localStorage.setItem('blog-settings-storage', JSON.stringify(storageForm))
  ElMessage.success('保存成功')
}

// 页面加载时读取配置
onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.settings-page {
  padding: 0;
}

.header-title {
  font-weight: bold;
  font-size: 16px;
}

.logo-uploader .logo {
  width: 178px;
  height: 178px;
  object-fit: contain;
  border-radius: 4px;
}

.logo-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.logo-uploader-icon:hover {
  border-color: #409eff;
  color: #409eff;
}

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}
</style>
