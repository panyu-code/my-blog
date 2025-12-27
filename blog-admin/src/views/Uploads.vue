<template>
  <div class="uploads-page">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon :size="20" style="margin-right: 8px;">
              <Folder />
            </el-icon>
            <span class="header-title">文件管理</span>
          </div>
          <div class="header-actions">
            <el-button
              v-if="selectedFiles.length > 0"
              type="danger"
              @click="handleBatchDelete"
            >
              <el-icon><Delete /></el-icon>
              批量删除 ({{ selectedFiles.length }})
            </el-button>
            <FileUpload
              type="file"
              :user-id="userStore.userInfo?.id"
              :show-file-list="true"
              :limit="5"
              list-type="picture-card"
              button-text="上传文件"
              @success="handleUploadSuccess"
            >
              <el-button type="primary">
                <el-icon style="margin-right: 4px;"><Upload /></el-icon>
                上传文件
              </el-button>
            </FileUpload>
          </div>
        </div>
      </template>

      <!-- 搜索表单 -->
      <div class="search-section">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="文件类型">
            <el-select v-model="searchForm.fileType" placeholder="全部类型" clearable style="width: 150px;">
              <el-option label="图片" value="image" />
              <el-option label="文档" value="application" />
              <el-option label="视频" value="video" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon style="margin-right: 4px;"><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        
        <!-- 统计信息 -->
        <div class="stats-info">
          <el-tag type="info" effect="plain">共 {{ pagination.total }} 个文件</el-tag>
        </div>
      </div>

      <!-- 文件列表 -->
      <div class="file-content" v-loading="loading">
        <!-- 空状态 -->
        <div v-if="tableData.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无文件">
            <FileUpload
              type="file"
              :user-id="userStore.userInfo?.id"
              :show-file-list="false"
              button-text="上传第一个文件"
              @success="handleUploadSuccess"
            />
          </el-empty>
        </div>
        
        <!-- 文件网格 -->
        <div v-else class="file-grid">
          <div
            v-for="file in tableData"
            :key="file.id"
            class="file-item"
            :class="{ 'selected': selectedFiles.includes(file.id) }"
            @click="toggleSelection(file.id)"
          >
            <!-- 选择复选框 -->
            <div class="file-checkbox">
              <el-checkbox
                :model-value="selectedFiles.includes(file.id)"
                @change="toggleSelection(file.id)"
                @click.stop
              />
            </div>
            <!-- 预览区 -->
            <div class="file-preview">
              <img v-if="isImageFile(file.fileType)" :src="file.fileUrl" :alt="file.originalName" />
              <div v-else class="file-icon-wrapper">
                <el-icon :size="60" color="#909399">
                  <component :is="getFileIcon(file.fileType)" />
                </el-icon>
              </div>
            </div>
            
            <!-- 信息区 -->
            <div class="file-info">
              <div class="file-name" :title="file.originalName">
                {{ file.originalName }}
              </div>
              <div class="file-meta">
                <span class="file-size">
                  <el-icon :size="12"><Document /></el-icon>
                  {{ formatFileSize(file.fileSize) }}
                </span>
                <span class="file-date">
                  <el-icon :size="12"><Clock /></el-icon>
                  {{ formatDate(file.createTime) }}
                </span>
              </div>
            </div>
            
            <!-- 操作区 -->
            <div class="file-actions">
              <el-button type="primary" size="small" link @click="handleCopy(file)">
                <el-icon style="margin-right: 2px;"><CopyDocument /></el-icon>
                复制
              </el-button>
              <el-button type="success" size="small" link @click="handleDownload(file)">
                <el-icon style="margin-right: 2px;"><Download /></el-icon>
                下载
              </el-button>
              <el-button type="danger" size="small" link @click="handleDelete(file)">
                <el-icon style="margin-right: 2px;"><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="tableData.length > 0">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[12, 24, 48, 96]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Document, 
  VideoPlay, 
  Files, 
  Folder,
  Upload,
  Search,
  Clock,
  CopyDocument,
  Delete,
  Download
} from '@element-plus/icons-vue'
import { getUploadList, deleteUpload } from '../api/upload'
import FileUpload from '../components/FileUpload.vue'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const loading = ref(false)
const selectedFiles = ref([])

const searchForm = reactive({
  fileType: null
})

