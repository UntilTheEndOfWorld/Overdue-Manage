<template>
  <view class="container" :class="themeClass">
    <view class="member-header">
      <text class="header-title">会员中心</text>
    </view>

    <!-- 用户状态卡片 -->
    <view class="status-card" v-if="!memberUtil.isMember()">
      <view class="status-title">{{ chargeEnabled ? '免费用户' : '普通用户' }}</view>
      <view class="status-desc">
        您有 <text class="highlight">{{ freePersonalLimit }}</text> 个物品的{{ chargeEnabled ? '免费' : '' }}管理额度，已使用 <text class="highlight">{{ usedQuota }}</text> 个，剩余 <text class="highlight">{{ remainingQuota }}</text> 个。
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
    <view class="privilege-section" v-if="chargeEnabled">
      <view class="section-title">
        <view class="title-text with-crown">
          <text>会员特权</text>
          <text class="title-crown">👑</text>
        </view>
        <view class="title-badges">
          <text class="title-badge crown">尊享权益</text>
          <text class="title-badge">无限物品</text>
          <text class="title-badge">多端同步</text>
        </view>
      </view>
    </view>

    <!-- 选择套餐 -->
    <view class="plans-section-card" v-if="chargeEnabled">
      <view class="section-title plans-title">
        <text class="title-text">选择套餐</text>
      </view>
      <view class="plans-list">
        <view 
          class="plan-card" 
          v-for="plan in plans" 
          :key="plan.id"
          :class="{
            'plan-selected': selectedPlan === plan.id,
            'plan-selected-recommended': selectedPlan === plan.id && plan.id === 'monthly',
            'plan-selected-lifetime': selectedPlan === plan.id && plan.id === 'lifetime'
          }"
          @click="selectPlan(plan.id)"
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

    <!-- 未开启收费时的提示 -->
    <view class="upgrade-section" v-if="!chargeEnabled">
      <view class="status-desc" style="text-align:center;color:var(--text-secondary);">
        当前未开启会员收费，默认额度：个人 {{ freePersonalLimit }} 个物品，{{ freeSharedSpaceLimit }} 个共享空间，每空间 {{ freeSharedItemLimit }} 个物品。
      </view>
    </view>

    <!-- 立即升级按钮 -->
    <view class="upgrade-section" v-if="chargeEnabled">
      <button class="btn-upgrade" @click="goToUpgrade">立即升级</button>
    </view>
  </view>
</template>

<script>
import memberUtil from '@/common/utils/member.js'
import appConfig from '@/common/utils/appConfig.js'
import storage from '@/common/utils/storage.js'
import themeMixin from '@/common/mixins/theme.js'

