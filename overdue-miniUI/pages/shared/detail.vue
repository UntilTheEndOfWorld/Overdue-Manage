<template>
  <view class="container" :class="themeClass">
    <!-- 邀请按钮和搜索框 -->
    <view class="top-actions">
      <view class="invite-btn" @click="inviteMember">
        <text class="invite-icon">👥</text>
        <text class="invite-text">邀请</text>
      </view>
      <view class="search-bar">
        <input 
          class="search-input" 
          v-model="searchKeyword" 
          placeholder="搜索物品名称..."
          @input="onSearch"
        />
      </view>
    </view>

    <!-- 成员列表 -->
    <view class="members-section">
      <view class="members-header">
        <text class="members-title">成员列表</text>
        <text class="members-count">{{ spaceMembers.length }}人</text>
      </view>
      <view class="members-list" v-if="spaceMembers.length > 0">
        <view 
          class="member-item" 
          v-for="member in spaceMembers" 
          :key="member.userId"
        >
          <view class="member-avatar">{{ member.userName ? member.userName.charAt(0) : 'U' }}</view>
          <text class="member-name">{{ member.userName || '未知用户' }}</text>
          <text class="member-role">{{ getRoleText(member.role) }}</text>
        </view>
      </view>
      <view class="members-empty" v-else>
        <text>暂无成员</text>
      </view>
    </view>

    <!-- 操作日志轮播卡片 -->
    <view class="logs-carousel-section" v-if="allLogs.length > 0">
      <view class="logs-header">
        <text class="logs-title">操作日志</text>
      </view>
      <swiper 
        class="logs-swiper" 
        :indicator-dots="true" 
        :autoplay="true" 
        :interval="3000" 
        :duration="500"
        :circular="true"
        indicator-color="rgba(255, 255, 255, 0.3)"
        indicator-active-color="var(--accent-blue)"
      >
        <swiper-item v-for="(log, index) in allLogs" :key="log.id || index">
          <view class="log-card">
            <view class="log-user">
              <view class="log-avatar">{{ log.operatorName ? log.operatorName.charAt(0) : 'U' }}</view>
              <text class="log-operator">{{ log.operatorName || '未知用户' }}</text>
            </view>
            <text class="log-text">{{ getLogText(log) }}</text>
            <text class="log-time">{{ formatTime(log.operationTime) }}</text>
          </view>
        </swiper-item>
      </swiper>
    </view>

    <!-- 筛选器 -->
    <view class="filter-bar">
      <view 
        class="filter-item" 
        :class="{ active: filter === 'all' }"
        @click="setFilter('all')"
      >
        全部
      </view>
      <view 
        class="filter-item" 
        :class="{ active: filter === 'normal' }"
        @click="setFilter('normal')"
      >
        正常
      </view>
      <view 
        class="filter-item" 
        :class="{ active: filter === 'near' }"
        @click="setFilter('near')"
      >
        即将过期
      </view>
      <view 
        class="filter-item" 
        :class="{ active: filter === 'expired' }"
        @click="setFilter('expired')"
      >
        已过期
      </view>
    </view>

    <!-- 物品列表 -->
    <view class="item-list">
      <item-card 
        v-for="item in filteredItems" 
        :key="item.id"
        :item="item"
        :show-dates="false"
        @click="editItem"
        @edit="editItem"
        @log="showItemLogs"
      />
    </view>

    <view class="fab" @click="addItem">
      <image class="fab-icon" src="/static/tabbar/add.png" mode="aspectFit"></image>
    </view>
  </view>
</template>

<script>
import storage from '@/common/utils/storage.js'
import itemUtil from '@/common/utils/item.js'
import ItemCard from '@/components/item-card/item-card.vue'
import themeMixin from '@/common/mixins/theme.js'
import logger from '@/common/utils/logger.js'
import api from '@/common/utils/api.js'
import { isLoggedIn } from '@/common/utils/auth.js'

