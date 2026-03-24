<template>
  <view class="login-page" :class="themeClass">
    <!-- 顶部品牌区域 -->
    <view class="brand-area">
      <view class="brand-bg"></view>
      <view class="brand-content">
        <image class="brand-logo" src="/static/tabbar/home-active.png" mode="aspectFit" />
        <text class="brand-name">时光守护</text>
        <text class="brand-slogan">智能管理您的物品有效期</text>
      </view>
    </view>

    <!-- 登录卡片 -->
    <view class="login-card">
      <!-- 登录方式切换 -->
      <view class="login-tabs">
        <view
          class="login-tab"
          :class="{ active: loginType === 'wechat' }"
          @click="loginType = 'wechat'"
        >
          微信登录
        </view>
        <view
          class="login-tab"
          :class="{ active: loginType === 'phone' }"
          @click="loginType = 'phone'"
        >
          手机号登录
        </view>
        <view class="tab-indicator" :style="{ left: loginType === 'wechat' ? '0' : '50%' }"></view>
      </view>

      <!-- 微信登录 -->
      <view v-if="loginType === 'wechat'" class="login-body">
        <view class="wechat-info">
          <view class="wechat-icon-wrap">
            <text class="wechat-icon">💬</text>
          </view>
          <text class="wechat-desc">使用微信账号一键登录，安全便捷</text>
        </view>
        <button class="btn-login btn-wechat" @click="wechatLogin">
          <text class="btn-login-text">微信快速登录</text>
        </button>
      </view>

      <!-- 手机号登录 -->
      <view v-if="loginType === 'phone'" class="login-body">
        <view class="form-item">
          <text class="form-label">手机号</text>
          <view class="form-input-wrap">
            <text class="input-prefix">+86</text>
            <input
              class="form-input"
              type="number"
              v-model="phone"
              placeholder="请输入手机号"
              maxlength="11"
            />
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">验证码</text>
          <view class="form-input-wrap">
            <input
              class="form-input"
              type="number"
              v-model="code"
              placeholder="请输入6位验证码"
              maxlength="6"
            />
            <view
              class="code-btn"
              :class="{ disabled: codeCountdown > 0 }"
              @click="sendCode"
            >
              <text class="code-btn-text">{{ codeCountdown > 0 ? codeCountdown + 's' : '获取验证码' }}</text>
            </view>
          </view>
        </view>

        <button class="btn-login btn-phone" @click="phoneLogin">
          <text class="btn-login-text">登录 / 注册</text>
        </button>
        <text class="auto-register-tip">未注册的手机号将自动创建账号</text>
      </view>
    </view>

    <!-- 底部协议 -->
    <view class="agreement-area">
      <text class="agreement-text">登录即表示同意</text>
      <text class="agreement-link" @click="showAgreement('user')">《用户协议》</text>
      <text class="agreement-text">和</text>
      <text class="agreement-link" @click="showAgreement('privacy')">《隐私政策》</text>
    </view>

    <!-- 安全提示 -->
    <view class="trust-bar">
      <view class="trust-item">
        <text class="trust-icon">🔒</text>
        <text class="trust-text">数据加密</text>
      </view>
      <view class="trust-item">
        <text class="trust-icon">🛡️</text>
        <text class="trust-text">隐私保护</text>
      </view>
      <view class="trust-item">
        <text class="trust-icon">✅</text>
        <text class="trust-text">安全认证</text>
      </view>
    </view>

    <!-- 微信手机号获取弹窗 -->
    <view class="phone-modal" v-if="showPhoneModal">
      <view class="modal-overlay" @click="closePhoneModal"></view>
      <view class="modal-content">
        <view class="modal-header">
          <view class="modal-close" @click="closePhoneModal">
            <text class="modal-close-icon">×</text>
          </view>
          <text class="modal-title">获取手机号</text>
          <view class="modal-close" style="visibility:hidden;">
            <text class="modal-close-icon">×</text>
          </view>
        </view>
        <view class="modal-body">
          <view class="modal-icon-wrap">
            <text class="modal-icon">📱</text>
          </view>
          <text class="modal-desc">通过关联手机号成为时光守护会员，可及时接收提醒信息</text>

          <!-- #ifdef MP-WEIXIN -->
          <button
            class="btn-login btn-wechat modal-btn"
            open-type="getPhoneNumber"
            @getphonenumber="onGetPhoneNumber"
          >
            <text class="btn-login-text">授权并绑定手机号</text>
          </button>
          <!-- #endif -->

          <!-- #ifndef MP-WEIXIN -->
          <view class="btn-login btn-disabled modal-btn">
            <text class="btn-login-text">请在微信小程序中使用</text>
          </view>
          <!-- #endif -->

          <view class="modal-skip" @click="closePhoneModal">
            <text class="modal-skip-text">暂不登录</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import storage from '@/common/utils/storage.js'
