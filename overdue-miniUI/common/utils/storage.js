/**
 * 本地存储工具类
 */
class Storage {
  /**
   * 设置存储
   */
  set(key, value) {
    try {
      uni.setStorageSync(key, value)
      return true
    } catch (e) {
      console.error('存储失败:', e)
      return false
    }
  }

  /**
   * 获取存储
   */
  get(key, defaultValue = null) {
    try {
      const value = uni.getStorageSync(key)
      return value !== '' ? value : defaultValue
    } catch (e) {
      console.error('获取存储失败:', e)
      return defaultValue
    }
  }

  /**
   * 删除存储
   */
  remove(key) {
    try {
      uni.removeStorageSync(key)
      return true
    } catch (e) {
      console.error('删除存储失败:', e)
      return false
    }
  }

  /**
   * 清空所有存储
   */
  clear() {
    try {
      uni.clearStorageSync()
      return true
    } catch (e) {
      console.error('清空存储失败:', e)
      return false
    }
  }
}

export default new Storage()
