<template>
  <view class="container" :class="themeClass">
    <view class="user-section">
      <view class="avatar">
        <image v-if="userInfo.avatarUrl" :src="userInfo.avatarUrl" mode="aspectFill" />
        <text v-else class="avatar-text">{{ userInfo.nickName?.charAt(0) || 'U' }}</text>
      </view>
      <text class="nickname">{{ userInfo.nickName || '未登录' }}</text>
    </view>

    <view class="stats-section">
      <view class="stat-item">
        <text class="stat-value">{{ totalCount }}</text>
        <text class="stat-label">总物品数</text>
      </view>
      <view class="stat-item">
        <text class="stat-value normal">{{ normalCount }}</text>
        <text class="stat-label">正常</text>
      </view>
      <view class="stat-item">
        <text class="stat-value warning">{{ nearCount }}</text>
        <text class="stat-label">即将过期</text>
      </view>
      <view class="stat-item">
        <text class="stat-value danger">{{ expiredCount }}</text>
        <text class="stat-label">已过期</text>
      </view>
    </view>

    <!-- 额度显示卡片 -->
    <view class="quota-section" v-if="!memberUtil.isMember()">
      <view class="quota-card">
        <view class="quota-header">
          <text class="quota-title">免费额度</text>
          <view class="quota-upgrade" @click="goToMember">
            <text class="upgrade-text">升级会员</text>
            <text class="upgrade-arrow">》</text>
          </view>
        </view>
        <view class="quota-content">
          <text class="quota-desc">您有 <text class="quota-highlight">{{ FREE_QUOTA }}</text> 个物品的免费管理额度，已使用 <text class="quota-highlight">{{ usedQuota }}</text> 个，剩余 <text class="quota-highlight">{{ remainingQuota }}</text> 个。</text>
        </view>
      </view>
    </view>

    <view class="quota-section" v-else>
      <view class="quota-card member">
        <view class="quota-header">
          <text class="quota-title">会员状态</text>
        </view>
        <view class="quota-content">
          <text class="quota-desc" v-if="memberInfo.planType === 'lifetime'">您已开通终身会员，享受无限物品管理。</text>
          <text class="quota-desc" v-else>您的会员将于 {{ formatExpireTime }} 到期</text>
        </view>
      </view>
    </view>

    <view class="menu-section">
      <view class="menu-item" @click="goToPersonal">
        <text class="icon">📦</text>
        <text class="text">个人空间</text>
        <text class="arrow">》</text>
      </view>
      <view class="menu-item" @click="goToShared">
        <text class="icon">👥</text>
        <text class="text">共享空间</text>
        <text class="arrow">》</text>
      </view>
      <!-- 深浅模式切换 -->
      <view class="menu-item">
        <text class="icon">🌓</text>
        <view class="setting-info">
          <text class="text">深色模式</text>
          <text class="subtext">切换到深色主题界面</text>
        </view>
        <switch 
          :checked="!isLightMode" 
          @change="toggleTheme"
          color="#3b82f6"
          class="theme-switch"
        />
      </view>
      <view class="menu-item" @click="exportData">
        <image class="menu-icon" src="/static/tabbar/download.png" mode="aspectFit"></image>
        <text class="text">导出数据</text>
        <text class="arrow">》</text>
      </view>
      <view class="menu-item" @click="showAbout">
        <text class="icon">ℹ️</text>
        <text class="text">关于我们</text>
        <text class="arrow">》</text>
      </view>
    </view>

    <view class="logout-section" v-if="isLoggedIn">
      <button class="btn-logout" @click="logout">退出登录</button>
    </view>
    
  </view>
</template>

<script>
import storage from '@/common/utils/storage.js'
import dateUtil from '@/common/utils/date.js'
import themeUtil from '@/common/utils/theme.js'
import themeMixin from '@/common/mixins/theme.js'
import memberUtil from '@/common/utils/member.js'
import { clearLoginData, isLoggedIn as checkLoggedIn } from '@/common/utils/auth.js'
import api from '@/common/utils/api.js'

