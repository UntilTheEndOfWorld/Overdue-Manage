<template>
  <view class="container" :class="themeClass">
    <view class="profile-shell">
      <!-- 头部：标题 + 装饰图标 -->
      <view class="profile-header">
        <text class="profile-header-title">个人中心</text>
        <view class="header-icon">
          <text class="header-icon-text">⚙</text>
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
      <view v-if="chargeEnabled && !memberUtil.isMember()" class="member-card">
        <view class="member-top">
          <text class="member-top-left">💎 免费额度</text>
          <view class="member-badge" @click.stop="goToMember">
            <text>升级会员 ›</text>
          </view>
        </view>
        <view class="member-desc">
          <text>
            您有 <text class="h-blue">{{ freePersonalLimit }}</text> 个物品免费额度，已用 <text class="h-orange">{{ usedQuota }}</text>，剩余 <text class="h-green">{{ remainingQuota }}</text>。
          </text>
        </view>
      </view>

      <view v-else-if="chargeEnabled && memberUtil.isMember()" class="member-card member-card--vip">
        <view class="member-top">
          <text class="member-top-left">💎 会员状态</text>
        </view>
        <view class="member-desc">
          <text v-if="memberInfo && memberInfo.planType === 'lifetime'">您已开通终身会员，享受无限物品管理。</text>
          <text v-else>您的会员将于 {{ formatExpireTime }} 到期</text>
        </view>
      </view>

      <view v-else class="member-card">
        <view class="member-top">
          <text class="member-top-left">💎 使用额度</text>
        </view>
        <view class="member-desc">
          <text>
            个人物品 <text class="h-blue">{{ usedQuota }}/{{ freePersonalLimit }}</text>，共享空间上限 <text class="h-orange">{{ freeSharedSpaceLimit }}</text> 个，每空间物品上限 <text class="h-green">{{ freeSharedItemLimit }}</text> 个。
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
        <text>· 加深边框 层次分明 ·</text>
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

$border-strong: #c8d6e8;
$text-title: #1a2642;
$page-bg-light: #f2f6fe;

.container {
  min-height: 100vh;
  padding-bottom: 200rpx;
  background: var(--primary-bg);
  transition: background-color 0.3s ease;
}

.light-mode.container {
  background: $page-bg-light;
  padding: 48rpx 32rpx 80rpx;
}

.container:not(.light-mode) {
  padding: 40rpx 32rpx 80rpx;
}

/* 内层白卡片（参考稿 container） */
.profile-shell {
  width: 100%;
  max-width: 800rpx;
  margin: 0 auto;
  background: #ffffff;
  border-radius: 64rpx;
  padding: 56rpx 48rpx 40rpx;
  box-shadow: 0 24rpx 96rpx rgba(42, 75, 150, 0.08);
  box-sizing: border-box;
}

.container:not(.light-mode) .profile-shell {
  background: var(--card-bg-solid);
  border: 4rpx solid var(--card-border);
  box-shadow: 0 16rpx 48rpx var(--shadow-color);
}

/* 头部 */
.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 48rpx;
  padding-bottom: 24rpx;
  border-bottom: 4rpx solid $border-strong;
}

.container:not(.light-mode) .profile-header {
  border-bottom-color: var(--card-border);
}

.profile-header-title {
  font-size: 44rpx;
  font-weight: 800;
  color: $text-title;
  letter-spacing: -0.6rpx;
}

.container:not(.light-mode) .profile-header-title {
  color: var(--text-primary);
}

.header-icon {
  width: 76rpx;
  height: 76rpx;
  background: #eef4fe;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.container:not(.light-mode) .header-icon {
  background: rgba(58, 107, 213, 0.2);
}

.header-icon-text {
  font-size: 34rpx;
  color: #3a6bd5;
}

.container:not(.light-mode) .header-icon-text {
  color: #7da2ff;
}

