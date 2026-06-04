<template>
  <view class="container" :class="themeClass">
    <view class="profile-shell">
      <!-- 头部：标题 + 装饰图标 -->
      <view class="profile-header">
        <view>
          <text class="profile-header-title">个人中心</text>
          <text class="profile-header-subtitle">资料、提醒和空间偏好</text>
        </view>
      </view>

      <!-- 个人信息：蓝渐变 + 深描边 -->
      <view class="profile-card">
        <view class="avatar" @click="goEditProfile">
          <image v-if="displayAvatar" :src="displayAvatar" mode="aspectFill" />
          <text v-else class="avatar-text">{{ (displayName || 'U').charAt(0) }}</text>
        </view>
        <view class="info-group">
          <text class="profile-name">{{ displayName }}</text>
          <view class="profile-info" v-if="isLoggedIn">
            <text class="profile-info-text">📱 {{ displayPhone }}　·　✉️ {{ displayEmail }}</text>
          </view>
          <view class="profile-info" v-else>
            <text class="profile-info-text">登录后可同步资料与物品统计</text>
          </view>
          <view class="profile-actions" v-if="isLoggedIn">
            <text class="profile-action-link" @click.stop="goPersonalInfo">个人信息</text>
            <text class="profile-action-divider">|</text>
            <text class="profile-action-link" @click.stop="goEditProfile">编辑资料</text>
          </view>
        </view>
      </view>

      <!-- 统计四宫格：深边框 -->
      <view class="stats-grid">
        <view class="stat-cell bg-blue">
          <text class="stat-num color-blue">{{ totalCount }}</text>
          <text class="stat-label">总物品</text>
        </view>
        <view class="stat-cell bg-green">
          <text class="stat-num color-green">{{ normalCount }}</text>
          <text class="stat-label">正常</text>
        </view>
        <view class="stat-cell bg-orange">
          <text class="stat-num color-orange">{{ nearCount }}</text>
          <text class="stat-label">即将过期</text>
        </view>
        <view class="stat-cell bg-red">
          <text class="stat-num color-red">{{ expiredCount }}</text>
          <text class="stat-label">已过期</text>
        </view>
      </view>

      <!-- 会员 / 额度：深色底 + 深描边 -->
      <view v-if="chargeEnabled && !memberUtil.isMember()" class="member-card quota-card">
        <view class="member-top">
          <view>
            <text class="member-kicker">当前套餐</text>
            <text class="member-top-left">免费额度</text>
          </view>
          <view class="member-badge" @click.stop="goToMember">
            <text>升级会员 ›</text>
          </view>
        </view>
        <view class="quota-meter">
          <view class="quota-meter-fill" :style="{ width: quotaPercent + '%' }"></view>
        </view>
        <view class="quota-meta">
          <text>已用 {{ usedQuota }}</text>
          <text>剩余 {{ remainingQuota }}</text>
        </view>
        <view class="member-desc">
          <text>
            免费可管理 <text class="h-blue">{{ freePersonalLimit }}</text> 个个人物品，升级后可获得更高额度。
          </text>
        </view>
      </view>

      <view v-else-if="chargeEnabled && memberUtil.isMember()" class="member-card member-card--vip">
        <view class="member-top">
          <view>
            <text class="member-kicker">当前套餐</text>
            <text class="member-top-left">会员状态</text>
          </view>
          <view class="member-badge member-badge--active">
            <text>已开通</text>
          </view>
        </view>
        <view class="member-desc">
          <text v-if="memberInfo && memberInfo.planType === 'lifetime'">您已开通终身会员，享受无限物品管理。</text>
          <text v-else>您的会员将于 {{ formatExpireTime }} 到期</text>
        </view>
      </view>

      <view v-else class="member-card member-card--free">
        <view class="member-top">
          <view>
            <text class="member-kicker">免费模式</text>
            <text class="member-top-left">使用额度</text>
          </view>
        </view>
        <view class="free-limit-grid">
          <view class="free-limit-cell">
            <text class="free-limit-num">{{ usedQuota }}/{{ freePersonalLimit }}</text>
            <text class="free-limit-label">个人物品</text>
          </view>
          <view class="free-limit-cell">
            <text class="free-limit-num">{{ freeSharedSpaceLimit }}</text>
            <text class="free-limit-label">共享空间</text>
          </view>
          <view class="free-limit-cell">
            <text class="free-limit-num">{{ freeSharedItemLimit }}</text>
            <text class="free-limit-label">每空间物品</text>
          </view>
        </view>
        <view class="member-desc">
          <text>
            当前可免费使用个人物品和共享空间额度。
          </text>
        </view>
      </view>

      <!-- 设置列表：浅底 + 深分割线 -->
      <view class="settings-group">
        <view class="setting-item theme-style-row">
          <view class="setting-left">
            <view class="setting-icon icon-theme">
              <text>🎨</text>
            </view>
            <text class="setting-label">主题风格</text>
          </view>
          <view class="palette-dots">
            <view
              v-for="opt in paletteOptions"
              :key="opt.id"
              class="palette-dot"
              :class="{ active: themePalette === opt.id, white: opt.id === 'white' }"
              :style="{ background: opt.color }"
              @click.stop="selectPalette(opt.id)"
            />
          </view>
        </view>

        <view class="setting-item" @click="goReminderSettings">
          <view class="setting-left">
            <view class="setting-icon icon-bell">
              <text>🔔</text>
            </view>
            <text class="setting-label">到期提醒</text>
          </view>
          <text class="setting-chevron">›</text>
        </view>

        <view class="setting-item" @click="goToPersonal">
          <view class="setting-left">
            <view class="setting-icon icon-box">
              <text>📦</text>
            </view>
            <text class="setting-label">个人空间</text>
          </view>
          <text class="setting-chevron">›</text>
        </view>

        <view class="setting-item" @click="goToShared">
          <view class="setting-left">
            <view class="setting-icon icon-shared">
              <text>👥</text>
            </view>
            <text class="setting-label">共享空间</text>
          </view>
          <text class="setting-chevron">›</text>
        </view>

        <view class="setting-item">
          <view class="setting-left">
            <view class="setting-icon icon-dark">
              <text>🌓</text>
            </view>
            <text class="setting-label">深色模式</text>
          </view>
          <switch
            :checked="!isLightMode"
            @change="toggleTheme"
            color="#3a6bd5"
            class="theme-switch"
          />
        </view>

        <view class="setting-item" @click="exportData">
          <view class="setting-left">
            <image class="setting-icon-img" src="/static/tabbar/download.png" mode="aspectFit" />
            <text class="setting-label">导出数据</text>
          </view>
          <text class="setting-chevron">›</text>
        </view>

        <view class="setting-item setting-item--last" @click="showAbout">
          <view class="setting-left">
            <view class="setting-icon icon-about">
              <text>ℹ️</text>
            </view>
            <text class="setting-label">关于我们</text>
          </view>
          <text class="setting-chevron">›</text>
        </view>
      </view>

      <view class="footer-tip">
        <text>过期了么，让物品状态一眼清楚</text>
      </view>

      <view class="logout-section" v-if="isLoggedIn">
        <button class="btn-logout" @click="logout">退出登录</button>
      </view>
    </view>
  </view>
