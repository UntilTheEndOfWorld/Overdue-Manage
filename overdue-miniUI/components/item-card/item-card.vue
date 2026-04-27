<template>
  <view 
    class="item-card" 
    :class="[cardClass, themeClass]"
    @click="handleClick"
  >
    <view class="item-header">
      <view>
        <text class="item-name">{{ item.name }}</text>
        <view class="expiry-status" :class="statusClass">
          {{ statusText }}
        </view>
      </view>
      <view class="edit-btn" v-if="showEdit" @click.stop="handleEdit">
        <image class="edit-icon" src="/static/tabbar/edit.png" mode="aspectFit"></image>
      </view>
    </view>
    <view class="item-date" v-if="showDates">
      <view class="date-item">
        <text class="date-label">📅 生产日期</text>
        <text class="date-value">{{ formatDate(item.productionDate) }}</text>
      </view>
      <view class="date-item">
        <text class="date-label">⏰ 过期日期</text>
        <text class="date-value">{{ formatDate(item.expiryDate) }}</text>
      </view>
      <view class="date-item" v-if="item.purchaseDate">
        <text class="date-label">🛒 购买日期</text>
        <text class="date-value">{{ formatDate(item.purchaseDate) }}</text>
      </view>
    </view>
    <view class="item-info" v-else>
      <text>过期日期：{{ formatDate(item.expiryDate) }}</text>
      <text v-if="item.creatorName">添加者：{{ item.creatorName }}</text>
    </view>
    <!-- 操作日志按钮 -->
    <view class="log-btn" @click.stop="handleLog">
      <text class="log-icon">📋</text>
      <text class="log-text">操作日志</text>
    </view>
    <view class="dispose-btn" v-if="showProcess && statusClass === 'status-expired'" @click.stop="handleProcess">
      <text class="dispose-icon">♻️</text>
      <text class="dispose-text">处理（移入删除列表）</text>
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
  @extend .glass-card;
  padding: 40rpx;
  margin-bottom: 24rpx;
  background: var(--card-bg);
  position: relative;
  overflow: hidden;
  cursor: pointer;
  
  /* 左侧渐变装饰条 - 高级样式 */
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
  
  /* 微妙的光泽效果 */
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1rpx;
    background: linear-gradient(90deg, 
      transparent, 
      rgba(255, 255, 255, 0.1), 
      transparent
    );
    pointer-events: none;
  }
  
  &:active {
    transform: translateY(2rpx);
    border-color: var(--accent-blue);
    box-shadow: 
      0 4rpx 16rpx -4rpx var(--glow-blue),
      0 2rpx 8rpx -2rpx var(--shadow-color);
    
    &::before {
      width: 12rpx;
    }
  }
}

/* 浅色模式下的卡片样式 - 高级样式 */
.item-card.light-mode {
  background: rgba(255, 255, 255, 0.9) !important;
  border: 2rpx solid rgba(37, 99, 235, 0.15) !important;
  box-shadow: 
    0 8rpx 32rpx -8rpx rgba(0, 0, 0, 0.08),
    0 0 0 1rpx rgba(255, 255, 255, 0.8) inset !important;
  
  &::after {
    background: linear-gradient(90deg, 
      transparent, 
      rgba(0, 0, 0, 0.03), 
      transparent
    ) !important;
  }
  
  &:active {
    border-color: var(--accent-blue) !important;
    box-shadow: 
      0 4rpx 16rpx -4rpx rgba(37, 99, 235, 0.2),
      0 2rpx 8rpx -2rpx rgba(0, 0, 0, 0.1) !important;
  }
}

/* 正常状态 - 蓝色渐变 */
.item-card:not(.expired):not(.near-expiry)::before {
  background: linear-gradient(180deg, var(--accent-blue), var(--accent-teal));
}

