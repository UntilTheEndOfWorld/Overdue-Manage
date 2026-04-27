<template>
  <view class="container" :class="themeClass">
    <!-- 搜索框 -->
    <view class="search-bar">
      <input 
        class="search-input" 
        v-model="searchKeyword" 
        placeholder="搜索物品名称..."
        @input="onSearch"
      />
    </view>

    <!-- 筛选器和添加按钮 -->
    <view class="filter-container">
      <view class="filter-bar">
        <view 
          class="filter-item" 
          :class="{ active: filter === 'all' }"
          @click="setFilter('all')"
        >
          全部<text v-if="itemStats.total > 0" class="filter-num">{{ itemStats.total }}</text>
        </view>
        <view 
          class="filter-item" 
          :class="{ active: filter === 'normal' }"
          @click="setFilter('normal')"
        >
          正常<text v-if="itemStats.normal > 0" class="filter-num">{{ itemStats.normal }}</text>
        </view>
        <view 
          class="filter-item" 
          :class="{ active: filter === 'near' }"
          @click="setFilter('near')"
        >
          即将过期<text v-if="itemStats.near > 0" class="filter-num">{{ itemStats.near }}</text>
        </view>
        <view 
          class="filter-item" 
          :class="{ active: filter === 'expired' }"
          @click="setFilter('expired')"
        >
          已过期<text v-if="itemStats.expired > 0" class="filter-num">{{ itemStats.expired }}</text>
        </view>
      </view>
      <view class="add-btn" @click="addItem">+</view>
    </view>

    <!-- 物品列表 -->
    <view class="item-list" v-if="filteredItems.length > 0" :key="listRenderKey">
      <item-card 
        v-for="item in filteredItems" 
        :key="item._renderKey"
        :item="item"
        :show-process="true"
        @click="editItem"
        @edit="editItem"
        @log="showItemLogs"
        @process="handleProcessItem"
      />
    </view>

    <view class="empty-state" v-if="filteredItems.length === 0 && (!loadError || items.length > 0)">
      <text>暂无物品</text>
    </view>

    <view class="error-state" v-if="loadError && items.length === 0">
      <text class="error-text">加载失败，请检查网络后重试</text>
      <button class="retry-btn" type="default" size="mini" @click="retryLoad">重试</button>
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
import memberUtil from '@/common/utils/member.js'

