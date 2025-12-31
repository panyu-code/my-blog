<template>
  <div class="articles-page">
    <el-card>
      <template #header>
        <div class="header-wrapper">
          <span class="header-title">文章管理</span>
          <div>
            <el-button
              v-if="selectedIds.length > 0"
              type="danger"
              @click="handleBatchDelete"
            >
              <el-icon><Delete /></el-icon>
              批量删除 ({{ selectedIds.length }})
            </el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="标题">
          <el-input v-model="searchForm.title" placeholder="请输入文章标题" clearable style="width: 200px;" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="选择分类" clearable style="width: 150px;">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 120px;">
            <el-option label="已发布" :value="1" />
            <el-option label="草稿" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        :data="tableData"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="标题" min-width="200" align="center">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)" :underline="false">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="作者" width="120" align="center">
          <template #default="{ row }">
            {{ row.authorName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="分类" width="100" align="center" >
          <template #default="{ row }">
            {{ categoryMap[row.categoryId] || row.categoryId || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="标签" width="200" align="center">
          <template #default="{ row }">
            <el-tag 
              v-if="(row.tags && row.tags.length > 0) || (row.tagList && row.tagList.length > 0)" 
              v-for="tag in row.tagList ? row.tagList : (row.tags ? row.tags : [])" 
              :key="tag.id || tag" 
              size="small" 
              style="margin-right: 5px;">
              {{ tag.name || tag }}
            </el-tag>
            <span v-else style="color: #909399;">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="100" align="center"/>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getAuditStatusType(row.auditStatus)">
              {{ getAuditStatusText(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElLink } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { deleteArticle, getCategoryList } from '../../api/article'
import request from '../../utils/request'

const router = useRouter()
const loading = ref(false)
const categories = ref([])
const categoryMap = ref({})
const selectedIds = ref([])

const searchForm = reactive({
  title: '',
  categoryId: null,
  status: null
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

const getAuditStatusType = (auditStatus) => {
  switch (auditStatus) {
    case 0: return 'warning' // 待审核
    case 1: return 'success' // 已通过
    case 2: return 'danger'  // 已拒绝
    default: return 'info'
  }
}

const getAuditStatusText = (auditStatus) => {
  switch (auditStatus) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '已拒绝'
    default: return '未知'
  }
}

const fetchCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
    // 创建分类ID到名称的映射
    categoryMap.value = categories.value.reduce((map, cat) => {
      map[cat.id] = cat.name
      return map
    }, {})
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/article/admin/list',
      method: 'get',
      params: {
        pageNum: pagination.page,
        pageSize: pagination.pageSize,
        ...searchForm
      }
    })
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('获取文章列表失败:', error)
    ElMessage.error('获取文章列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.title = ''
  searchForm.categoryId = null
  searchForm.status = null
  handleSearch()
}

const handleEdit = (row) => {
  router.push(`/articles/edit/${row.id}`)
}

const handleView = (row) => {
  router.push(`/articles/edit/${row.id}?readonly=true`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除文章“${row.title}”吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteArticle(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 篇文章吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 逐个删除
    for (const id of selectedIds.value) {
      await deleteArticle(id)
    }
    
    ElMessage.success(`成功删除 ${selectedIds.value.length} 篇文章`)
    selectedIds.value = []
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

onMounted(() => {
  fetchCategories()
  fetchData()
})
</script>

<style scoped>
.articles-page {
  padding: 0;
}

.header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-weight: bold;
  font-size: 16px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