/* 过期状态 - 玫瑰红渐变 */
.item-card.expired::before {
  background: linear-gradient(180deg, var(--accent-rose), #f87171);
}

/* 即将过期状态 - 琥珀色渐变 */
.item-card.near-expiry::before {
  background: linear-gradient(180deg, var(--accent-amber), #fbbf24);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30rpx;
}

.item-name {
  font-weight: 600;
  font-size: 48rpx;
  color: var(--text-primary);
  margin-bottom: 10rpx;
}

.edit-btn {
  background-color: var(--hover-bg);
  border: 2rpx solid var(--card-border);
  padding: 16rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active {
    background: var(--active-bg);
    transform: scale(0.95);
  }
}

/* 浅色模式下的编辑按钮 */
.light-mode .edit-btn {
  background-color: rgba(245, 158, 11, 0.1) !important;
  border-color: rgba(245, 158, 11, 0.2) !important;
  
  &:active {
    background-color: rgba(245, 158, 11, 0.15) !important;
  }
}

.edit-icon {
  width: 32rpx;
  height: 32rpx;
}

.item-date {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-top: 20rpx;
  font-size: 28rpx;
}

.date-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  color: var(--text-secondary);
}

.date-label {
  color: var(--text-secondary);
  font-size: 26rpx;
  min-width: 160rpx;
}

.date-value {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 28rpx;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  font-size: 28rpx;
  color: var(--text-secondary);
  margin-top: 20rpx;
}

.expiry-status {
  display: inline-flex;
  align-items: center;
  padding: 10rpx 24rpx;
  border-radius: 40rpx;
  font-size: 24rpx;
  font-weight: 600;
  letter-spacing: 1rpx;
  margin-top: 12rpx;
  backdrop-filter: blur(10rpx);
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

/* 操作日志按钮 */
.log-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 16rpx 24rpx;
  margin-top: 24rpx;
  background: var(--hover-bg);
  border: 2rpx solid var(--card-border);
  border-radius: 20rpx;
  transition: all 0.3s ease;
  
  &:active {
    background: var(--active-bg);
    transform: scale(0.98);
  }
}

.dispose-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 16rpx 24rpx;
  margin-top: 16rpx;
  background: rgba(239, 68, 68, 0.12);
  border: 2rpx solid rgba(239, 68, 68, 0.35);
  border-radius: 20rpx;
  transition: all 0.3s ease;
  
  &:active {
    background: rgba(239, 68, 68, 0.2);
    transform: scale(0.98);
  }
}

.dispose-icon {
  font-size: 26rpx;
}

.dispose-text {
  font-size: 26rpx;
  color: #ef4444;
  font-weight: 600;
}

.log-icon {
  font-size: 28rpx;
}

.log-text {
  font-size: 26rpx;
  color: var(--text-secondary);
  font-weight: 500;
}

/* 浅色模式下的日志按钮 - 增强对比度 */
.item-card.light-mode .log-btn {
  background: #ffffff !important;
  border: 2rpx solid rgba(59, 130, 246, 0.5) !important;
  box-shadow: 0 2rpx 8rpx rgba(59, 130, 246, 0.2) !important;
  
  .log-icon {
    filter: brightness(0.9);
  }
  
  .log-text {
    color: #3b82f6 !important;
    font-weight: 600 !important;
  }
  
  &:active {
    background: rgba(59, 130, 246, 0.1) !important;
    border-color: rgba(59, 130, 246, 0.7) !important;
  }
}

/* 浅色模式下即将过期卡片的日志按钮 - 使用更深的背景色 */
.item-card.light-mode.near-expiry .log-btn {
  background: #ffffff !important;
  border: 2rpx solid rgba(245, 158, 11, 0.5) !important;
  box-shadow: 0 2rpx 8rpx rgba(245, 158, 11, 0.2) !important;
  
  .log-text {
    color: #f59e0b !important;
  }
  
  &:active {
    background: rgba(245, 158, 11, 0.1) !important;
    border-color: rgba(245, 158, 11, 0.7) !important;
  }
}

/* 浅色模式下已过期卡片的日志按钮 */
.item-card.light-mode.expired .log-btn {
  background: #ffffff !important;
  border: 2rpx solid rgba(239, 68, 68, 0.5) !important;
  box-shadow: 0 2rpx 8rpx rgba(239, 68, 68, 0.2) !important;
  
  .log-text {
    color: #ef4444 !important;
  }
  
  &:active {
    background: rgba(239, 68, 68, 0.1) !important;
    border-color: rgba(239, 68, 68, 0.7) !important;
  }
}
</style>
