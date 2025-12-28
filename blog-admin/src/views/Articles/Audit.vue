<template>
  <div class="audit-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>文章审核</span>
        </div>
      </template>

      <el-empty v-if="articles.length === 0" description="暂无待审核的文章" />
      
      <div v-else class="article-list">
        <div v-for="article in articles" :key="article.id" class="article-item">
          <div class="article-header">
            <h3 class="article-title">{{ article.title }}</h3>
            <span class="article-author">作者：{{ article.authorName }}</span>
          </div>

          <div class="article-info">
            <span class="info-item">
              <el-tag>{{ article.categoryName }}</el-tag>
            </span>
            <span class="info-item">
              <div class="article-tags">
                <el-tag 
                  v-for="tag in article.tagList || []" 
                  :key="tag.id"
                  type="info"
                  size="small"
                  style="margin-right: 4px;">
                  {{ tag.name }}
                </el-tag>
              </div>
            </span>
            <span class="info-item">
              <el-icon><Clock /></el-icon>
              {{ formatTime(article.createTime) }}
            </span>
          </div>

          <div class="article-summary">
            {{ article.summary }}
          </div>

          <div class="article-actions">
            <el-button type="success" @click="handleApprove(article.id)">审核通过</el-button>
            <el-button type="danger" @click="handleReject(article.id)">审核拒绝</el-button>
            <el-button @click="handleView(article.id)">查看详情</el-button>
          </div>
        </div>
      </div>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handlePageSizeChange"
        style="margin-top: 20px; text-align: right;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import request from '../../utils/request'

const router = useRouter()
const articles = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const fetchArticles = async () => {
  try {
    const result = await request.get('/article/audit/list', {
      params: {
        pageNum: currentPage.value,
        pageSize: pageSize.value
      }
    })
    articles.value = result.data.list
    total.value = result.data.total
  } catch (error) {
    console.error('获取待审核文章列表失败:', error)
  }
}

const handleApprove = async (articleId) => {
  try {
    await ElMessageBox.confirm('确定审核通过此文章吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })

    await request.post(`/article/${articleId}/audit`, null, {
      params: {
        auditStatus: 1
      }
    })
    ElMessage.success('审核通过')
    fetchArticles()
  } catch (error) {
    if (error.message !== 'request cancelled.') {
      console.error('审核失败:', error)
    }
  }
}

const handleReject = async (articleId) => {
  try {
    let auditReason = ''
    // 使用prompt输入挥绝理由
    auditReason = await ElMessageBox.prompt(
      '请输入拒绝理由',
      '审核拒绝文章',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '请描述拒绝原因...',
        inputValue: ''
      }
    ).catch()

    if (auditReason === undefined) {
      return
    }

    await request.post(`/article/${articleId}/audit`, null, {
      params: {
        auditStatus: 2,
        auditReason: auditReason || ''
      }
    })
    ElMessage.success('已拒绝')
    fetchArticles()
  } catch (error) {
    if (error && error.message !== 'request cancelled.') {
      console.error('拒绝失败:', error)
    }
  }
}

const handleView = (articleId) => {
  router.push({
    name: 'ArticleEdit',
    params: { id: articleId },
    query: { readonly: true }
  })
}

const handlePageChange = () => {
  fetchArticles()
}

const handlePageSizeChange = () => {
  currentPage.value = 1
  fetchArticles()
}

onMounted(() => {
  fetchArticles()
})
</script>

<style scoped>
.audit-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.article-item {
  padding: 16px;
  border: 1px solid #e6e6e6;
  border-radius: 4px;
  transition: all 0.3s;
}

.article-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.article-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  max-width: 60%;
}

.article-author {
  font-size: 14px;
  color: #909399;
}

.article-info {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #606266;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.article-summary {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.article-actions {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}
</style>
