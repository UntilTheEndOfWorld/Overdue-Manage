/**
 * 登录状态检查工具
 */

/**
 * 检查用户是否已登录
 * @returns {Object} 登录状态信息
 */
export function checkLoginStatus() {
  try {
    // 正常登录检查逻辑
    // 统一使用'token'键存储token
    const token = uni.getStorageSync('token')
    const userInfo = uni.getStorageSync('userInfo')
    const loginTime = uni.getStorageSync('loginTime')
    
    console.log('[auth.js] 登录状态检查:', {
      token: token ? '存在' : '不存在',
      userInfo: userInfo ? '存在' : '不存在',
      isGuest: userInfo?.isGuest,
      userId: userInfo?.userId
    })
    
    const isLoggedIn = !!(token && userInfo && !userInfo.isGuest)
    
    console.log('[auth.js] 登录状态结果:', {
      isLoggedIn,
      hasToken: !!token,
      hasUserInfo: !!userInfo,
      isGuest: userInfo?.isGuest || false
    })
    
    return {
      isLoggedIn,
      hasToken: !!token,
      hasUserInfo: !!userInfo,
      isGuest: userInfo?.isGuest || false,
      loginTime: loginTime,
      userInfo: userInfo
    }
  } catch (error) {
    console.error('检查登录状态失败:', error)
    return {
      isLoggedIn: false,
      hasToken: false,
      hasUserInfo: false,
      isGuest: false,
      error: error.message
    }
  }
}

/**
 * 检查用户是否已登录（简化版）
 * @returns {Boolean} 是否已登录
 */
export function isLoggedIn() {
  const status = checkLoginStatus()
  return status.isLoggedIn
}

/**
 * 获取用户信息
 * @returns {Object|null} 用户信息
 */
export function getUserInfo() {
  try {
    const userInfo = uni.getStorageSync('userInfo')
    return userInfo || null
  } catch (error) {
    console.error('获取用户信息失败:', error)
    return null
  }
}

/**
 * 获取用户Token
 * @returns {String|null} 用户Token
 */
export function getUserToken() {
  try {
    const token = uni.getStorageSync('token')
    return token || null
  } catch (error) {
    console.error('获取用户Token失败:', error)
    return null
  }
}

/**
 * 处理401未授权错误
 * @param {Object} error 错误对象
 * @param {String} context 错误上下文
 */
export function handleUnauthorizedError(error, context = '') {
  console.error(`[${context}] 401未授权错误:`, error)
  
  // 清除本地登录信息
  try {
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
    uni.removeStorageSync('loginTime')
  } catch (e) {
    console.error('清除登录信息失败:', e)
  }
  
  // 显示提示
  uni.showToast({
    title: '登录已过期，请重新登录',
    icon: 'none',
    duration: 2000
  })
  
  // 延迟跳转到登录页面
  setTimeout(() => {
    uni.reLaunch({
      url: '/pages/auth/login'
    })
  }, 2000)
}

/**
 * 检查是否需要登录
 * @param {Function} callback 需要登录时的回调函数
 * @returns {Boolean} 是否需要登录
 */
export function checkNeedLogin(callback = null) {
  const status = checkLoginStatus()
  
  if (!status.isLoggedIn) {
    console.log('用户未登录，需要先登录')
    
    if (callback && typeof callback === 'function') {
      callback()
    } else {
      // 默认跳转到登录页面
      uni.navigateTo({
        url: '/pages/auth/login'
      })
    }
    
    return true
  }
  
  return false
}

/**
 * 安全的API调用包装器
 * @param {Function} apiCall API调用函数
 * @param {String} context 上下文信息
 * @returns {Promise} API调用结果
 */
export function safeApiCall(apiCall, context = '') {
  return new Promise((resolve, reject) => {
    // 检查登录状态
    if (!isLoggedIn()) {
      console.log(`[${context}] 用户未登录，跳过API调用`)
      reject(new Error('用户未登录'))
      return
    }
    
    // 执行API调用
    apiCall()
      .then(resolve)
      .catch(error => {
        // 检查是否是401错误
        if (error.message && error.message.includes('401')) {
          handleUnauthorizedError(error, context)
        }
        reject(error)
      })
  })
}

/**
 * 清除登录数据
 */
export function clearLoginData() {
  try {
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
    uni.removeStorageSync('loginTime')
    console.log('登录数据已清除')
  } catch (error) {
    console.error('清除登录数据失败:', error)
  }
}

/**
 * 检查登录状态并在需要时跳转到登录页面
 * @param {string} action - 操作名称，用于提示用户
 * @param {Function} callback - 登录成功后的回调函数
 * @returns {boolean} 是否已登录
 */
export function requireLogin(action = '此操作', callback = null) {
  const loginStatus = checkLoginStatus()
  
  if (!loginStatus.isLoggedIn) {
    // 显示提示
    uni.showModal({
      title: '需要登录',
      content: `请先登录后再${action}`,
      confirmText: '去登录',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          // 跳转到登录页面
          uni.navigateTo({
            url: '/pages/auth/login'
          })
        }
      }
    })
    return false
  }
  
  // 如果已登录且有回调函数，执行回调
  if (callback && typeof callback === 'function') {
    callback()
  }
  
  return true
}

/**
 * 统一的token管理函数
 */
export const tokenManager = {
  // 获取token
  getToken() {
    return uni.getStorageSync('token')
  },
  
  // 设置token
  setToken(token) {
    if (token && typeof token === 'string' && token.length > 10) {
      uni.setStorageSync('token', token)
      console.log('[TokenManager] Token设置成功，长度:', token.length)
      return true
    } else {
      console.warn('[TokenManager] Token格式无效:', token)
      return false
    }
  },
  
  // 清除token
  clearToken() {
    uni.removeStorageSync('token')
    console.log('[TokenManager] Token已清除')
  },
  
  // 检查token是否存在且有效
  hasValidToken() {
    const token = this.getToken()
    return token && typeof token === 'string' && token.length > 10
  },
  
  // 获取token信息（用于调试）
  getTokenInfo() {
    const token = this.getToken()
    return {
      exists: !!token,
      type: typeof token,
      length: token ? token.length : 0,
      prefix: token ? token.substring(0, 20) + '...' : '无'
    }
  }
}
