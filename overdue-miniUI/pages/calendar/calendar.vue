<template>
  <view class="container" :class="themeClass">
    <!-- 筛选栏 -->
    <view class="filter-container">
      <view class="filter-bar">
        <view 
          class="filter-item" 
          :class="{ active: filterType === 'all' }"
          @click="setFilter('all')"
        >
          全部
        </view>
        <view 
          class="filter-item" 
          :class="{ active: filterType === 'personal' }"
          @click="setFilter('personal')"
        >
          个人物品
        </view>
        <view 
          class="filter-item" 
          :class="{ active: filterType === 'shared' }"
          @click="setFilter('shared')"
        >
          共享物品
        </view>
      </view>
    </view>

    <!-- 日历容器 -->
    <view class="calendar-container">
      <!-- 月份导航栏 -->
      <view class="month-nav">
        <view class="nav-btn" @click="prevMonth">
          <text class="nav-icon">{{ '<' }}</text>
        </view>
        <view class="month-text">{{ currentYear }}年{{ currentMonth }}月</view>
        <view class="nav-btn" @click="nextMonth">
          <text class="nav-icon">{{ '>' }}</text>
        </view>
      </view>

      <!-- 星期标题行 -->
      <view class="weekdays">
        <view class="weekday" v-for="day in weekdays" :key="day">{{ day }}</view>
      </view>

      <!-- 日历日期网格 -->
      <view class="calendar-grid">
        <!-- 月初空白占位 -->
        <view 
          class="calendar-day empty" 
          v-for="n in firstDayOfMonth" 
          :key="'empty-' + n"
        ></view>
        
        <!-- 每一天的日期格 -->
        <view 
          class="calendar-day" 
          :class="{
            'today': isToday(year, month, day),
            'selected': isSelected(year, month, day),
            'has-items': hasItems(year, month, day),
            'expired': hasExpiredItems(year, month, day),
            'expiring': hasExpiringItems(year, month, day)
          }"
          v-for="day in daysInMonth" 
          :key="day"
          @click="selectDate(year, month, day)"
        >
          <text class="day-number">{{ day }}</text>
          <view class="day-indicator" v-if="hasItems(year, month, day)">
            <view 
              class="indicator-dot" 
              :class="{
                'expired': hasExpiredItems(year, month, day),
                'expiring': hasExpiringItems(year, month, day) && !hasExpiredItems(year, month, day),
                'normal': !hasExpiredItems(year, month, day) && !hasExpiringItems(year, month, day)
              }"
            ></view>
          </view>
          <view class="item-count" v-if="getItemCount(year, month, day) > 0">
            {{ getItemCount(year, month, day) }}
          </view>
        </view>
      </view>
    </view>

    <!-- 选中日期的物品列表 -->
    <view class="items-section" v-if="selectedDate">
      <view class="section-header">
        <text class="section-title">{{ formatSelectedDate() }} 的过期物品</text>
        <text class="item-count-text">共 {{ selectedDateItems.length }} 件</text>
      </view>
      
      <view class="items-list" v-if="selectedDateItems.length > 0">
        <item-card 
          v-for="item in selectedDateItems" 
          :key="item.id"
          :item="item"
          @click="goToItemDetail(item)"
          @edit="editItem(item)"
        />
      </view>
      
      <view class="empty-state" v-else>
        <text class="empty-text">该日期没有到期的物品</text>
      </view>
    </view>
  </view>
</template>

<script>
import storage from '@/common/utils/storage.js'
import itemUtil from '@/common/utils/item.js'
import ItemCard from '@/components/item-card/item-card.vue'
import themeMixin from '@/common/mixins/theme.js'
import api from '@/common/utils/api.js'
import { isLoggedIn } from '@/common/utils/auth.js'

