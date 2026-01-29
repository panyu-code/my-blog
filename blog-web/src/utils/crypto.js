/**
 * 简单的加密解密工具，用于安全存储敏感信息
 */
export class SimpleCrypto {
  constructor(key = 'myBlogSecretKey') {
    this.key = key
  }

  /**
   * 加密数据
   * @param {string} data - 待加密的数据
   * @returns {string} - 加密后的数据（Base64格式）
   */
  encrypt(data) {
    if (!data) return ''
    
    try {
      // 简单的XOR加密
      let result = ''
      for (let i = 0; i < data.length; i++) {
        result += String.fromCharCode(data.charCodeAt(i) ^ this.key.charCodeAt(i % this.key.length))
      }
      return btoa(encodeURIComponent(result))
    } catch (error) {
      console.error('加密失败:', error)
      return ''
    }
  }

  /**
   * 解密数据
   * @param {string} encryptedData - 已加密的数据（Base64格式）
   * @returns {string} - 解密后的原始数据
   */
  decrypt(encryptedData) {
    if (!encryptedData) return ''
    
    try {
      const decoded = decodeURIComponent(atob(encryptedData))
      let result = ''
      for (let i = 0; i < decoded.length; i++) {
        result += String.fromCharCode(decoded.charCodeAt(i) ^ this.key.charCodeAt(i % this.key.length))
      }
      return result
    } catch (error) {
      console.error('解密失败:', error)
      return ''
    }
  }
  
  /**
   * 清理旧的明文密码存储
   */
  cleanupLegacyStorage() {
    // 移除旧的明文密码存储
    if (localStorage.getItem('rememberedPassword')) {
      localStorage.removeItem('rememberedPassword')
    }
    if (localStorage.getItem('admin_rememberedPassword')) {
      localStorage.removeItem('admin_rememberedPassword')
    }
  }
}

// 创建默认实例并清理旧存储
const crypto = new SimpleCrypto()
crypto.cleanupLegacyStorage()

export default crypto