import themeMixin from '@/common/mixins/theme.js'
import { userAPI } from '@/common/utils/api.js'
import { tokenManager } from '@/common/utils/auth.js'
import { executeLoginCallback } from '@/common/utils/loginCallback.js'

export default {
  mixins: [themeMixin],
  data() {
    return {
      loginType: 'wechat',
      phone: '',
      code: '',
      codeCountdown: 0,
      inviteCode: '',
      countdownTimer: null,
      timers: [],
      showPhoneModal: false,
      phoneData: null,
      userInfo: null,
      loginCode: null
    }
  },
  onLoad(options) {
    if (options.inviteCode) {
      this.inviteCode = options.inviteCode
      storage.set('pendingInviteCode', this.inviteCode)
    }
  },
  onUnload() {
    this.clearAllTimers()
  },
  onHide() {
    this.clearAllTimers()
  },
  methods: {
    clearAllTimers() {
      if (this.countdownTimer) {
        clearInterval(this.countdownTimer)
        this.countdownTimer = null
      }
      this.timers.forEach(function(timerId) {
        clearTimeout(timerId)
        clearInterval(timerId)
      })
      this.timers = []
    },

    showAgreement(type) {
      uni.showToast({ title: '协议页面开发中', icon: 'none' })
    },

    // 微信登录
    async wechatLogin() {
      this.showPhoneModal = true

      try {
        var profileRes = await uni.getUserProfile({
          desc: '用于完善用户资料'
        })
        this.userInfo = profileRes.userInfo

        var loginResult = await new Promise(function(resolve, reject) {
          uni.login({
            provider: 'weixin',
            success: resolve,
            fail: reject
          })
        })
        this.loginCode = loginResult.code
        console.log('用户信息和登录code获取成功')
      } catch (error) {
        console.error('获取用户信息失败:', error)
        uni.showToast({ title: '获取用户信息失败，请重试', icon: 'none' })
        this.showPhoneModal = false
      }
    },

    // 处理微信手机号获取
    async onGetPhoneNumber(e) {
      console.log('微信手机号获取结果:', e)

      if (e.detail.errMsg === 'getPhoneNumber:ok') {
        this.phoneData = {
          encryptedData: e.detail.encryptedData,
          iv: e.detail.iv
        }
        this.showPhoneModal = false
        await this.getPhoneNumberFromServer()
      } else {
        uni.showToast({ title: '需要手机号才能完成登录', icon: 'none' })
      }
    },

    // 从服务器获取手机号并完成注册
    async getPhoneNumberFromServer() {
      if (!this.phoneData || !this.phoneData.encryptedData || !this.phoneData.iv) {
        uni.showToast({ title: '手机号数据无效，请重新获取', icon: 'none' })
        return
      }

      uni.showLoading({ title: '注册中...' })

      try {
        if (!this.userInfo || !this.loginCode) {
          throw new Error('用户信息或登录凭证缺失，请重新获取')
        }

        var res = await userAPI.wechatRegisterWithPhone({
          code: this.loginCode,
          encryptedData: this.phoneData.encryptedData,
          iv: this.phoneData.iv,
          nickname: this.userInfo.nickName,
          avatarUrl: this.userInfo.avatarUrl
        })

        uni.hideLoading()

        if (res && res.data) {
          var userData = {
            id: res.userId || res.memberId || 'wechat_user_' + Date.now(),
            userId: res.userId || res.memberId || 'wechat_user_' + Date.now(),
            nickname: res.nickname || this.userInfo.nickName,
            avatar: res.avatar || this.userInfo.avatarUrl,
            phone: res.phone || '',
            isGuest: false,
            loginTime: Date.now()
          }

          uni.setStorageSync('userInfo', userData)
          tokenManager.setToken(res.data)
          uni.setStorageSync('loginTime', Date.now())

          uni.$emit('loginSuccess', { action: 'refresh' })
          this.handleInvite()

          var callbackData = executeLoginCallback()

          uni.showToast({ title: '登录成功', icon: 'success', duration: 2000 })

          var timerId = setTimeout(function() {
            if (callbackData && callbackData.url) {
              if (callbackData.type === 'switchTab') {
                uni.switchTab({ url: callbackData.url })
              } else {
                uni.navigateTo({ url: callbackData.url })
              }
            } else {
              uni.reLaunch({ url: '/pages/index/index' })
            }
          }, 1500)
          this.timers.push(timerId)
        } else {
          throw new Error('注册失败')
        }
      } catch (error) {
        uni.hideLoading()
        console.error('注册失败:', error)
        var errorMessage = '注册失败，请重试'
        if (error.message && error.message.indexOf('微信登录失败') !== -1) {
          errorMessage = '微信授权已过期，请重新获取'
        } else if (error.message && error.message.indexOf('invalid code') !== -1) {
          errorMessage = '授权码已过期，请重新获取'
        } else if (error.message && error.message.indexOf('网络') !== -1) {
          errorMessage = '网络连接异常，请检查网络'
        }
        uni.showToast({ title: errorMessage, icon: 'none', duration: 3000 })
      }
    },

    // 发送验证码
    async sendCode() {
      if (this.codeCountdown > 0) return
      if (!this.phone) {
        uni.showToast({ title: '请输入手机号', icon: 'none' })
        return
      }
      if (!/^1[3-9]\d{9}$/.test(this.phone)) {
        uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
        return
      }

      try {
        uni.showLoading({ title: '发送中...' })
        await userAPI.sendSmsCode({ phone: this.phone })
        uni.hideLoading()
        uni.showToast({ title: '验证码已发送', icon: 'success' })

        if (this.countdownTimer) {
          clearInterval(this.countdownTimer)
        }
        var self = this
        this.codeCountdown = 60
        this.countdownTimer = setInterval(function() {
          if (self.codeCountdown <= 0) {
            clearInterval(self.countdownTimer)
            self.countdownTimer = null
          } else {
            self.codeCountdown--
          }
        }, 1000)
      } catch (error) {
        uni.hideLoading()
        console.error('发送验证码失败:', error)
        uni.showToast({ title: error.message || '发送验证码失败', icon: 'none' })
      }
    },

    // 手机号登录
    async phoneLogin() {
      if (!this.phone) {
        uni.showToast({ title: '请输入手机号', icon: 'none' })
        return
      }
      if (!/^1[3-9]\d{9}$/.test(this.phone)) {
        uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
        return
      }
      if (!this.code) {
        uni.showToast({ title: '请输入验证码', icon: 'none' })
        return
      }
      if (this.code.length !== 6) {
        uni.showToast({ title: '验证码格式不正确', icon: 'none' })
        return
      }

      uni.showLoading({ title: '登录中...' })

      try {
        var res = await userAPI.smsLogin({
          phone: this.phone,
          code: this.code
        })

        if (res && res.data) {
          var userData = {
            id: res.userId || res.memberId || 'phone_user_' + Date.now(),
            userId: res.userId || res.memberId || 'phone_user_' + Date.now(),
            nickname: res.nickname || this.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2'),
            avatar: res.avatar || '',
            phone: this.phone,
            isGuest: false,
            loginTime: Date.now()
          }

          uni.setStorageSync('userInfo', userData)
          tokenManager.setToken(res.data)
          uni.setStorageSync('loginTime', Date.now())

          uni.$emit('loginSuccess', { action: 'refresh' })
          this.handleInvite()

          var callbackData = executeLoginCallback()

          uni.hideLoading()
          uni.showToast({ title: '登录成功', icon: 'success' })

          var timerId = setTimeout(function() {
            if (callbackData && callbackData.url) {
              if (callbackData.type === 'switchTab') {
                uni.switchTab({ url: callbackData.url })
              } else {
                uni.navigateTo({ url: callbackData.url })
              }
            } else {
              uni.reLaunch({ url: '/pages/index/index' })
            }
          }, 1500)
          this.timers.push(timerId)
        } else {
          throw new Error('登录失败')
        }
      } catch (error) {
        uni.hideLoading()
        console.error('手机登录失败:', error)
        uni.showToast({ title: error.message || '登录失败，请重试', icon: 'none' })
      }
    },

    // 处理邀请
    handleInvite() {
      var pendingInviteCode = storage.get('pendingInviteCode')
      if (pendingInviteCode) {
        storage.remove('pendingInviteCode')
        var timerId = setTimeout(function() {
          uni.navigateTo({
            url: '/pages/shared/join?inviteCode=' + pendingInviteCode
          })
        }, 2000)
        this.timers.push(timerId)
      }
    },

    closePhoneModal() {
      this.showPhoneModal = false
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/common/style/common.scss';

.login-page {
  min-height: 100vh;
  background: #f5f7fa;
  position: relative;
  overflow: hidden;
}

/* ===== 顶部品牌区域 ===== */
.brand-area {
  position: relative;
  height: 480rpx;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 60rpx;
}

.brand-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 100%;
  background: linear-gradient(145deg, #2563eb 0%, #0d9488 100%);
  border-radius: 0 0 60rpx 60rpx;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background:
      radial-gradient(ellipse at 20% 80%, rgba(255,255,255,0.15), transparent 50%),
      radial-gradient(ellipse at 80% 20%, rgba(255,255,255,0.1), transparent 50%);
    pointer-events: none;
  }
}

.brand-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.brand-logo {
  width: 120rpx;
  height: 120rpx;
  border-radius: 30rpx;
  background: rgba(255,255,255,0.2);
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.15);
}

.brand-name {
  font-size: 56rpx;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 4rpx;
  margin-bottom: 12rpx;
}

.brand-slogan {
  font-size: 26rpx;
  color: rgba(255,255,255,0.8);
  letter-spacing: 2rpx;
}

/* ===== 登录卡片 ===== */
.login-card {
  margin: -40rpx 32rpx 0;
  background: #ffffff;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 40rpx rgba(0,0,0,0.08);
  overflow: hidden;
  position: relative;
  z-index: 2;
}

/* Tab 切换 */
.login-tabs {
  display: flex;
  position: relative;
  border-bottom: 2rpx solid #f0f0f0;
}

.login-tab {
  flex: 1;
  text-align: center;
  padding: 32rpx 0;
  font-size: 30rpx;
  color: #999;
  font-weight: 500;
  transition: color 0.3s;
  position: relative;
  z-index: 1;

  &.active {
    color: #2563eb;
    font-weight: 600;
  }
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  width: 50%;
  height: 4rpx;
  background: linear-gradient(90deg, #2563eb, #0d9488);
  border-radius: 4rpx;
  transition: left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 登录主体 */
.login-body {
  padding: 48rpx 40rpx 40rpx;
}

/* 微信登录 */
.wechat-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 48rpx;
}

.wechat-icon-wrap {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #07c160, #06ad56);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
  box-shadow: 0 12rpx 32rpx rgba(7, 193, 96, 0.25);
}

.wechat-icon {
  font-size: 64rpx;
}

.wechat-desc {
  font-size: 26rpx;
  color: #999;
}

/* 表单 */
.form-item {
  margin-bottom: 32rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 16rpx;
}

.form-input-wrap {
  display: flex;
  align-items: center;
  height: 96rpx;
  background: #f8f9fb;
  border: 2rpx solid #eef0f5;
  border-radius: 16rpx;
  padding: 0 24rpx;
  transition: all 0.25s ease;

  &:focus-within {
    border-color: #2563eb;
    background: #fff;
    box-shadow: 0 0 0 4rpx rgba(37, 99, 235, 0.1);
  }
}

.input-prefix {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-right: 16rpx;
  padding-right: 16rpx;
  border-right: 2rpx solid #e5e7eb;
}

.form-input {
  flex: 1;
  height: 96rpx;
  line-height: 96rpx;
  font-size: 30rpx;
  color: #333;
  background: transparent;
  border: none;
  padding: 0;
  margin: 0;
}

/* 验证码按钮 */
.code-btn {
  flex-shrink: 0;
  padding: 0 24rpx;
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 12rpx;
  background: linear-gradient(135deg, #2563eb, #0d9488);
  margin-left: 16rpx;
  transition: all 0.25s ease;

  &.disabled {
    background: #e5e7eb;
  }

  &:active:not(.disabled) {
    opacity: 0.85;
    transform: scale(0.97);
  }
}

.code-btn-text {
  font-size: 24rpx;
  color: #fff;
  white-space: nowrap;
}

.code-btn.disabled .code-btn-text {
  color: #999;
}

/* 登录按钮 */
.btn-login {
  width: 100%;
  height: 96rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  margin-top: 16rpx;
  transition: all 0.25s ease;

  &::after {
    border: none;
  }

  &:active {
    opacity: 0.9;
    transform: scale(0.985);
  }
}

.btn-wechat {
  background: linear-gradient(135deg, #07c160, #06ad56);
  box-shadow: 0 8rpx 24rpx rgba(7, 193, 96, 0.25);
}

.btn-phone {
  background: linear-gradient(135deg, #2563eb, #0d9488);
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.25);
}

.btn-disabled {
  background: #e5e7eb;
}

.btn-login-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
  letter-spacing: 2rpx;
}

.auto-register-tip {
  display: block;
  text-align: center;
  font-size: 24rpx;
  color: #bbb;
  margin-top: 20rpx;
}

/* ===== 底部协议 ===== */
.agreement-area {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  padding: 40rpx 40rpx 16rpx;
}

.agreement-text {
  font-size: 24rpx;
  color: #bbb;
}

.agreement-link {
  font-size: 24rpx;
  color: #2563eb;
}

/* ===== 信任标识 ===== */
.trust-bar {
  display: flex;
  justify-content: center;
  gap: 48rpx;
  padding: 24rpx 0 60rpx;
}

.trust-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.trust-icon {
  font-size: 28rpx;
}

.trust-text {
  font-size: 22rpx;
  color: #bbb;
}

/* ===== 手机号弹窗 ===== */
.phone-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
}

.modal-content {
  position: relative;
  width: 100%;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  padding: 0 40rpx 60rpx;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 0;
}

.modal-close {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f5f5f5;
}

.modal-close-icon {
  font-size: 40rpx;
  color: #999;
  line-height: 1;
}

.modal-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}

.modal-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 20rpx;
}

