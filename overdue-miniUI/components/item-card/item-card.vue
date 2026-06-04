<template>
  <view 
    class="item-card" 
    :class="[cardClass, themeClass]"
    @click="handleClick"
  >
    <view class="item-header">
      <view class="item-title-group">
        <text class="item-name">{{ item.name }}</text>
        <view class="expiry-status" :class="statusClass">
          {{ statusText }}
        </view>
      </view>
      <view class="edit-btn" v-if="showEdit" @click.stop="handleEdit">
        <image class="edit-icon" src="/static/tabbar/edit.png" mode="aspectFit"></image>
      </view>
    </view>
    <view class="date-grid" v-if="showDates">
      <view class="date-item">
        <view class="date-icon">产</view>
        <view class="date-copy">
          <text class="date-label">生产日期</text>
          <text class="date-value">{{ formatDate(item.productionDate) }}</text>
        </view>
      </view>
      <view class="date-item expiry-date">
        <view class="date-icon">期</view>
        <view class="date-copy">
          <text class="date-label">过期日期</text>
          <text class="date-value">{{ formatDate(item.expiryDate) }}</text>
        </view>
      </view>
      <view class="date-item" v-if="item.purchaseDate">
        <view class="date-icon">购</view>
        <view class="date-copy">
          <text class="date-label">购买日期</text>
          <text class="date-value">{{ formatDate(item.purchaseDate) }}</text>
        </view>
      </view>
    </view>
    <view class="item-info" v-else>
      <view class="info-line">
        <text class="info-label">过期日期</text>
        <text class="date-value">{{ formatDate(item.expiryDate) }}</text>
      </view>
      <view class="info-line" v-if="item.creatorName">
        <text class="info-label">添加者</text>
        <text class="date-value">{{ item.creatorName }}</text>
      </view>
    </view>

    <view class="action-row">
      <view class="log-btn" @click.stop="handleLog">
        <text class="log-icon">≡</text>
        <text class="log-text">日志</text>
      </view>
      <view class="dispose-btn" v-if="showProcess && statusClass === 'status-expired'" @click.stop="handleProcess">
        <text class="dispose-text">处理过期</text>
      </view>
    </view>
  </view>
</template>

<script>
import dateUtil from '@/common/utils/date.js'
import themeMixin from '@/common/mixins/theme.js'

