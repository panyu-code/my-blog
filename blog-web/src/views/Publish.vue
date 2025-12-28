<template>
  <div class="article-edit">
    <el-card>
      <template #header>
        <div class="header-wrapper">
          <span class="header-title">{{ articleId ? '编辑文章' : '发布新文章' }}</span>
          <div>
            <el-button @click="handleBack">返回</el-button>
            <el-button type="primary" @click="handleSave(0)">保存草稿</el-button>
            <el-button type="success" @click="handleSave(1)">发布文章</el-button>
          </div>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="文章标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入文章标题" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="文章分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 300px;">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="文章标签" prop="tags">
          <el-select
            v-model="form.tags"
            multiple
            filterable
            allow-create
            placeholder="请选择或输入标签"
            style="width: 500px;"
          >
            <el-option
              v-for="tag in tags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="封面图片" prop="cover">
          <div class="cover-upload-wrapper">
            <div v-if="form.cover" class="cover-preview">
              <img :src="form.cover" class="cover-image" />
              <div class="cover-actions">
                <el-button type="danger" size="small" @click="removeCover">删除</el-button>
              </div>
            </div>
            <FileUpload
              v-else
              type="cover"
              :userId="userStore.userInfo?.id"
              accept="image/*"
              list-type="picture-card"
              :show-file-list="false"
              button-text="上传封面"
              tip="建议尺寸：800x600px，大小不超过 2MB"
              :max-size="2"
              @success="handleCoverSuccess"
            >
              <div class="upload-trigger">
                <el-icon class="cover-uploader-icon"><Plus /></el-icon>
                <div>上传封面</div>
              </div>
            </FileUpload>
          </div>
        </el-form-item>

        <el-form-item label="文章摘要" prop="summary">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="3"
            placeholder="请输入文章摘要"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="文章内容" prop="content">
          <div class="editor-wrapper">
            <Toolbar
              :editor="editorRef"
              :defaultConfig="toolbarConfig"
              mode="default"
              class="editor-toolbar"
            />
            <Editor
              v-model="form.content"
              :defaultConfig="editorConfig"
              mode="default"
              class="editor-content"
              @onCreated="handleCreated"
            />
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, shallowRef, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import { getCategoryList, getTagList, createArticle, getArticleDetail, updateArticle, updateUserArticle, getUserArticleDetail } from '../api/article'
import { useUserStore } from '../stores/user'
import FileUpload from '../components/FileUpload.vue'
import { nextTick } from 'vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const editorRef = shallowRef()

const categories = ref([])
const allTags = ref([])

// 文章ID（用于编辑）
const articleId = ref(route.params.id)

// 根据选中的分类过滤标签
const tags = computed(() => {
  if (!form.categoryId) {
    return []
  }
  return allTags.value.filter(tag => tag.categoryId === form.categoryId)
})

const form = reactive({
  title: '',
  categoryId: null,
  tags: [],
  cover: '',
  summary: '',
  content: ''
})

// 监听分类变化，清空已选标签
watch(() => form.categoryId, (newVal, oldVal) => {
  if (oldVal && newVal !== oldVal) {
    form.tags = []
  }
})

const rules = {
  title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择文章分类', trigger: 'change' }
  ],
  tags: [
    { required: true, message: '请选择文章标签', trigger: 'change' }
  ],
  cover: [
    { required: true, message: '请上传封面图片', trigger: 'change' }
  ],
  summary: [
    { required: true, message: '请输入文章摘要', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入文章内容', trigger: 'blur' }
  ]
}

const toolbarConfig = {
  excludeKeys: []
}

const editorConfig = {
  placeholder: '请输入文章内容...',
  MENU_CONF: {
    uploadImage: {
      server: '/api/upload/image',
      fieldName: 'file',
      maxFileSize: 5 * 1024 * 1024,
      allowedFileTypes: ['image/*'],
      headers: {
        Authorization: `Bearer ${userStore.token}`
      },
      onSuccess(file, res) {
        console.log('图片上传成功', res)
      },
      onFailed(file, res) {
        ElMessage.error('图片上传失败')
      },
      customInsert(res, insertFn) {
        const url = res.data?.url || res.url
        if (url) {
          insertFn(url, '', '')
        }
      }
    }
  }
}

const handleCoverSuccess = (data) => {
  form.cover = data.url
  ElMessage.success('封面上传成功')
}

const removeCover = () => {
  form.cover = ''
}