export default {
  mixins: [themeMixin],
  components: {
    ItemCard
  },
  data() {
    return {
      filterType: 'all', // all, personal, shared
      currentYear: new Date().getFullYear(),
      currentMonth: new Date().getMonth() + 1,
      selectedDate: null, // { year, month, day }
      itemsByDate: {}, // 按日期分组的物品集合 { '2024-01-15': [item1, item2] }
      personalItems: [],
      sharedItems: [],
      weekdays: ['日', '一', '二', '三', '四', '五', '六'],
      loading: false
    }
  },
  computed: {
    year() {
      return this.currentYear
    },
    month() {
      return this.currentMonth
    },
    daysInMonth() {
      return new Date(this.currentYear, this.currentMonth, 0).getDate()
    },
    firstDayOfMonth() {
      return new Date(this.currentYear, this.currentMonth - 1, 1).getDay()
    },
    selectedDateItems() {
      if (!this.selectedDate) return []
      const dateKey = this.buildDateKey(this.selectedDate.year, this.selectedDate.month, this.selectedDate.day)
      return this.itemsByDate[dateKey] || []
    }
  },
  onLoad() {
    this.loadData()
  },
  onShow() {
    this.loadData()
  },
  onPullDownRefresh() {
    this.loadData()
    setTimeout(() => {
      uni.stopPullDownRefresh()
    }, 500)
  },
  methods: {
    buildDateKey(year, month, day) {
      return year + '-' + String(month).padStart(2, '0') + '-' + String(day).padStart(2, '0')
    },

    loadData() {
      if (!isLoggedIn()) {
        // 未登录则使用本地缓存数据
        this.loadLocalData()
        return
      }
      this.loadApiData()
    },

    loadLocalData() {
      // 从本地存储加载数据作为兜底
      this.personalItems = storage.get('personalItems', [])
      this.sharedItems = storage.get('sharedItems', [])
      this.groupItemsByDate()
    },

    async loadApiData() {
      if (this.loading) return
      this.loading = true
      try {
        // 计算当前月份的起止日期
        var startDate = this.buildDateKey(this.currentYear, this.currentMonth, 1)
        var lastDay = new Date(this.currentYear, this.currentMonth, 0).getDate()
        var endDate = this.buildDateKey(this.currentYear, this.currentMonth, lastDay)

        var res = await api.getItemsByDate({
          type: this.filterType,
          startDate: startDate,
          endDate: endDate
        })

        if (res && res.code === 200 && res.data) {
          // 后端直接返回按日期分组的数据
          if (res.data.itemsByDate) {
            this.itemsByDate = res.data.itemsByDate
          } else if (res.data.personalItems || res.data.sharedItems) {
            // 后端返回个人和共享物品列表，前端分组
            this.personalItems = res.data.personalItems || []
            this.sharedItems = res.data.sharedItems || []
            this.groupItemsByDate()
          } else if (Array.isArray(res.data)) {
            // 后端返回物品数组，前端按日期分组
            this.itemsByDate = {}
            res.data.forEach(function(item) {
              if (!item.expiryDate) return
              var dateKey = item.expiryDate
              if (!this.itemsByDate[dateKey]) {
                this.itemsByDate[dateKey] = []
              }
              this.itemsByDate[dateKey].push(item)
            }.bind(this))
          } else {
            // 数据格式不确定，尝试当作列表处理
            this.itemsByDate = res.data
          }
        } else {
          // API 返回异常，兜底使用本地数据
          this.loadLocalData()
        }
      } catch (error) {
        console.error('加载日历数据失败:', error)
        // 出错时使用本地缓存
        this.loadLocalData()
        uni.showToast({ title: '加载数据失败，使用缓存', icon: 'none' })
      } finally {
        this.loading = false
      }
    },

    groupItemsByDate() {
      this.itemsByDate = {}
      var allItems = []
      
      if (this.filterType === 'all' || this.filterType === 'personal') {
        allItems.push.apply(allItems, this.personalItems.map(function(item) {
          return Object.assign({}, item, { type: 'personal' })
        }))
      }
      
      if (this.filterType === 'all' || this.filterType === 'shared') {
        allItems.push.apply(allItems, this.sharedItems.map(function(item) {
          return Object.assign({}, item, { type: 'shared' })
        }))
      }
      
      var self = this
      allItems.forEach(function(item) {
        if (!item.expiryDate) return
        var dateKey = item.expiryDate
        if (!self.itemsByDate[dateKey]) {
          self.itemsByDate[dateKey] = []
        }
        self.itemsByDate[dateKey].push(item)
      })
    },

    setFilter(type) {
      this.filterType = type
      this.selectedDate = null
      this.loadData()
    },

    prevMonth() {
      if (this.currentMonth === 1) {
        this.currentMonth = 12
        this.currentYear--
      } else {
        this.currentMonth--
      }
      this.loadData()
    },

    nextMonth() {
      if (this.currentMonth === 12) {
        this.currentMonth = 1
        this.currentYear++
      } else {
        this.currentMonth++
      }
      this.loadData()
    },

    isToday(year, month, day) {
      var today = new Date()
      return year === today.getFullYear() && 
             month === today.getMonth() + 1 && 
             day === today.getDate()
    },

    isSelected(year, month, day) {
      if (!this.selectedDate) return false
      return this.selectedDate.year === year && 
             this.selectedDate.month === month && 
             this.selectedDate.day === day
    },

    hasItems(year, month, day) {
      var dateKey = this.buildDateKey(year, month, day)
      return this.itemsByDate[dateKey] && this.itemsByDate[dateKey].length > 0
    },

    hasExpiredItems(year, month, day) {
      var dateKey = this.buildDateKey(year, month, day)
      var items = this.itemsByDate[dateKey] || []
      var today = new Date()
      today.setHours(0, 0, 0, 0)
      return items.some(function(item) {
        var expiryDate = new Date(item.expiryDate)
        expiryDate.setHours(0, 0, 0, 0)
        return expiryDate < today
      })
    },

    hasExpiringItems(year, month, day) {
      var dateKey = this.buildDateKey(year, month, day)
      var items = this.itemsByDate[dateKey] || []
      var today = new Date()
      today.setHours(0, 0, 0, 0)
      var sevenDaysLater = new Date(today)
      sevenDaysLater.setDate(sevenDaysLater.getDate() + 7)
      return items.some(function(item) {
        var expiryDate = new Date(item.expiryDate)
        expiryDate.setHours(0, 0, 0, 0)
        return expiryDate >= today && expiryDate <= sevenDaysLater
      })
    },

    getItemCount(year, month, day) {
      var dateKey = this.buildDateKey(year, month, day)
      return this.itemsByDate[dateKey] ? this.itemsByDate[dateKey].length : 0
    },

    selectDate(year, month, day) {
      this.selectedDate = { year: year, month: month, day: day }
    },

    formatSelectedDate() {
      if (!this.selectedDate) return ''
      return this.selectedDate.year + '年' + this.selectedDate.month + '月' + this.selectedDate.day + '日'
    },

    goToItemDetail(item) {
      if (item.type === 'shared') {
        uni.navigateTo({
          url: '/pages/personal/edit-item?id=' + item.id + '&spaceId=' + item.spaceId + '&type=shared'
        })
      } else {
        uni.navigateTo({
          url: '/pages/personal/edit-item?id=' + item.id
        })
      }
    },

    editItem(item) {
      this.goToItemDetail(item)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/common/style/common.scss';

.container {
  padding: 20rpx;
  min-height: 100vh;
  padding-bottom: 200rpx;
  background: var(--primary-bg);
  transition: background-color 0.3s ease;
}

/* 筛选栏 */
.filter-container {
  margin-bottom: 30rpx;
}

.filter-bar {
  display: flex;
  gap: 20rpx;
  background: var(--card-bg-solid);
  border-radius: 40rpx;
  padding: 10rpx;
  border: 2rpx solid var(--card-border);
}

.filter-item {
  flex: 1;
  padding: 16rpx 32rpx;
  text-align: center;
  border-radius: 32rpx;
  font-size: 28rpx;
  color: var(--text-primary);
  transition: all 0.3s;
  font-weight: 500;
  
  &:active {
    transform: scale(0.98);
  }
  
  &.active {
    background: linear-gradient(135deg, var(--accent-blue), var(--accent-teal));
    color: #ffffff;
    box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3);
    font-weight: 600;
  }
}

/* 日历容器 */
.calendar-container {
  background: var(--card-bg-solid);
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  border: 2rpx solid var(--card-border);
  box-shadow: 0 8rpx 24rpx var(--shadow-color);
}

/* 月份导航 */
.month-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30rpx;
}

.nav-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--card-bg-solid);
  border: 2rpx solid var(--card-border);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(37, 99, 235, 0.2), transparent);
    transform: translate(-50%, -50%);
    transition: width 0.3s ease, height 0.3s ease;
  }
  
  &:active {
    background: var(--active-bg);
    border-color: var(--accent-blue);
    transform: scale(0.95);
    
    &::before {
      width: 200%;
      height: 200%;
    }
  }
}

