<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in statistics" :key="item.title">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: item.color }">
              <el-icon :size="30">
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-title">{{ item.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>访问趋势</span>
            </div>
          </template>
          <div ref="visitChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>文章分类统计</span>
            </div>
          </template>
          <div ref="categoryChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近文章</span>
            </div>
          </template>
          <el-table :data="recentArticles" style="width: 100%" v-loading="loading">
            <el-table-column prop="title" label="标题" align="center" />
            <el-table-column label="作者" width="100" align="center">
              <template #default="{ row }">
                {{ row.authorName || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">
                  {{ row.status === 1 ? '已发布' : '草稿' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="审核状态" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="getAuditStatusType(row.auditStatus)">
                  {{ getAuditStatusText(row.auditStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="viewCount" label="浏览量" width="100" align="center" />
            <el-table-column label="发布时间" width="180" align="center">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近评论</span>
            </div>
          </template>
          <el-table :data="recentComments" style="width: 100%" v-loading="loading">
            <el-table-column label="用户" width="100" align="center">
              <template #default="{ row }">
                {{ row.user?.username || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="content" label="评论内容" show-overflow-tooltip align="center" />
            <el-table-column label="评论时间" width="180" align="center">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStats, getRecentArticles, getRecentComments, getVisitTrend, getCategoryStats } from '../api/dashboard'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const visitChartRef = ref(null)
const categoryChartRef = ref(null)
let visitChart = null
let categoryChart = null

const statistics = ref([
  {
    title: '文章总数',
    value: 0,
    icon: 'Document',
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    title: '评论总数',
    value: 0,
    icon: 'ChatDotRound',
    color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    title: '用户总数',
    value: 0,
    icon: 'User',
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    title: '浏览总量',
    value: 0,
    icon: 'View',
    color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  }
])

const recentArticles = ref([])
const recentComments = ref([])

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

// 审核状态类型
const getAuditStatusType = (auditStatus) => {
  switch (auditStatus) {
    case 0: return 'warning' // 待审核
    case 1: return 'success' // 已通过
    case 2: return 'danger'  // 已拒绝
    default: return 'info'
  }
}

// 审核状态文本
const getAuditStatusText = (auditStatus) => {
  switch (auditStatus) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '已拒绝'
    default: return '未知'
  }
}

// 获取统计数据
const fetchStats = async () => {
  try {
    const res = await getDashboardStats()
    const { data } = res
    statistics.value[0].value = data.articleCount
    statistics.value[1].value = data.commentCount
    statistics.value[2].value = data.userCount
    statistics.value[3].value = data.totalViews >= 1000 ? (data.totalViews / 1000).toFixed(1) + 'k' : data.totalViews
  } catch (error) {
    ElMessage.error('获取统计数据失败')
  }
}

// 获取最近文章
const fetchRecentArticles = async () => {
  loading.value = true
  try {
    const res = await getRecentArticles()
    recentArticles.value = res.data
  } catch (error) {
    ElMessage.error('获取最近文章失败')
  } finally {
    loading.value = false
  }
}

// 获取最近评论
const fetchRecentComments = async () => {
  loading.value = true
  try {
    const res = await getRecentComments()
    recentComments.value = res.data
  } catch (error) {
    ElMessage.error('获取最近评论失败')
  } finally {
    loading.value = false
  }
}

// 初始化访问趋势图
const initVisitChart = async () => {
  visitChart = echarts.init(visitChartRef.value)
  
  try {
    const res = await getVisitTrend()
    const { dates, views } = res.data
    
    const option = {
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: dates,
        boundaryGap: false
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '访问量',
          type: 'line',
          smooth: true,
          data: views,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(102, 126, 234, 0.5)' },
              { offset: 1, color: 'rgba(102, 126, 234, 0.1)' }
            ])
          },
          itemStyle: {
            color: '#667eea'
          }
        }
      ]
    }
    visitChart.setOption(option)
  } catch (error) {
    ElMessage.error('获取访问趋势失败')
  }
}

// 初始化分类统计图
const initCategoryChart = async () => {
  categoryChart = echarts.init(categoryChartRef.value)
  
  try {
    const res = await getCategoryStats()
    const categoryData = res.data
    
    const option = {
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '文章分类',
          type: 'pie',
          radius: '50%',
          data: categoryData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    }
    categoryChart.setOption(option)
  } catch (error) {
    ElMessage.error('获取分类统计失败')
  }
}

// 窗口大小变化时重新绘制图表
const handleResize = () => {
  visitChart?.resize()
  categoryChart?.resize()
}

onMounted(async () => {
  await fetchStats()
  await fetchRecentArticles()
  await fetchRecentComments()
  
  // 初始化图表
  initVisitChart()
  initCategoryChart()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  visitChart?.dispose()
  categoryChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-title {
  font-size: 14px;
  color: #999;
}

.card-header {
  font-weight: bold;
}
</style>