const handleCreated = (editor) => {
  editorRef.value = editor
}

const handleSave = async (status) => {
  try {
    await formRef.value.validate()

    if (!userStore.userInfo || !userStore.userInfo.id) {
      ElMessage.error('用户信息不存在，请重新登录')
      router.push('/login')
      return
    }

    const tagStrings = form.tags.map(id => String(id))

    const data = {
      title: form.title,
      categoryId: form.categoryId,
      tags: tagStrings,
      cover: form.cover,
      summary: form.summary,
      content: form.content,
      authorId: userStore.userInfo.id,
      status: status,
      auditStatus: 0  // 设置为待审核状态
    }
    
    if (articleId.value) {
      // 更新现有文章
      data.id = parseInt(articleId.value)
      await updateUserArticle(data)
      ElMessage.success('文章更新成功，等待管理员审核')
      router.push('/my-articles')
    } else {
      // 创建新文章
      await createArticle(data)
      ElMessage.success('文章已提交发布，稍候将由管理员审核，请注意邮件通知')
      router.push('/')
    }
  } catch (error) {
    console.error('保存失败:', error)
    if (error?.response?.status === 403) {
      ElMessage.error('无权限操作此文章')
    } else {
      ElMessage.error('保存失败')
    }
  }
}

const handleBack = () => {
  router.back()
}

onMounted(async () => {
  try {
    const [categoriesRes, tagsRes] = await Promise.all([
      getCategoryList(),
      getTagList()
    ])
    categories.value = categoriesRes.data || []
    allTags.value = tagsRes.data || []
    
    // 如果是编辑模式，加载文章详情
    if (articleId.value) {
      await loadArticleDetail()
    }
  } catch (error) {
    console.error('加载分类和标签失败:', error)
    // 如果是编辑模式，加载失败则返回我的文章页面
    if (articleId.value) {
      router.push('/my-articles')
    }
  }
})

// 加载文章详情
const loadArticleDetail = async () => {
  try {
    const res = await getUserArticleDetail(articleId.value)
    const article = res.data
    
    // 填充表单数据
    form.title = article.title
    form.categoryId = article.categoryId
    form.cover = article.cover
    form.summary = article.summary
    form.content = article.content
    
    // 处理标签数据
    try {
      // 优先使用 tagList（标签对象列表）来匹配标签ID
      if (article.tagList && article.tagList.length > 0) {
        const tagIds = []
        article.tagList.forEach(tag => {
          if (tag && tag.name) {
            const foundTag = allTags.value.find(t => t && t.name === tag.name)
            if (foundTag && foundTag.id) {
              tagIds.push(foundTag.id)
            }
          }
        })
        form.tags = tagIds
      } else if (article.tags && article.tags.length > 0) {
        // 备选方案：使用 article.tags（字符串ID数组）
        form.tags = article.tags.map(id => parseInt(id))
      } else {
        form.tags = []
      }
    } catch (tagError) {
      console.error('处理标签时出错:', tagError)
      form.tags = []
    }
    
    ElMessage.success('文章信息加载成功')
  } catch (error) {
    console.error('加载文章详情失败:', error)
    if (error?.response?.status === 404) {
      ElMessage.error('文章不存在')
    } else if (error?.response?.status === 403) {
      ElMessage.error('无权限访问此文章')
    } else {
      ElMessage.error('加载文章详情失败')
    }
    router.push('/my-articles') // 如果加载失败，返回我的文章页面
  }
}

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor) {
    editor.destroy()
  }
})
</script>

<style scoped>
.article-edit {
  padding: 20px;
}

.header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
}

.cover-upload-wrapper {
  display: inline-block;
}

.cover-preview {
  position: relative;
  display: inline-block;
}

.cover-preview:hover .cover-actions {
  opacity: 1;
}

.cover-image {
  width: 200px;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
  display: block;
}

.cover-actions {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 4px;
}

.upload-trigger {
  width: 200px;
  height: 150px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-trigger:hover {
  border-color: #409eff;
}

.upload-trigger:hover .cover-uploader-icon {
  color: #409eff;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  transition: all 0.3s;
}

.editor-wrapper {
  border: 1px solid #ccc;
  width: 100%;
}

.editor-toolbar {
  border-bottom: 1px solid #ccc;
}

.editor-content {
  height: 800px !important;
  overflow-y: auto;
}

:deep(.w-e-editor) {
  height: 800px !important;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}
</style>