/* 个人信息卡片 */
.profile-card {
  display: flex;
  gap: 32rpx;
  align-items: center;
  margin-bottom: 56rpx;
  padding: 36rpx 40rpx;
  background: linear-gradient(145deg, #5b8df7, #3b73e6);
  border-radius: 40rpx;
  color: #ffffff;
  box-shadow: 0 16rpx 48rpx rgba(59, 115, 230, 0.25);
  border: 4rpx solid #2a5abf;
  box-sizing: border-box;
}

.avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
}

.avatar image {
  width: 100%;
  height: 100%;
}

.avatar-text {
  font-size: 56rpx;
  color: #ffffff;
  font-weight: 600;
}

.info-group {
  flex: 1;
  min-width: 0;
}

.profile-name {
  font-size: 36rpx;
  font-weight: 700;
  color: #ffffff;
}

.profile-info {
  margin-top: 4rpx;
}

.profile-info-text {
  font-size: 26rpx;
  color: #dce8ff;
  line-height: 1.5;
  opacity: 0.95;
  word-break: break-all;
}

.profile-actions {
  margin-top: 12rpx;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8rpx;
  font-size: 24rpx;
  font-weight: 600;
  color: #ffffff;
  opacity: 0.95;
}

.profile-action-link {
  text-decoration: underline;
}

.profile-action-divider {
  opacity: 0.45;
}

/* 统计四宫格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
  background: #ffffff;
  border-radius: 40rpx;
  padding: 24rpx 8rpx;
  margin-bottom: 56rpx;
  border: 4rpx solid $border-strong;
  box-sizing: border-box;
}

.container:not(.light-mode) .stats-grid {
  background: var(--card-bg-solid);
  border-color: var(--card-border);
}

.stat-cell {
  text-align: center;
  border-radius: 24rpx;
  padding: 8rpx 0;
}

.stat-cell.bg-blue {
  background: #dce8ff;
  padding: 16rpx 0;
}

.stat-cell.bg-green {
  background: #e5f8ef;
  padding: 16rpx 0;
}

.stat-cell.bg-orange {
  background: #fef5e5;
  padding: 16rpx 0;
}

.stat-cell.bg-red {
  background: #fde8e8;
  padding: 16rpx 0;
}

.container:not(.light-mode) .stat-cell.bg-blue {
  background: rgba(45, 107, 227, 0.2);
}

.container:not(.light-mode) .stat-cell.bg-green {
  background: rgba(31, 203, 138, 0.15);
}

.container:not(.light-mode) .stat-cell.bg-orange {
  background: rgba(244, 178, 42, 0.15);
}

.container:not(.light-mode) .stat-cell.bg-red {
  background: rgba(239, 78, 78, 0.15);
}

.stat-num {
  display: block;
  font-size: 56rpx;
  font-weight: 800;
  letter-spacing: -0.6rpx;
  line-height: 1.1;
}

.color-blue {
  color: #2d6be3;
}

.color-green {
  color: #1fcb8a;
}

.color-orange {
  color: #f4b22a;
}

.color-red {
  color: #ef4e4e;
}

.container:not(.light-mode) .color-blue {
  color: #7da2ff;
}

.container:not(.light-mode) .color-green {
  color: #41dfa5;
}

.container:not(.light-mode) .color-orange {
  color: #f4b22a;
}

.container:not(.light-mode) .color-red {
  color: #ff7a7a;
}

.stat-label {
  display: block;
  font-size: 20rpx;
  color: #7d8fb3;
  font-weight: 600;
  margin-top: 4rpx;
  line-height: 1.2;
}

.container:not(.light-mode) .stat-label {
  color: var(--text-secondary);
}

/* 会员卡片 */
.member-card {
  background: #1a2642;
  border-radius: 40rpx;
  padding: 36rpx 40rpx;
  margin-bottom: 56rpx;
  color: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  box-shadow: 0 16rpx 48rpx rgba(26, 38, 66, 0.08);
  border: 4rpx solid #2c3d66;
  box-sizing: border-box;
}

.member-card--vip {
  border-color: rgba(244, 178, 42, 0.45);
}

.member-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 30rpx;
}

.member-top-left {
  color: #ffffff;
}