</template>

<script>
import storage from '@/common/utils/storage.js'
import themeUtil from '@/common/utils/theme.js'
import themeMixin from '@/common/mixins/theme.js'
import memberUtil from '@/common/utils/member.js'
import appConfig from '@/common/utils/appConfig.js'
import { clearLoginData, isLoggedIn as checkLoggedIn } from '@/common/utils/auth.js'
import api from '@/common/utils/api.js'

export default {
  mixins: [themeMixin],
  data() {
    return {
      memberUtil,
      usedQuota: 0,
      remainingQuota: 0,
      memberInfo: {},
      userInfo: {},
      totalCount: 0,
      normalCount: 0,
      nearCount: 0,
      expiredCount: 0,
      isLoggedIn: false,
      profile: {},
      paletteOptions: [
        { id: 'mint', color: '#5A9B7C' },
        { id: 'peach', color: '#F4A261' },
        { id: 'lavender', color: '#B7A6CF' },
        { id: 'ocean', color: '#5C9EAD' },
        { id: 'white', color: '#FFFFFF' }
      ]
    }
  },
  computed: {
    chargeEnabled() {
      return memberUtil.isChargeEnabled()
    },
    freePersonalLimit() {
      return memberUtil.getFreePersonalItemLimit()
    },
    freeSharedSpaceLimit() {
      return memberUtil.getFreeSharedSpaceLimit()
    },
    freeSharedItemLimit() {
      return memberUtil.getFreeSharedItemLimit()
    },
    quotaPercent() {
      var limit = Number(this.freePersonalLimit) || 0
      if (!limit) return 0
      var used = Number(this.usedQuota) || 0
      var percent = Math.round((used / limit) * 100)
      if (percent < 0) return 0
      if (percent > 100) return 100
      return percent
    },
    formatExpireTime() {
      if (!this.memberInfo.expireTime) return ''
      const date = new Date(this.memberInfo.expireTime)
      return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
    },
    displayName() {
      if (this.profile && this.profile.nickname) return this.profile.nickname
      return this.userInfo.nickName || this.userInfo.nickname || '未登录'
    },
    displayAvatar() {
      if (this.profile && this.profile.avatarUrl) return this.profile.avatarUrl
      return this.userInfo.avatarUrl || this.userInfo.avatar
    },
    displayPhone() {
      if (this.profile && this.profile.phone) return this.profile.phone
      return this.userInfo.phone || '未填写'
    },
    displayEmail() {
      if (this.profile && this.profile.email) return this.profile.email
      return this.userInfo.email || '未填写'
    }
  },
  onLoad() {
    this.loadData()
  },
  onShow() {
    appConfig.loadConfig(false)
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

      if (this.isLoggedIn) {
        try {
          var pres = await api.getProfile()
          if (pres && pres.data) {
            this.profile = pres.data
            var merged = Object.assign({}, this.userInfo, {
              nickName: pres.data.nickname || this.userInfo.nickName,
              nickname: pres.data.nickname,
              avatarUrl: pres.data.avatarUrl,
              avatar: pres.data.avatarUrl,
              phone: pres.data.phone,
              email: pres.data.email
            })
            storage.set('userInfo', merged)
            this.userInfo = merged
          }
        } catch (pe) {
          console.warn('加载个人资料失败', pe)
        }
      } else {
        this.profile = {}
      }

      var statsOk = false
      if (this.isLoggedIn) {
        try {
          var sres = await api.getPersonalItemStats()
          if (sres && sres.code === 200 && sres.data) {
            var sd = sres.data
            this.totalCount = Number(sd.total) || 0
            this.normalCount = Number(sd.normal) || 0
            this.nearCount = Number(sd.near) || 0
            this.expiredCount = Number(sd.expired) || 0
            statsOk = true
          }
        } catch (se) {
          console.warn('加载物品统计失败', se)
        }
      }

      if (!statsOk) {
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
    selectPalette(id) {
      themeUtil.setThemePalette(id)
      uni.showToast({ title: '已切换主题风格', icon: 'none', duration: 1200 })
    },
    goEditProfile() {
      if (!this.isLoggedIn) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      uni.navigateTo({ url: '/pages/profile/edit-profile' })
    },
    goPersonalInfo() {
      if (!this.isLoggedIn) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      uni.navigateTo({ url: '/pages/profile/personal-info' })
    },
    goReminderSettings() {
      uni.navigateTo({ url: '/pages/profile/reminder-settings' })
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
  min-height: 100vh;
  padding: 38rpx 30rpx 180rpx;
  background: var(--primary-bg);
  transition: background-color 0.3s ease;
  box-sizing: border-box;
}

.light-mode.container {
  background: linear-gradient(180deg, #f6f9ff 0%, var(--primary-bg) 45%);
}

.profile-shell {
  width: 100%;
  max-width: 800rpx;
  margin: 0 auto;
}

.profile-header {
  margin-bottom: 26rpx;
}

.profile-header-title,
.profile-header-subtitle {
  display: block;
}

.profile-header-title {
  font-size: 46rpx;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1.2;
}

.profile-header-subtitle {
  margin-top: 8rpx;
  color: var(--text-secondary);
  font-size: 25rpx;
  line-height: 1.4;
}

.profile-card,
.stats-grid,
.member-card,
.settings-group {
  border-radius: 30rpx;
  border: 2rpx solid var(--card-border);
  box-shadow: 0 16rpx 44rpx -30rpx var(--shadow-color);
  box-sizing: border-box;
}

.profile-card {
  display: flex;
  align-items: center;
  gap: 26rpx;
  padding: 34rpx;
  margin-bottom: 24rpx;
  color: #ffffff;
  background: linear-gradient(135deg, #2563eb 0%, #0d9488 100%);
  border-color: rgba(255, 255, 255, 0.22);
  overflow: hidden;
  position: relative;
}

.profile-card::after {
  content: '';
  position: absolute;
  right: -90rpx;
  top: -110rpx;
  width: 260rpx;
  height: 260rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
  pointer-events: none;
}

.avatar {
  width: 124rpx;
  height: 124rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 4rpx solid rgba(255, 255, 255, 0.38);
  position: relative;
  z-index: 1;
}

.avatar image {
  width: 100%;
  height: 100%;
}

.avatar-text {
  font-size: 54rpx;
  color: #ffffff;
  font-weight: 800;
}

.info-group {
  flex: 1;
  min-width: 0;
  position: relative;
  z-index: 1;
}

.profile-name {
  display: block;
  font-size: 36rpx;
  font-weight: 800;
  color: #ffffff;
  line-height: 1.25;
  word-break: break-all;
}

.profile-info {
  margin-top: 8rpx;
}

.profile-info-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.82);
  line-height: 1.5;
  word-break: break-all;
}

.profile-actions {
  margin-top: 16rpx;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10rpx;
}

.profile-action-link {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  color: #ffffff;
  font-size: 23rpx;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.16);
}

.profile-action-divider {
  display: none;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12rpx;
  padding: 16rpx;
  margin-bottom: 24rpx;
  background: var(--card-bg-solid);
}

.stat-cell {
  min-height: 112rpx;
  border-radius: 22rpx;
  padding: 14rpx 4rpx;
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
  box-sizing: border-box;
}

.stat-cell.bg-blue {
  background: rgba(37, 99, 235, 0.11);
}

.stat-cell.bg-green {
  background: rgba(5, 150, 105, 0.12);
}

.stat-cell.bg-orange {
  background: rgba(245, 158, 11, 0.13);
}

.stat-cell.bg-red {
  background: rgba(225, 29, 72, 0.11);
}

.stat-num {
  display: block;
  font-size: 42rpx;
  font-weight: 800;
  line-height: 1.1;
}

.color-blue {
  color: #2563eb;
}

.color-green {
  color: #059669;
}

.color-orange {
  color: #d97706;
}

.color-red {
  color: #e11d48;
}

.container:not(.light-mode) .color-blue {
  color: #7da2ff;
}

.container:not(.light-mode) .color-green {
  color: #41dfa5;
}

.container:not(.light-mode) .color-orange {
  color: #fbbf24;
}

.container:not(.light-mode) .color-red {
  color: #ff7a7a;
}

.stat-label {
  display: block;
  margin-top: 6rpx;
  color: var(--text-secondary);
  font-size: 20rpx;
  font-weight: 700;
  line-height: 1.2;
}

.member-card {
  padding: 32rpx;
  margin-bottom: 24rpx;
  color: #ffffff;
  background: #172033;
  border-color: rgba(148, 163, 184, 0.22);
}

.quota-card {
  background: linear-gradient(135deg, #172033, #20314f);
}

.member-card--vip {
  background: linear-gradient(135deg, #1f2937, #3d2f12);
  border-color: rgba(245, 158, 11, 0.36);
}

.member-card--free {
  background: linear-gradient(135deg, #172033, #0f3f3b);
}

.member-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18rpx;
}

.member-kicker,
.member-top-left {
  display: block;
}

.member-kicker {
  margin-bottom: 6rpx;
  color: rgba(255, 255, 255, 0.58);
  font-size: 22rpx;
  font-weight: 700;
}

.member-top-left {
  color: #ffffff;
  font-size: 34rpx;
  font-weight: 800;
  line-height: 1.2;
}

.member-badge {
  padding: 12rpx 22rpx;
  border-radius: 999rpx;
  color: #fbbf24;
  background: rgba(245, 158, 11, 0.14);
  font-size: 24rpx;
  font-weight: 800;
  flex-shrink: 0;
}

.member-badge--active {
  color: #5eead4;
  background: rgba(13, 148, 136, 0.18);
}

.quota-meter {
  height: 18rpx;
  margin-top: 28rpx;
  border-radius: 999rpx;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.12);
}

.quota-meter-fill {
  height: 100%;
  border-radius: 999rpx;
  background: linear-gradient(90deg, #5eead4, #fbbf24);
}

.quota-meta {
  display: flex;
  justify-content: space-between;
  margin-top: 12rpx;
  color: rgba(255, 255, 255, 0.7);
  font-size: 23rpx;
  font-weight: 700;
}

.member-desc {
  margin-top: 20rpx;
  color: rgba(255, 255, 255, 0.72);
  font-size: 25rpx;
  line-height: 1.55;
}

.member-desc .h-blue,
.member-desc .h-orange,
.member-desc .h-green {
  color: #ffffff;
  font-weight: 800;
}

.free-limit-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
  margin-top: 24rpx;
}

.free-limit-cell {
  min-height: 104rpx;
  padding: 16rpx 8rpx;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.12);
  text-align: center;
  box-sizing: border-box;
}

.free-limit-num,
.free-limit-label {
  display: block;
}

.free-limit-num {
  color: #ffffff;
  font-size: 30rpx;
  font-weight: 800;
  line-height: 1.2;
}

.free-limit-label {
  margin-top: 8rpx;
  color: rgba(255, 255, 255, 0.68);
  font-size: 20rpx;
  font-weight: 700;
  line-height: 1.2;
}

.settings-group {
  padding: 8rpx 28rpx;
  margin-bottom: 26rpx;
  background: var(--card-bg-solid);
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 96rpx;
  padding: 14rpx 0;
  border-bottom: 2rpx solid rgba(148, 163, 184, 0.18);
  box-sizing: border-box;

  &:active {
    opacity: 0.86;
  }
}

.setting-item--last {
  border-bottom: none;
}

.theme-style-row {
  flex-wrap: wrap;
  gap: 16rpx;
}

.setting-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
  flex: 1;
  min-width: 0;
}

.setting-label {
  color: var(--text-primary);
  font-size: 29rpx;
  font-weight: 700;
  line-height: 1.25;
}

.setting-icon,
.setting-icon-img {
  width: 62rpx;
  height: 62rpx;
  border-radius: 20rpx;
  flex-shrink: 0;
  box-sizing: border-box;
}

.setting-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 31rpx;
}

