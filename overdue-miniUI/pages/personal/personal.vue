<template>
  <view class="container" :class="themeClass">
    <!-- 搜索框 -->
    <view class="search-bar">
      <input 
        class="search-input" 
        v-model="searchKeyword" 
        placeholder="🔍 搜索物品名称…"
        @input="onSearch"
      />
    </view>

    <!-- 筛选胶囊 + 添加（参考清新胶囊风格） -->
    <view class="filter-group">
      <view 
        class="filter-chip" 
        :class="{ active: filter === 'all' }"
        @click="setFilter('all')"
      >
        <text>全部</text>
        <text v-if="itemStats && itemStats.total > 0" class="filter-num">{{ itemStats.total }}</text>
      </view>
      <view 
        class="filter-chip" 
        :class="{ active: filter === 'normal' }"
        @click="setFilter('normal')"
      >
        <view class="dot dot-green"></view>
        <text>正常</text>
        <text v-if="itemStats && itemStats.normal > 0" class="filter-num">{{ itemStats.normal }}</text>
      </view>
      <view 
        class="filter-chip" 
        :class="{ active: filter === 'near' }"
        @click="setFilter('near')"
      >
        <view class="dot dot-yellow"></view>
        <text>临期</text>
        <text v-if="itemStats && itemStats.near > 0" class="filter-num">{{ itemStats.near }}</text>
      </view>
      <view 
        class="filter-chip" 
        :class="{ active: filter === 'expired' }"
        @click="setFilter('expired')"
      >
        <view class="dot dot-red"></view>
        <text>过期</text>
        <text v-if="itemStats && itemStats.expired > 0" class="filter-num">{{ itemStats.expired }}</text>
      </view>
      <view class="filter-chip filter-chip-add" @click="addItem">
        <text class="icon-plus">+</text>
        <text>添加</text>
      </view>
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
        memberUtil.handlePersonalQuotaExceeded()
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

/* 筛选行：胶囊 + 可换行 */
.filter-group {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 40rpx;
}

.filter-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 20rpx 36rpx;
  min-height: 64rpx;
  border-radius: 999rpx;
  font-size: 28rpx;
  font-weight: 500;
  letter-spacing: 0.5rpx;
  background: var(--card-bg-solid);
  color: var(--text-secondary);
  border: 2rpx solid var(--card-border);
  box-sizing: border-box;
  transition: background 0.25s ease, color 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
  
  &:active {
    transform: scale(0.98);
  }
  
  &:not(.filter-chip-add):not(.active) {
    background: rgba(240, 244, 250, 0.35);
    color: #94a3b8;
    border-color: transparent;
    box-shadow: none;
  }
  
  &.active:not(.filter-chip-add) {
    background: #5e8cd9;
    color: #ffffff;
    border-color: transparent;
    box-shadow: 0 12rpx 32rpx rgba(94, 140, 217, 0.3);
    font-weight: 600;
  }
}

.filter-chip .dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  flex-shrink: 0;
  transition: background 0.2s ease;
}

.filter-chip:not(.active) .dot-green {
  background: #80c6a8;
}

.filter-chip:not(.active) .dot-yellow {
  background: #e8c87a;
}

.filter-chip:not(.active) .dot-red {
  background: #e68a8a;
}

.filter-chip.active .dot-green,
.filter-chip.active .dot-yellow,
.filter-chip.active .dot-red {
  background: #ffffff;
}

/* 渐变添加按钮，同行时靠右 */
.filter-chip-add {
  margin-left: auto;
  padding: 20rpx 40rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #ffffff !important;
  border: none !important;
  background: linear-gradient(135deg, #48c9b0, #1abc9c) !important;
  box-shadow: 0 12rpx 36rpx rgba(26, 188, 156, 0.35);
  
  &:active {
    transform: scale(0.97);
    box-shadow: 0 8rpx 28rpx rgba(26, 188, 156, 0.4);
  }
}

.filter-chip-add .icon-plus {
  font-size: 36rpx;
  font-weight: 300;
  line-height: 1;
}

/* 深色主题：未选中胶囊略提亮，选中保持品牌蓝 */
.container:not(.light-mode) .filter-chip:not(.filter-chip-add):not(.active) {
  background: rgba(255, 255, 255, 0.06);
  color: var(--text-secondary);
  border-color: var(--card-border);
}

/* 浅色主题：与参考稿一致的无边框灰底未选中 */
.light-mode .filter-chip:not(.filter-chip-add):not(.active) {
  background: #f0f4fa !important;
  color: #8796b3 !important;
  border: none !important;
  box-shadow: none !important;
}

.light-mode .filter-chip.active:not(.filter-chip-add) {
  background: #5e8cd9 !important;
  color: #ffffff !important;
  border: none !important;
  box-shadow: 0 12rpx 32rpx rgba(94, 140, 217, 0.3) !important;
}

.light-mode .filter-chip-add {
  background: linear-gradient(135deg, #48c9b0, #1abc9c) !important;
  box-shadow: 0 12rpx 36rpx rgba(26, 188, 156, 0.35) !important;
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

/* 浅色模式搜索框：与参考稿一致的浅底 + 柔和描边 */
.light-mode .search-input {
  background: #fafcff !important;
  border: 3rpx solid #e7edf4 !important;
  color: #1e2a44 !important;
  box-shadow: none !important;
}

.light-mode .search-input::placeholder {
  color: #b7c4db !important;
}

.light-mode .search-input:focus {
  border-color: #7ea3e0 !important;
  background: #ffffff !important;
  box-shadow: 0 0 0 8rpx rgba(126, 163, 224, 0.12) !important;
}

.empty-state {
  text-align: center;
  padding: 100rpx 0;
  color: var(--text-secondary);
  font-size: 32rpx;
}

.filter-num {
  margin-left: 4rpx;
  font-size: 22rpx;
  opacity: 0.9;
}

.light-mode .filter-chip.active:not(.filter-chip-add) .filter-num {
  opacity: 0.95;
  color: rgba(255, 255, 255, 0.92);
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
