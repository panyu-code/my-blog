<template>
  <div class="tags-page">
    <el-card>
      <template #header>
        <div class="header-wrapper">
          <span class="header-title">标签管理</span>
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
              新增标签
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
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="标签名称" width="150" />
        <el-table-column label="所属分类" width="150">
          <template #default="{ row }">
            {{ getCategoryName(row.categoryId) || '未分类' }}
          </template>
        </el-table-column>
        <el-table-column label="标签颜色" width="150">
          <template #default="{ row }">
            <el-tag :color="row.color" effect="dark">{{ row.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="count" label="文章数" width="100" />
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="所属分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%;">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标签颜色" prop="color">
          <el-color-picker v-model="form.color" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { getTagList, createTag, updateTag, deleteTag, getCategoryList } from '../api/article'

const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const currentRow = ref(null)
const selectedIds = ref([])

const tableData = ref([])
const categories = ref([])

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

const getCategoryName = (categoryId) => {
  const category = categories.value.find(cat => cat.id === categoryId)
  return category ? category.name : ''
}

const form = reactive({
  name: '',
  categoryId: null,
  color: '#409eff'
})

const rules = {
  name: [
    { required: true, message: '请输入标签名称', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择所属分类', trigger: 'change' }
  ]
}

const dialogTitle = computed(() => {
  return currentRow.value ? '编辑标签' : '新增标签'
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTagList()
    tableData.value = res.data
    
    // 获取分类列表
    const catRes = await getCategoryList()
    categories.value = catRes.data || []
  } catch (error) {
    ElMessage.error('获取标签列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  currentRow.value = null
  Object.assign(form, { name: '', categoryId: null, color: '#409eff' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  currentRow.value = row
  Object.assign(form, { name: row.name, categoryId: row.categoryId, color: row.color })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除标签“${row.name}”吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteTag(row.id)
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
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个标签吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    for (const id of selectedIds.value) {
      await deleteTag(id)
    }
    
    ElMessage.success(`成功删除 ${selectedIds.value.length} 个标签`)
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
      await updateTag({ ...form, id: currentRow.value.id })
      ElMessage.success('编辑成功')
    } else {
      await createTag(form)
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

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.tags-page {
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
</style>
