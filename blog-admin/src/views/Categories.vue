<template>
  <div class="categories-page">
    <el-card>
      <template #header>
        <div class="header-wrapper">
          <span class="header-title">分类管理</span>
          <div>
            <el-button
              v-if="selectedIds.length > 0"
              type="danger"
              @click="handleBatchDelete"
            >
              <el-icon><Delete /></el-icon>
              批量删除 ({{ selectedIds.length }})
            </el-button>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增分类
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="tableData"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="分类名称" width="150" align="center"/>
        <el-table-column prop="description" label="描述"  align="center" />
        <el-table-column label="标签" min-width="300" align="center">
          <template #default="{ row }">
            <div class="tags-wrapper">
              <el-tag
                v-for="tag in row.tags"
                :key="tag.id"
                :color="tag.color"
                effect="dark"
                size="small"
                closable
                @close="handleRemoveTag(row, tag)"
                style="margin-right: 8px; margin-bottom: 4px;"
              >
                {{ tag.name }}
              </el-tag>
              <el-button
                type="primary"
                size="small"
                link
                @click="handleAddTag(row)"
              >
                <el-icon><Plus /></el-icon>
                添加标签
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="count" label="文章数" width="100" align="center" />
        <el-table-column label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑分类对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加标签对话框 -->
    <el-dialog
      v-model="tagDialogVisible"
      title="添加标签"
      width="500px"
      @close="handleTagDialogClose"
    >
      <el-form :model="tagForm" :rules="tagRules" ref="tagFormRef" label-width="100px">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="tagForm.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="标签颜色" prop="color">
          <el-color-picker v-model="tagForm.color" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tagDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTagSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { getCategoryList, createCategory, updateCategory, deleteCategory } from '../api/article'
import { getTagList, createTag, deleteTag } from '../api/article'

const loading = ref(false)
const dialogVisible = ref(false)
const tagDialogVisible = ref(false)
const formRef = ref(null)
const tagFormRef = ref(null)
const currentRow = ref(null)
const currentCategory = ref(null)
const selectedIds = ref([])

const tableData = ref([])

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

const form = reactive({
  name: '',
  description: ''
})

const tagForm = reactive({
  name: '',
  color: '#409EFF'
})

const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' }
  ]
}

const tagRules = {
  name: [
    { required: true, message: '请输入标签名称', trigger: 'blur' }
  ],
  color: [
    { required: true, message: '请选择标签颜色', trigger: 'change' }
  ]
}

const dialogTitle = computed(() => {
  return currentRow.value ? '编辑分类' : '新增分类'
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCategoryList()
    const categories = res.data || []
    
    // 获取所有标签
    const tagRes = await getTagList()
    const allTags = tagRes.data || []
    
    // 为每个分类匹配其标签
    tableData.value = categories.map(category => {
      return {
        ...category,
        tags: allTags.filter(tag => tag.categoryId === category.id)
      }
    })
  } catch (error) {
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  currentRow.value = null
  Object.assign(form, { name: '', description: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  currentRow.value = row
  Object.assign(form, { name: row.name, description: row.description })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除分类“${row.name}”吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCategory(row.id)
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
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个分类吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 逐个删除
    for (const id of selectedIds.value) {
      await deleteCategory(id)
    }
    
    ElMessage.success(`成功删除 ${selectedIds.value.length} 个分类`)
    selectedIds.value = []
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (currentRow.value) {
      await updateCategory({ ...form, id: currentRow.value.id })
      ElMessage.success('编辑成功')
    } else {
      await createCategory(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(currentRow.value ? '编辑失败' : '新增失败')
    }
  }
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
}

// 标签管理相关方法
const handleAddTag = (category) => {
  currentCategory.value = category
  Object.assign(tagForm, { name: '', color: '#409EFF' })
  tagDialogVisible.value = true
}

const handleTagSubmit = async () => {
  try {
    await tagFormRef.value.validate()
    await createTag({
      ...tagForm,
      categoryId: currentCategory.value.id
    })
    ElMessage.success('添加标签成功')
    tagDialogVisible.value = false
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('添加标签失败')
    }
  }
}

const handleRemoveTag = async (category, tag) => {
  try {
    await ElMessageBox.confirm(`确定要删除标签“${tag.name}”吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteTag(tag.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleTagDialogClose = () => {
  tagFormRef.value?.resetFields()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.categories-page {
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

.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}
</style>