.nav-icon {
  font-size: 40rpx;
  color: var(--text-primary);
  font-weight: 600;
  line-height: 1;
  position: relative;
  z-index: 1;
}

.month-text {
  font-size: 38rpx;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 1rpx;
  background: linear-gradient(135deg, var(--text-primary), var(--text-secondary));
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 星期标题 */
.weekdays {
  display: flex;
  margin-bottom: 20rpx;
}

.weekday {
  flex: 1;
  text-align: center;
  font-size: 24rpx;
  color: var(--text-secondary);
  font-weight: 500;
}

/* 日历网格 */
.calendar-grid {
  display: flex;
  flex-wrap: wrap;
}

.calendar-day {
  width: calc(100% / 7);
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  border-radius: 16rpx;
  margin: 4rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  background: transparent;
  border: 2rpx solid transparent;
  
  &.empty {
    visibility: hidden;
    pointer-events: none;
  }
  
  &.today {
    background: rgba(37, 99, 235, 0.12);
    border: 2rpx solid var(--accent-blue);
    box-shadow: 0 0 0 2rpx rgba(37, 99, 235, 0.1);
    
    .day-number {
      color: var(--accent-blue);
      font-weight: 700;
    }
  }
  
  &.selected {
    background: linear-gradient(135deg, var(--accent-blue), var(--accent-teal));
    border: 2rpx solid transparent;
    box-shadow: 
      0 4rpx 16rpx -4rpx var(--glow-blue),
      0 2rpx 8rpx -2rpx var(--shadow-color);
    transform: scale(1.05);
    
    .day-number {
      color: white;
      font-weight: 700;
    }
    
    .item-count {
      color: rgba(255, 255, 255, 0.9);
    }
    
    .day-indicator {
      opacity: 0;
    }
  }
  
  &.has-items {
    &:hover {
      background: var(--hover-bg);
      border-color: var(--card-border);
    }
    
    &:active {
      transform: scale(0.95);
    }
  }
  
  &.expired {
    .day-number {
      color: var(--accent-rose);
      font-weight: 700;
    }
    
    .indicator-dot.expired {
      box-shadow: 0 0 8rpx rgba(225, 29, 72, 0.5);
    }
  }
  
  &.expiring {
    .day-number {
      color: var(--accent-amber);
      font-weight: 700;
    }
    
    .indicator-dot.expiring {
      box-shadow: 0 0 8rpx rgba(245, 158, 11, 0.5);
    }
  }
}

.day-number {
  font-size: 28rpx;
  color: var(--text-primary);
  margin-bottom: 4rpx;
}

.day-indicator {
  position: absolute;
  bottom: 8rpx;
  left: 50%;
  transform: translateX(-50%);
}

.indicator-dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  transition: all 0.3s ease;
  
  &.expired {
    background: var(--accent-rose);
    box-shadow: 0 0 6rpx rgba(225, 29, 72, 0.4);
  }
  
  &.expiring {
    background: var(--accent-amber);
    box-shadow: 0 0 6rpx rgba(245, 158, 11, 0.4);
  }
  
  &.normal {
    background: var(--accent-blue);
    box-shadow: 0 0 6rpx rgba(37, 99, 235, 0.3);
  }
}

.item-count {
  font-size: 20rpx;
  color: var(--text-secondary);
  margin-top: 4rpx;
}

/* 物品列表区域 */
.items-section {
  background: var(--card-bg-solid);
  border-radius: 24rpx;
  padding: 30rpx;
  border: 2rpx solid var(--card-border);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.item-count-text {
  font-size: 24rpx;
  color: var(--text-secondary);
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.empty-state {
  text-align: center;
  padding: 80rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: var(--text-secondary);
}

/* 浅色模式 */
.light-mode .filter-item.active {
  background: linear-gradient(135deg, #3b82f6, #14b8a6) !important;
}

.light-mode .calendar-day.selected {
  background: linear-gradient(135deg, #3b82f6, #14b8a6) !important;
}
</style>
