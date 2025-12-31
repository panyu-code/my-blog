<template>
  <div class="home">
<!--    &lt;!&ndash; Banner 区域 &ndash;&gt;-->
<!--    <div class="banner">-->
<!--      <h1>记录技术成长，分享编程心得</h1>-->
<!--    </div>-->

    <!-- 分类和标签 -->
    <div class="category-tags-section">
      <div class="section-header">
        <el-icon :size="18"><Folder /></el-icon>
        <span class="section-title">文章分类</span>
      </div>
      
      <div class="category-tabs">
        <div
          v-for="category in categories"
          :key="category.id"
          class="category-item"
          :class="{ 'active': selectedCategory === category.id }"
          @click="selectCategory(category.id)"
        >
          <span class="category-name">{{ category.name }}</span>
          <span class="category-count">{{ category.count }}</span>
        </div>
      </div>
      
      <div class="tags-section" v-if="selectedCategory && filteredTags.length > 0">
        <div class="tags-header">
          <el-icon :size="14"><PriceTag /></el-icon>
          <span>相关标签</span>
        </div>
        <div class="tags-area">
          <div
            v-for="tag in filteredTags"
            :key="tag.id"
            class="tag-item"
            :style="{ '--tag-color': tag.color }"
            @click="router.push(`/tag/${tag.id}`)"
          >
            <span class="tag-name">{{ tag.name }}</span>
            <span class="tag-count">{{ tag.count }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 文章列表 -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="16" :lg="18">
        <div class="article-list">
          <el-skeleton v-if="articleStore.loading" :rows="5" animated />
          
          <div v-else-if="!articleStore.articleList || articleStore.articleList.length === 0" class="empty-state">
            <el-empty description="暂无文章" />
          </div>

          <div v-else>
            <el-card
              v-for="article in articleStore.articleList"
              :key="article.id"
              class="article-card"
              shadow="hover"
              @click="toArticleDetail(article.id)"
            >
              <div class="article-header">
                <h2 class="article-title">{{ article.title }}</h2>
                <div class="article-meta">
                  <span>
                    <el-icon><Calendar /></el-icon>
                    {{ formatDate(article.createTime) }}
                  </span>
                  <span>
                    <el-icon><User /></el-icon>
                    {{ article.authorName || '匿名' }}
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

              <div class="article-cover" v-if="article.cover">
                <el-image :src="article.cover" fit="cover" lazy />
              </div>

              <div class="article-summary">
                {{ article.summary }}
              </div>

              <div class="article-footer">
                <div class="article-info">
                  <!-- 分类 -->
                  <el-tag
                    v-if="article.categoryName"
                    type="success"
                    size="default"
                    effect="dark"
                    class="category-tag"
                  >
                    <template #default>
                      <el-icon><Folder /></el-icon>
                      <span>{{ article.categoryName }}</span>
                    </template>
                  </el-tag>
                  <!-- 标签 -->
                  <div class="article-tags">
                    <el-tag
                      v-for="(tag, index) in article.tagList"
                      :key="index"
                      :color="tag.color"
                      size="small"
                      effect="dark"
                    >
                      {{ tag.name }}
                    </el-tag>
                  </div>
                </div>
                <el-button type="primary" link>
                  阅读全文
                  <el-icon class="el-icon--right"><ArrowRight /></el-icon>
                </el-button>
              </div>
            </el-card>

            <!-- 分页 -->
            <div class="pagination">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :page-sizes="[10, 20, 30, 50]"
                :total="articleStore.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </div>
          </div>
        </div>
      </el-col>

      <!-- 侧边栏 -->
      <el-col :xs="24" :sm="24" :md="8" :lg="6">
        <div class="sidebar">
          <!-- 热门文章 -->
          <el-card class="sidebar-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><TrendCharts /></el-icon>
                <span>热门文章</span>
              </div>
            </template>
            <div class="hot-articles">
              <div
                v-for="(article, index) in hotArticles"
                :key="article.id"
                class="hot-article-item"
                @click="toArticleDetail(article.id)"
              >
                <span class="rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
                <span class="title">{{ article.title }}</span>
              </div>
            </div>
          </el-card>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  Calendar,
  User,
  View,
  ChatDotRound,
  ArrowRight,
  TrendCharts,
  Folder,
  PriceTag
} from '@element-plus/icons-vue'
import { useArticleStore } from '../stores/article'
import { useUserStore } from '../stores/user'
import { getCategoryList, getTagList } from '../api/article'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const articleStore = useArticleStore()
const userStore = useUserStore()

const currentPage = ref(1)
const pageSize = ref(10)
const tags = ref([])
const categories = ref([])
const hotArticles = ref([])
const selectedCategory = ref(null)

