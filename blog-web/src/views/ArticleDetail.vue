<template>
  <div class="article-detail">
    <el-skeleton v-if="articleStore.loading" :rows="10" animated />

    <div v-else-if="article" class="article-container">
      <!-- 文章头部 -->
      <div class="article-header">
        <h1 class="article-title">{{ article.title }}</h1>
        
        <div class="article-meta">
          <el-avatar :size="40">
            {{ article.authorName?.charAt(0) || 'A' }}
          </el-avatar>
          <div class="meta-info">
            <div class="author-name">{{ article.authorName || '匿名' }}</div>
            <div class="meta-details">
              <span>
                <el-icon><Calendar /></el-icon>
                {{ formatDate(article.createTime) }}
              </span>
              <span>
                <el-icon><View /></el-icon>
                {{ article.viewCount }} 阅读
              </span>
              <span>
                <el-icon><ChatDotRound /></el-icon>
                {{ article.commentCount }} 评论
              </span>
            </div>
          </div>
        </div>

        <!-- 分类 -->
        <div class="article-category" v-if="article.categoryName">
          <span style="color: #909399; margin-right: 10px;">分类：</span>
          <el-tag
            type="success"
            effect="dark"
            size="default"
          >
            <template #default>
              <el-icon><Folder /></el-icon>
              <span>{{ article.categoryName }}</span>
            </template>
          </el-tag>
        </div>

        <!-- 标签 -->
        <div class="article-tags">
          <span style="color: #909399; margin-right: 10px;">标签：</span>
          <template v-if="article.tagList && article.tagList.length > 0">
            <el-tag
              v-for="(tag, index) in article.tagList"
              :key="index"
              :color="tag.color"
              effect="dark"
            >
              {{ tag.name }}
            </el-tag>
          </template>
          <span v-else style="color: #909399;">暂无标签</span>
        </div>
      </div>

      <!-- 文章封面 -->
      <div v-if="article.cover" class="article-cover">
        <el-image :src="article.cover" fit="cover" />
      </div>

      <!-- 文章内容 -->
      <div class="article-content markdown-body" v-html="renderedContent"></div>

      <!-- 文章操作 -->
      <div class="article-actions">
        <el-button
          :type="isLiked ? 'danger' : 'default'"
          :icon="isLiked ? StarFilled : Star"
          @click="handleLike"
        >
          点赞 ({{ article.likeCount }})
        </el-button>
        <el-button :icon="Share">分享</el-button>
      </div>

      <!-- 上一篇/下一篇 -->
      <div class="article-nav">
        <div v-if="prevArticle" class="nav-item prev" @click="toArticle(prevArticle.id)">
          <span class="label">上一篇</span>
          <span class="title">{{ prevArticle.title }}</span>
        </div>
        <div v-if="nextArticle" class="nav-item next" @click="toArticle(nextArticle.id)">
          <span class="label">下一篇</span>
          <span class="title">{{ nextArticle.title }}</span>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comment-section">
        <CommentList :article-id="articleId" />
      </div>
    </div>

    <el-empty v-else description="文章不存在" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Calendar,
  View,
  ChatDotRound,
  Star,
  StarFilled,
  Share,
  Folder
} from '@element-plus/icons-vue'
import { useArticleStore } from '../stores/article'
import { likeArticle, increaseView } from '../api/article'
import CommentList from '../components/CommentList.vue'
import md from '../utils/markdown'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import 'highlight.js/styles/github-dark.css'

const route = useRoute()
const router = useRouter()
const articleStore = useArticleStore()

const articleId = computed(() => route.params.id)
const article = computed(() => articleStore.articleDetail)
const isLiked = ref(false)
const prevArticle = ref(null)
const nextArticle = ref(null)

