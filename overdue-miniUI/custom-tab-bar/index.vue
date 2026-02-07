<template>
  <view class="custom-tab-bar">
    <view 
      class="tab-item" 
      v-for="(item, index) in list" 
      :key="index"
      :class="{ active: current === index }"
      @click="switchTab(index, item.pagePath)"
    >
      <image 
        class="tab-icon" 
        :src="current === index ? item.selectedIconPath : item.iconPath" 
        mode="aspectFit"
      />
      <text class="tab-text">{{ item.text }}</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      current: 0,
      list: [
        {
          pagePath: '/pages/index/index',
          text: '首页',
          iconPath: 'static/tabbar/home.png',
          selectedIconPath: 'static/tabbar/home-active.png'
        },
        {
          pagePath: '/pages/personal/personal',
          text: '个人',
          iconPath: 'static/tabbar/personal.png',
          selectedIconPath: 'static/tabbar/personal-active.png'
        },
        {
          pagePath: '/pages/shared/list',
          text: '共享',
          iconPath: 'static/tabbar/shared.png',
          selectedIconPath: 'static/tabbar/shared-active.png'
        },
        {
          pagePath: '/pages/profile/profile',
          text: '我的',
          iconPath: 'static/tabbar/profile.png',
          selectedIconPath: 'static/tabbar/profile-active.png'
        }
      ]
    }
  },
  onLoad() {
    this.updateCurrent()
  },
  onShow() {
    this.updateCurrent()
  },
  methods: {
    updateCurrent() {
      // 获取当前页面路径，设置选中状态
      const pages = getCurrentPages()
      if (pages.length === 0) return
      
      const currentPage = pages[pages.length - 1]
      const currentPath = '/' + currentPage.route
      
      const index = this.list.findIndex(item => {
        // 处理路径匹配
        return currentPath === item.pagePath || currentPath.indexOf(item.pagePath) === 0
      })
      if (index !== -1) {
        this.current = index
      }
    },
    switchTab(index, url) {
      if (this.current === index) {
        return
      }
      
      this.current = index
      uni.switchTab({
        url: url,
        fail: (err) => {
          console.error('切换tab失败:', err)
        }
      })
    },
    // 供外部调用的方法，用于更新选中状态
    setData(data) {
      if (data.current !== undefined) {
        this.current = data.current
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/common/style/common.scss';

.custom-tab-bar {
  position: fixed;
  bottom: 30rpx;
  left: 30rpx;
  right: 30rpx;
  height: 120rpx;
  padding-bottom: env(safe-area-inset-bottom);
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: rgba(30, 41, 59, 0.95);
  backdrop-filter: blur(20rpx);
  z-index: 9999;
  border-radius: 40rpx;
  box-shadow: 0 20rpx 50rpx -10rpx rgba(0, 0, 0, 0.6);
  border: 2rpx solid $glass-border;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16rpx 20rpx;
  border-radius: 24rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  margin: 0 8rpx;
}

.tab-item.active {
  background: rgba(59, 130, 246, 0.2);
  border: 2rpx solid rgba(59, 130, 246, 0.3);
}

.tab-item.active .tab-text {
  color: $accent-blue;
  font-weight: 600;
}

.tab-item.active .tab-icon {
  transform: scale(1.1);
}

.tab-item:active {
  transform: scale(0.95);
}

.tab-icon {
  width: 44rpx;
  height: 44rpx;
  margin-bottom: 6rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-text {
  font-size: 22rpx;
  color: $text-secondary;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  line-height: 1.2;
}
</style>
