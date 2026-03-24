<template>
  <view class="container" :class="themeClass">
    <view class="invite-content">
      <view class="header-section">
        <text class="title">邀请成员加入</text>
        <text class="subtitle">{{ spaceInfo.name || '共享空间' }}</text>
      </view>

      <view class="qr-code-section">
        <text class="section-title">邀请二维码</text>
        <view class="qr-code">
          <image 
            v-if="qrCodePath" 
            :src="qrCodePath" 
            mode="aspectFit"
            class="qr-image"
          />
          <text v-else class="qr-placeholder">生成中...</text>
        </view>
        <view class="invite-code-box">
          <text class="invite-code-label">邀请码</text>
          <text class="invite-code">{{ inviteCode }}</text>
          <button class="btn-copy-code" @click="copyCode">复制</button>
        </view>
      </view>

      <view class="invite-link-section">
        <text class="section-title">邀请链接</text>
        <view class="link-box">
          <text class="link">{{ inviteLink }}</text>
          <button class="btn-copy" @click="copyLink">复制</button>
        </view>
        <button class="btn-share" @click="shareInvite">
          <text class="share-icon">📤</text>
          <text>分享给微信好友</text>
        </button>
      </view>

      <view class="tips">
        <view class="tip-item">
          <text class="tip-icon">💡</text>
          <text>分享二维码或链接给好友</text>
        </view>
        <view class="tip-item">
          <text class="tip-icon">⏰</text>
          <text>邀请码有效期7天</text>
        </view>
        <view class="tip-item">
          <text class="tip-icon">✅</text>
          <text>好友加入后即可共同管理物品</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import storage from '@/common/utils/storage.js'
import inviteUtil from '@/common/utils/invite.js'
import themeMixin from '@/common/mixins/theme.js'
import api from '@/common/utils/api.js'

