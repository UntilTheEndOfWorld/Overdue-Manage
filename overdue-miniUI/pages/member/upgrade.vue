<template>
  <view class="container" :class="themeClass">
    <view class="header">
      <text class="header-title">选择套餐</text>
    </view>

    <!-- 免费额度说明（仅免费用户显示） -->
    <view class="quota-info" v-if="!memberUtil.isMember()">
      <view class="quota-text">
        您已用完 <text class="highlight">{{ FREE_QUOTA }}</text> 个物品的免费管理额度。请选择以下方式继续添加物品：
      </view>
    </view>

    <!-- 选项一：观看广告 -->
    <view class="option-card ad-option">
      <view class="option-header">
        <view class="option-icon ad-icon">📺</view>
        <view class="option-badge ad-badge">推荐</view>
      </view>
      <view class="option-title">观看广告，免费获得1次额度</view>
      <view class="option-desc">观看一段15-30秒的视频广告，即可立即添加1个新物品。</view>
      <view class="option-time">立即解锁 约30秒</view>
      <button class="option-btn ad-btn" @click="goToAd">观看广告</button>
    </view>

    <!-- 选项二：升级会员 -->
    <view class="option-card member-option">
      <view class="option-header">
        <view class="option-icon member-icon">👑</view>
        <view class="option-badge member-badge">超值</view>
      </view>
      <view class="option-title">升级会员，无限添加</view>
      <view class="option-desc">解锁无限物品管理、多设备同步、共享空间等全部高级功能。</view>
      <view class="option-price">¥6/月起 永久无限制</view>
      <button class="option-btn member-btn" @click="selectPlan">升级会员</button>
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
      FREE_QUOTA: 5,
      plans: []
    }
  },
  onLoad() {
    this.plans = Object.values(memberUtil.PLANS)
  },
  methods: {
    goToAd() {
      uni.navigateTo({
        url: '/pages/member/ad'
      })
    },
    selectPlan() {
      uni.navigateTo({
        url: '/pages/member/member'
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

.header {
  text-align: center;
  margin-bottom: 40rpx;
}

.header-title {
  font-size: 48rpx;
  font-weight: bold;
  color: var(--text-primary);
}

.quota-info {
  background: var(--card-bg-solid);
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  border: 2rpx solid var(--card-border);
}

.quota-text {
  font-size: 28rpx;
  color: var(--text-primary);
  line-height: 1.6;
}

.highlight {
  font-weight: bold;
  color: var(--accent-blue);
}

.option-card {
  background: var(--card-bg-solid);
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  border: 2rpx solid var(--card-border);
  box-shadow: 0 8rpx 24rpx var(--shadow-color);
}

.option-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.option-icon {
  font-size: 48rpx;
}

.option-badge {
  font-size: 22rpx;
  color: white;
  padding: 6rpx 14rpx;
  border-radius: 16rpx;
}

.ad-badge {
  background: var(--accent-green);
}

.member-badge {
  background: #f59e0b;
}

.option-title {
  font-size: 32rpx;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 16rpx;
}

.option-desc {
  font-size: 26rpx;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 16rpx;
}

.option-time,
.option-price {
  font-size: 24rpx;
  color: var(--text-tertiary);
  margin-bottom: 24rpx;
}

.option-btn {
  width: 100%;
  padding: 24rpx;
  border-radius: 20rpx;
  font-size: 28rpx;
  font-weight: 600;
  border: none;
}

.ad-btn {
  background: var(--accent-green);
  color: white;
}

.member-btn {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white;
}

</style>