export default {
  mixins: [themeMixin],
  components: {
    ItemCard
  },
  data() {
    return {
      items: [],
      filter: 'all',
      searchKeyword: '',
      loading: false,
      loadError: false,
      renderTick: 0
    }
  },
  computed: {
    itemStats() {
      return itemUtil.getItemStats(Array.isArray(this.items) ? this.items : [])
    },
    listRenderKey() {
      return [this.filter, this.searchKeyword, this.filteredItems.length, this.renderTick].join('|')
    },
    filteredItems() {
      var result = this.items
      if (this.searchKeyword) {
        result = itemUtil.searchItems(result, this.searchKeyword)
      }
      return itemUtil.filterItems(result, this.filter)
    }
  },
  onLoad() {
    this.loadItems()
  },
  onShow() {
    this.loadItems()
  },
  onPullDownRefresh() {
    this.loadItems()
    setTimeout(function() {
      uni.stopPullDownRefresh()
    }, 500)
  },
  methods: {
    retryLoad() {
      this.loadError = false
      this.loadItems()
    },
    async loadItems() {
      if (!isLoggedIn()) return
      if (this.loading) return
      this.loading = true
      this.loadError = false
      try {
        var res = await api.getPersonalItems()
        if (res && res.code === 200 && res.data) {
          var rawItems = Array.isArray(res.data) ? res.data : (res.data.rows || res.data.list || [])
          this.items = this.normalizeItems(rawItems)
          storage.set('personalItems', this.items)
        } else {
          this.loadError = true
          this.items = []
        }
      } catch (error) {
        console.error('加载个人物品失败:', error)
        this.loadError = true
        this.items = this.normalizeItems(storage.get('personalItems', []) || [])
        if (!this.items.length) {
          uni.showToast({ title: '网络异常', icon: 'none' })
        } else {
          uni.showToast({ title: '已显示本地缓存', icon: 'none' })
        }
      } finally {
        this.renderTick++
        this.loading = false
      }
    },
    setFilter(type) {
      this.filter = type
      this.renderTick++
    },
    onSearch() {
      // 搜索逻辑已在computed中处理；递增 tick 强制列表区重建，避免小程序渲染残影
      this.renderTick++
    },
    addItem() {
      // 检查额度
      if (!memberUtil || typeof memberUtil.canAddItemWithAd !== 'function') {
        uni.showToast({ title: '会员能力加载失败', icon: 'none' })
        return
      }
      if (!memberUtil.canAddItemWithAd()) {
        // 额度已用完，跳转到升级页面
        uni.navigateTo({
          url: '/pages/member/upgrade'
        })
        return
      }
      uni.navigateTo({
        url: '/pages/personal/add-item'
      })
    },
    editItem(item) {
      uni.navigateTo({
        url: `/pages/personal/edit-item?id=${item.id}`
      })
    },
    showLogs() {
      uni.navigateTo({
        url: '/pages/personal/logs'
      })
    },
    // 显示物品日志
    showItemLogs(item) {
      uni.navigateTo({
        url: `/pages/personal/logs?itemId=${item.id}`
      })
    },
    handleProcessItem(item) {
      var itemId = item && item.id
      if (!itemId) {
        uni.showToast({ title: '物品信息异常', icon: 'none' })
        return
      }
      var self = this
      uni.showModal({
        title: '处理过期物品',
        content: '处理后将移入删除列表，并释放1个可用额度，确定处理吗？',
        success: function(res) {
          if (!res.confirm) return
          api.deletePersonalItem(itemId, 'process').then(function() {
            uni.showToast({ title: '处理完成', icon: 'success' })
            self.loadItems()
          }).catch(function(error) {
            console.error('处理过期物品失败:', error)
            uni.showToast({ title: (error && error.message) || '处理失败', icon: 'none' })
          })
        }
      })
    },
    normalizeItems(items) {
      if (!Array.isArray(items)) {
        return []
      }
      return items.map(function(item, index) {
        var id = item && item.id != null ? String(item.id) : ''
        var fallbackKey = [item && item.name ? item.name : '', item && item.expiryDate ? item.expiryDate : '', index].join('_')
        return Object.assign({}, item, {
          id: id || item.id,
          _renderKey: id || fallbackKey
        })
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

/* 筛选器和添加按钮容器 */
.filter-container {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 40rpx;
}

.add-btn {
  background: linear-gradient(135deg, var(--accent-blue), var(--accent-teal));
  color: white;
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  box-shadow: 
    0 8rpx 24rpx -8rpx var(--glow-blue),
    0 4rpx 12rpx -4rpx var(--shadow-color);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  
  /* 微妙的光泽效果 */
  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.3), transparent);
    transform: translate(-50%, -50%);
    transition: width 0.3s ease, height 0.3s ease;
  }
  
  &:active {
    transform: scale(0.95) translateY(2rpx);
    box-shadow: 
      0 4rpx 12rpx -4rpx var(--glow-blue),
      0 2rpx 6rpx -2rpx var(--shadow-color);
    
    &::before {
      width: 200%;
      height: 200%;
    }
  }
}

/* 浅色模式下的添加按钮 - 与首页共享空间添加按钮一致 */
.light-mode .add-btn {
  background: linear-gradient(135deg, #3b82f6, #14b8a6) !important;
  box-shadow: 0 8rpx 20rpx rgba(59, 130, 246, 0.3) !important;
}


.filter-bar {
  display: flex;
  gap: 20rpx;
  flex: 1;
}

.filter-item {
  padding: 20rpx 32rpx;
  background: var(--card-bg-solid);
  border-radius: 40rpx;
  font-size: 28rpx;
  color: var(--text-secondary);
  border: 2rpx solid var(--card-border);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
  cursor: pointer;
  position: relative;
  
  &:active {
    transform: scale(0.98);
  }
  
  &.active {
    background: linear-gradient(135deg, var(--accent-blue), var(--accent-teal));
    color: #ffffff;
    border-color: transparent;
    box-shadow: 
      0 4rpx 16rpx -4rpx var(--glow-blue),
      0 2rpx 8rpx -2rpx var(--shadow-color);
    font-weight: 600;
    
    /* 光泽效果 */
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 1rpx;
      background: linear-gradient(90deg, 
        transparent, 
        rgba(255, 255, 255, 0.3), 
        transparent
      );
      border-radius: 40rpx 40rpx 0 0;
    }
  }
}

/* 浅色模式下的筛选按钮 */
.light-mode .filter-item {
  background: #ffffff !important;
  border: 2rpx solid rgba(0, 0, 0, 0.1) !important;
  color: #1e293b !important;
  box-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.05) !important;
  font-weight: 500 !important;
}

/* 浅色模式下选中状态 - 浅米色/米黄色背景 + 深色文字，参考图片中的焦糖色按钮 */
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

.item-card {
  background: white;
  border-radius: 24rpx;
  padding: 36rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.05);
  border-left: 10rpx solid #6a89cc;
}

.item-card.expired {
  border-left-color: #ff7979;
}

.item-card.near-expiry {
  border-left-color: #ffbe76;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.item-name {
  font-weight: 600;
  font-size: 36rpx;
  color: #2c3e50;
}

.edit-btn {
  background-color: transparent;
  border: none;
  color: #6a89cc;
  font-size: 28rpx;
  padding: 10rpx 20rpx;
  border-radius: 10rpx;
}

.item-date {
  display: flex;
  justify-content: space-between;
  margin-top: 16rpx;
  font-size: 26rpx;
}

.date-item {
  display: flex;
  flex-direction: column;
}

.date-label {
  color: #7f8c8d;
  font-size: 24rpx;
  margin-bottom: 8rpx;
}

.date-value {
  font-weight: 500;
  color: #2c3e50;
  font-size: 26rpx;
}

.expiry-status {
  display: inline-block;
  padding: 6rpx 20rpx;
  border-radius: 40rpx;
  font-size: 24rpx;
  font-weight: 500;
  margin-top: 16rpx;
}

.status-normal {
  background-color: #d1f7c4;
  color: #2e7d32;
}

.status-near {
  background-color: #fff4c6;
  color: #f39c12;
}

.status-expired {
  background-color: #ffeaea;
  color: #e74c3c;
}

.search-bar {
  margin-bottom: 30rpx;
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

.empty-state {
  text-align: center;
  padding: 100rpx 0;
  color: var(--text-secondary);
  font-size: 32rpx;
}

.filter-num {
  margin-left: 6rpx;
  font-size: 22rpx;
  opacity: 0.85;
}

.error-state {
  text-align: center;
  padding: 80rpx 40rpx;
  color: var(--text-secondary);
}

.error-text {
  display: block;
  font-size: 28rpx;
  margin-bottom: 24rpx;
}

.retry-btn {
  margin-top: 8rpx;
}
</style>
