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
        // 验证邀请码
        const inviteInfo = inviteUtil.getInviteInfo(this.inviteCode)
        
        if (!inviteInfo) {
          this.error = '邀请码不存在或已过期'
          return
        }

        // 检查是否已过期
        const now = new Date()
        const expireTime = new Date(inviteInfo.expireTime)
        if (now > expireTime) {
          this.error = '邀请码已过期'
          inviteUtil.removeInvite(this.inviteCode)
          return
        }

        // 获取空间信息
        const spaces = storage.get('sharedSpaces', [])
        const space = spaces.find(s => s.id === inviteInfo.spaceId)
        
        if (!space) {
          this.error = '共享空间不存在'
          return
        }

        // 检查是否已经是成员
        const userInfo = storage.get('userInfo', {})
        if (space.members && space.members.some(m => m.userId === userInfo.openid)) {
          uni.showModal({
            title: '提示',
            content: '您已经是该空间的成员了',
            showCancel: false,
            success: () => {
              uni.navigateTo({
                url: `/pages/shared/detail?id=${space.id}`
              })
            }
          })
          return
        }

        this.spaceInfo = space
        this.inviterName = inviteInfo.inviterName || '未知'
      } catch (error) {
        console.error('加载邀请信息失败:', error)
        this.error = '加载失败，请重试'
      }
    },

    // 加入空间
    async joinSpace() {
      if (this.joining) return

      this.joining = true

      try {
        const userInfo = storage.get('userInfo', {})
        
        if (!userInfo || !userInfo.openid) {
          uni.showModal({
            title: '提示',
            content: '请先登录',
            showCancel: false,
            success: () => {
              uni.navigateTo({
                url: '/pages/auth/login?inviteCode=' + this.inviteCode
              })
            }
          })
          this.joining = false
          return
        }

        // TODO: 实际项目中应该调用后端API
        // await api.joinSpace({ inviteCode: this.inviteCode })

        // 添加到空间成员列表
        const spaces = storage.get('sharedSpaces', [])
        const spaceIndex = spaces.findIndex(s => s.id === this.spaceInfo.id)
        
        if (spaceIndex !== -1) {
          if (!spaces[spaceIndex].members) {
            spaces[spaceIndex].members = []
          }
          
          // 检查是否已存在
          const exists = spaces[spaceIndex].members.some(m => m.userId === userInfo.openid)
          if (!exists) {
            spaces[spaceIndex].members.push({
              userId: userInfo.openid,
              userName: userInfo.nickName,
              role: 'member',
              joinedAt: new Date().toISOString()
            })
            
            // 更新成员数量
            spaces[spaceIndex].memberCount = spaces[spaceIndex].members.length
            
            storage.set('sharedSpaces', spaces)
          }
        }

        // 标记邀请码为已使用
        inviteUtil.markInviteAsUsed(this.inviteCode)

        uni.showToast({ title: '加入成功', icon: 'success' })

        // 跳转到空间详情
        setTimeout(() => {
          uni.navigateTo({
            url: `/pages/shared/detail?id=${this.spaceInfo.id}`
          })
        }, 1500)
      } catch (error) {
        console.error('加入空间失败:', error)
        uni.showToast({ title: '加入失败，请重试', icon: 'none' })
        this.joining = false
      }
    },

    // 取消
    cancel() {
      uni.navigateBack()
    },

    // 获取空间类型文本
    getSpaceTypeText(type) {
      const typeMap = {
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
