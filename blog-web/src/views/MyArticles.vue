<template>
  <div class="my-articles-page">
    <div class="header">
      <h2>我的文章</h2>
    </div>

    <div class="filter-bar">
      <el-input 
        v-model="searchForm.title" 
        placeholder="搜索文章标题" 
        style="width: 200px; margin-right: 10px;"
        @keyup.enter="handleSearch"
      />
      <el-select 
        v-model="searchForm.status" 
        placeholder="文章状态" 
        style="width: 120px; margin-right: 10px;"
        clearable
      >
        <el-option label="草稿" :value="0" />
        <el-option label="已发布" :value="1" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部文章" name="all">
        <el-table 
          :data="tableData" 
          v-loading="loading"
          style="width: 100%"
        >
          <el-table-column prop="title" label="标题" min-width="200">
            <template #default="{ row }">
              <router-link :to="`/article/${row.id}`" class="article-link">
                {{ row.title }}
              </router-link>
            </template>
          </el-table-column>
          <el-table-column prop="categoryName" label="分类" width="100" />
          <el-table-column label="标签" width="150">
            <template #default="{ row }">
              <el-tag 
                v-for="tag in row.tagList" 
                :key="tag.name" 
                size="small" 
                style="margin-right: 5px; margin-bottom: 5px;"
                :style="{ backgroundColor: tag.color, color: 'white' }"
              >
                {{ tag.name }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">
                {{ row.status === 1 ? '已发布' : '草稿' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="审核状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getAuditStatusType(row.auditStatus)">
                {{ getAuditStatusText(row.auditStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="viewCount" label="浏览量" width="80" />
          <el-table-column prop="likeCount" label="点赞数" width="80" />
          <el-table-column prop="commentCount" label="评论数" width="80" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
              <el-button 
                v-if="row.auditStatus === 2" 
                type="warning" 
                size="small" 
                link 
                @click="handleReSubmit(row)"
              >
                重新提交
              </el-button>
              <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="草稿" name="draft">
        <el-table 
          :data="tableData" 
          v-loading="loading"
          style="width: 100%"
        >
          <el-table-column prop="title" label="标题" min-width="200">
            <template #default="{ row }">
              <router-link :to="`/article/${row.id}`" class="article-link">
                {{ row.title }}
              </router-link>
            </template>
          </el-table-column>
          <el-table-column prop="categoryName" label="分类" width="100" />
          <el-table-column label="标签" width="150">
            <template #default="{ row }">
              <el-tag 
                v-for="tag in row.tagList" 
                :key="tag.name" 
                size="small" 
                style="margin-right: 5px; margin-bottom: 5px;"
                :style="{ backgroundColor: tag.color, color: 'white' }"
              >
                {{ tag.name }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="审核状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getAuditStatusType(row.auditStatus)">
                {{ getAuditStatusText(row.auditStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
              <el-button 
                v-if="row.auditStatus === 2" 
                type="warning" 
                size="small" 
                link 
                @click="handleReSubmit(row)"
              >
                重新提交
              </el-button>
              <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="已发布" name="published">
        <el-table 
          :data="tableData" 
          v-loading="loading"
          style="width: 100%"
        >
          <el-table-column prop="title" label="标题" min-width="200">
            <template #default="{ row }">
              <router-link :to="`/article/${row.id}`" class="article-link">
                {{ row.title }}
              </router-link>
            </template>
          </el-table-column>
          <el-table-column prop="categoryName" label="分类" width="100" />
          <el-table-column label="标签" width="150">
            <template #default="{ row }">
              <el-tag 
                v-for="tag in row.tagList" 
                :key="tag.name" 
                size="small" 
                style="margin-right: 5px; margin-bottom: 5px;"
                :style="{ backgroundColor: tag.color, color: 'white' }"
              >
                {{ tag.name }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="审核状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getAuditStatusType(row.auditStatus)">
                {{ getAuditStatusText(row.auditStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="viewCount" label="浏览量" width="80" />
          <el-table-column prop="likeCount" label="点赞数" width="80" />
          <el-table-column prop="commentCount" label="评论数" width="80" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
              <el-button 
                v-if="row.auditStatus === 2" 
                type="warning" 
                size="small" 
                link 
                @click="handleReSubmit(row)"
              >
                重新提交
              </el-button>
              <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserArticles, deleteArticle } from '../api/article'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('all')
const tableData = ref([])
const selectedRows = ref([])

const searchForm = reactive({
  title: '',
  status: null
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 审核状态类型
const getAuditStatusType = (auditStatus) => {
  switch (auditStatus) {
    case 0: return 'warning' // 待审核
    case 1: return 'success' // 已通过
    case 2: return 'danger'  // 已拒绝
    default: return 'info'
  }
}

// 审核状态文本
const getAuditStatusText = (auditStatus) => {
  switch (auditStatus) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '已拒绝'
    default: return '未知'
  }
}

// 获取文章列表
const fetchArticles = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      title: searchForm.title,
      status: searchForm.status
    }
    
    // 根据标签页设置状态
    if (activeTab.value === 'draft') {
      params.status = 0
    } else if (activeTab.value === 'published') {
      params.status = 1
    }
    
    const res = await getUserArticles(params)
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('获取文章列表失败:', error)
    ElMessage.error('获取文章列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchArticles()
}

// 重置
const handleReset = () => {
  searchForm.title = ''
  searchForm.status = null
  handleSearch()
}

// 标签页切换
const handleTabChange = () => {
  handleSearch()
}

// 编辑文章
const handleEdit = (row) => {
  router.push(`/publish/${row.id}`)
}

// 重新提交审核
const handleReSubmit = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要重新提交文章《${row.title}》进行审核吗？`,
      '重新提交审核',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await resubmitArticle(row.id)
    ElMessage.success('重新提交成功，等待管理员审核')
    fetchArticles() // 刷新列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重新提交失败:', error)
      ElMessage.error('重新提交失败')
    }
  }
}

// 删除文章
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除文章"${row.title}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteArticle(row.id)
    ElMessage.success('删除成功')
    fetchArticles()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchArticles()
})
</script>

<style scoped>
.my-articles-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.filter-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.article-link {
  color: #409eff;
  text-decoration: none;
}

.article-link:hover {
  text-decoration: underline;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>