export default {
  mixins: [themeMixin],
  data() {
    return {
      spaceId: '',
      spaceInfo: {},
      inviteCode: '',
      inviteLink: '',
      qrCodePath: ''
    }
  },
  onLoad(options) {
    this.spaceId = options.spaceId
    this.loadSpaceInfo()
    this.generateInvite()
  },
  methods: {
    // 加载空间信息
    async loadSpaceInfo() {
      try {
        var res = await api.getSharedSpaceDetail(this.spaceId)
        if (res && res.code === 200 && res.data) {
          this.spaceInfo = res.data
        }
      } catch (error) {
        console.error('加载空间信息失败:', error)
        var spaces = storage.get('sharedSpaces', [])
        var self = this
        this.spaceInfo = spaces.find(function(s) { return String(s.id) === String(self.spaceId) }) || {}
      }
    },

    // 生成邀请
    async generateInvite() {
      try {
        var res = await api.generateInviteCode(this.spaceId)
        if (res && res.code === 200 && res.data) {
          this.inviteCode = res.data.inviteCode || res.data
          this.inviteLink = inviteUtil.generateInviteLink(this.inviteCode)
        }
      } catch (error) {
        console.error('生成邀请码失败:', error)
        // 兜底使用本地邀请码生成
        var userInfo = storage.get('userInfo', {})
        this.inviteCode = inviteUtil.generateInviteCode(
          this.spaceId,
          userInfo.openid || '',
          userInfo.nickName || '用户',
          7
        )
        this.inviteLink = inviteUtil.generateInviteLink(this.inviteCode)
      }
      this.generateQRCode()
    },

    // 生成二维码
    generateQRCode() {
      // TODO: 实际项目中应该调用后端API或使用uni-app的二维码生成插件
      // 这里使用一个占位符，实际应该使用真实的二维码生成库
      // 例如：使用 uQRCode 插件
      
      // 示例：使用 uQRCode（需要先安装）
      // #ifdef MP-WEIXIN
      // const QR = require('@/common/utils/uqrcode.js')
      // QR.make({
      //   canvasId: 'qrcode',
      //   componentInstance: this,
      //   text: this.inviteLink,
      //   size: 400,
      //   success: (res) => {
      //     this.qrCodePath = res
      //   }
      // })
      // #endif
      
      // 临时方案：显示一个占位符
      this.qrCodePath = ''
    },

    // 复制邀请码
    copyCode() {
      uni.setClipboardData({
        data: this.inviteCode,
        success: () => {
          uni.showToast({ title: '邀请码已复制', icon: 'success' })
        }
      })
    },

    // 复制链接
    copyLink() {
      uni.setClipboardData({
        data: this.inviteLink,
        success: () => {
          uni.showToast({ title: '链接已复制', icon: 'success' })
        }
      })
    },

    // 分享给微信好友
    shareInvite() {
      // #ifdef MP-WEIXIN
      // 小程序分享
      // #endif
      
      // #ifdef H5
      // H5分享（需要配置微信JS-SDK）
      // #endif
      
      // 临时方案：复制链接并提示
      this.copyLink()
      uni.showToast({ 
        title: '链接已复制，请粘贴发送给好友', 
        icon: 'success',
        duration: 2000
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

.invite-content {
  @extend .glass-card;
  padding: 60rpx 40rpx;
}

.header-section {
  text-align: center;
  margin-bottom: 60rpx;
}

.title {
  display: block;
  font-size: 48rpx;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16rpx;
}

.subtitle {
  display: block;
  font-size: 28rpx;
  color: var(--text-secondary);
}

.qr-code-section {
  text-align: center;
  margin-bottom: 60rpx;
}

.section-title {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 40rpx;
}

.qr-code {
  width: 400rpx;
  height: 400rpx;
  background: rgba(255, 255, 255, 0.05);
  border: 2rpx dashed $glass-border;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 40rpx;
  overflow: hidden;
}

.qr-image {
  width: 100%;
  height: 100%;
}

.qr-placeholder {
  color: var(--text-secondary);
  font-size: 28rpx;
}

.invite-code-box {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
  padding: 24rpx;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 24rpx;
  border: 2rpx solid $glass-border;
}

.invite-code-label {
  font-size: 28rpx;
  color: var(--text-secondary);
}

.invite-code {
  font-size: 32rpx;
  color: var(--accent-blue);
  font-weight: 600;
  letter-spacing: 4rpx;
}

.btn-copy-code {
  padding: 12rpx 24rpx;
  background: var(--active-bg);
  border: 2rpx solid var(--accent-blue);
  color: var(--accent-blue);
  border-radius: 20rpx;
  font-size: 24rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active {
    background: rgba(59, 130, 246, 0.25);
  }
}

.invite-link-section {
  margin-bottom: 60rpx;
}

.link-box {
  display: flex;
  gap: 20rpx;
  align-items: center;
  margin-bottom: 30rpx;
}

.link {
  flex: 1;
  padding: 24rpx;
  background: var(--hover-bg);
  border: 2rpx solid var(--card-border);
  border-radius: 24rpx;
  font-size: 26rpx;
  color: var(--text-primary);
  word-break: break-all;
}

.btn-copy {
  padding: 24rpx 40rpx;
  background: var(--active-bg);
  border: 2rpx solid var(--accent-blue);
  color: var(--accent-blue);
  border-radius: 24rpx;
  font-size: 28rpx;
  white-space: nowrap;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active {
    background: rgba(59, 130, 246, 0.25);
  }
}

.btn-share {
  width: 100%;
  padding: 32rpx;
  background: linear-gradient(135deg, #07c160, #06ad56);
  color: white;
  border-radius: 24rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  box-shadow: 0 10rpx 20rpx rgba(7, 193, 96, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active {
    transform: translateY(2rpx);
  }
}

.share-icon {
  font-size: 40rpx;
}

.tips {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  padding: 40rpx;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 24rpx;
  border: 2rpx solid $glass-border;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  font-size: 28rpx;
  color: var(--text-secondary);
}

.tip-icon {
  font-size: 32rpx;
}

/* 浅色模式下的邀请卡片 */
.light-mode .invite-content {
  background: var(--card-bg-solid) !important;
  border: 2rpx solid var(--card-border) !important;
}

/* 浅色模式下的二维码区域 */
.light-mode .qr-code {
  background: #f8fafc !important;
  border: 2rpx dashed rgba(59, 130, 246, 0.3) !important;
}

/* 浅色模式下的邀请码框 */
.light-mode .invite-code-box {
  background: #f8fafc !important;
  border: 2rpx solid rgba(59, 130, 246, 0.3) !important;
}

/* 浅色模式下的链接框 */
.light-mode .link {
  background: #ffffff !important;
  border: 2rpx solid rgba(59, 130, 246, 0.3) !important;
  color: #1e293b !important;
}

/* 浅色模式下的按钮 */
.light-mode .btn-copy-code,
.light-mode .btn-copy {
  background: rgba(59, 130, 246, 0.1) !important;
  border: 2rpx solid rgba(59, 130, 246, 0.3) !important;
  color: #3b82f6 !important;
}

.light-mode .btn-copy-code:active,
.light-mode .btn-copy:active {
  background: rgba(59, 130, 246, 0.2) !important;
}

/* 浅色模式下的提示区域 */
.light-mode .tips {
  background: #f8fafc !important;
  border: 2rpx solid rgba(59, 130, 246, 0.2) !important;
}
</style>
