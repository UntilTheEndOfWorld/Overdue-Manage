<template>
  <view class="container" :class="themeClass">
    <view class="member-header">
      <text class="header-title">会员中心</text>
    </view>

    <!-- 用户状态卡片 -->
    <view class="status-card" v-if="!memberUtil.isMember()">
      <view class="status-title">免费用户</view>
      <view class="status-desc">
        您有 <text class="highlight">{{ FREE_QUOTA }}</text> 个物品的免费管理额度，已使用 <text class="highlight">{{ usedQuota }}</text> 个，剩余 <text class="highlight">{{ remainingQuota }}</text> 个。
      </view>
    </view>

    <view class="status-card member" v-else>
      <view class="status-title">会员用户</view>
      <view class="status-desc">
        <text v-if="memberInfo.planType === 'lifetime'">您已开通终身会员，享受无限物品管理。</text>
        <text v-else>您的会员将于 {{ formatExpireTime }} 到期</text>
      </view>
    </view>

    <!-- 会员特权 -->
    <view class="privilege-section">
      <view class="section-title">
        <text class="title-text">会员特权</text>
        <text class="title-badge">无限物品</text>
      </view>
    </view>

    <!-- 选择套餐 -->
    <view class="plans-section">
      <view class="section-title">
        <text class="title-text">选择套餐</text>
      </view>
      
      <view class="plans-list">
        <view 
          class="plan-card" 
          v-for="plan in plans" 
          :key="plan.id"
          :class="{ 'plan-recommended': plan.id === 'monthly', 'plan-lifetime': plan.id === 'lifetime' }"
        >
          <view class="plan-header">
            <text class="plan-name">{{ plan.name }}</text>
            <view class="plan-badge" v-if="plan.id === 'monthly'">推荐</view>
            <view class="plan-badge lifetime" v-if="plan.id === 'lifetime'">超值</view>
          </view>
          <view class="plan-price">
            <text class="price-symbol">¥</text>
            <text class="price-value">{{ plan.price }}</text>
            <text class="price-unit">/{{ plan.period }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 立即升级按钮 -->
    <view class="upgrade-section">
      <button class="btn-upgrade" @click="goToUpgrade">立即升级</button>
    </view>
  </view>
</template>

<script>
import memberUtil from '@/common/utils/member.js'
import storage from '@/common/utils/storage.js'
import themeMixin from '@/common/mixins/theme.js'

export default {
  mixins: [themeMixin],
  data() {
    return {
      memberUtil,
      FREE_QUOTA: 5,
      usedQuota: 0,
      remainingQuota: 0,
      memberInfo: {},
      plans: []
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
    loadData() {
      this.memberInfo = memberUtil.getMemberInfo()
      this.usedQuota = memberUtil.getUsedQuota()
      this.remainingQuota = memberUtil.getRemainingQuota()
      
      // 获取套餐列表
      this.plans = Object.values(memberUtil.PLANS)
    },
    goToUpgrade() {
      uni.navigateTo({
        url: '/pages/member/upgrade'
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

.member-header {
  text-align: center;
  margin-bottom: 40rpx;
}

.header-title {
  font-size: 48rpx;
  font-weight: bold;
  color: var(--text-primary);
}

.status-card {
  background: linear-gradient(135deg, #a78bfa, #8b5cf6);
  border-radius: 32rpx;
  padding: 40rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 20rpx 50rpx -10rpx var(--shadow-color);
  border: 2rpx solid rgba(255, 255, 255, 0.2);
}

.status-card.member {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.light-mode .status-card {
  background: var(--card-bg-solid);
  border: 2rpx solid var(--card-border);
}

.status-title {
  font-size: 36rpx;
  font-weight: bold;
  color: white;
  margin-bottom: 20rpx;
}

.light-mode .status-title {
  color: var(--text-primary);
}

.status-desc {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.95);
  line-height: 1.6;
}

.light-mode .status-desc {
  color: var(--text-primary);
}

.highlight {
  font-weight: bold;
  font-size: 32rpx;
  color: white;
}

.light-mode .highlight {
  color: var(--accent-blue);
}

.privilege-section,
.plans-section {
  margin-bottom: 40rpx;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30rpx;
}

.title-text {
  font-size: 36rpx;
  font-weight: bold;
  color: var(--text-primary);
}

.title-badge {
  font-size: 24rpx;
  color: var(--accent-blue);
  background: rgba(59, 130, 246, 0.1);
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
}

.plans-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.plan-card {
  background: var(--card-bg-solid);
  border-radius: 24rpx;
  padding: 30rpx;
  border: 2rpx solid var(--card-border);
  box-shadow: 0 8rpx 24rpx var(--shadow-color);
  transition: all 0.3s ease;
}

.plan-card.plan-recommended {
  border: 2rpx solid var(--accent-blue);
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(59, 130, 246, 0.05));
}

.plan-card.plan-lifetime {
  border: 2rpx solid #f59e0b;
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.1), rgba(245, 158, 11, 0.05));
}

.plan-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.plan-name {
  font-size: 32rpx;
  font-weight: bold;
  color: var(--text-primary);
}

.plan-badge {
  font-size: 22rpx;
  color: white;
  background: var(--accent-blue);
  padding: 6rpx 14rpx;
  border-radius: 16rpx;
}

.plan-badge.lifetime {
  background: #f59e0b;
}

.plan-price {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 28rpx;
  color: var(--text-primary);
  font-weight: 600;
}

.price-value {
  font-size: 48rpx;
  color: var(--text-primary);
  font-weight: bold;
  margin: 0 8rpx;
}

.price-unit {
  font-size: 28rpx;
  color: var(--text-secondary);
}

.upgrade-section {
  margin-top: 40rpx;
}

.btn-upgrade {
  width: 100%;
  padding: 28rpx;
  background: linear-gradient(135deg, var(--accent-blue), var(--accent-teal));
  color: white;
  border-radius: 24rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(59, 130, 246, 0.3);
  transition: all 0.3s ease;
}

.btn-upgrade:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3);
}

.light-mode .btn-upgrade {
  background: linear-gradient(135deg, #3b82f6, #14b8a6);
  box-shadow: 0 8rpx 24rpx rgba(59, 130, 246, 0.2);
}
</style>