const renderedContent = computed(() => {
  if (article.value?.content) {
    return md.render(article.value.content)
  }
  return ''
})

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const handleLike = async () => {
  try {
    await likeArticle(articleId.value)
    isLiked.value = !isLiked.value
    article.value.likeCount += isLiked.value ? 1 : -1
    ElMessage.success(isLiked.value ? '点赞成功' : '取消点赞')
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

const toArticle = (id) => {
  router.push(`/article/${id}`)
  fetchArticleDetail()
}

const fetchArticleDetail = async () => {
  try {
    await articleStore.getArticleDetailAction(articleId.value)
    console.log('Web端文章详情:', article.value)
    console.log('Web端标签数据:', article.value?.tags)
    // 增加浏览量
    await increaseView(articleId.value)
    // TODO: 获取上一篇和下一篇文章
  } catch (error) {
    console.error('获取文章详情失败:', error)
  }
}

onMounted(() => {
  fetchArticleDetail()
})
</script>

<style scoped>
.article-detail {
  max-width: 900px;
  margin: 0 auto;
}

.article-container {
  background: white;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.article-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.article-title {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 20px;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.meta-info {
  flex: 1;
}

.author-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 5px;
}

.meta-details {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 14px;
}

.meta-details span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-category {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 15px;
}

.article-category .el-tag {
  display: inline-flex !important;
  flex-direction: row !important;
  align-items: center !important;
  gap: 6px !important;
  cursor: pointer;
  font-size: 14px;
  padding: 6px 12px;
  white-space: nowrap;
}

.article-category .el-tag .el-icon {
  font-size: 14px;
  display: inline-block;
  vertical-align: middle;
}

.article-category .el-tag span {
  display: inline-block;
  vertical-align: middle;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.article-tags .el-tag {
  cursor: pointer;
}

.article-cover {
  width: 100%;
  height: 400px;
  margin-bottom: 30px;
  border-radius: 8px;
  overflow: hidden;
}

.article-cover .el-image {
  width: 100%;
  height: 100%;
}

.article-content {
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
  margin-bottom: 30px;
  min-height: 400px;
}

.article-actions {
  display: flex;
  gap: 15px;
  padding: 20px 0;
  border-top: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 30px;
}

.article-nav {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.nav-item {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item:hover {
  border-color: #409eff;
  background: #ecf5ff;
}

.nav-item .label {
  font-size: 12px;
  color: #909399;
}

.nav-item .title {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nav-item.next {
  text-align: right;
}

.comment-section {
  margin-top: 40px;
}

@media (max-width: 768px) {
  .article-container {
    padding: 20px;
  }

  .article-title {
    font-size: 24px;
  }

  .article-content {
    font-size: 14px;
  }

  .meta-details {
    flex-wrap: wrap;
    gap: 10px;
  }
}
</style>

<!-- Markdown 样式 -->
<style>
.markdown-body {
  word-wrap: break-word;
}

.markdown-body h1,
.markdown-body h2,
.markdown-body h3,
.markdown-body h4,
.markdown-body h5,
.markdown-body h6 {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
}

.markdown-body h1 {
  font-size: 2em;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 0.3em;
}

.markdown-body h2 {
  font-size: 1.5em;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 0.3em;
}

.markdown-body p {
  margin-top: 0;
  margin-bottom: 16px;
}

.markdown-body a {
  color: #409eff;
  text-decoration: none;
}

.markdown-body a:hover {
  text-decoration: underline;
}

.markdown-body ul,
.markdown-body ol {
  padding-left: 2em;
  margin-top: 0;
  margin-bottom: 16px;
}

.markdown-body li {
  margin-bottom: 8px;
}

.markdown-body code {
  padding: 0.2em 0.4em;
  margin: 0;
  font-size: 85%;
  background-color: rgba(175, 184, 193, 0.2);
  border-radius: 6px;
}

.markdown-body pre {
  padding: 16px;
  overflow: auto;
  font-size: 85%;
  line-height: 1.45;
  background-color: #f6f8fa;
  border-radius: 6px;
  margin-bottom: 16px;
}

.markdown-body pre code {
  display: inline;
  padding: 0;
  margin: 0;
  overflow: visible;
  line-height: inherit;
  background-color: transparent;
  border: 0;
}

.markdown-body blockquote {
  padding: 0 1em;
  color: #57606a;
  border-left: 0.25em solid #d0d7de;
  margin: 0 0 16px 0;
}

.markdown-body img {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
}

.markdown-body table {
  border-spacing: 0;
  border-collapse: collapse;
  margin-bottom: 16px;
  width: 100%;
  overflow: auto;
}

.markdown-body table th,
.markdown-body table td {
  padding: 6px 13px;
  border: 1px solid #d0d7de;
}

.markdown-body table th {
  font-weight: 600;
  background-color: #f6f8fa;
}

.markdown-body table tr {
  background-color: #ffffff;
  border-top: 1px solid #d0d7de;
}

.markdown-body table tr:nth-child(2n) {
  background-color: #f6f8fa;
}
</style>
