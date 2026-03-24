<template>
  <view class="container" :class="themeClass">
    <view class="join-content">
      <view class="header-section">
        <view class="space-icon">{{ spaceInfo.name ? spaceInfo.name.charAt(0) : '空' }}</view>
        <text class="space-name">{{ spaceInfo.name || '加载中...' }}</text>
        <text class="space-desc" v-if="spaceInfo.memberCount">
          {{ spaceInfo.memberCount }} 个成员 · {{ spaceInfo.itemCount || 0 }} 件物品
        </text>
      </view>

      <view class="info-section" v-if="spaceInfo.name">
        <view class="info-item">
          <text class="info-label">邀请人</text>
          <text class="info-value">{{ inviterName || '未知' }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">空间类型</text>
          <text class="info-value">{{ getSpaceTypeText(spaceInfo.type) }}</text>
        </view>
      </view>

      <view class="action-section" v-if="spaceInfo.name">
        <button class="join-btn" @click="joinSpace" :disabled="joining">
          {{ joining ? '加入中...' : '加入共享空间' }}
        </button>
        <button class="cancel-btn" @click="cancel">取消</button>
      </view>

      <view class="loading-section" v-if="!spaceInfo.name && !error">
        <text class="loading-text">正在验证邀请码...</text>
      </view>

      <view class="error-section" v-if="error">
        <text class="error-icon">❌</text>
        <text class="error-text">{{ error }}</text>
        <button class="retry-btn" @click="loadInviteInfo">重试</button>
      </view>
    </view>
  </view>
</template>

<script>
import storage from '@/common/utils/storage.js'
import inviteUtil from '@/common/utils/invite.js'
import themeMixin from '@/common/mixins/theme.js'
import api from '@/common/utils/api.js'
import { isLoggedIn } from '@/common/utils/auth.js'

export default {
  mixins: [themeMixin],
  data() {
    return {
      inviteCode: '',
      spaceInfo: {},
      inviterName: '',
      joining: false,
      error: ''
    }
  },
  onLoad(options) {
    if (options.inviteCode) {
      this.inviteCode = options.inviteCode
      this.loadInviteInfo()
    } else {
      this.error = '邀请码无效'
    }
  },
  methods: {
    // 加载邀请信息
    async loadInviteInfo() {
      this.error = ''
      
      try {
        // 优先从后端获取邀请信息
        var res = await api.getInviteInfo(this.inviteCode)
        if (res && res.code === 200 && res.data) {
          this.spaceInfo = res.data.space || res.data
          this.inviterName = res.data.inviterName || '未知'
          return
        }
        this.error = '邀请码无效或已过期'
      } catch (error) {
        console.error('加载邀请信息失败:', error)
        // 兜底尝试本地验证
        try {
          var inviteInfo = inviteUtil.getInviteInfo(this.inviteCode)
          if (!inviteInfo) {
            this.error = '邀请码不存在或已过期'
            return
          }
          var now = new Date()
          var expireTime = new Date(inviteInfo.expireTime)
          if (now > expireTime) {
            this.error = '邀请码已过期'
            return
          }
          this.inviterName = inviteInfo.inviterName || '未知'
          // 尝试从后端获取空间信息
          try {
            var spaceRes = await api.getSharedSpaceDetail(inviteInfo.spaceId)
            if (spaceRes && spaceRes.code === 200 && spaceRes.data) {
              this.spaceInfo = spaceRes.data
            }
          } catch (e2) {
            this.spaceInfo = { name: '共享空间', id: inviteInfo.spaceId }
          }
        } catch (localError) {
          this.error = '加载失败，请重试'
        }
      }
    },

    // 加入空间
    async joinSpace() {
      if (this.joining) return
      this.joining = true

      try {
        if (!isLoggedIn()) {
          var self = this
          uni.showModal({
            title: '提示',
            content: '请先登录',
            showCancel: false,
            success: function() {
              uni.navigateTo({
                url: '/pages/auth/login?inviteCode=' + self.inviteCode
              })
            }
          })
          this.joining = false
          return
        }

        // 调用后端API加入空间
        var res = await api.joinSpace(this.inviteCode)

        uni.showToast({ title: '加入成功', icon: 'success' })

        var spaceId = this.spaceInfo.id
        setTimeout(function() {
          uni.navigateTo({
            url: '/pages/shared/detail?id=' + spaceId
          })
        }, 1500)
      } catch (error) {
        console.error('加入空间失败:', error)
        uni.showToast({ title: error.message || '加入失败，请重试', icon: 'none' })
      } finally {
        this.joining = false
      }
    },

    // 取消
    cancel() {
      uni.navigateBack()
    },

    // 获取空间类型文本
    getSpaceTypeText(type) {
      var typeMap = {
        'family': '家庭',
        'office': '办公室',
        'roommate': '室友',
        'other': '其他'
      }
      return typeMap[type] || '其他'
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/common/style/common.scss';

.container {
  min-height: 100vh;
  background: var(--primary-bg);
  transition: background-color 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.join-content {
  width: 100%;
  max-width: 600rpx;
  @extend .glass-card;
  padding: 60rpx 40rpx;
}

.header-section {
  text-align: center;
  margin-bottom: 60rpx;
}

.space-icon {
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #8b5cf6, $accent-blue);
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64rpx;
  font-weight: 600;
  color: white;
  margin: 0 auto 30rpx;
  box-shadow: 0 20rpx 40rpx rgba(139, 92, 246, 0.3);
}

.space-name {
  display: block;
  font-size: 48rpx;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 16rpx;
}

.space-desc {
  display: block;
  font-size: 28rpx;
  color: $text-secondary;
}

.info-section {
  margin-bottom: 60rpx;
  padding: 40rpx;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 24rpx;
  border: 2rpx solid $glass-border;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 2rpx solid $glass-border;
  
  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  font-size: 28rpx;
  color: $text-secondary;
}

.info-value {
  font-size: 28rpx;
  color: $text-primary;
  font-weight: 500;
}

.action-section {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.join-btn {
  width: 100%;
  padding: 32rpx;
  background: linear-gradient(135deg, $accent-blue, $accent-teal);
  color: white;
  border-radius: 24rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  box-shadow: 0 10rpx 20rpx rgba(59, 130, 246, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active:not(:disabled) {
    transform: translateY(2rpx);
  }
  
  &:disabled {
    opacity: 0.6;
  }
}

.cancel-btn {
  width: 100%;
  padding: 28rpx;
  background: rgba(255, 255, 255, 0.05);
  border: 2rpx solid $glass-border;
  color: $text-secondary;
  border-radius: 24rpx;
  font-size: 28rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active {
    background: rgba(255, 255, 255, 0.1);
  }
}

.loading-section {
  text-align: center;
  padding: 80rpx 0;
}

.loading-text {
  font-size: 28rpx;
  color: $text-secondary;
}

.error-section {
  text-align: center;
  padding: 40rpx 0;
}

.error-icon {
  display: block;
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.error-text {
  display: block;
  font-size: 28rpx;
  color: $accent-rose;
  margin-bottom: 40rpx;
}

.retry-btn {
  padding: 24rpx 48rpx;
  background: rgba(59, 130, 246, 0.15);
  border: 2rpx solid rgba(59, 130, 246, 0.3);
  color: $accent-blue;
  border-radius: 24rpx;
  font-size: 28rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active {
    background: rgba(59, 130, 246, 0.25);
  }
}

/* 浅色模式下的卡片 */
.light-mode .join-content {
  background: var(--card-bg-solid) !important;
  border: 2rpx solid var(--card-border) !important;
}

/* 浅色模式下的信息区域 */
.light-mode .info-section {
  background: var(--hover-bg) !important;
  border: 2rpx solid var(--card-border) !important;
}

/* 浅色模式下的按钮 */
.light-mode .join-btn {
  background: linear-gradient(135deg, #3b82f6, #14b8a6) !important;
  box-shadow: 0 10rpx 20rpx rgba(59, 130, 246, 0.3) !important;
}

.light-mode .cancel-btn {
  background: #f1f5f9 !important;
  border: 2rpx solid rgba(59, 130, 246, 0.3) !important;
  color: #475569 !important;
}

.light-mode .retry-btn {
  background: rgba(59, 130, 246, 0.1) !important;
  border: 2rpx solid rgba(59, 130, 246, 0.3) !important;
  color: #3b82f6 !important;
}
</style>
