/**
 * 主题混入 - 用于页面和组件
 */
import themeUtil from '@/common/utils/theme.js'

export default {
  data() {
    return {
      isLightMode: false
    }
  },
  created() {
    // 组件和页面都会调用created
    this.initTheme()
    // 监听主题变化
    uni.$on('theme-changed', this.onThemeChanged)
  },
  mounted() {
    // 组件挂载时也初始化一次
    this.initTheme()
  },
  onLoad() {
    // 页面生命周期（仅页面会调用）
    this.initTheme()
  },
  onShow() {
    // 页面生命周期（仅页面会调用）
    this.initTheme()
  },
  beforeDestroy() {
    // 组件销毁时移除监听
    uni.$off('theme-changed', this.onThemeChanged)
  },
  onUnload() {
    // 页面卸载时移除监听（仅页面会调用）
    uni.$off('theme-changed', this.onThemeChanged)
  },
  methods: {
    initTheme() {
      const savedTheme = themeUtil.getTheme()
      this.isLightMode = savedTheme === 'light'
    },
    onThemeChanged(theme) {
      this.isLightMode = theme === 'light'
    }
  },
  computed: {
    themeClass() {
      return this.isLightMode ? 'light-mode' : ''
    }
  }
}
