<template>
  <div class="search-page">
    <!-- 搜索头部 -->
    <div class="search-header">
      <div class="search-title">
        <el-icon :size="32" color="#409eff"><Search /></el-icon>
        <h1>搜索文章</h1>
      </div>
      <div class="search-input-wrapper">
        <el-input
          v-model="keyword"
          size="large"
          placeholder="请输入关键词搜索..."
          clearable
          @keyup.enter="handleSearch"
          @clear="handleClear"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div v-if="searched" class="search-result">
      <div class="result-header">
        <div class="result-info">
          <template v-if="articleStore.articleList.length > 0">
            <span class="result-text">
              找到 <strong class="result-count">{{ articleStore.total }}</strong> 篇相关文章
            </span>
            <el-tag type="info" effect="plain">关键词：{{ keyword }}</el-tag>
          </template>
          <template v-else>
            <el-empty 
              description="未找到相关文章" 
              :image-size="120"
            >
              <el-button type="primary" @click="searched = false">重新搜索</el-button>
            </el-empty>
          </template>
        </div>
      </div>

      <el-skeleton v-if="articleStore.loading" :rows="5" animated />

      <div v-else-if="articleStore.articleList.length > 0" class="article-list">
        <el-card
          v-for="article in articleStore.articleList"
          :key="article.id"
          class="article-card"
          shadow="hover"
          @click="router.push(`/article/${article.id}`)"
        >
          <div class="article-content">
            <div class="article-main">
              <h3 class="article-title" v-html="highlightKeyword(article.title)"></h3>
              <p class="article-summary" v-html="highlightKeyword(article.summary)"></p>
              <div class="article-footer">
                <div class="article-meta">
                  <span class="meta-item">
                    <el-icon><User /></el-icon>
                    {{ article.authorName || '匿名' }}
                  </span>
                  <span class="meta-item">
                    <el-icon><Calendar /></el-icon>
                    {{ formatDate(article.createTime) }}
                  </span>
                  <span class="meta-item">
                    <el-icon><View /></el-icon>
                    {{ article.viewCount }} 阅读
                  </span>
                </div>
                <el-button type="primary" link>
                  阅读全文
                  <el-icon class="el-icon--right"><ArrowRight /></el-icon>
                </el-button>
              </div>
            </div>
            <div v-if="article.cover" class="article-cover">
              <el-image :src="article.cover" fit="cover" lazy />
            </div>
          </div>
        </el-card>

        <!-- 分页 -->
        <div v-if="articleStore.total > pageSize" class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="articleStore.total"
            layout="total, prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>

    <!-- 搜索提示 -->
    <div v-else class="search-tips">
      <div class="tips-icon">
        <el-icon :size="100" color="#409eff"><Search /></el-icon>
      </div>
      <h2 class="tips-title">输入关键词开始搜索</h2>
      <p class="tips-desc">支持搜索文章标题和内容</p>
      
      <div class="hot-keywords">
        <div class="keyword-header">
          <el-icon><TrendCharts /></el-icon>
          <span>热门搜索</span>
        </div>
        <div class="keyword-list">
          <el-tag
            v-for="tag in hotKeywords"
            :key="tag"
            class="keyword-tag"
            size="large"
            effect="plain"
            @click="searchByKeyword(tag)"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, User, Calendar, View, ArrowRight, TrendCharts } from '@element-plus/icons-vue'
import { useArticleStore } from '../stores/article'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const articleStore = useArticleStore()

const keyword = ref('')
const searched = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const hotKeywords = ref(['Vue', 'JavaScript', 'React', 'Node.js', 'TypeScript'])

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const highlightKeyword = (text) => {
  if (!keyword.value || !text) return text
  const reg = new RegExp(keyword.value, 'gi')
  return text.replace(reg, (match) => `<span class="highlight">${match}</span>`)
}

const handleSearch = async () => {
  if (!keyword.value.trim()) {
    return
  }
  
  searched.value = true
  currentPage.value = 1
  
  try {
    await articleStore.searchArticlesAction(keyword.value)
    // 更新 URL
    router.push({ query: { keyword: keyword.value } })
  } catch (error) {
    console.error('搜索失败:', error)
  }
}

const handleClear = () => {
  // 清空关键词和搜索结果
  keyword.value = ''
  searched.value = false
  articleStore.articleList = []
  articleStore.total = 0
  // 清空 URL 参数
  router.push({ query: {} })
}

const searchByKeyword = (kw) => {
  keyword.value = kw
  handleSearch()
}

const handlePageChange = () => {
  handleSearch()
}

onMounted(() => {
  // 从 URL 获取关键词
  const queryKeyword = route.query.keyword
  if (queryKeyword) {
    keyword.value = queryKeyword
    handleSearch()
  }
})
</script>

<style scoped>
.search-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0;
}

/* 搜索头部 */
.search-header {
  background: linear-gradient(135deg, #5d9cec 0%, #4a8cc7 100%);
  padding: 50px 40px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.search-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 24px;
}

.search-title h1 {
  font-size: 32px;
  color: white;
  margin: 0;
  font-weight: 600;
}

.search-input-wrapper {
  max-width: 700px;
  margin: 0 auto;
}

.search-input-wrapper :deep(.el-input__wrapper) {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  border-radius: 24px;
  padding: 8px 16px;
}

/* 搜索结果 */
.search-result {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.result-header {
  margin-bottom: 24px;
}

.result-info {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8edf3 100%);
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.result-text {
  font-size: 16px;
  color: #606266;
}

.result-count {
  color: #409eff;
  font-size: 20px;
  font-weight: 600;
  margin: 0 4px;
}

/* 文章列表 */
.article-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.article-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.article-content {
  display: flex;
  gap: 20px;
}

.article-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.article-cover {
  width: 200px;
  height: 140px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.article-cover :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.article-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-summary {
  color: #606266;
  font-size: 14px;
  line-height: 1.8;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.article-meta {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 13px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

/* 搜索提示 */
.search-tips {
  background: white;
  padding: 80px 40px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.tips-icon {
  margin-bottom: 24px;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.tips-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 12px;
  font-weight: 600;
}

.tips-desc {
  font-size: 16px;
  color: #909399;
  margin-bottom: 40px;
}

.hot-keywords {
  max-width: 600px;
  margin: 0 auto;
}

.keyword-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 16px;
  color: #606266;
  font-weight: 500;
  margin-bottom: 16px;
}

.keyword-list {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.keyword-tag {
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 8px 16px;
  font-size: 14px;
}

.keyword-tag:hover {
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

/* 高亮样式 */
:deep(.highlight) {
  color: #f56c6c;
  font-weight: 600;
  background: linear-gradient(120deg, #fef0f0 0%, #ffe5e5 100%);
  padding: 2px 6px;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(245, 108, 108, 0.1);
}

/* 响应式 */
@media (max-width: 768px) {
  .search-header {
    padding: 30px 20px;
  }

  .search-title h1 {
    font-size: 24px;
  }

  .search-result {
    padding: 20px;
  }

  .article-content {
    flex-direction: column;
  }

  .article-cover {
    width: 100%;
    height: 180px;
  }

  .article-meta {
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .article-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .tips-title {
    font-size: 20px;
  }

  .tips-desc {
    font-size: 14px;
  }
}
</style>