export default {
  mixins: [themeMixin],
  components: {
    ItemCard
  },
  data() {
    return {
      spaceId: '',
      space: {},
      items: [],
      filter: 'all',
      searchKeyword: '',
      allLogs: [],
      loading: false
    }
  },
  computed: {
    filteredItems() {
      var result = this.items
      if (this.searchKeyword) {
        result = itemUtil.searchItems(result, this.searchKeyword)
      }
      return itemUtil.filterItems(result, this.filter)
    },
    displaySpaceName() {
      if (!this.space || !this.space.name) {
        return '共享空间'
      }
      if (this.space.name.endsWith('箱')) {
        return this.space.name.slice(0, -1)
      }
      return this.space.name
    },
    spaceMembers() {
      if (!this.space || !this.space.members) {
        if (this.space && this.space.creatorId) {
          return [{
            userId: this.space.creatorId,
            userName: this.space.creatorName || '创建者',
            role: 'creator'
          }]
        }
        return []
      }
      return this.space.members
    }
  },
  onLoad(options) {
    // 空间 ID 为雪花 Long，仅作字符串使用，避免参与 Number 运算丢精度
    this.spaceId = options.id != null && options.id !== '' ? String(options.id) : ''
    this.loadData()
  },
  onShow() {
    this.loadData()
  },
  onPullDownRefresh() {
    this.loadData()
    setTimeout(function() {
      uni.stopPullDownRefresh()
    }, 500)
  },
  methods: {
    async loadData() {
      if (!isLoggedIn() || !this.spaceId) return
      if (this.loading) return
      this.loading = true
      try {
        // 并行请求空间详情、物品列表、操作日志
        var spaceRes = await api.getSharedSpaceDetail(this.spaceId)
        if (spaceRes && spaceRes.code === 200 && spaceRes.data) {
          this.space = spaceRes.data
        }

        var itemsRes = await api.getSharedItems(this.spaceId)
        if (itemsRes && itemsRes.code === 200 && itemsRes.data) {
          this.items = Array.isArray(itemsRes.data) ? itemsRes.data : (itemsRes.data.rows || itemsRes.data.list || [])
        }

        // 加载操作日志
        this.loadAllLogs()
      } catch (error) {
        console.error('加载空间详情失败:', error)
        // 兜底使用缓存
        var spaces = storage.get('sharedSpaces', [])
        var self = this
        this.space = spaces.find(function(s) { return String(s.id) === String(self.spaceId) }) || {}
        var allItems = storage.get('sharedItems', [])
        this.items = allItems.filter(function(item) { return String(item.spaceId) === String(self.spaceId) })
        this.loadAllLogs()
      } finally {
        this.loading = false
      }
    },
    async loadAllLogs() {
      try {
        var res = await api.getSpaceLogs(this.spaceId)
        if (res && res.code === 200 && res.data) {
          var logs = Array.isArray(res.data) ? res.data : (res.data.rows || res.data.list || [])
          this.allLogs = logs.slice(0, 10)
        }
      } catch (error) {
        console.error('加载操作日志失败:', error)
        // 兜底使用本地日志
        this.allLogs = logger.getSpaceLogs(this.spaceId, 'all').slice(0, 10)
      }
    },
    getRoleText(role) {
      const roleMap = {
        'creator': '创建者',
        'admin': '管理员',
        'member': '成员'
      }
      return roleMap[role] || '成员'
    },
    getLogText(log) {
      if (log && log.operationDesc) return log.operationDesc
      return logger.getOperationText(log)
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
    },
    setFilter(type) {
      this.filter = type
    },
    addItem() {
      uni.navigateTo({
        url: `/pages/personal/add-item?spaceId=${this.spaceId}&type=shared`
      })
    },
    editItem(item) {
      // 编辑共享物品
      uni.navigateTo({
        url: `/pages/personal/edit-item?id=${item.id}&spaceId=${this.spaceId}&type=shared`
      })
    },
    onSearch() {
      // 搜索逻辑已在computed中处理
    },
    showItemLogs(item) {
      uni.navigateTo({
        url: `/pages/shared/logs?spaceId=${this.spaceId}&itemId=${item.id}`
      })
    },
    inviteMember() {
      uni.navigateTo({
        url: `/pages/shared/invite?spaceId=${this.spaceId}`
      })
    },
  }
}
</script>

<style lang="scss" scoped>
@import '@/common/style/common.scss';

.container {
  padding: 40rpx;
  padding-bottom: 120rpx;
  min-height: 100vh;
  background: var(--primary-bg);
  transition: background-color 0.3s ease;
}

/* 顶部操作区域 */
.top-actions {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.invite-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 24rpx;
  background: linear-gradient(135deg, var(--accent-blue), var(--accent-teal));
  color: white;
  border-radius: 40rpx;
  font-size: 28rpx;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3);
  transition: all 0.3s ease;
  flex-shrink: 0;
  white-space: nowrap;
  
  &:active {
    transform: scale(0.95);
  }
}

.invite-icon {
  font-size: 32rpx;
}

.invite-text {
  font-weight: 500;
}

