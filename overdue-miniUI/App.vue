<template>
  <view id="app">
    <!-- 应用入口 -->
  </view>
</template>

<script>
import storage from '@/common/utils/storage.js'
import inviteUtil from '@/common/utils/invite.js'
import themeUtil from '@/common/utils/theme.js'

export default {
  onLaunch: function(options) {
    console.log('App Launch', options)
    // 初始化主题
    themeUtil.initTheme()
    // 监听主题变化
    uni.$on('theme-changed', (theme) => {
      this.applyThemeToPages(theme)
    })
    // 初始化应用
    this.initApp(options)
  },
  onShow: function(options) {
    console.log('App Show', options)
    // 处理邀请链接
    this.handleInviteLink(options)
  },
  onHide: function() {
    console.log('App Hide')
  },
  methods: {
    // 应用主题到所有页面
    applyThemeToPages(theme) {
      // 在uni-app中，主题通过CSS变量自动应用
      // 这里主要确保storage中的主题值是最新的
      const pages = getCurrentPages()
      pages.forEach(page => {
        if (page && page.$vm && page.$vm.isLightMode !== undefined) {
          page.$vm.isLightMode = theme === 'light'
        }
      })
    },
    initApp(options) {
      // 清理过期的邀请码
      inviteUtil.cleanExpiredInvites()
      
      // 检查登录状态
      const userInfo = uni.getStorageSync('userInfo')
      if (!userInfo) {
        // 未登录，检查是否有邀请链接
        if (options && options.query && options.query.inviteCode) {
          // 有邀请码，保存起来，登录后使用
          storage.set('pendingInviteCode', options.query.inviteCode)
        }
      }
    },
    
    // 处理邀请链接
    handleInviteLink(options) {
      // 处理小程序码扫码进入
      if (options && options.scene) {
        // scene 1047: 扫描小程序码
        // scene 1048: 长按图片识别小程序码
        // scene 1049: 手机相册选取小程序码
        if (options.scene === 1047 || options.scene === 1048 || options.scene === 1049) {
          if (options.query && options.query.inviteCode) {
            this.processInviteCode(options.query.inviteCode)
          }
        }
      }
      
      // 处理普通链接参数
      if (options && options.query && options.query.inviteCode) {
        this.processInviteCode(options.query.inviteCode)
      }
    },
    
    // 处理邀请码
    processInviteCode(inviteCode) {
      // 验证邀请码
      if (!inviteUtil.validateInviteCode(inviteCode)) {
        uni.showModal({
          title: '提示',
          content: '邀请码无效或已过期',
          showCancel: false
        })
        return
      }
      
      // 检查登录状态
      const userInfo = storage.get('userInfo')
      if (!userInfo || !userInfo.openid) {
        // 未登录，保存邀请码，跳转到登录页
        storage.set('pendingInviteCode', inviteCode)
        uni.reLaunch({
          url: `/pages/auth/login?inviteCode=${inviteCode}`
        })
      } else {
        // 已登录，直接跳转到加入页面
        uni.navigateTo({
          url: `/pages/shared/join?inviteCode=${inviteCode}`
        })
      }
    }
  }
}
</script>

<style lang="scss">
/* 全局样式 */
@import './common/style/common.scss';

page {
  background: #{$primary-bg-dark};
  color: #{$text-primary-dark};
  font-family: 'Inter', 'Microsoft YaHei', sans-serif;
  min-height: 100vh;
  transition: background-color 0.3s ease, color 0.3s ease;
}

/* 通用按钮样式 */
.btn-primary {
  background: linear-gradient(135deg, $accent-blue, $accent-teal);
  color: white;
  border: none;
  padding: 28rpx;
  border-radius: 24rpx;
  font-size: 32rpx;
  font-weight: 600;
  text-align: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 10rpx 20rpx rgba(59, 130, 246, 0.3);
  
  &:active {
    transform: translateY(2rpx);
    box-shadow: 0 5rpx 10rpx rgba(59, 130, 246, 0.2);
  }
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.05);
  color: $text-secondary;
  border: 2rpx solid $glass-border;
  padding: 28rpx;
  border-radius: 24rpx;
  font-size: 32rpx;
  font-weight: 500;
  text-align: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active {
    background: rgba(255, 255, 255, 0.1);
  }
}

/* 卡片样式 - 玻璃态效果 */
.card {
  background: $glass-bg;
  border-radius: 40rpx;
  padding: 36rpx;
  margin-bottom: 30rpx;
  border: 2rpx solid $glass-border;
  backdrop-filter: blur(20rpx);
  box-shadow: 0 20rpx 50rpx -10rpx rgba(0, 0, 0, 0.5);
}

/* 输入框样式 */
.input {
  width: 100%;
  padding: 28rpx 36rpx;
  background: rgba(255, 255, 255, 0.05);
  border: 2rpx solid $glass-border;
  border-radius: 24rpx;
  font-size: 32rpx;
  color: $text-primary;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.input:focus {
  outline: none;
  border-color: $accent-blue;
  background: rgba(255, 255, 255, 0.08);
}
</style>