.member-badge {
  font-size: 26rpx;
  color: #f4b22a;
  background: rgba(244, 178, 42, 0.12);
  padding: 4rpx 24rpx;
  border-radius: 999rpx;
}

.member-desc {
  font-size: 26rpx;
  color: #bcc6df;
  line-height: 1.5;
}

.member-desc .h-blue {
  font-weight: 700;
  color: #7da2ff;
}

.member-desc .h-orange {
  font-weight: 700;
  color: #f4b22a;
}

.member-desc .h-green {
  font-weight: 700;
  color: #41dfa5;
}

/* 设置列表 */
.settings-group {
  background: #fafcff;
  border-radius: 40rpx;
  padding: 12rpx 36rpx;
  border: 4rpx solid $border-strong;
  box-sizing: border-box;
  margin-bottom: 32rpx;
}

.container:not(.light-mode) .settings-group {
  background: rgba(255, 255, 255, 0.04);
  border-color: var(--card-border);
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 0;
  border-bottom: 4rpx solid $border-strong;
  
  &:active {
    opacity: 0.92;
  }
}

.container:not(.light-mode) .setting-item {
  border-bottom-color: var(--card-border);
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
  gap: 24rpx;
  flex: 1;
  min-width: 0;
}

.setting-label {
  font-size: 30rpx;
  font-weight: 500;
  color: $text-title;
}

.container:not(.light-mode) .setting-label {
  color: var(--text-primary);
}

.setting-icon {
  width: 68rpx;
  height: 68rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 34rpx;
}

.icon-theme {
  background: #dce8ff;
  color: #2d6be3;
}

.icon-bell {
  background: #fef5e5;
  color: #f4b22a;
}

.icon-box {
  background: #e5f0ff;
  color: #2d6be3;
}

.icon-shared {
  background: #e8f5e9;
  color: #1fcb8a;
}

.icon-dark {
  background: #ede7f6;
  color: #5c4d7a;
}

.icon-about {
  background: #fce4ec;
  color: #c2185b;
}

.setting-icon-img {
  width: 68rpx;
  height: 68rpx;
  border-radius: 24rpx;
  flex-shrink: 0;
  background: #e3f2fd;
  padding: 8rpx;
  box-sizing: border-box;
}

.setting-chevron {
  color: #8da0c2;
  font-size: 32rpx;
  flex-shrink: 0;
}

.container:not(.light-mode) .setting-chevron {
  color: var(--text-secondary);
}

.palette-dots {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-wrap: wrap;
  justify-content: flex-end;
  max-width: 360rpx;
}

.palette-dot {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  border: 4rpx solid #ffffff;
  box-shadow: 0 0 0 2rpx #bccae2;
  box-sizing: border-box;
}

.palette-dot.white {
  box-shadow: 0 0 0 2rpx #94a3b8;
}

.palette-dot.active {
  border-color: #1a2642;
  box-shadow: 0 0 0 4rpx #1a2642;
}

.container:not(.light-mode) .palette-dot.active {
  border-color: #e2e8f0;
  box-shadow: 0 0 0 4rpx #e2e8f0;
}

.theme-switch {
  transform: scale(0.92);
  transform-origin: center right;
}

.footer-tip {
  margin-top: 8rpx;
  margin-bottom: 8rpx;
  font-size: 24rpx;
  color: #bccae2;
  text-align: center;
}

.container:not(.light-mode) .footer-tip {
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
  border: 4rpx solid rgba(239, 68, 68, 0.4);
  transition: all 0.3s ease;
  font-weight: 600;
  box-shadow: 0 4rpx 16rpx rgba(239, 68, 68, 0.2);
  
  &:active {
    background: rgba(239, 68, 68, 0.25);
    box-shadow: 0 8rpx 24rpx rgba(239, 68, 68, 0.3);
  }
}

.light-mode .btn-logout {
  background: #fee2e2;
  color: #dc2626;
  border: 4rpx solid #f87171;
  
  &:active {
    background: #fecaca;
  }
}
</style>