export default {
  name: 'ItemCard',
  mixins: [themeMixin],
  props: {
    item: {
      type: Object,
      required: true
    },
    showEdit: {
      type: Boolean,
      default: true
    },
    showDates: {
      type: Boolean,
      default: true
    },
    showProcess: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    cardClass() {
      const status = dateUtil.getItemStatus(this.item.expiryDate)
      if (status.status === 'expired') return 'expired'
      if (status.status === 'near') return 'near-expiry'
      return ''
    },
    statusClass() {
      const status = dateUtil.getItemStatus(this.item.expiryDate)
      return `status-${status.status}`
    },
    statusText() {
      const status = dateUtil.getItemStatus(this.item.expiryDate)
      if (status.status === 'expired') return '已过期'
      if (status.status === 'near') return '即将过期'
      return '正常'
    }
  },
  methods: {
    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const month = date.getMonth() + 1
      const day = date.getDate()
      return `${month}月${day}日`
    },
    handleClick() {
      this.$emit('click', this.item)
    },
    handleEdit() {
      this.$emit('edit', this.item)
    },
    handleLog() {
      this.$emit('log', this.item)
    },
    handleProcess() {
      this.$emit('process', this.item)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/common/style/common.scss';

.item-card {
  padding: 34rpx;
  margin-bottom: 0;
  background: var(--card-bg);
  position: relative;
  overflow: hidden;
  cursor: pointer;
  border-radius: 30rpx;
  border: 2rpx solid var(--card-border);
  box-shadow: 0 14rpx 38rpx -24rpx var(--shadow-color);
  box-sizing: border-box;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 8rpx;
    height: 100%;
    background: linear-gradient(180deg, var(--accent-blue), var(--accent-teal));
    border-radius: 32rpx 0 0 32rpx;
    transition: width 0.3s ease;
  }

  &::after {
    content: '';
    position: absolute;
    top: -90rpx;
    right: -90rpx;
    width: 210rpx;
    height: 210rpx;
    border-radius: 50%;
    background: rgba(37, 99, 235, 0.08);
    pointer-events: none;
  }

  &:active {
    transform: translateY(2rpx);

    &::before {
      width: 12rpx;
    }
  }
}

.item-card.light-mode {
  background: rgba(255, 255, 255, 0.96) !important;
  border-color: rgba(148, 163, 184, 0.18) !important;
  box-shadow: 0 18rpx 48rpx -34rpx rgba(15, 23, 42, 0.45) !important;
}

.item-card:not(.expired):not(.near-expiry)::before {
  background: linear-gradient(180deg, var(--accent-blue), var(--accent-teal));
}

.item-card.expired::before {
  background: linear-gradient(180deg, var(--accent-rose), #f87171);
}

.item-card.near-expiry::before {
  background: linear-gradient(180deg, var(--accent-amber), #fbbf24);
}

.item-card.near-expiry::after {
  background: rgba(245, 158, 11, 0.1);
}

.item-card.expired::after {
  background: rgba(225, 29, 72, 0.09);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20rpx;
  margin-bottom: 26rpx;
  position: relative;
  z-index: 1;
}

.item-title-group {
  flex: 1;
  min-width: 0;
}

.item-name {
  display: block;
  font-weight: 800;
  font-size: 38rpx;
  color: var(--text-primary);
  line-height: 1.25;
  word-break: break-all;
}

.edit-btn {
  background-color: var(--hover-bg);
  border: 2rpx solid var(--card-border);
  width: 64rpx;
  height: 64rpx;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active {
    background: var(--active-bg);
    transform: scale(0.95);
  }
}

.light-mode .edit-btn {
  background-color: #f8fafc !important;
  border-color: #e2e8f0 !important;
}

.edit-icon {
  width: 30rpx;
  height: 30rpx;
}

.date-grid {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  position: relative;
  z-index: 1;
}

.date-item {
  display: flex;
  align-items: center;
  gap: 18rpx;
  padding: 18rpx 20rpx;
  border-radius: 22rpx;
  background: rgba(148, 163, 184, 0.1);
  box-sizing: border-box;
}

.date-icon {
  width: 48rpx;
  height: 48rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #ffffff;
  font-size: 22rpx;
  font-weight: 800;
  background: #64748b;
}

.expiry-date .date-icon {
  background: var(--accent-blue);
}

.near-expiry .expiry-date .date-icon {
  background: var(--accent-amber);
}

.expired .expiry-date .date-icon {
  background: var(--accent-rose);
}

.date-copy {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  flex: 1;
  min-width: 0;
}

.date-label {
  color: var(--text-secondary);
  font-size: 24rpx;
  font-weight: 600;
  flex-shrink: 0;
}

.date-value {
  font-weight: 800;
  color: var(--text-primary);
  font-size: 27rpx;
  text-align: right;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  position: relative;
  z-index: 1;
}

.info-line {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
  padding: 18rpx 20rpx;
  border-radius: 22rpx;
  background: rgba(148, 163, 184, 0.1);
}

.info-label {
  color: var(--text-secondary);
  font-size: 24rpx;
  font-weight: 600;
}

.expiry-status {
  display: inline-flex;
  align-items: center;
  padding: 8rpx 20rpx;
  border-radius: 40rpx;
  font-size: 23rpx;
  font-weight: 800;
  margin-top: 12rpx;
  border: 1rpx solid transparent;
  transition: all 0.3s ease;
}

.status-normal {
  background: rgba(5, 150, 105, 0.15);
  color: var(--accent-green);
  border-color: rgba(5, 150, 105, 0.25);
}

.status-near {
  background: rgba(245, 158, 11, 0.15);
  color: var(--accent-amber);
  border-color: rgba(245, 158, 11, 0.25);
}

.status-expired {
  background: rgba(225, 29, 72, 0.15);
  color: var(--accent-rose);
  border-color: rgba(225, 29, 72, 0.25);
}

.action-row {
  display: flex;
  gap: 14rpx;
  margin-top: 22rpx;
  position: relative;
  z-index: 1;
}

.log-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  height: 64rpx;
  padding: 0 26rpx;
  background: var(--hover-bg);
  border: 2rpx solid var(--card-border);
  border-radius: 22rpx;
  transition: all 0.3s ease;
  flex: 1;
  box-sizing: border-box;
  
  &:active {
    background: var(--active-bg);
    transform: scale(0.98);
  }
}

.dispose-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 64rpx;
  padding: 0 26rpx;
  background: rgba(239, 68, 68, 0.12);
  border: 2rpx solid rgba(239, 68, 68, 0.35);
  border-radius: 22rpx;
  transition: all 0.3s ease;
  flex: 1.2;
  box-sizing: border-box;
  
  &:active {
    background: rgba(239, 68, 68, 0.2);
    transform: scale(0.98);
  }
}

.dispose-text {
  font-size: 26rpx;
  color: #ef4444;
  font-weight: 800;
}

.log-icon {
  font-size: 30rpx;
  color: var(--text-secondary);
  line-height: 1;
}

.log-text {
  font-size: 26rpx;
  color: var(--text-secondary);
  font-weight: 800;
}

.item-card.light-mode .log-btn {
  background: #f8fafc !important;
  border-color: #e2e8f0 !important;
  box-shadow: none !important;
}

.item-card.light-mode .date-item,
.item-card.light-mode .info-line {
  background: #f8fafc !important;
}

.item-card.light-mode .log-text,
.item-card.light-mode .log-icon {
  color: #475569 !important;
}
</style>
