<template>
  <view class="container" :class="themeClass">
    <view class="header">
      <text class="header-title">确认购买</text>
    </view>

    <!-- 套餐信息卡片 -->
    <view class="plan-info-card" v-if="selectedPlanData">
      <view class="plan-header">
        <text class="plan-icon">👑</text>
        <view class="plan-details">
          <text class="plan-name">{{ selectedPlanData.name }}</text>
          <text class="plan-period">{{ selectedPlanData.period }}会员</text>
        </view>
      </view>
      <view class="plan-price-section">
        <text class="price-symbol">¥</text>
        <text class="price-value">{{ selectedPlanData.price }}</text>
        <text class="price-unit">/{{ selectedPlanData.period }}</text>
      </view>
    </view>

    <!-- 会员特权 -->
    <view class="privileges-section">
      <view class="section-title">会员特权</view>
      <view class="privilege-list">
        <view class="privilege-item">
          <text class="privilege-icon">✓</text>
          <text class="privilege-text">无限物品管理</text>
        </view>
        <view class="privilege-item">
          <text class="privilege-icon">✓</text>
          <text class="privilege-text">无限共享空间</text>
        </view>
        <view class="privilege-item">
          <text class="privilege-icon">✓</text>
          <text class="privilege-text">多设备同步</text>
        </view>
        <view class="privilege-item">
          <text class="privilege-icon">✓</text>
          <text class="privilege-text">优先客服支持</text>
        </view>
      </view>
    </view>

    <!-- 支付摘要 -->
    <view class="payment-summary">
      <view class="summary-row">
        <text class="summary-label">选择的套餐</text>
        <text class="summary-value">{{ selectedPlanData?.name || '' }}</text>
      </view>
      <view class="summary-row">
        <text class="summary-label">价格</text>
        <text class="summary-value">¥{{ selectedPlanData?.price || 0 }}</text>
      </view>
      <view class="summary-divider"></view>
      <view class="summary-row total">
        <text class="summary-label">总计</text>
        <text class="summary-value total-price">¥{{ selectedPlanData?.price || 0 }}</text>
      </view>
    </view>

    <!-- 支付按钮 -->
    <view class="payment-actions">
      <button class="btn-cancel" @click="goBack">取消</button>
      <button class="btn-pay" @click="processPayment">安全支付</button>
    </view>

    <!-- 支付安全提示 -->
    <view class="security-tips">
      <text class="security-icon">🛡️</text>
      <text class="security-text">支付安全由微信支付保障 · 可随时取消续订 · 7天无忧退款</text>
    </view>
  </view>
</template>

<script>
import memberUtil from '@/common/utils/member.js'
import themeMixin from '@/common/mixins/theme.js'

