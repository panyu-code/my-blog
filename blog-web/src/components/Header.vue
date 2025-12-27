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
        <span>我的博客</span>
      </div>

      <nav class="nav">
        <router-link to="/" class="nav-item">首页</router-link>
        <template v-if="userStore.isLogin">
          <router-link to="/search" class="nav-item">搜索</router-link>
          <el-dropdown class="nav-item" trigger="hover">
            <span class="el-dropdown-link">
              分类
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item 
                  v-for="category in categories" 
                  :key="category.id"
                  @click="router.push(`/category/${category.id}`)"
                >
                  {{ category.name }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <router-link to="/about" class="nav-item">关于</router-link>
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
import { Reading, ArrowDown, Search, Moon, Sunny, ArrowLeft } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { getCategoryList } from '../api/article'
import { ElMessage } from 'element-plus'
import { useDark, useToggle } from '@vueuse/core'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const searchKeyword = ref('')
const categories = ref([])

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

onMounted(() => {
  fetchCategories()
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
