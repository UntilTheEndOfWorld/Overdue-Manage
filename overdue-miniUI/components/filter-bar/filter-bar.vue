<template>
  <view class="filter-bar">
    <view 
      class="filter-item" 
      :class="{ active: currentFilter === 'all' }"
      @click="setFilter('all')"
    >
      全部
    </view>
    <view 
      class="filter-item" 
      :class="{ active: currentFilter === 'near' }"
      @click="setFilter('near')"
    >
      即将过期
    </view>
    <view 
      class="filter-item" 
      :class="{ active: currentFilter === 'expired' }"
      @click="setFilter('expired')"
    >
      已过期
    </view>
    <view 
      class="filter-item" 
      :class="{ active: currentFilter === 'normal' }"
      @click="setFilter('normal')"
    >
      正常
    </view>
  </view>
</template>

<script>
export default {
  name: 'FilterBar',
  props: {
    filter: {
      type: String,
      default: 'all'
    }
  },
  data() {
    return {
      currentFilter: this.filter
    }
  },
  watch: {
    filter(newVal) {
      this.currentFilter = newVal
    }
  },
  methods: {
    setFilter(type) {
      this.currentFilter = type
      this.$emit('change', type)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/common/style/common.scss';

.filter-bar {
  display: flex;
  gap: 20rpx;
  flex: 1;
}

.filter-item {
  padding: 16rpx 32rpx;
  background: var(--card-bg-solid);
  border-radius: 40rpx;
  font-size: 28rpx;
  color: var(--text-primary);
  border: 2rpx solid var(--card-border);
  transition: all 0.3s;
  font-weight: 500;
  
  &:active {
    transform: scale(0.98);
  }
}

.filter-item.active {
  background: linear-gradient(135deg, var(--accent-blue), var(--accent-teal));
  color: #ffffff;
  border-color: transparent;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3);
  font-weight: 600;
}
</style>

<style lang="scss">
/* 浅色模式下的筛选按钮 - 使用全局样式以覆盖 scoped */
.light-mode .filter-bar .filter-item {
  background: #ffffff !important;
  border: 2rpx solid rgba(0, 0, 0, 0.1) !important;
  color: #1e293b !important;
  box-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.05) !important;
  font-weight: 500 !important;
}

/* 浅色模式下选中状态 - 浅米色/米黄色背景 + 深色文字，参考图片中的焦糖色按钮 */
.light-mode .filter-bar .filter-item.active {
  background: #fcf8ed !important;
  color: #1e293b !important;
  border-color: #e3c89c !important;
  box-shadow: 0 2rpx 8rpx rgba(227, 200, 156, 0.2) !important;
  font-weight: 600 !important;
}
</style>