export default {
  mixins: [themeMixin],
  data() {
    return {
      memberUtil,
      FREE_QUOTA: 5,
      usedQuota: 0,
      remainingQuota: 0,
      memberInfo: {},
      userInfo: {},
      totalCount: 0,
      normalCount: 0,
      nearCount: 0,
      expiredCount: 0,
      isLoggedIn: false
    }
  },
  computed: {
    formatExpireTime() {
      if (!this.memberInfo.expireTime) return ''
      const date = new Date(this.memberInfo.expireTime)
      return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
    }
  },
  onLoad() {
    this.loadData()
  },
  onShow() {
    this.loadData()
  },
  methods: {
    // 切换主题
    toggleTheme(e) {
      const isDark = e.detail.value
      themeUtil.setTheme(isDark ? 'dark' : 'light')
      
      // 提示用户
      uni.showToast({
        title: isDark ? '已切换到深色模式' : '已切换到浅色模式',
        icon: 'success',
        duration: 1500
      })
    },
    async loadData() {
      this.userInfo = storage.get('userInfo', {})
      this.isLoggedIn = checkLoggedIn()

      var items = []
      try {
        if (this.isLoggedIn) {
          var res = await api.getPersonalItems()
          if (res && res.code === 200 && res.data) {
            items = Array.isArray(res.data) ? res.data : (res.data.rows || res.data.list || [])
          }
        }
      } catch (error) {
        console.error('加载个人物品统计失败:', error)
        items = storage.get('personalItems', []) || []
      }

      this.totalCount = items.length

      if (items.length === 0) {
        this.normalCount = 0
        this.nearCount = 0
        this.expiredCount = 0
      } else {
        var now = new Date()
        now.setHours(0, 0, 0, 0)
        var sevenDaysLater = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000)

        this.expiredCount = items.filter(function(item) {
          if (!item.expiryDate) return false
          var expiry = new Date(item.expiryDate)
          expiry.setHours(0, 0, 0, 0)
          return expiry < now
        }).length

        this.nearCount = items.filter(function(item) {
          if (!item.expiryDate) return false
          try {
            var expiry = new Date(item.expiryDate)
            expiry.setHours(0, 0, 0, 0)
            return expiry >= now && expiry <= sevenDaysLater
          } catch (e) {
            return false
          }
        }).length

        this.normalCount = this.totalCount - this.expiredCount - this.nearCount
        if (this.normalCount < 0) {
          this.normalCount = 0
        }
      }
      
      // 更新会员和额度信息
      this.memberInfo = memberUtil.getMemberInfo()
      this.usedQuota = memberUtil.getUsedQuota()
      this.remainingQuota = memberUtil.getRemainingQuota()
      
      this.$forceUpdate()
    },
    goToMember() {
      uni.navigateTo({
        url: '/pages/member/member'
      })
    },
    goToPersonal() {
      uni.switchTab({
        url: '/pages/personal/personal'
      })
    },
    goToShared() {
      uni.switchTab({
        url: '/pages/shared/list'
      })
    },
    exportData() {
      // 导出数据功能
      const items = storage.get('personalItems', [])
      const sharedItems = storage.get('sharedItems', [])
      
      const exportData = {
        exportTime: new Date().toISOString(),
        personalItems: items,
        sharedItems: sharedItems,
        userInfo: this.userInfo
      }
      
      // 将数据转换为JSON字符串
      const dataStr = JSON.stringify(exportData, null, 2)
      
      // 在微信小程序中，可以使用文件系统API保存文件
      // 这里先显示数据，实际项目中可以调用文件保存API
      uni.showModal({
        title: '导出数据',
        content: `共导出 ${items.length} 件个人物品，${sharedItems.length} 件共享物品。\n\n数据已复制到剪贴板（开发中）`,
        showCancel: false,
        success: () => {
          // TODO: 实际项目中实现文件保存或分享功能
          console.log('导出数据:', exportData)
        }
      })
    },
    showAbout() {
      uni.showModal({
        title: '关于我们',
        content: '过期了么 v1.0.0\n一款专注于个人及家庭物品有效期管理的智能协作小程序',
        showCancel: false
      })
    },
    logout() {
      uni.showModal({
        title: '确认退出',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            // 清除 token、userInfo、loginTime，与 401 处理一致，避免退出后请求仍带旧 token
            clearLoginData()
            uni.showToast({ title: '已退出登录', icon: 'none' })
            uni.reLaunch({
              url: '/pages/index/index'
            })
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/common/style/common.scss';

.container {
  padding: 40rpx;
  min-height: 100vh;
  padding-bottom: 200rpx;
  background: var(--primary-bg);
  transition: background-color 0.3s ease;
}

.user-section {
  position: relative;
  text-align: center;
  padding: 32rpx 30rpx;
  background: linear-gradient(135deg, #8b5cf6, #3b82f6, #14b8a6);
  border-radius: 32rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 20rpx 50rpx -10rpx var(--shadow-color);
  border: 2rpx solid rgba(255, 255, 255, 0.2);
}

/* 浅色模式下使用卡片背景，增加区分度 */
.light-mode .user-section {
  background: var(--card-bg-solid);
  border: 2rpx solid var(--card-border);
  box-shadow: 0 8rpx 24rpx var(--shadow-color);
}

/* 浅色模式下添加顶部渐变条作为装饰 */
.light-mode .user-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 6rpx;
  background: linear-gradient(90deg, #8b5cf6, #3b82f6, #14b8a6);
  border-radius: 32rpx 32rpx 0 0;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  margin: 0 auto 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 3rpx solid rgba(255, 255, 255, 0.3);
}

/* 浅色模式下的头像样式 - 使用渐变背景 */
.light-mode .avatar {
  background: linear-gradient(135deg, #8b5cf6, #3b82f6);
  border: 3rpx solid rgba(59, 130, 246, 0.15);
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.2);
}

.avatar image {
  width: 100%;
  height: 100%;
}

.avatar-text {
  font-size: 48rpx;
  color: white;
  font-weight: 600;
}

.nickname {
  font-size: 32rpx;
  color: white;
  font-weight: 600;
}

/* 浅色模式下的昵称 - 使用深色文字 */
.light-mode .nickname {
  color: var(--text-primary);
  font-weight: 600;
}

.stats-section {
  display: flex;
  justify-content: space-around;
  background: var(--card-bg-solid);
  border-radius: 40rpx;
  padding: 60rpx 40rpx;
  margin-bottom: 40rpx;
  border: 2rpx solid var(--card-border);
  box-shadow: 0 20rpx 50rpx -10rpx var(--shadow-color);
  transition: all 0.3s ease;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 56rpx;
  font-weight: bold;
  margin-bottom: 10rpx;
  line-height: 1.2;
  /* 深色模式：使用亮色渐变确保可见性 */
  background: linear-gradient(135deg, #60a5fa, #34d399);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  /* 备用颜色，如果渐变不支持则使用 */
  @supports not (-webkit-background-clip: text) {
    color: #60a5fa;
    background: none;
    -webkit-text-fill-color: #60a5fa;
  }
}

.stat-value.normal {
  background: linear-gradient(135deg, #34d399, #22c55e);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  @supports not (-webkit-background-clip: text) {
    color: #34d399;
    background: none;
    -webkit-text-fill-color: #34d399;
  }
}

.stat-value.warning {
  background: linear-gradient(135deg, #fbbf24, #f97316);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  @supports not (-webkit-background-clip: text) {
    color: #fbbf24;
    background: none;
    -webkit-text-fill-color: #fbbf24;
  }
}

.stat-value.danger {
  background: linear-gradient(135deg, #fb7185, #ef4444);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  @supports not (-webkit-background-clip: text) {
    color: #fb7185;
    background: none;
    -webkit-text-fill-color: #fb7185;
  }
}

/* 浅色模式下的统计数字 - 确保可见性 */
.light-mode .stat-value {
  background: linear-gradient(135deg, #3b82f6, #14b8a6) !important;
  -webkit-background-clip: text !important;
  background-clip: text !important;
  -webkit-text-fill-color: transparent !important;
}

.light-mode .stat-value.normal {
  background: linear-gradient(135deg, #10b981, #22c55e) !important;
  -webkit-background-clip: text !important;
  background-clip: text !important;
  -webkit-text-fill-color: transparent !important;
}

.light-mode .stat-value.warning {
  background: linear-gradient(135deg, #f59e0b, #f97316) !important;
  -webkit-background-clip: text !important;
  background-clip: text !important;
  -webkit-text-fill-color: transparent !important;
}

.light-mode .stat-value.danger {
  background: linear-gradient(135deg, #ef4444, #dc2626) !important;
  -webkit-background-clip: text !important;
  background-clip: text !important;
  -webkit-text-fill-color: transparent !important;
}

.stat-label {
  font-size: 28rpx;
  color: var(--text-secondary);
}

/* 额度显示卡片 */
.quota-section {
  margin-bottom: 40rpx;
}

.quota-card {
  background: linear-gradient(135deg, #a78bfa, #8b5cf6);
  border-radius: 32rpx;
  padding: 30rpx;
  box-shadow: 0 20rpx 50rpx -10rpx var(--shadow-color);
  border: 2rpx solid rgba(255, 255, 255, 0.2);
}

.quota-card.member {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.light-mode .quota-card {
  background: var(--card-bg-solid);
  border: 2rpx solid var(--card-border);
}

.quota-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.quota-title {
  font-size: 32rpx;
  font-weight: bold;
  color: white;
}

.light-mode .quota-title {
  color: var(--text-primary);
}

.quota-upgrade {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.upgrade-text {
  font-size: 26rpx;
  color: white;
  text-decoration: underline;
}

.light-mode .upgrade-text {
  color: var(--accent-blue);
}

.upgrade-arrow {
  font-size: 24rpx;
  color: white;
}

.light-mode .upgrade-arrow {
  color: var(--accent-blue);
}

.quota-content {
  margin-top: 10rpx;
}

.quota-desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.95);
  line-height: 1.6;
}

.light-mode .quota-desc {
  color: var(--text-primary);
}

.quota-highlight {
  font-weight: bold;
  font-size: 28rpx;
  color: white;
}

.light-mode .quota-highlight {
  color: var(--accent-blue);
}

.menu-section {
  background: var(--card-bg-solid);
  border-radius: 40rpx;
  overflow: hidden;
  margin-bottom: 40rpx;
  border: 2rpx solid var(--card-border);
  box-shadow: 0 20rpx 50rpx -10rpx var(--shadow-color);
  transition: all 0.3s ease;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 40rpx;
  border-bottom: 2rpx solid var(--card-border);
  transition: all 0.3s ease;
  
  &:active {
    background: var(--hover-bg);
  }
}

.menu-item:last-child {
  border-bottom: none;
}

.setting-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.subtext {
  font-size: 24rpx;
  color: var(--text-secondary);
}

.theme-switch {
  transform: scale(1.2);
}

.menu-item .icon {
  font-size: 48rpx;
  margin-right: 30rpx;
}

.menu-icon {
  width: 48rpx;
  height: 48rpx;
  margin-right: 30rpx;
}

.menu-item .text {
  flex: 1;
  font-size: 32rpx;
  color: var(--text-primary);
}

.menu-item .arrow {
  font-size: 32rpx;
  color: var(--text-secondary);
}

.logout-section {
  margin-top: 40rpx;
}

.btn-logout {
  width: 100%;
  padding: 28rpx;
  background: rgba(239, 68, 68, 0.15);
  color: #f87171;
  border-radius: 24rpx;
  font-size: 32rpx;
  border: 2rpx solid rgba(239, 68, 68, 0.4);
  transition: all 0.3s ease;
  font-weight: 600;
  box-shadow: 0 2rpx 8rpx rgba(239, 68, 68, 0.2);
  
  &:active {
    background: rgba(239, 68, 68, 0.25);
    box-shadow: 0 4rpx 12rpx rgba(239, 68, 68, 0.3);
  }
}

/* 浅色模式下的退出登录按钮 - 增加对比度 */
.light-mode .btn-logout {
  background: #fee2e2 !important;
  color: #dc2626 !important;
  border: 2rpx solid #f87171 !important;
  box-shadow: 0 2rpx 8rpx rgba(239, 68, 68, 0.2) !important;
  font-weight: 600 !important;
  
  &:active {
    background: #fecaca !important;
    box-shadow: 0 4rpx 12rpx rgba(239, 68, 68, 0.3) !important;
  }
}
</style>
