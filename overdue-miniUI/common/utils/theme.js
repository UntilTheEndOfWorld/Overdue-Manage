/**
 * 主题切换工具
 */
export default {
  // 获取当前主题
  getTheme() {
    return uni.getStorageSync('theme') || 'light'
  },
  
  // 设置主题
  setTheme(theme) {
    uni.setStorageSync('theme', theme)
    this.applyTheme(theme)
  },
  
  // 应用主题到页面
  applyTheme(theme) {
    // 触发全局事件通知主题变化
    uni.$emit('theme-changed', theme)
    
    // #ifdef H5
    // 在H5平台，可以直接设置body或html的class
    if (typeof document !== 'undefined') {
      const html = document.documentElement
      const body = document.body
      if (theme === 'light') {
        html.classList.add('light-mode')
        body.classList.add('light-mode')
      } else {
        html.classList.remove('light-mode')
        body.classList.remove('light-mode')
      }
    }
    // #endif
    
    // #ifdef MP-WEIXIN
    // 在小程序平台，需要通过页面实例来更新
    const pages = getCurrentPages()
    pages.forEach(page => {
      if (page && page.$vm) {
        if (page.$vm.isLightMode !== undefined) {
          page.$vm.isLightMode = theme === 'light'
        }
      }
    })
    // #endif
  },
  
  // 切换主题
  toggleTheme() {
    const currentTheme = this.getTheme()
    const newTheme = currentTheme === 'light' ? 'dark' : 'light'
    this.setTheme(newTheme)
    return newTheme
  },
  
  // 初始化主题
  initTheme() {
    const savedTheme = this.getTheme()
    // 检查系统偏好（如果支持）
    // #ifdef H5
    if (typeof window !== 'undefined' && window.matchMedia) {
      const prefersLight = window.matchMedia('(prefers-color-scheme: light)').matches
      if (!savedTheme && prefersLight) {
        this.setTheme('light')
        return 'light'
      }
    }
    // #endif
    
    this.applyTheme(savedTheme)
    return savedTheme
  }
}
