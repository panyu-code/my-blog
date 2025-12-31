<template>
  <header class="header">
    <div class="header-content">
      <!-- 返回按钮 -->
      <el-button
        v-if="canGoBack"
        class="back-button"
        :icon="ArrowLeft"
        circle
        @click="goBack"
        title="返回上一页"
      />
      
      <div class="logo" @click="router.push('/')">
        <el-icon :size="28"><Reading /></el-icon>
        <span>My-Blog</span>
      </div>

      <nav class="nav">
        <router-link to="/" class="nav-item">首页</router-link>
        <template v-if="userStore.isLogin">
          <router-link to="/publish" class="nav-item">发布文章</router-link>
          <router-link to="/my-articles" class="nav-item">我的文章</router-link>
          <router-link to="/search" class="nav-item">搜索</router-link>
          <router-link to="/about" class="nav-item">关于</router-link>
          <div class="weather-section">
            <el-popover
              placement="bottom"
              :width="300"
              trigger="hover"
            >
              <template #reference>
                <div class="weather-display" v-if="weatherData">
                  <div class="weather-icon">{{ getWeatherIcon(weatherData.weather) }}</div>
                  <div class="weather-temp">{{ weatherData.temperature.split('-')[1] || weatherData.temperature }}</div>
                  <div class="weather-location">{{ city }}</div>
                </div>
                <div v-else class="weather-display">
                  <el-icon><Loading /></el-icon>
                  <span>获取中...</span>
                </div>
              </template>
              <div class="weather-popover">
                <h4>{{ city }} 天气详情</h4>
                <div class="weather-detail">
                  <div class="weather-main">
                    <div class="weather-icon-large">{{ getWeatherIcon(weatherData?.weather) }}</div>
                    <div class="weather-info">
                      <div class="weather-current">{{ weatherData?.temperature }}</div>
                      <div class="weather-desc">{{ weatherData?.weather }}</div>
                      <div class="weather-wind">{{ weatherData?.wind }}</div>
                    </div>
                  </div>
                  <div class="weather-air-quality">
                    <span class="air-quality-label">空气质量：</span>
                    <span class="air-quality-value">{{ weatherData?.air_quality }}</span>
                  </div>
                </div>
              </div>
            </el-popover>
          </div>
        </template>
      </nav>

      <div class="user-section">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文章..."
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #suffix>
            <el-icon class="search-icon" @click="handleSearch"><Search /></el-icon>
          </template>
        </el-input>
        
        <!-- 暗黑模式切换 -->
        <el-switch
          v-model="isDark"
          class="dark-switch"
          inline-prompt
          :active-icon="Moon"
          :inactive-icon="Sunny"
          @change="toggleDark"
        />

        <div v-if="userStore.isLogin" class="user-info">
          <el-dropdown trigger="hover">
            <div class="user-avatar">
              <el-avatar :size="36" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.username?.charAt(0) }}
              </el-avatar>
              <span class="username-text">{{ userStore.userInfo?.username }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div v-else class="auth-buttons">
          <el-button size="small" @click="router.push('/login')">登录</el-button>
          <el-button type="primary" size="small" @click="router.push('/register')">注册</el-button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Reading, ArrowDown, Search, Moon, Sunny, ArrowLeft, Loading } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { getCategoryList } from '../api/article'
import { getIpLocation, getWeather } from '../api/weather'
import { ElMessage } from 'element-plus'
import { useDark, useToggle } from '@vueuse/core'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const searchKeyword = ref('')
const categories = ref([])

// 天气相关数据
const weatherData = ref(null)
const city = ref('')

// 判断是否可以返回
const canGoBack = computed(() => {
  // 在首页不显示返回按钮
  return route.path !== '/'
})

// 返回上一页
const goBack = () => {
  router.back()
}

// 暗黑模式
const isDark = useDark({
  storageKey: 'blog-theme',
  valueDark: 'dark',
  valueLight: 'light',
})
const toggleDark = useToggle(isDark)

const handleSearch = () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: searchKeyword.value } })
  }
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('退出成功')
  router.push('/')
}

const fetchCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

// 天气图标映射
const getWeatherIcon = (weather) => {
  if (!weather) return '🌈'
  
  const weatherMap = {
    '晴': '☀️',
    '多云': '⛅',
    '阴': '☁️',
    '雨': '🌧️',
    '小雨': '🌧️',
    '中雨': '🌧️',
    '大雨': '⛈️',
    '雷阵雨': '⛈️',
    '雪': '❄️',
    '小雪': '❄️',
    '中雪': '❄️',
    '大雪': '❄️',
    '雾': '🌫️',
    '霾': '😷',
    '沙尘暴': '😷'
  }
  
  for (const [key, icon] of Object.entries(weatherMap)) {
    if (weather.includes(key)) {
      return icon
    }
  }
  
  return '🌈' // 默认图标
}

