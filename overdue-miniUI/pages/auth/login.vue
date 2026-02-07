<template>
  <view class="container" :class="themeClass">
    <view class="login-content">
      <view class="logo-section">
        <view class="logo-icon">⏰</view>
        <text class="app-name">时光守护</text>
        <text class="app-desc">智能管理您的物品有效期</text>
      </view>

      <view class="login-form">
        <view class="login-tabs">
          <view 
            class="tab-item" 
            :class="{ active: loginType === 'wechat' }"
            @click="loginType = 'wechat'"
          >
            <text class="tab-icon">💬</text>
            <text>微信登录</text>
          </view>
          <view 
            class="tab-item" 
            :class="{ active: loginType === 'phone' }"
            @click="loginType = 'phone'"
          >
            <text class="tab-icon">📱</text>
            <text>手机登录</text>
          </view>
        </view>

        <!-- 微信登录 -->
        <view v-if="loginType === 'wechat'" class="login-section">
          <button class="login-btn wechat-btn" @click="wechatLogin">
            <text class="btn-icon">💬</text>
            <text>微信快速登录</text>
          </button>
          <text class="login-tip">使用微信账号快速登录</text>
        </view>

        <!-- 手机号登录 -->
        <view v-if="loginType === 'phone'" class="login-section">
          <view class="input-group">
            <input 
              class="input" 
              type="number" 
              v-model="phone" 
              placeholder="请输入手机号"
              maxlength="11"
            />
          </view>
          <view class="input-group">
            <input 
              class="input" 
              type="number" 
              v-model="code" 
              placeholder="请输入验证码"
              maxlength="6"
            />
            <button 
              class="code-btn" 
              :disabled="codeCountdown > 0"
              @click="sendCode"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}秒` : '获取验证码' }}
            </button>
          </view>
          <button class="login-btn phone-btn" @click="phoneLogin">
            <text>登录 / 注册</text>
          </button>
          <text class="login-tip">未注册的手机号将自动创建账号</text>
        </view>
      </view>

      <view class="agreement">
        <text>登录即表示同意</text>
        <text class="link">《用户协议》</text>
        <text>和</text>
        <text class="link">《隐私政策》</text>
      </view>
    </view>
    
    <!-- 微信手机号获取弹窗 -->
    <view class="phone-modal" v-if="showPhoneModal">
      <view class="modal-overlay" @click="closePhoneModal"></view>
      <view class="modal-content">
        <view class="modal-header">
          <view class="back-btn" @click="closePhoneModal">
            <text class="back-icon">←</text>
          </view>
          <text class="modal-title">申请获取并验证你的手机号</text>
          <view class="header-right"></view>
        </view>
        <view class="modal-body">
          <text class="modal-desc">通过关联手机号成为时光守护会员，可及时接收提醒信息等</text>
          
          <!-- 微信手机号获取组件 -->
          <view class="wechat-phone-section">
            <!-- #ifdef MP-WEIXIN -->
            <button 
              class="wechat-phone-btn"
              open-type="getPhoneNumber"
              @getphonenumber="onGetPhoneNumber"
            >
              <text class="btn-text">获取手机号</text>
            </button>
            <!-- #endif -->
            
            <!-- #ifndef MP-WEIXIN -->
            <view class="wechat-phone-btn disabled">
              <text class="btn-text">请在微信小程序中使用</text>
            </view>
            <!-- #endif -->
          </view>
          
          <view class="skip-btn" @click="closePhoneModal">
            <text class="btn-text">暂不登录</text>
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
      loginType: 'wechat', // 'wechat' 或 'phone'
      phone: '',
      code: '',
      codeCountdown: 0,
      inviteCode: '', // 邀请码，如果有则登录后自动加入空间
      countdownTimer: null, // 倒计时定时器
      timers: [], // 存储所有定时器ID，用于统一清理
      showPhoneModal: false, // 显示手机号获取弹窗
      phoneData: null, // 微信手机号数据
      userInfo: null, // 微信用户信息
      loginCode: null // 微信登录code
    }
  },
  onLoad(options) {
    // 如果有邀请码，保存起来，登录后使用
    if (options.inviteCode) {
      this.inviteCode = options.inviteCode
      storage.set('pendingInviteCode', this.inviteCode)
    }
  },
  onUnload() {
    // 页面卸载时清理所有定时器
    this.clearAllTimers()
  },
  onHide() {
    // 页面隐藏时清理所有定时器
    this.clearAllTimers()
  },
  methods: {
    // 清理所有定时器
    clearAllTimers() {
      if (this.countdownTimer) {
        clearInterval(this.countdownTimer)
        this.countdownTimer = null
      }
      // 清理所有存储的定时器
      this.timers.forEach(timerId => {
        clearTimeout(timerId)
        clearInterval(timerId)
      })
      this.timers = []
    },
    
    // 微信登录
    async wechatLogin() {
      // 显示手机号获取弹窗
      this.showPhoneModal = true
      
      // 先获取用户信息和登录code
      try {
        const userInfo = await uni.getUserProfile({
          desc: '用于完善用户资料'
        })
        this.userInfo = userInfo.userInfo
        
        // 获取登录code
        const loginResult = await new Promise((resolve, reject) => {
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
        uni.showToast({
          title: '获取用户信息失败，请重试',
          icon: 'none'
        })
        this.showPhoneModal = false
      }
    },
    
    // 处理微信手机号获取
    async onGetPhoneNumber(e) {
      console.log('微信手机号获取结果:', e)
      
      if (e.detail.errMsg === 'getPhoneNumber:ok') {
        // 保存手机号数据
        this.phoneData = {
          encryptedData: e.detail.encryptedData,
          iv: e.detail.iv
        }
        console.log('保存手机号数据:', this.phoneData)
        
        // 关闭弹窗
        this.showPhoneModal = false
        
        // 调用注册接口
        await this.getPhoneNumberFromServer()
      } else {
        // 用户拒绝授权
        uni.showToast({
          title: '需要手机号才能完成登录',
          icon: 'none'
        })
      }
    },
    
    // 从服务器获取手机号并完成注册
    async getPhoneNumberFromServer() {
      // 检查手机号数据是否有效
      if (!this.phoneData || !this.phoneData.encryptedData || !this.phoneData.iv) {
        uni.showToast({
          title: '手机号数据无效，请重新获取',
          icon: 'none'
        })
        return
      }

      uni.showLoading({
        title: '注册中...'
      })

      try {
        console.log('开始调用微信注册API')
        
        // 检查是否有用户信息和登录code
        if (!this.userInfo || !this.loginCode) {
          throw new Error('用户信息或登录凭证缺失，请重新获取')
        }
        
        // 调用注册接口
        const res = await userAPI.wechatRegisterWithPhone({
          code: this.loginCode,
          encryptedData: this.phoneData.encryptedData,
          iv: this.phoneData.iv,
          nickname: this.userInfo.nickName,
          avatarUrl: this.userInfo.avatarUrl
        })
        
        uni.hideLoading()
        console.log('注册API响应:', res)
        
        // 处理注册结果
        if (res && res.data) {
          // 保存用户信息到本地存储
          const userData = {
            id: res.userId || res.memberId || 'wechat_user_' + Date.now(),
            userId: res.userId || res.memberId || 'wechat_user_' + Date.now(),
            nickname: res.nickname || this.userInfo.nickName,
            avatar: res.avatar || this.userInfo.avatarUrl,
            phone: res.phone || '',
            isGuest: false,
            loginTime: Date.now()
          }
          
          // 保存到本地存储
          uni.setStorageSync('userInfo', userData)
          tokenManager.setToken(res.data) // token在data字段中
          uni.setStorageSync('loginTime', Date.now())
          
          // 发送登录成功事件
          uni.$emit('loginSuccess', {
            action: 'refresh'
          })
          
          // 处理邀请
          this.handleInvite()
          
          // 执行登录回调
          const callbackData = executeLoginCallback()
          
          uni.showToast({
            title: '登录成功',
            icon: 'success',
            duration: 2000
          })
          
          // 跳转到首页
          const timerId = setTimeout(() => {
            if (callbackData && callbackData.url) {
              // 如果有回调URL，跳转到回调URL
              if (callbackData.type === 'switchTab') {
                uni.switchTab({ url: callbackData.url })
              } else {
                uni.navigateTo({ url: callbackData.url })
              }
            } else {
              // 否则跳转到首页
              uni.reLaunch({
                url: '/pages/index/index'
              })
            }
          }, 1500)
          this.timers.push(timerId)
        } else {
          throw new Error('注册失败')
        }
      } catch (error) {
        uni.hideLoading()
        console.error('注册失败:', error)
        
        // 根据错误类型显示不同的提示
        let errorMessage = '注册失败，请重试'
        if (error.message && error.message.includes('微信登录失败')) {
          errorMessage = '微信授权已过期，请重新获取'
        } else if (error.message && error.message.includes('invalid code')) {
          errorMessage = '授权码已过期，请重新获取'
        } else if (error.message && error.message.includes('网络')) {
          errorMessage = '网络连接异常，请检查网络'
        }
        
        uni.showToast({
          title: errorMessage,
          icon: 'none',
          duration: 3000
        })
      }
    },

    // 发送验证码
    async sendCode() {
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
        
        // 开始倒计时
        if (this.countdownTimer) {
          clearInterval(this.countdownTimer)
        }
        this.codeCountdown = 60
        this.countdownTimer = setInterval(() => {
          if (this.codeCountdown <= 0) {
            clearInterval(this.countdownTimer)
            this.countdownTimer = null
          } else {
            this.codeCountdown--
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
        // 调用短信登录接口
        const res = await userAPI.smsLogin({
          phone: this.phone,
          code: this.code
        })
        
        console.log('登录API响应:', res)
        
        // 处理登录结果
        if (res && res.data) {
          // 保存用户信息到本地存储
          const userData = {
            id: res.userId || res.memberId || 'phone_user_' + Date.now(),
            userId: res.userId || res.memberId || 'phone_user_' + Date.now(),
            nickname: res.nickname || this.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2'),
            avatar: res.avatar || '',
            phone: this.phone,
            isGuest: false,
            loginTime: Date.now()
          }
          
          // 保存到本地存储
          uni.setStorageSync('userInfo', userData)
          tokenManager.setToken(res.data) // token在data字段中
          uni.setStorageSync('loginTime', Date.now())
          
          // 发送登录成功事件
          uni.$emit('loginSuccess', {
            action: 'refresh'
          })
          
          // 处理邀请
          this.handleInvite()
          
          // 执行登录回调
          const callbackData = executeLoginCallback()
          
          uni.hideLoading()
          uni.showToast({ title: '登录成功', icon: 'success' })
          
          // 跳转到首页
          const timerId = setTimeout(() => {
            if (callbackData && callbackData.url) {
              // 如果有回调URL，跳转到回调URL
              if (callbackData.type === 'switchTab') {
                uni.switchTab({ url: callbackData.url })
              } else {
                uni.navigateTo({ url: callbackData.url })
              }
            } else {
              // 否则跳转到首页
              uni.reLaunch({
                url: '/pages/index/index'
              })
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
      const pendingInviteCode = storage.get('pendingInviteCode')
      if (pendingInviteCode) {
        // 清除待处理的邀请码
        storage.remove('pendingInviteCode')
        
        // 跳转到邀请处理页面
        const timerId = setTimeout(() => {
          uni.navigateTo({
            url: `/pages/shared/join?inviteCode=${pendingInviteCode}`
          })
        }, 2000)
        this.timers.push(timerId)
      }
    },
    
    // 关闭手机号弹窗
    closePhoneModal() {
      this.showPhoneModal = false
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

.login-content {
  width: 100%;
  max-width: 600rpx;
}

.logo-section {
  text-align: center;
  margin-bottom: 80rpx;
}

.logo-icon {
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, $accent-blue, $accent-teal);
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 80rpx;
  margin: 0 auto 30rpx;
  box-shadow: 0 20rpx 40rpx rgba(59, 130, 246, 0.3);
}

.app-name {
  display: block;
  font-size: 64rpx;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 20rpx;
  background: linear-gradient(135deg, $text-primary 0%, $text-secondary 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.app-desc {
  display: block;
  font-size: 28rpx;
  color: $text-secondary;
}

.login-form {
  @extend .glass-card;
  padding: 60rpx 40rpx;
  margin-bottom: 40rpx;
}

.login-tabs {
  display: flex;
  gap: 20rpx;
  margin-bottom: 50rpx;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 30rpx;
  padding: 8rpx;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  padding: 24rpx;
  border-radius: 24rpx;
  color: $text-secondary;
  font-size: 28rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active {
    transform: scale(0.98);
  }
  
  &.active {
    background: linear-gradient(135deg, $accent-blue, $accent-teal);
    color: white;
    box-shadow: 0 8rpx 20rpx rgba(59, 130, 246, 0.3);
  }
}

.tab-icon {
  font-size: 48rpx;
}

.login-section {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.input-group {
  display: flex;
  gap: 20rpx;
  align-items: center;
}

.input {
  flex: 1;
  padding: 28rpx 36rpx;
  background: rgba(255, 255, 255, 0.05);
  border: 2rpx solid $glass-border;
  border-radius: 24rpx;
  color: $text-primary;
  font-size: 32rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-sizing: border-box;
  margin: 0;
  
  &:focus {
    outline: none;
    border-color: $accent-blue;
    background: rgba(255, 255, 255, 0.08);
  }
}

.code-btn {
  padding: 28rpx 36rpx;
  background: rgba(59, 130, 246, 0.15);
  border: 2rpx solid rgba(59, 130, 246, 0.3);
  border-radius: 24rpx;
  color: $accent-blue;
  font-size: 28rpx;
  white-space: nowrap;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active:not(:disabled) {
    background: rgba(59, 130, 246, 0.25);
  }
  
  &:disabled {
    opacity: 0.5;
  }
}

.login-btn {
  width: 100%;
  padding: 32rpx;
  border-radius: 24rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active {
    transform: translateY(2rpx);
  }
}

.wechat-btn {
  background: linear-gradient(135deg, #07c160, #06ad56);
  color: white;
  box-shadow: 0 10rpx 20rpx rgba(7, 193, 96, 0.3);
}

.phone-btn {
  background: linear-gradient(135deg, $accent-blue, $accent-teal);
  color: white;
  box-shadow: 0 10rpx 20rpx rgba(59, 130, 246, 0.3);
}

.btn-icon {
  font-size: 40rpx;
}

.login-tip {
  text-align: center;
  font-size: 24rpx;
  color: $text-secondary;
}

.agreement {
  text-align: center;
  font-size: 24rpx;
  color: $text-secondary;
  line-height: 1.6;
}

.link {
  color: $accent-blue;
  text-decoration: underline;
}

/* 浅色模式下的登录卡片 */
.light-mode .login-form {
  background: var(--card-bg-solid) !important;
  border: 2rpx solid var(--card-border) !important;
}

/* 浅色模式下的输入框 */
.light-mode .input {
  background: #ffffff !important;
  border: 2rpx solid rgba(59, 130, 246, 0.3) !important;
  color: #1e293b !important;
}

.light-mode .input:focus {
  border-color: #3b82f6 !important;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.2) !important;
}

/* 浅色模式下的标签页 */
.light-mode .login-tabs {
  background: #f1f5f9 !important;
}

.light-mode .tab-item {
  color: #64748b !important;
}

.light-mode .tab-item.active {
  background: linear-gradient(135deg, #3b82f6, #14b8a6) !important;
  color: white !important;
}

/* 浅色模式下的按钮 */
.light-mode .code-btn {
  background: rgba(59, 130, 246, 0.1) !important;
  border: 2rpx solid rgba(59, 130, 246, 0.3) !important;
  color: #3b82f6 !important;
}

.light-mode .wechat-btn {
  background: linear-gradient(135deg, #07c160, #06ad56) !important;
}

.light-mode .phone-btn {
  background: linear-gradient(135deg, #3b82f6, #14b8a6) !important;
}

/* 手机号弹窗样式 */
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
  background: rgba(0, 0, 0, 0.5);
}

.modal-content {
  position: relative;
  width: 100%;
  background: var(--card-bg-solid);
  border-radius: 30rpx 30rpx 0 0;
  padding: 40rpx;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
}

.back-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 36rpx;
  color: $text-primary;
}

.header-right {
  width: 60rpx;
}

.modal-title {
  font-size: 36rpx;
  font-weight: bold;
  color: $text-primary;
  margin-bottom: 20rpx;
  display: block;
}

.modal-desc {
  font-size: 28rpx;
  color: $text-secondary;
  line-height: 1.5;
  margin-bottom: 40rpx;
  display: block;
}

.wechat-phone-section {
  margin-bottom: 40rpx;
}

.wechat-phone-btn {
  width: 100%;
  height: 100rpx;
  background: linear-gradient(135deg, #07c160, #06ad56);
  color: white;
  border: none;
  border-radius: 50rpx;
  font-size: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20rpx;
}

.wechat-phone-btn.disabled {
  background: #666;
  color: #ccc;
}

.wechat-phone-btn::after {
  border: none;
}

.skip-btn {
  width: 100%;
  height: 80rpx;
  background: transparent;
  border: 2rpx solid $glass-border;
  border-radius: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 20rpx;
}

.skip-btn .btn-text {
  color: $text-secondary;
}
</style>