export default {
  mixins: [themeMixin],
  data() {
    return {
      memberUtil,
      usedQuota: 0,
      remainingQuota: 0,
      memberInfo: {},
      plans: [],
      selectedPlan: ''
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
    loadData() {
      this.memberInfo = memberUtil.getMemberInfo()
      this.usedQuota = memberUtil.getUsedQuota()
      this.remainingQuota = memberUtil.getRemainingQuota()
      
      // 获取套餐列表
      this.plans = Object.values(memberUtil.PLANS)
      if (!this.selectedPlan && this.plans && this.plans.length > 0) {
        const defaultPlan = this.plans.find((plan) => plan && plan.id === 'monthly')
        this.selectedPlan = (defaultPlan && defaultPlan.id) || (this.plans[0] && this.plans[0].id) || ''
      }
    },
    selectPlan(planId) {
      if (!planId) return
      this.selectedPlan = planId
    },
    goToUpgrade() {
      if (!this.selectedPlan) {
        uni.showToast({
          title: '请选择套餐',
          icon: 'none'
        })
        return
      }
      uni.navigateTo({
        url: `/pages/member/purchase?planId=${this.selectedPlan}`
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
  background: linear-gradient(135deg, #2b2f36, #0f1115);
  border-radius: 32rpx;
  padding: 40rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 20rpx 50rpx -10rpx rgba(0, 0, 0, 0.35);
  border: 2rpx solid rgba(255, 255, 255, 0.12);
}

.status-card.member {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  border: 2rpx solid rgba(255, 255, 255, 0.22);
  box-shadow: 0 20rpx 50rpx -10rpx rgba(217, 119, 6, 0.35);
}

.status-title {
  font-size: 36rpx;
  font-weight: bold;
  color: white;
  margin-bottom: 20rpx;
}

.status-desc {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.95);
  line-height: 1.6;
}

.highlight {
  font-weight: bold;
  font-size: 32rpx;
  color: #ffffff;
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

.title-text.with-crown {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.title-crown {
  position: absolute;
  top: -16rpx;
  right: -24rpx;
  font-size: 24rpx;
  transform: rotate(25deg);
  transform-origin: center;
}

.title-badge {
  font-size: 24rpx;
  color: var(--accent-blue);
  background: rgba(59, 130, 246, 0.1);
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
}

.title-badges {
  display: flex;
  align-items: center;
  gap: 10rpx;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.title-badge.crown {
  color: #7a4a00;
  background: linear-gradient(135deg, #ffe8ad, #ffd164);
}

.plans-section-card {
  margin-bottom: 40rpx;
  padding: 24rpx;
  border-radius: 24rpx;
  border: 2rpx solid rgba(245, 179, 1, 0.65);
  background: rgba(255, 248, 220, 0.35);
  box-shadow: 0 10rpx 28rpx rgba(245, 179, 1, 0.18);
}

.plans-title {
  margin-bottom: 24rpx;
}

.plans-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.plan-card {
  background: linear-gradient(135deg, #ffd86b, #f5b301);
  border-radius: 24rpx;
  padding: 30rpx;
  border: 2rpx solid #f0b429;
  box-shadow: 0 8rpx 24rpx rgba(245, 179, 1, 0.22);
  transition: all 0.3s ease;
}

.plan-card:active {
  transform: scale(0.98);
}

.plan-card .plan-name,
.plan-card .price-symbol,
.plan-card .price-value,
.plan-card .price-unit {
  color: #4a3412;
}

.plan-card:not(.plan-selected) {
  filter: grayscale(100%);
  opacity: 0.58;
}

.plan-card.plan-selected {
  border: 2rpx solid #f59e0b;
  background: linear-gradient(135deg, #ffe48d, #f5b301);
  box-shadow: 0 14rpx 36rpx rgba(245, 179, 1, 0.35);
  transform: scale(1.02);
}

.plan-card.plan-selected .plan-name,
.plan-card.plan-selected .price-symbol,
.plan-card.plan-selected .price-value,
.plan-card.plan-selected .price-unit {
  color: #2b1f05;
}

.plan-card.plan-selected-recommended {
  border-color: #f59e0b;
}

.plan-card.plan-selected-lifetime {
  border: 2rpx solid #f59e0b;
  background: linear-gradient(135deg, #ffe9a6, #f5b301);
  box-shadow: 0 12rpx 32rpx rgba(245, 158, 11, 0.32);
}

.plan-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.plan-name {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.plan-badge {
  font-size: 22rpx;
  color: #2b1f05;
  background: #ffd24d;
  padding: 6rpx 14rpx;
  border-radius: 16rpx;
}

.plan-badge.lifetime {
  background: #ffc329;
}

.plan-price {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 28rpx;
  color: var(--text-primary);
  font-weight: 600;
  transition: all 0.3s ease;
}

.price-value {
  font-size: 48rpx;
  color: var(--text-primary);
  font-weight: bold;
  margin: 0 8rpx;
  transition: all 0.3s ease;
}

.price-unit {
  font-size: 28rpx;
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

.plan-card.plan-selected .plan-name {
  font-size: 36rpx;
  font-weight: 800;
}

.plan-card.plan-selected .price-symbol {
  font-size: 30rpx;
  font-weight: 700;
}

.plan-card.plan-selected .price-value {
  font-size: 56rpx;
  font-weight: 800;
}

.plan-card.plan-selected .price-unit {
  font-size: 30rpx;
  font-weight: 600;
}

.upgrade-section {
  margin-top: 40rpx;
}

.btn-upgrade {
  width: 100%;
  padding: 28rpx;
  background: linear-gradient(135deg, #ffd86b, #f5b301);
  color: #1f1a0d;
  border-radius: 24rpx;
  font-size: 32rpx;
  font-weight: 700;
  border: none;
  box-shadow: 0 10rpx 28rpx rgba(245, 179, 1, 0.35);
  transition: all 0.3s ease;
}

.btn-upgrade:active {
  transform: scale(0.98);
  box-shadow: 0 6rpx 16rpx rgba(245, 179, 1, 0.35);
}

.light-mode .btn-upgrade {
  background: linear-gradient(135deg, #ffe08a, #f5b301);
  color: #1f1a0d;
  box-shadow: 0 8rpx 24rpx rgba(245, 179, 1, 0.3);
}
</style>