.icon-theme {
  background: rgba(37, 99, 235, 0.12);
  color: #2563eb;
}

.icon-bell {
  background: rgba(245, 158, 11, 0.14);
  color: #d97706;
}

.icon-box {
  background: rgba(13, 148, 136, 0.13);
  color: #0d9488;
}

.icon-shared {
  background: rgba(5, 150, 105, 0.13);
  color: #059669;
}

.icon-dark {
  background: rgba(124, 58, 237, 0.12);
  color: #7c3aed;
}

.icon-about {
  background: rgba(225, 29, 72, 0.1);
  color: #e11d48;
}

.setting-icon-img {
  background: rgba(37, 99, 235, 0.1);
  padding: 12rpx;
}

.setting-chevron {
  color: var(--text-tertiary);
  font-size: 34rpx;
  flex-shrink: 0;
}

.palette-dots {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12rpx;
  flex-wrap: wrap;
  max-width: 370rpx;
}

.palette-dot {
  width: 42rpx;
  height: 42rpx;
  border-radius: 50%;
  border: 4rpx solid var(--card-bg-solid);
  box-shadow: 0 0 0 2rpx rgba(148, 163, 184, 0.45);
  box-sizing: border-box;
}

.palette-dot.white {
  box-shadow: 0 0 0 2rpx rgba(100, 116, 139, 0.65);
}

.palette-dot.active {
  box-shadow: 0 0 0 4rpx var(--accent-blue);
}

.theme-switch {
  transform: scale(0.9);
  transform-origin: center right;
}

.footer-tip {
  margin: 8rpx 0 0;
  color: var(--text-tertiary);
  font-size: 23rpx;
  text-align: center;
}

.logout-section {
  margin-top: 30rpx;
}

.btn-logout {
  width: 100%;
  padding: 24rpx;
  border-radius: 24rpx;
  color: #dc2626;
  background: rgba(239, 68, 68, 0.1);
  border: 2rpx solid rgba(239, 68, 68, 0.22);
  font-size: 30rpx;
  font-weight: 800;
  box-sizing: border-box;

  &:active {
    background: rgba(239, 68, 68, 0.16);
  }
}

.container:not(.light-mode) .profile-card,
.container:not(.light-mode) .stats-grid,
.container:not(.light-mode) .settings-group {
  background-color: var(--card-bg-solid);
  border-color: var(--card-border);
}

.container:not(.light-mode) .setting-item {
  border-bottom-color: var(--card-border);
}
</style>
