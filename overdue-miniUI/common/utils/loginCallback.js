/**
 * 登录回调处理工具
 */

/**
 * 执行登录成功后的回调
 * @returns {Object|null} 回调数据
 */
export function executeLoginCallback() {
  try {
    const callbackData = uni.getStorageSync('loginCallback')
    if (callbackData) {
      const data = JSON.parse(callbackData)
      uni.removeStorageSync('loginCallback')
      console.log('执行登录回调:', data)
      return data
    }
    return null
  } catch (error) {
    console.error('执行登录回调失败:', error)
    uni.removeStorageSync('loginCallback')
    return null
  }
}

/**
 * 设置登录回调数据
 * @param {Object} data 回调数据
 */
export function setLoginCallback(data) {
  try {
    uni.setStorageSync('loginCallback', JSON.stringify(data))
    console.log('设置登录回调:', data)
  } catch (error) {
    console.error('设置登录回调失败:', error)
  }
}

/**
 * 清除登录回调数据
 */
export function clearLoginCallback() {
  try {
    uni.removeStorageSync('loginCallback')
    console.log('清除登录回调')
  } catch (error) {
    console.error('清除登录回调失败:', error)
  }
}
