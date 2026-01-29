import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getUserInfo } from '../api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  
  console.log('User Store 初始化:')
  console.log('- token:', token.value)
  console.log('- userInfo:', userInfo.value)
  
  // 使用 computed 动态计算登录状态
  const isLogin = computed(() => {
    const loginStatus = !!token.value && !!userInfo.value
    console.log('isLogin computed:', loginStatus)
    return loginStatus
  })

  // 登录
  const loginAction = async (loginForm) => {
    try {
      const res = await login(loginForm)
      console.log('登录响应:', res)
      token.value = res.data.token
      userInfo.value = res.data.userInfo
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userInfo', JSON.stringify(res.data.userInfo))
      console.log('登录成功，已保存到 localStorage')
      return res
    } catch (error) {
      throw error
    }
  }

  // 注册
  const registerAction = async (registerForm) => {
    try {
      const res = await register(registerForm)
      return res
    } catch (error) {
      throw error
    }
  }

  // 获取用户信息
  const getUserInfoAction = async () => {
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
      return res
    } catch (error) {
      throw error
    }
  }

  // 退出登录
  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    // 清除记住我的相关信息
    // localStorage.removeItem('rememberedUsername')
    // localStorage.removeItem('rememberedEncryptedPassword')
    // localStorage.removeItem('rememberMe')
  }

  return {
    token,
    userInfo,
    isLogin,
    loginAction,
    registerAction,
    getUserInfoAction,
    logout
  }
})
