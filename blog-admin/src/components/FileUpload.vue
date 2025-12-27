<template>
  <div class="file-upload">
    <el-upload
      :action="uploadUrl"
      :headers="headers"
      :data="uploadData"
      :show-file-list="showFileList"
      :before-upload="beforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-remove="handleRemove"
      :limit="limit"
      :accept="accept"
      :list-type="listType"
      :disabled="disabled"
    >
      <slot>
        <el-button type="primary" :disabled="disabled">
          <el-icon><Upload /></el-icon>
          {{ buttonText }}
        </el-button>
      </slot>
      <template #tip v-if="tip">
        <div class="el-upload__tip">{{ tip }}</div>
      </template>
    </el-upload>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'

const props = defineProps({
  // 上传类型：file(通用), image(图片), cover(封面), avatar(头像)
  type: {
    type: String,
    default: 'file'
  },
  // 用户ID
  userId: {
    type: Number,
    default: null
  },
  // 业务目录
  bizDir: {
    type: String,
    default: ''
  },
  // 是否显示文件列表
  showFileList: {
    type: Boolean,
    default: true
  },
  // 文件数量限制
  limit: {
    type: Number,
    default: 1
  },
  // 接受的文件类型
  accept: {
    type: String,
    default: ''
  },
  // 列表类型
  listType: {
    type: String,
    default: 'text'
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 按钮文字
  buttonText: {
    type: String,
    default: '上传文件'
  },
  // 提示文字
  tip: {
    type: String,
    default: ''
  },
  // 文件大小限制（MB）
  maxSize: {
    type: Number,
    default: 10
  }
})

const emit = defineEmits(['success', 'error', 'remove'])

// 上传地址
const uploadUrl = computed(() => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  return `${baseUrl}/upload/${props.type}`
})

// 请求头
const headers = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

// 上传参数
const uploadData = computed(() => {
  const data = {}
  if (props.userId) {
    data.userId = props.userId
  }
  if (props.bizDir) {
    data.bizDir = props.bizDir
  }
  console.log('上传参数 uploadData:', data)
  return data
})

// 上传前的校验
const beforeUpload = (file) => {
  console.log('准备上传文件:', file.name, '用户ID:', props.userId)
  
  // 文件大小校验
  const isLt = file.size / 1024 / 1024 < props.maxSize
  if (!isLt) {
    ElMessage.error(`文件大小不能超过 ${props.maxSize}MB!`)
    return false
  }

  // 图片类型校验
  if (props.type === 'image' || props.type === 'cover' || props.type === 'avatar') {
    const isImage = file.type.startsWith('image/')
    if (!isImage) {
      ElMessage.error('只能上传图片文件!')
      return false
    }
  }

  return true
}

// 上传成功
const handleSuccess = (response, file) => {
  if (response.code === 200) {
    ElMessage.success('上传成功')
    emit('success', response.data, file)
  } else {
    ElMessage.error(response.message || '上传失败')
    emit('error', response, file)
  }
}

// 上传失败
const handleError = (error, file) => {
  ElMessage.error('上传失败: ' + error.message)
  emit('error', error, file)
}

// 移除文件
const handleRemove = (file) => {
  emit('remove', file)
}
</script>

<style scoped>
.file-upload {
  width: 100%;
}
</style>
