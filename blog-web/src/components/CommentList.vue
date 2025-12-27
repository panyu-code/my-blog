<template>
  <div class="comment-list">
    <div class="comment-header">
      <h3>评论 ({{ total }})</h3>
    </div>

    <!-- 发表评论 -->
    <div class="comment-editor" v-if="userStore.isLogin">
      <el-input
        v-model="commentContent"
        type="textarea"
        :rows="4"
        placeholder="写下你的评论..."
        maxlength="500"
        show-word-limit
      />
      <div class="editor-actions">
        <el-button type="primary" @click="handleSubmitComment" :loading="submitting">
          发表评论
        </el-button>
      </div>
    </div>
    <div v-else class="login-tip">
      <el-alert
        title="请先登录后再发表评论"
        type="info"
        :closable="false"
        show-icon
      >
        <template #default>
          <el-button type="primary" link @click="router.push('/login')">
            去登录
          </el-button>
        </template>
      </el-alert>
    </div>

    <!-- 评论列表 -->
    <div class="comments">
      <el-skeleton v-if="loading" :rows="5" animated />
      
      <el-empty v-else-if="comments.length === 0" description="暂无评论，快来抢沙发吧！" />

      <div v-else class="comment-items">
        <div
          v-for="comment in comments"
          :key="comment.id"
          class="comment-item"
        >
          <el-avatar :size="40" :src="comment.user?.avatar">
            {{ comment.user?.username?.charAt(0) }}
          </el-avatar>

          <div class="comment-content-wrapper">
            <div class="comment-user-info">
              <span class="username">{{ comment.user?.username }}</span>
              <span class="time">{{ formatDate(comment.createTime) }}</span>
            </div>

            <div class="comment-content">{{ comment.content }}</div>

            <div class="comment-actions">
              <el-button
                text
                :icon="comment.isLiked ? StarFilled : Star"
                @click="handleLikeComment(comment)"
              >
                {{ comment.likeCount || 0 }}
              </el-button>
              <el-button
                text
                :icon="ChatLineRound"
                @click="handleReply(comment)"
              >
                回复
              </el-button>
              <el-button
                v-if="userStore.userInfo?.id === comment.user?.id"
                text
                type="danger"
                :icon="Delete"
                @click="handleDeleteComment(comment.id)"
              >
                删除
              </el-button>
            </div>

            <!-- 回复输入框 -->
            <div v-if="replyingCommentId === comment.id" class="reply-editor">
              <el-input
                v-model="replyContent"
                type="textarea"
                :rows="3"
                :placeholder="`回复 @${comment.user?.username}`"
                maxlength="300"
                show-word-limit
              />
              <div class="reply-actions">
                <el-button size="small" @click="cancelReply">取消</el-button>
                <el-button
                  size="small"
                  type="primary"
                  @click="handleSubmitReply(comment.id)"
                  :loading="submitting"
                >
                  回复
                </el-button>
              </div>
            </div>

            <!-- 子评论 -->
            <div v-if="comment.replies && comment.replies.length > 0" class="replies">
              <div
                v-for="reply in comment.replies"
                :key="reply.id"
                class="reply-item"
              >
                <el-avatar :size="32" :src="reply.user?.avatar">
                  {{ reply.user?.username?.charAt(0) }}
                </el-avatar>
                <div class="reply-content-wrapper">
                  <div class="reply-user-info">
                    <span class="username">{{ reply.user?.username }}</span>
                    <span v-if="reply.replyTo" class="reply-to">
                      回复 @{{ reply.replyTo?.username }}
                    </span>
                    <span class="time">{{ formatDate(reply.createTime) }}</span>
                  </div>
                  <div class="reply-content">{{ reply.content }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Star,
  StarFilled,
  ChatLineRound,
  Delete
} from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import {
  getCommentList,
  addComment,
  deleteComment,
  likeComment,
  replyComment
} from '../api/comment'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const props = defineProps({
  articleId: {
    type: [String, Number],
    required: true
  }
})

const router = useRouter()
const userStore = useUserStore()

const comments = ref([])
const total = ref(0)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

const commentContent = ref('')
const submitting = ref(false)

const replyingCommentId = ref(null)
const replyContent = ref('')

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const fetchComments = async () => {
  loading.value = true
  try {
    const res = await getCommentList(props.articleId, {
      page: currentPage.value,
      pageSize: pageSize.value
    })
    comments.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error('获取评论失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSubmitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }

  submitting.value = true
  try {
    console.log('提交评论，用户ID:', userStore.userInfo?.id)
    await addComment({
      articleId: props.articleId,
      userId: userStore.userInfo?.id,
      content: commentContent.value
    })
    ElMessage.success('评论发表成功')
    commentContent.value = ''
    currentPage.value = 1
    await fetchComments()
  } catch (error) {
    console.error('发表评论失败:', error)
    ElMessage.error('发表评论失败，请重试')
  } finally {
    submitting.value = false
  }
}

const handleLikeComment = async (comment) => {
  try {
    await likeComment(comment.id)
    comment.isLiked = !comment.isLiked
    comment.likeCount += comment.isLiked ? 1 : -1
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

const handleReply = (comment) => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  replyingCommentId.value = comment.id
  replyContent.value = ''
}

const cancelReply = () => {
  replyingCommentId.value = null
  replyContent.value = ''
}

const handleSubmitReply = async (commentId) => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('回复内容不能为空')
    return
  }

  submitting.value = true
  try {
    await replyComment({
      commentId,
      articleId: props.articleId,
      userId: userStore.userInfo?.id,
      parentId: commentId,
      content: replyContent.value
    })
    ElMessage.success('回复成功')
    cancelReply()
    await fetchComments()
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败，请重试')
  } finally {
    submitting.value = false
  }
}

const handleDeleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteComment(commentId)
    ElMessage.success('删除成功')
    await fetchComments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
    }
  }
}

const handlePageChange = () => {
  fetchComments()
}

onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.comment-list {
  margin-top: 40px;
}

.comment-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #ebeef5;
}

.comment-header h3 {
  font-size: 20px;
  color: #303133;
}

.comment-editor {
  margin-bottom: 30px;
}

.editor-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.login-tip {
  margin-bottom: 30px;
}

.comments {
  margin-top: 20px;
}

.comment-items {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 15px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.comment-content-wrapper {
  flex: 1;
}

.comment-user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.username {
  font-weight: 600;
  color: #303133;
}

.time {
  font-size: 12px;
  color: #909399;
}

.comment-content {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 10px;
  word-break: break-all;
}

.comment-actions {
  display: flex;
  gap: 10px;
}

.reply-editor {
  margin-top: 15px;
  padding: 15px;
  background: white;
  border-radius: 4px;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 10px;
}

.replies {
  margin-top: 15px;
  padding-left: 15px;
  border-left: 2px solid #dcdfe6;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.reply-item {
  display: flex;
  gap: 10px;
}

.reply-content-wrapper {
  flex: 1;
}

.reply-user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.reply-to {
  color: #409eff;
  font-size: 13px;
}

.reply-content {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-all;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

@media (max-width: 768px) {
  .comment-item {
    padding: 15px;
  }

  .comment-user-info {
    flex-wrap: wrap;
  }
}
</style>