// 根据选中的分类过滤标签
const filteredTags = computed(() => {
  if (!selectedCategory.value) {
    return []
  }
  return tags.value.filter(tag => tag.categoryId === selectedCategory.value)
})

// 选择分类
const selectCategory = (categoryId) => {
  selectedCategory.value = categoryId
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const toArticleDetail = (id) => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push(`/article/${id}`)
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchArticles()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchArticles()
}

const fetchArticles = async () => {
  try {
    await articleStore.getArticleListAction({
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
  } catch (error) {
    console.error('获取文章列表失败:', error)
  }
}

const fetchCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const fetchTags = async () => {
  try {
    const res = await getTagList()
    tags.value = res.data
  } catch (error) {
    console.error('获取标签失败:', error)
  }
}

const fetchHotArticles = async () => {
  try {
    const res = await articleStore.getArticleListAction({
      pageNum: 1,
      pageSize: 5,
      sort: 'viewCount'
    })
    hotArticles.value = res.data.list
  } catch (error) {
    console.error('获取热门文章失败:', error)
  }
}

onMounted(() => {
  fetchArticles()
  fetchCategories()
  fetchTags()
  fetchHotArticles()
})
</script>

<style scoped>
.home {
  width: 100%;
}

.banner {
  background: linear-gradient(135deg, #5d9cec 0%, #4a8cc7 100%);
  color: white;
  padding: 20px 10px;
  text-align: center;
  border-radius: 8px;
  margin-bottom: 30px;
}

.banner h1 {
  font-size: 32px;
  margin-bottom: 8px;
}

.banner p {
  font-size: 16px;
  opacity: 0.9;
}

.category-tags-section {
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
  color: #606266;
  font-size: 15px;
  font-weight: 600;
}

.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.category-item {
  padding: 8px 16px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.category-item:hover {
  background: #e8f4ff;
  border-color: #409eff;
  color: #409eff;
}

.category-item.active {
  background: #409eff;
  border-color: #409eff;
  color: white;
}

.category-name {
  font-weight: 500;
}

.category-count {
  font-size: 12px;
  opacity: 0.8;
}

.tags-section {
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.tags-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  color: #909399;
  font-size: 13px;
}

.tags-area {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  padding: 6px 12px;
  background: var(--tag-color, #409eff);
  color: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.tag-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.tag-name {
  font-weight: 500;
}

.tag-count {
  background: rgba(255, 255, 255, 0.3);
  padding: 1px 6px;
  border-radius: 10px;
  font-size: 11px;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.article-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.article-card:hover {
  transform: translateY(-4px);
}

.article-header {
  margin-bottom: 15px;
}

.article-title {
  font-size: 22px;
  margin-bottom: 10px;
  color: #303133;
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

.article-cover {
  width: 100%;
  height: 360px;
  margin-bottom: 15px;
  border-radius: 8px;
  overflow: hidden;
}

.article-cover :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.article-cover :deep(.el-image__inner) {
  object-fit: cover;
  width: 100%;
  height: 100%;
}

.article-summary {
  color: #606266;
  font-size: 14px;
  line-height: 1.8;
  margin-bottom: 15px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.article-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  flex: 1;
}

.category-tag {
  display: inline-flex !important;
  flex-direction: row !important;
  align-items: center !important;
  gap: 6px !important;
  font-size: 14px;
  padding: 6px 12px;
  white-space: nowrap;
}

.category-tag .el-icon {
  font-size: 14px;
  display: inline-block;
  vertical-align: middle;
}

.category-tag span {
  display: inline-block;
  vertical-align: middle;
}

.article-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.sidebar {
  position: sticky;
  top: 84px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-card {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}

.hot-articles {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hot-article-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}

.hot-article-item:hover {
  background: #f5f7fa;
}

.rank {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #909399;
  color: white;
  border-radius: 50%;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
}

.rank.top {
  background: #f56c6c;
}

.hot-article-item .title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.categories {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}

.category-item:hover {
  background: #f5f7fa;
}

.empty-state {
  padding: 60px 0;
}

@media (max-width: 768px) {
  .banner h1 {
    font-size: 24px;
  }

  .banner p {
    font-size: 14px;
  }
  
  .category-tags-section {
    padding: 16px;
  }
  
  .category-item {
    font-size: 13px;
    padding: 6px 12px;
  }

  .article-title {
    font-size: 18px;
  }

  .article-meta {
    flex-wrap: wrap;
    gap: 10px;
  }

  .sidebar {
    margin-top: 30px;
    position: static;
  }
}
</style>