.modal-icon-wrap {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #eef2ff, #e0f2fe);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
}

.modal-icon {
  font-size: 60rpx;
}

.modal-desc {
  font-size: 28rpx;
  color: #666;
  text-align: center;
  line-height: 1.6;
  margin-bottom: 48rpx;
  padding: 0 20rpx;
}

.modal-btn {
  margin-top: 0;
  margin-bottom: 24rpx;
}

.modal-skip {
  padding: 20rpx 0;
}

.modal-skip-text {
  font-size: 28rpx;
  color: #999;
}

/* ===== 深色模式适配 ===== */
.dark-mode .login-page,
.login-page:not(.light-mode) {
  // 默认已是浅色，深色模式下覆盖
}

// 如果有深色 class 则覆盖
page[data-theme="dark"] .login-page,
.login-page.dark-mode {
  background: #0a0e1a;

  .brand-bg {
    background: linear-gradient(145deg, #1e40af, #0f766e);
  }

  .login-card {
    background: #1a1f2e;
    box-shadow: 0 8rpx 40rpx rgba(0,0,0,0.3);
  }

  .login-tabs {
    border-bottom-color: rgba(255,255,255,0.08);
  }

  .login-tab {
    color: #94a3b8;
    &.active { color: #60a5fa; }
  }

  .tab-indicator {
    background: linear-gradient(90deg, #3b82f6, #14b8a6);
  }

  .form-label { color: #e2e8f0; }
  .form-input-wrap {
    background: rgba(255,255,255,0.05);
    border-color: rgba(255,255,255,0.1);
    &:focus-within {
      border-color: #3b82f6;
      background: rgba(255,255,255,0.08);
      box-shadow: 0 0 0 4rpx rgba(59,130,246,0.15);
    }
  }
  .input-prefix { color: #e2e8f0; border-right-color: rgba(255,255,255,0.15); }
  .form-input { color: #f8fafc; }
  .wechat-desc { color: #94a3b8; }
  .auto-register-tip { color: #64748b; }
  .agreement-text { color: #64748b; }
  .agreement-link { color: #60a5fa; }
  .trust-text { color: #64748b; }

  .modal-content { background: #1a1f2e; }
  .modal-close { background: rgba(255,255,255,0.1); }
  .modal-close-icon { color: #94a3b8; }
  .modal-title { color: #f8fafc; }
  .modal-icon-wrap { background: rgba(59,130,246,0.15); }
  .modal-desc { color: #94a3b8; }
  .modal-skip-text { color: #64748b; }
}
</style>