/* 浅色模式下的邀请按钮 */
.light-mode .invite-btn {
  background: linear-gradient(135deg, #3b82f6, #14b8a6) !important;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3) !important;
}

.search-bar {
  flex: 1;
}

.search-input {
  width: 100%;
  height: 80rpx;
  padding: 0 30rpx;
  background: var(--card-bg-solid);
  border-radius: 40rpx;
  font-size: 28rpx;
  border: 2rpx solid var(--card-border);
  color: var(--text-primary);
  transition: all 0.3s ease;
  box-sizing: border-box;
  margin: 0;
  line-height: 80rpx;
}

.search-input:focus {
  border-color: var(--accent-blue);
  background: var(--hover-bg);
}

/* 浅色模式下的搜索框 */
.light-mode .search-input {
  background: #ffffff !important;
  border: 2rpx solid rgba(59, 130, 246, 0.3) !important;
  color: #1e293b !important;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05) !important;
}

.light-mode .search-input::placeholder {
  color: #64748b !important;
}

.light-mode .search-input:focus {
  border-color: var(--accent-blue) !important;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.2) !important;
}

/* 成员列表 */
.members-section {
  margin-bottom: 30rpx;
  padding: 30rpx;
  background: var(--card-bg-solid);
  border-radius: 20rpx;
  border: 2rpx solid var(--card-border);
}

.members-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.members-title {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.members-count {
  font-size: 24rpx;
  color: var(--text-secondary);
}

.members-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx;
  background: var(--hover-bg);
  border-radius: 16rpx;
}

.member-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent-blue), var(--accent-teal));
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 600;
  flex-shrink: 0;
}

.member-name {
  flex: 1;
  font-size: 28rpx;
  color: var(--text-primary);
  font-weight: 500;
}

.member-role {
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  background: var(--card-bg-solid);
  color: var(--accent-blue);
  border-radius: 20rpx;
  border: 1rpx solid var(--card-border);
}

/* 浅色模式下的成员列表 */
.light-mode .members-section {
  background: #ffffff !important;
  border: 2rpx solid rgba(59, 130, 246, 0.2) !important;
}

.light-mode .member-item {
  background: #f8fafc !important;
}

.light-mode .member-role {
  background: #f1f5f9 !important;
  border: 1rpx solid rgba(59, 130, 246, 0.2) !important;
}

.members-empty {
  text-align: center;
  padding: 40rpx 0;
  color: var(--text-secondary);
  font-size: 28rpx;
}

/* 操作日志轮播卡片 */
.logs-carousel-section {
  margin-bottom: 30rpx;
}

.logs-header {
  margin-bottom: 20rpx;
}

.logs-title {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.logs-swiper {
  height: 200rpx;
  border-radius: 20rpx;
  overflow: hidden;
}

.log-card {
  height: 100%;
  padding: 30rpx;
  background: var(--card-bg-solid);
  border-radius: 20rpx;
  border: 2rpx solid var(--card-border);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.log-user {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.log-avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent-blue), var(--accent-teal));
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 600;
  flex-shrink: 0;
}

.log-operator {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.log-text {
  flex: 1;
  font-size: 26rpx;
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 12rpx;
}

.log-time {
  font-size: 22rpx;
  color: var(--text-secondary);
  opacity: 0.7;
}

/* 浅色模式下的日志卡片 */
.light-mode .log-card {
  background: #ffffff !important;
  border: 2rpx solid rgba(59, 130, 246, 0.2) !important;
}

.filter-bar {
  display: flex;
  gap: 20rpx;
  margin-bottom: 40rpx;
  flex-wrap: wrap;
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

/* 浅色模式下的筛选按钮 */
.light-mode .filter-item {
  background: #ffffff !important;
  border: 2rpx solid rgba(0, 0, 0, 0.1) !important;
  color: #1e293b !important;
  box-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.05) !important;
  font-weight: 500 !important;
}

/* 浅色模式下选中状态 - 浅米色/米黄色背景 + 深色文字 */
.light-mode .filter-item.active {
  background: #fcf8ed !important;
  color: #1e293b !important;
  border-color: #e3c89c !important;
  box-shadow: 0 2rpx 8rpx rgba(227, 200, 156, 0.2) !important;
  font-weight: 600 !important;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.fab {
  position: fixed;
  right: 40rpx;
  bottom: 120rpx;
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, var(--accent-blue), var(--accent-teal));
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 16rpx rgba(59, 130, 246, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 100;
}

.fab:active {
  transform: scale(0.95);
}

.fab-icon {
  width: 60rpx;
  height: 60rpx;
}
</style>