export default {
  mixins: [themeMixin],
  data() {
    return {
      memberUtil,
      planId: '',
      selectedPlanData: null
    }
  },
  onLoad(options) {
    // 从页面参数获取选择的套餐ID
    if (options.planId) {
      this.planId = options.planId
      this.selectedPlanData = memberUtil.PLANS[this.planId]
    } else {
      // 如果没有传递参数，返回上一页
      uni.showToast({
        title: '请选择套餐',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    processPayment() {
      if (!this.planId) {
        uni.showToast({
          title: '套餐信息错误',
          icon: 'none'
        })
        return
      }

      // 实际项目中需要对接微信支付API
      // #ifdef MP-WEIXIN
      // 微信小程序支付示例（需要后端接口支持）
      // uni.request({
      //   url: 'https://your-api.com/payment/create',
      //   method: 'POST',
      //   data: {
      //     planId: this.planId,
      //     userId: uni.getStorageSync('userInfo').openid
      //   },
      //   success: (res) => {
      //     if (res.data.success) {
      //       // 调用微信支付
      //       uni.requestPayment({
      //         timeStamp: res.data.payment.timeStamp,
      //         nonceStr: res.data.payment.nonceStr,
      //         package: res.data.payment.package,
      //         signType: res.data.payment.signType,
      //         paySign: res.data.payment.paySign,
      //         success: () => {
      //           // 支付成功，更新会员状态
      //           this.handlePaymentSuccess()
      //         },
      //         fail: (err) => {
      //           console.error('支付失败:', err)
      //           uni.showToast({
      //             title: '支付取消或失败',
      //             icon: 'none'
      //           })
      //         }
      //       })
      //     }
      //   }
      // })
      // #endif

      // 模拟支付流程（开发测试用）
      uni.showLoading({
        title: '处理中...'
      })
      
      setTimeout(() => {
        uni.hideLoading()
        // 模拟支付成功
        this.handlePaymentSuccess()
      }, 2000)
    },
    handlePaymentSuccess() {
      // 更新会员状态
      const success = memberUtil.purchaseMember(this.planId)
      
      if (success) {
        uni.showToast({
          title: '购买成功！',
          icon: 'success',
          duration: 2000
        })
        
        setTimeout(() => {
          // 返回到会员中心或首页
          uni.redirectTo({
            url: '/pages/member/member'
          })
        }, 2000)
      } else {
        uni.showToast({
          title: '购买失败，请重试',
          icon: 'none'
        })
      }
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

.header {
  text-align: center;
  margin-bottom: 40rpx;
}

.header-title {
  font-size: 48rpx;
  font-weight: bold;
  color: var(--text-primary);
}

/* 套餐信息卡片 */
.plan-info-card {
  background: linear-gradient(135deg, #8b5cf6, #3b82f6);
  border-radius: 32rpx;
  padding: 40rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 20rpx 50rpx -10rpx var(--shadow-color);
  border: 2rpx solid rgba(255, 255, 255, 0.2);
}

.light-mode .plan-info-card {
  background: linear-gradient(135deg, #8b5cf6, #3b82f6);
  border: 2rpx solid var(--card-border);
}

.plan-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.plan-icon {
  font-size: 64rpx;
}

.plan-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.plan-name {
  font-size: 36rpx;
  font-weight: bold;
  color: white;
}

.plan-period {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.plan-price-section {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 32rpx;
  color: white;
  font-weight: 600;
}

.price-value {
  font-size: 64rpx;
  color: white;
  font-weight: bold;
  margin: 0 8rpx;
}

.price-unit {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

/* 会员特权 */
.privileges-section {
  background: var(--card-bg-solid);
  border-radius: 32rpx;
  padding: 40rpx;
  margin-bottom: 40rpx;
  border: 2rpx solid var(--card-border);
  box-shadow: 0 8rpx 24rpx var(--shadow-color);
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 30rpx;
}

.privilege-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.privilege-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.privilege-icon {
  width: 40rpx;
  height: 40rpx;
  background: linear-gradient(135deg, var(--accent-green), #22c55e);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: bold;
  flex-shrink: 0;
}

.privilege-text {
  font-size: 28rpx;
  color: var(--text-primary);
}

/* 支付摘要 */
.payment-summary {
  background: var(--card-bg-solid);
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 40rpx;
  border: 2rpx solid var(--card-border);
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.summary-row.total {
  margin-bottom: 0;
  margin-top: 10rpx;
  padding-top: 20rpx;
  border-top: 2rpx solid var(--card-border);
}

.summary-label {
  font-size: 28rpx;
  color: var(--text-secondary);
}

.summary-value {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.summary-value.total-price {
  font-size: 36rpx;
  font-weight: bold;
  color: var(--accent-blue);
}

.summary-divider {
  height: 2rpx;
  background: var(--card-border);
  margin: 20rpx 0;
}

/* 支付按钮 */
.payment-actions {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.btn-cancel {
  flex: 1;
  padding: 28rpx;
  background: var(--card-bg-solid);
  color: var(--text-primary);
  border-radius: 24rpx;
  font-size: 28rpx;
  font-weight: 600;
  border: 2rpx solid var(--card-border);
}

.btn-pay {
  flex: 2;
  padding: 28rpx;
  background: linear-gradient(135deg, var(--accent-blue), var(--accent-teal));
  color: white;
  border-radius: 24rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(59, 130, 246, 0.3);
}

.btn-pay:active {
  transform: scale(0.98);
}

.light-mode .btn-pay {
  background: linear-gradient(135deg, #3b82f6, #14b8a6);
  box-shadow: 0 8rpx 24rpx rgba(59, 130, 246, 0.2);
}

/* 安全提示 */
.security-tips {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx;
  background: rgba(59, 130, 246, 0.1);
  border-radius: 16rpx;
}

.security-icon {
  font-size: 32rpx;
  flex-shrink: 0;
}

.security-text {
  font-size: 22rpx;
  color: var(--text-secondary);
  line-height: 1.5;
}
</style>
