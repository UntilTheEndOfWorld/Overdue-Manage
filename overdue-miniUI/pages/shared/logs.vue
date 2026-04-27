<template>
  <view class="container" :class="themeClass">
    <view class="header">
      <text class="title">操作日志</text>
    </view>

    <view class="filter-bar">
      <view 
        class="filter-item" 
        :class="{ active: filterType === 'all' }"
        @click="setFilterType('all')"
      >
        全部
      </view>
      <view 
        class="filter-item" 
        :class="{ active: filterType === 'add' }"
        @click="setFilterType('add')"
      >
        添加
      </view>
      <view 
        class="filter-item" 
        :class="{ active: filterType === 'update' }"
        @click="setFilterType('update')"
      >
        更新
      </view>
      <view 
        class="filter-item" 
        :class="{ active: filterType === 'delete' }"
        @click="setFilterType('delete')"
      >
        删除
      </view>
    </view>

    <view class="log-list">
      <view 
        class="log-item" 
        v-for="log in filteredLogs" 
        :key="log.id"
      >
        <view class="log-header">
          <view class="user-info">
            <text class="avatar">{{ log.operatorName.charAt(0) }}</text>
            <text class="name">{{ log.operatorName }}</text>
          </view>
          <text class="time">{{ formatTime(log.operationTime) }}</text>
        </view>
        <view class="log-content">
          <text>{{ getLogText(log) }}</text>
        </view>
        <view class="log-type" :class="log.operationType">
          {{ getTypeText(log.operationType) }}
        </view>
      </view>
    </view>

    <view class="empty-state" v-if="filteredLogs.length === 0">
      <text>暂无操作日志</text>
    </view>
  </view>
</template>

<script>
import storage from '@/common/utils/storage.js'
import logger from '@/common/utils/logger.js'
import themeMixin from '@/common/mixins/theme.js'

export default {
  mixins: [themeMixin],
  data() {
    return {
      spaceId: '',
      logs: [],
      filterType: 'all',
      itemId: '' // 物品ID，用于筛选特定物品的日志
    }
  },
  computed: {
    filteredLogs() {
      let result = this.logs
      
      // 如果指定了物品ID，只显示该物品的日志
      if (this.itemId) {
        result = result.filter(log => log.itemId === this.itemId)
      }
      
      // 按操作类型筛选
      if (this.filterType === 'all') {
        return result
      }
      return result.filter(log => log.operationType === this.filterType)
    }
  },
  onLoad(options) {
    this.spaceId = options.spaceId
    // 获取物品ID参数
    if (options && options.itemId) {
      this.itemId = options.itemId
    }
    this.loadLogs()
  },
  onShow() {
    this.loadLogs()
  },
  methods: {
    loadLogs() {
      this.logs = logger.getSpaceLogs(this.spaceId, this.filterType)
    },
    setFilterType(type) {
      this.filterType = type
      this.loadLogs()
    },
    getLogText(log) {
      if (log && log.operationDesc) return log.operationDesc
      return logger.getOperationText(log)
    },
    getTypeText(type) {
      const typeMap = {
        'add': '添加',
        'update': '更新',
        'delete': '删除',
        'expire': '过期',
        'process': '处理'
      }
      return typeMap[type] || '未知'
    },
    formatTime(time) {
      const date = new Date(time)
      const now = new Date()
      const diff = now - date
      const minutes = Math.floor(diff / 60000)
      const hours = Math.floor(diff / 3600000)
      const days = Math.floor(diff / 86400000)
      
      if (minutes < 1) {
        return '刚刚'
      } else if (minutes < 60) {
        return `${minutes}分钟前`
      } else if (hours < 24) {
        return `${hours}小时前`
      } else if (days < 7) {
        return `${days}天前`
      } else {
        const month = date.getMonth() + 1
        const day = date.getDate()
        const hour = date.getHours()
        const minute = date.getMinutes()
        return `${month}-${day} ${hour}:${String(minute).padStart(2, '0')}`
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
  margin-bottom: 40rpx;
}

.title {
  font-size: 56rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.filter-bar {
  display: flex;
  gap: 20rpx;
  margin-bottom: 40rpx;
  flex-wrap: wrap;
}

.filter-item {
  padding: 16rpx 32rpx;
  background: rgba(255, 255, 255, 0.05);
  border: 2rpx solid $glass-border;
  border-radius: 40rpx;
  font-size: 28rpx;
  color: var(--text-secondary);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active {
    transform: scale(0.98);
  }
  
  &.active {
    background: rgba(59, 130, 246, 0.2);
    color: $accent-blue;
    border-color: rgba(59, 130, 246, 0.3);
  }
}

.log-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.log-item {
  @extend .glass-card;
  padding: 36rpx;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active {
    transform: translateY(-5rpx);
    border-color: rgba(255, 255, 255, 0.15);
  }
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.avatar {
  width: 60rpx;
  height: 60rpx;
  background: linear-gradient(135deg, #8b5cf6, $accent-blue);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 600;
  border: 2rpx solid rgba(255, 255, 255, 0.1);
}

.name {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.time {
  font-size: 24rpx;
  color: var(--text-secondary);
}

.log-content {
  font-size: 32rpx;
  color: var(--text-primary);
  line-height: 1.6;
  margin-bottom: 10rpx;
}

.log-type {
  display: inline-block;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 500;
  
  &.add {
    background: rgba(34, 197, 94, 0.15);
    color: #22c55e;
  }
  
  &.update {
    background: rgba(59, 130, 246, 0.15);
    color: $accent-blue;
  }
  
  &.delete {
    background: rgba(244, 63, 94, 0.15);
    color: $accent-rose;
  }
  
  &.expire {
    background: rgba(245, 158, 11, 0.15);
    color: #f59e0b;
  }
  
  &.process {
    background: rgba(16, 185, 129, 0.15);
    color: #10b981;
  }
}

.empty-state {
  text-align: center;
  padding: 160rpx 40rpx;
  color: var(--text-secondary);
}

.empty-icon {
  display: block;
  font-size: 120rpx;
  margin-bottom: 30rpx;
  opacity: 0.3;
}

.empty-text {
  display: block;
  font-size: 32rpx;
  color: var(--text-secondary);
}

/* 浅色模式下的筛选按钮 */
.light-mode .filter-item {
  background: #ffffff !important;
  border: 2rpx solid rgba(0, 0, 0, 0.1) !important;
  color: #1e293b !important;
  box-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.05) !important;
}

.light-mode .filter-item.active {
  background: #fcf8ed !important;
  color: #1e293b !important;
  border-color: #e3c89c !important;
  box-shadow: 0 2rpx 8rpx rgba(227, 200, 156, 0.2) !important;
}

/* 浅色模式下的日志卡片 */
.light-mode .log-item {
  background: var(--card-bg-solid) !important;
  border: 2rpx solid var(--card-border) !important;
}

/* 浅色模式下的头像 */
.light-mode .avatar {
  background: linear-gradient(135deg, #8b5cf6, #3b82f6) !important;
  border: 2rpx solid rgba(59, 130, 246, 0.15) !important;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.2) !important;
}
</style>
