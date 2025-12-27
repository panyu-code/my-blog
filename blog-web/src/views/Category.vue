<template>
  <div class="category-page">
    <div class="page-header">
      <h1>{{ categoryInfo.name }}</h1>
      <p>{{ categoryInfo.description }}</p>
      <div class="category-meta">
        <el-tag type="info">共 {{ total }} 篇文章</el-tag>
      </div>
    </div>

    <div class="article-list">
      <el-skeleton v-if="loading" :rows="5" animated />

      <el-empty v-else-if="articles.length === 0" description="该分类下暂无文章" />

      <div v-else>
        <el-card
          v-for="article in articles"
          :key="article.id"
          class="article-card"
          shadow="hover"
          @click="router.push(`/article/${article.id}`)"
        >
          <div class="article-content">
            <h3 class="article-title">{{ article.title }}</h3>
            <p class="article-summary">{{ article.summary }}</p>
            <div class="article-meta">
              <span>
                <el-icon><Calendar /></el-icon>
                {{ formatDate(article.createTime) }}
              </span>
              <span>
                <el-icon><View /></el-icon>
                {{ article.viewCount }}
              </span>
              <span>
                <el-icon><ChatDotRound /></el-icon>
                {{ article.commentCount }}
              </span>
            </div>
          </div>
        </el-card>

        <div v-if="total > pageSize" class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Calendar, View, ChatDotRound } from '@element-plus/icons-vue'
import { getArticlesByCategory } from '../api/article'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const categoryId = computed(() => route.params.id)
const categoryInfo = ref({})
const articles = ref([])
const total = ref(0)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const fetchArticles = async () => {
  loading.value = true
  try {
    console.log('查询分类ID:', categoryId.value)
    const res = await getArticlesByCategory(categoryId.value, {
      page: currentPage.value,
      pageSize: pageSize.value
    })
    console.log('分类文章返回数据:', res.data)
    articles.value = res.data.list
    total.value = res.data.total
    if (res.data.categoryInfo) {
      categoryInfo.value = res.data.categoryInfo
    }
  } catch (error) {
    console.error('获取分类文章失败:', error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = () => {
  fetchArticles()
}

// 监听路由参数变化，切换分类时重新加载数据
watch(() => route.params.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    console.log('分类变化:', oldId, '->', newId)
    currentPage.value = 1 // 重置页码
    fetchArticles()
  }
})

onMounted(() => {
  fetchArticles()
})
</script>

<style scoped>
.category-page {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  background: linear-gradient(135deg, #5d9cec 0%, #4a8cc7 100%);
  color: white;
  padding: 40px;
  border-radius: 8px;
  margin-bottom: 30px;
  text-align: center;
}

.page-header h1 {
  font-size: 32px;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 15px;
}

.category-meta {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.article-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.article-card:hover {
  transform: translateY(-2px);
}

.article-content {
  padding: 10px;
}

.article-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 10px;
}

.article-summary {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 15px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 14px;
}

.article-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

@media (max-width: 768px) {
  .page-header {
    padding: 30px 20px;
  }

  .page-header h1 {
    font-size: 24px;
  }
}
</style>