const pagination = reactive({
  page: 1,
  pageSize: 12,
  total: 0
})

const tableData = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUploadList({
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      fileType: searchForm.fileType
      // 管理端显示所有文件，不过滤 userId
    })
    console.log('文件列表返回数据:', res.data)
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('获取文件列表失败:', error)
    ElMessage.error('获取文件列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.fileType = null
  handleSearch()
}

const handleUploadSuccess = (data) => {
  ElMessage.success('上传成功')
  fetchData()
}

const isImageFile = (fileType) => {
  return fileType && fileType.startsWith('image/')
}

const getFileIcon = (fileType) => {
  if (!fileType) return 'Files'
  
  if (fileType.startsWith('application/')) {
    return 'Document'
  } else if (fileType.startsWith('video/')) {
    return 'VideoPlay'
  }
  return 'Files'
}

const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

const handleCopy = async (file) => {
  try {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(file.fileUrl)
      ElMessage.success('链接已复制到剪贴板')
    } else {
      // 降级方案
      const textarea = document.createElement('textarea')
      textarea.value = file.fileUrl
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      try {
        document.execCommand('copy')
        ElMessage.success('链接已复制到剪贴板')
      } catch (err) {
        ElMessage.error('复制失败，请手动复制')
      }
      document.body.removeChild(textarea)
    }
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const handleDownload = async (file) => {
  try {
    // 使用 fetch API 获取文件数据
    const response = await fetch(file.fileUrl)
    const blob = await response.blob()
    
    // 创建 Blob URL
    const blobUrl = window.URL.createObjectURL(blob)
    
    // 创建下载链接
    const link = document.createElement('a')
    link.href = blobUrl
    link.download = file.originalName || 'download'
    link.style.display = 'none'
    
    // 添加到 DOM、点击、然后移除
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    // 释放 Blob URL
    window.URL.revokeObjectURL(blobUrl)
    
    ElMessage.success('开始下载')
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败，请重试')
  }
}

const handleDelete = async (file) => {
  try {
    await ElMessageBox.confirm(`确定要删除文件"${file.originalName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteUpload(file.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 切换选中状态
const toggleSelection = (fileId) => {
  const index = selectedFiles.value.indexOf(fileId)
  if (index > -1) {
    selectedFiles.value.splice(index, 1)
  } else {
    selectedFiles.value.push(fileId)
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedFiles.value.length} 个文件吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    for (const id of selectedFiles.value) {
      await deleteUpload(id)
    }
    
    ElMessage.success(`成功删除 ${selectedFiles.value.length} 个文件`)
    selectedFiles.value = []
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.uploads-page {
  padding: 0;
}

.main-card {
  border-radius: 8px;
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
}

.header-title {
  font-size: 18px;
  white-space: nowrap;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 搜索区域 */
.search-section {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.search-form {
  margin-bottom: 12px;
}

.stats-info {
  display: flex;
  gap: 12px;
}

/* 文件内容区 */
.file-content {
  min-height: 300px;
}

/* 空状态 */
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

/* 文件网格 */
.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

/* 文件卡片 */
.file-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  background-color: #fff;
  display: flex;
  flex-direction: column;
  position: relative;
  cursor: pointer;
}

.file-item.selected {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.file-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-4px);
  border-color: #409eff;
}

/* 选择复选框 */
.file-checkbox {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 2;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 4px;
  padding: 2px;
}

/* 预览区 */
.file-preview {
  width: 100%;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8edf3 100%);
  overflow: hidden;
  position: relative;
}

.file-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.file-item:hover .file-preview img {
  transform: scale(1.1);
}

.file-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 信息区 */
.file-info {
  padding: 16px;
  flex: 1;
  background-color: #fff;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.5;
}

.file-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

.file-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.file-size {
  color: #67c23a;
}

.file-date {
  color: #909399;
}

/* 操作区 */
.file-actions {
  display: flex;
  justify-content: space-around;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  background-color: #fafafa;
  gap: 8px;
}

.file-actions .el-button {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 分页 */
.pagination {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  justify-content: flex-end;
}

/* 响应式 */
@media (max-width: 1200px) {
  .file-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .file-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 16px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .search-form {
    display: flex;
    flex-direction: column;
  }
  
  .search-form :deep(.el-form-item) {
    margin-right: 0;
    margin-bottom: 12px;
  }
}
</style>
