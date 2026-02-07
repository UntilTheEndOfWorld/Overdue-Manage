<template>
  <view class="container" :class="themeClass">
    <!-- ???? -->
    <view class="filter-container">
      <view class="filter-bar">
        <view 
          class="filter-item" 
          :class="{ active: filterType === 'all' }"
          @click="setFilter('all')"
        >
          ???
        </view>
        <view 
          class="filter-item" 
          :class="{ active: filterType === 'personal' }"
          @click="setFilter('personal')"
        >
          ????
        </view>
        <view 
          class="filter-item" 
          :class="{ active: filterType === 'shared' }"
          @click="setFilter('shared')"
        >
          ????
        </view>
      </view>
    </view>

    <!-- ??????? -->
    <view class="calendar-container">
      <!-- ??????? -->
      <view class="month-nav">
        <view class="nav-btn" @click="prevMonth">
          <text class="nav-icon">?</text>
        </view>
        <view class="month-text">{{ currentYear }}??{{ currentMonth }}??</view>
        <view class="nav-btn" @click="nextMonth">
          <text class="nav-icon">?</text>
        </view>
      </view>

      <!-- ??????? -->
      <view class="weekdays">
        <view class="weekday" v-for="day in weekdays" :key="day">{{ day }}</view>
      </view>

      <!-- ???????? -->
      <view class="calendar-grid">
        <!-- ?????? -->
        <view 
          class="calendar-day empty" 
          v-for="n in firstDayOfMonth" 
          :key="'empty-' + n"
        ></view>
        
        <!-- ???????? -->
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

    <!-- ??????????????? -->
    <view class="items-section" v-if="selectedDate">
      <view class="section-header">
        <text class="section-title">{{ formatSelectedDate() }} 的过期物品</text>
        <text class="item-count-text">?? {{ selectedDateItems.length }} ??</text>
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
        <text class="empty-text">???????????????</text>
      </view>
    </view>
  </view>
</template>

<script>
import storage from '@/common/utils/storage.js'
import itemUtil from '@/common/utils/item.js'
import ItemCard from '@/components/item-card/item-card.vue'
import themeMixin from '@/common/mixins/theme.js'
import { userAPI } from '@/common/utils/api.js'

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
      itemsByDate: {}, // ????????????? { '2024-01-15': [item1, item2] }
      personalItems: [],
      sharedItems: [],
      weekdays: ['??', '?', '??', '??', '??', '??', '??'],
      useMockData: true
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
      const dateKey = `${this.selectedDate.year}-${String(this.selectedDate.month).padStart(2, '0')}-${String(this.selectedDate.day).padStart(2, '0')}`
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
    loadData() {
      if (this.useMockData) {
        this.loadMockData()
      } else {
        this.loadApiData()
      }
    },
    loadMockData() {
      // ??????????
      this.personalItems = storage.get('personalItems', [])
      
      // ??????????
      const allSharedItems = storage.get('sharedItems', [])
      this.sharedItems = allSharedItems
      
      // ?????????
      this.groupItemsByDate()
    },
    async loadApiData() {
      try {
        // TODO: ????API??????????
        // const personalItems = await userAPI.getPersonalItems()
        // const sharedItems = await userAPI.getSharedItems()
        // this.personalItems = personalItems
        // this.sharedItems = sharedItems
        // this.groupItemsByDate()
      } catch (error) {
        console.error('???????????:', error)
        uni.showToast({ title: '???????????', icon: 'none' })
      }
    },
    groupItemsByDate() {
      this.itemsByDate = {}
      const allItems = []
      
      if (this.filterType === 'all' || this.filterType === 'personal') {
        allItems.push(...this.personalItems.map(item => ({ ...item, type: 'personal' })))
      }
      
      if (this.filterType === 'all' || this.filterType === 'shared') {
        allItems.push(...this.sharedItems.map(item => ({ ...item, type: 'shared' })))
      }
      
      allItems.forEach(item => {
        if (!item.expiryDate) return
        const dateKey = item.expiryDate
        if (!this.itemsByDate[dateKey]) {
          this.itemsByDate[dateKey] = []
        }
        this.itemsByDate[dateKey].push(item)
      })
    },
    setFilter(type) {
      this.filterType = type
      this.groupItemsByDate()
      this.selectedDate = null
    },
    prevMonth() {
      if (this.currentMonth === 1) {
        this.currentMonth = 12
        this.currentYear--
      } else {
        this.currentMonth--
      }
    },
    nextMonth() {
      if (this.currentMonth === 12) {
        this.currentMonth = 1
        this.currentYear++
      } else {
        this.currentMonth++
      }
    },
    isToday(year, month, day) {
      const today = new Date()
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
      const dateKey = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
      return this.itemsByDate[dateKey] && this.itemsByDate[dateKey].length > 0
    },
    hasExpiredItems(year, month, day) {
      const dateKey = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
      const items = this.itemsByDate[dateKey] || []
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      return items.some(item => {
        const expiryDate = new Date(item.expiryDate)
        expiryDate.setHours(0, 0, 0, 0)
        return expiryDate < today
      })
    },
    hasExpiringItems(year, month, day) {
      const dateKey = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
      const items = this.itemsByDate[dateKey] || []
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      const sevenDaysLater = new Date(today)
      sevenDaysLater.setDate(sevenDaysLater.getDate() + 7)
      return items.some(item => {
        const expiryDate = new Date(item.expiryDate)
        expiryDate.setHours(0, 0, 0, 0)
        return expiryDate >= today && expiryDate <= sevenDaysLater
      })
    },
    getItemCount(year, month, day) {
      const dateKey = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
      return this.itemsByDate[dateKey] ? this.itemsByDate[dateKey].length : 0
    },
    selectDate(year, month, day) {
      this.selectedDate = { year, month, day }
    },
    formatSelectedDate() {
      if (!this.selectedDate) return ''
      return `${this.selectedDate.year}年${this.selectedDate.month}月${this.selectedDate.day}日`
    },
    goToItemDetail(item) {
      if (item.type === 'shared') {
        uni.navigateTo({
          url: `/pages/personal/edit-item?id=${item.id}&spaceId=${item.spaceId}&type=shared`
        })
      } else {
        uni.navigateTo({
          url: `/pages/personal/edit-item?id=${item.id}`
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

/* ???? */
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

/* ???????? */
.calendar-container {
  background: var(--card-bg-solid);
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  border: 2rpx solid var(--card-border);
  box-shadow: 0 8rpx 24rpx var(--shadow-color);
}

/* ??????? */
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
  
  /* ?????? */
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

/* ??????? */
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

/* ???????? */
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

/* ??????????? */
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

/* ???? */
.light-mode .filter-item.active {
  background: linear-gradient(135deg, #3b82f6, #14b8a6) !important;
}

.light-mode .calendar-day.selected {
  background: linear-gradient(135deg, #3b82f6, #14b8a6) !important;
}
</style>
