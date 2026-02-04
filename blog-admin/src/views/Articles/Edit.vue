<template>
  <div class="article-edit">
    <el-card>
      <template #header>
        <div class="header-wrapper">
          <span class="header-title">{{ isReadonly ? '查看文章' : (isEdit ? '编辑文章' : '新建文章') }}</span>
          <div>
            <el-button @click="handleBack">返回</el-button>
            <template v-if="!isReadonly">
              <el-button type="primary" @click="handleSave">保存</el-button>
            </template>
          </div>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" :disabled="isReadonly">
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
            placeholder="请输入文章摘要（选填）"
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
import { getCategoryList, getTagList, getArticleDetail, createArticle, updateArticle } from '../../api/article'
import { useUserStore } from '../../stores/user'
import FileUpload from '../../components/FileUpload.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const editorRef = shallowRef()

const isEdit = ref(false)
const isReadonly = computed(() => route.query.readonly === 'true')
const categories = ref([])
const allTags = ref([])

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
        Authorization: `Bearer ${localStorage.getItem('token')}`
      },
      onSuccess(file, res) {
        console.log('图片上传成功', res)
      },
      onFailed(file, res) {
        ElMessage.error('图片上传失败')
      },
      customInsert(res, insertFn) {
        // 从 res 中找到 url
        const url = res.data?.url || res.url
        if (url) {
          insertFn(url, '', '')
        }
      }
    },
    // 新增：视频上传配置
    uploadVideo: {
      server: '/api/upload/file',   // 走后端通用文件上传接口 /upload/file
      fieldName: 'file',
      maxFileSize: 100 * 1024 * 1024, // 100MB，可按需改
      allowedFileTypes: ['video/*'],
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}`
      },
      onSuccess(file, res) {
        console.log('视频上传成功', res)
      },
      onFailed(file, res) {
        ElMessage.error('视频上传失败')
      },
      customInsert(res, insertFn) {
        const url = res.data?.url || res.url
        if (url) {
          insertFn(url)
        } else {
          ElMessage.error('未获取到视频地址')
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

const handleSave = async () => {
  try {
    await formRef.value.validate()
    
    // 检查用户信息
    if (!userStore.userInfo || !userStore.userInfo.id) {
      ElMessage.error('用户信息不存在，请重新登录')
      router.push('/login')
      return
    }
    
    console.log('当前用户信息:', userStore.userInfo)
    console.log('作者ID:', userStore.userInfo.id)
    console.log('选择的标签:', form.tags)
    
    // 将标签ID转换为字符串数组（后端需要字符串）
    const tagStrings = form.tags.map(id => String(id))
    console.log('转换后的标签:', tagStrings)
    
    const data = {
      title: form.title,
      categoryId: form.categoryId,
      tags: tagStrings,
      cover: form.cover,
      summary: form.summary,
      content: form.content,
      authorId: userStore.userInfo.id,  // 添加作者ID
      status: 1,  // 总是设置为已发布状态
      auditStatus: 0  // 设置为待审核状态
    }
    
    console.log('提交的数据:', data)
    
    if (isEdit.value) {
      await updateArticle({ ...data, id: route.params.id })
      ElMessage.success('保存成功')
    } else {
      await createArticle(data)
      ElMessage.success('保存成功')
    }
    router.push('/articles')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

const handleBack = () => {
  router.back()
}

const fetchArticleDetail = async (id) => {
  try {
    const res = await getArticleDetail(id)
    const { data } = res
    console.log('文章详情返回数据:', data)
    console.log('标签数据:', data.tags)
    
    // 将标签转换为ID
    let tagIds = []
    if (data.tags && data.tags.length > 0) {
      // 如果是数字或数字字符串，直接转换
      if (typeof data.tags[0] === 'number' || !isNaN(data.tags[0])) {
        tagIds = data.tags.map(id => Number(id))
      } else {
        // 如果是标签名称，根据名称查找ID
        tagIds = data.tags.map(tagName => {
          const tag = allTags.value.find(t => t.name === tagName)
          return tag ? tag.id : null
        }).filter(id => id !== null)
      }
    } else if (data.tagList && data.tagList.length > 0) {
      // 如果没有tags字段，尝试使用tagList字段
      tagIds = data.tagList.map(tag => {
        const foundTag = allTags.value.find(t => t.name === tag.name)
        return foundTag ? foundTag.id : null
      }).filter(id => id !== null)
    }
    console.log('转换后的标签IDs:', tagIds)
    
    Object.assign(form, {
      title: data.title,
      categoryId: data.categoryId,
      tags: tagIds,
      cover: data.cover,
      summary: data.summary,
      content: data.content
    })
  } catch (error) {
    console.error('获取文章详情失败:', error)
    ElMessage.error('获取文章详情失败')
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data
  } catch (error) {
    ElMessage.error('获取分类列表失败')
  }
}

// 获取标签列表
const fetchTags = async () => {
  try {
    const res = await getTagList()
    allTags.value = res.data
  } catch (error) {
    ElMessage.error('获取标签列表失败')
  }
}

onMounted(async () => {
  await fetchCategories()
  await fetchTags()
  
  if (route.params.id) {
    isEdit.value = true
    try {
      await fetchArticleDetail(route.params.id)
    } catch (error) {
      ElMessage.error('加载文章详情失败')
      console.error('加载文章详情失败:', error)
    }
  }
})

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor) {
    editor.destroy()
  }
})
</script>

<style scoped>
.article-edit {
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

.cover-uploader .cover-image {
  width: 200px;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
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
  height: 600px !important;
  overflow-y: auto;
}

</style>