// 获取天气信息
const fetchWeather = async () => {
  try {
    // 从用户信息中获取IP地址
    let ip = userStore.userInfo?.lastLoginIp || ''
    
    // 如果用户信息中没有IP，则让API自动识别
    if (!ip) {
      console.log('用户信息中没有IP地址，使用API自动识别')
    }
    
    // 获取IP地理位置
    const ipResponse = await getIpLocation(ip)
    
    if (ipResponse.code === 200 && ipResponse.data && ipResponse.data.data) {
      const address = ipResponse.data.data.address
      // 解析地址，获取省之后的部分
      const provinceIndex = address.indexOf('省')
      let cityName = address
      if (provinceIndex !== -1) {
        cityName = address.substring(provinceIndex + 1)
        // 进一步提取市及市后面的信息（如区、县等）
        const cityIndex = cityName.indexOf('市')
        if (cityIndex !== -1) {
          // 找到市之后的第一个分隔符（如逗号或其他分隔符），或者取到下一个可能的地理单位
          let endIndex = cityName.length
          
          // 查找市之后的其他地理单位
          const nextGeographicUnit = ['区', '县', '旗', '自治县', '市辖区']
          for (const unit of nextGeographicUnit) {
            const unitIndex = cityName.indexOf(unit, cityIndex)
            if (unitIndex !== -1 && unitIndex > cityIndex) {
              endIndex = Math.min(endIndex, unitIndex + unit.length)
              break
            }
          }
          
          cityName = cityName.substring(0, endIndex)
        }
      }
      
      city.value = cityName
      
      // 获取天气信息
      const weatherResponse = await getWeather(cityName)
      
      if (weatherResponse.code === 200 && weatherResponse.data && weatherResponse.data.data && weatherResponse.data.data.data && weatherResponse.data.data.data.length > 0) {
        // 获取当天的天气数据
        weatherData.value = weatherResponse.data.data.data[1]
      }
    } else {
      console.error('获取IP位置失败:', ipResponse)
    }
  } catch (error) {
    console.error('获取天气信息失败:', error)
    ElMessage.warning('天气信息获取失败')
  }
}

onMounted(() => {
  fetchCategories()
  fetchWeather() // 页面加载时获取天气信息
})
</script>

<style scoped>
.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.weather-section {
  display: flex;
  align-items: center;
  margin-left: 15px;
  padding: 5px 10px;
  border-radius: 20px;
  background: linear-gradient(135deg, #74b9ff, #0984e3);
  color: white;
  cursor: pointer;
  transition: all 0.3s;
}

.weather-section:hover {
  background: linear-gradient(135deg, #0984e3, #74b9ff);
  transform: translateY(-2px);
}

.weather-display {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
}

.weather-icon {
  font-size: 18px;
}

.weather-temp {
  font-weight: bold;
}

.weather-location {
  font-size: 12px;
  opacity: 0.9;
}

.weather-popover {
  padding: 15px;
}

.weather-popover h4 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 16px;
}

.weather-detail {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.weather-main {
  display: flex;
  align-items: center;
  gap: 15px;
}

.weather-icon-large {
  font-size: 36px;
}

.weather-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.weather-current {
  font-size: 20px;
  font-weight: bold;
}

.weather-desc {
  color: #666;
}

.weather-wind {
  font-size: 14px;
  color: #999;
}

.weather-air-quality {
  padding-top: 10px;
  border-top: 1px solid #eee;
}

.air-quality-label {
  color: #666;
  font-size: 14px;
}

.air-quality-value {
  font-weight: bold;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 15px;
}

.back-button {
  flex-shrink: 0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
  user-select: none;
}

.nav {
  display: flex;
  align-items: center;
  gap: 30px;
  flex: 1;
  margin-left: 50px;
}

.nav-item {
  color: #333;
  text-decoration: none;
  font-size: 16px;
  transition: color 0.3s;
  cursor: pointer;
}

.nav-item:hover,
.nav-item.router-link-active {
  color: #409eff;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.search-input {
  width: 200px;
}

.dark-switch {
  --el-switch-on-color: #409eff;
  --el-switch-off-color: #dcdfe6;
}

.search-icon {
  cursor: pointer;
}

.user-avatar {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.username-text {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.auth-buttons {
  display: flex;
  gap: 10px;
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 10px;
  }

  .nav {
    display: none;
  }

  .search-input {
    width: 150px;
  }

  .logo span {
    display: none;
  }
}
</style>
