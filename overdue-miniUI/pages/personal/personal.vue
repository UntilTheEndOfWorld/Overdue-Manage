<template>
  <view class="container" :class="themeClass">
    <view class="page-hero">
      <view class="hero-copy">
        <text class="hero-title">物品管理</text>
        <text class="hero-subtitle">关注临期和过期物品，及时处理库存</text>
      </view>
      <view class="hero-add" @click="addItem">
        <text class="hero-add-icon">+</text>
        <text>添加</text>
      </view>
    </view>

    <view class="overview-card">
      <view class="overview-main">
        <text class="overview-num">{{ itemStats.total || 0 }}</text>
        <text class="overview-label">全部物品</text>
      </view>
      <view class="overview-side">
        <view class="overview-pill normal">
          <text class="overview-pill-num">{{ itemStats.normal || 0 }}</text>
          <text>正常</text>
        </view>
        <view class="overview-pill near">
          <text class="overview-pill-num">{{ itemStats.near || 0 }}</text>
          <text>临期</text>
        </view>
        <view class="overview-pill expired">
          <text class="overview-pill-num">{{ itemStats.expired || 0 }}</text>
          <text>过期</text>
        </view>
      </view>
    </view>

    <view class="search-panel">
      <text class="search-icon">⌕</text>
      <input 
        class="search-input" 
        v-model="searchKeyword" 
        placeholder="搜索物品名称"
        @input="onSearch"
      />
    </view>

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
      <view class="empty-visual">📦</view>
      <text class="empty-title">暂无匹配物品</text>
      <text class="empty-desc">换个筛选条件，或添加一个新的物品</text>
      <view class="empty-action" @click="addItem">添加物品</view>
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
  min-height: 100vh;
  padding: 36rpx 30rpx 200rpx;
  background: var(--primary-bg);
  transition: background-color 0.3s ease;
}

.page-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24rpx;
  margin-bottom: 28rpx;
}

.hero-copy {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  min-width: 0;
}

.hero-title {
  color: var(--text-primary);
  font-size: 46rpx;
  font-weight: 800;
  line-height: 1.2;
}

.hero-subtitle {
  color: var(--text-secondary);
  font-size: 25rpx;
  line-height: 1.35;
}

.hero-add {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  min-width: 146rpx;
  height: 70rpx;
  padding: 0 24rpx;
  border-radius: 999rpx;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 700;
  background: linear-gradient(135deg, #2563eb, #0d9488);
  box-shadow: 0 14rpx 34rpx rgba(37, 99, 235, 0.22);
  box-sizing: border-box;

  &:active {
    transform: scale(0.98);
  }
}

.hero-add-icon {
  font-size: 34rpx;
  line-height: 1;
  margin-top: -2rpx;
}

.overview-card {
  display: flex;
  align-items: stretch;
  gap: 22rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  border-radius: 30rpx;
  background: var(--card-bg-solid);
  border: 2rpx solid var(--card-border);
  box-shadow: 0 12rpx 34rpx -20rpx var(--shadow-color);
  box-sizing: border-box;
}

.overview-main {
  width: 190rpx;
  min-height: 148rpx;
  border-radius: 24rpx;
  background: linear-gradient(145deg, rgba(37, 99, 235, 0.14), rgba(13, 148, 136, 0.12));
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  flex-shrink: 0;
}

.overview-num {
  color: var(--text-primary);
  font-size: 62rpx;
  font-weight: 800;
  line-height: 1;
}

.overview-label {
  margin-top: 8rpx;
  color: var(--text-secondary);
  font-size: 24rpx;
  font-weight: 600;
}

.overview-side {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 12rpx;
}

.overview-pill {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 40rpx;
  padding: 10rpx 18rpx;
  border-radius: 18rpx;
  color: var(--text-secondary);
  font-size: 24rpx;
  font-weight: 600;
}

.overview-pill-num {
  color: var(--text-primary);
  font-size: 28rpx;
  font-weight: 800;
}

.overview-pill.normal {
  background: rgba(5, 150, 105, 0.12);
}

.overview-pill.near {
  background: rgba(245, 158, 11, 0.14);
}

.overview-pill.expired {
  background: rgba(225, 29, 72, 0.12);
}

.search-panel {
  display: flex;
  align-items: center;
  height: 84rpx;
  padding: 0 26rpx;
  margin-bottom: 22rpx;
  border-radius: 24rpx;
  background: var(--card-bg-solid);
  border: 2rpx solid var(--card-border);
  box-sizing: border-box;
}

.search-icon {
  width: 44rpx;
  color: var(--text-tertiary);
  font-size: 34rpx;
  line-height: 1;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  min-width: 0;
  height: 80rpx;
  padding: 0;
  margin: 0;
  background: transparent;
  border: none;
  color: var(--text-primary);
  font-size: 28rpx;
  line-height: 80rpx;
}

.filter-group {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 14rpx;
  margin-bottom: 28rpx;
  overflow-x: auto;
  white-space: nowrap;
}

.filter-chip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  min-width: 128rpx;
  height: 66rpx;
  padding: 0 24rpx;
  border-radius: 999rpx;
  font-size: 26rpx;
  font-weight: 700;
  background: var(--card-bg-solid);
  color: var(--text-secondary);
  border: 2rpx solid var(--card-border);
  flex-shrink: 0;
  box-sizing: border-box;
  transition: background 0.25s ease, color 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;

  &:active {
    transform: scale(0.98);
  }

  &:not(.active) {
    background: rgba(148, 163, 184, 0.1);
    border-color: transparent;
  }

  &.active {
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

.container:not(.light-mode) .filter-chip:not(.active) {
  background: rgba(255, 255, 255, 0.06);
  color: var(--text-secondary);
  border-color: var(--card-border);
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 22rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 90rpx 36rpx;
  margin-top: 18rpx;
  border-radius: 30rpx;
  background: var(--card-bg-solid);
  border: 2rpx dashed var(--card-border);
  color: var(--text-secondary);
  text-align: center;
  box-sizing: border-box;
}

.empty-visual {
  font-size: 58rpx;
  margin-bottom: 18rpx;
}

.empty-title {
  color: var(--text-primary);
  font-size: 31rpx;
  font-weight: 800;
  margin-bottom: 10rpx;
}

.empty-desc {
  color: var(--text-secondary);
  font-size: 25rpx;
  line-height: 1.5;
}

.empty-action {
  margin-top: 28rpx;
  padding: 18rpx 34rpx;
  border-radius: 999rpx;
  color: #ffffff;
  font-size: 26rpx;
  font-weight: 700;
  background: linear-gradient(135deg, #2563eb, #0d9488);
}

.filter-num {
  margin-left: 4rpx;
  font-size: 22rpx;
  opacity: 0.9;
}

.light-mode .filter-chip.active .filter-num {
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

.light-mode.container {
  background: linear-gradient(180deg, #f6f9ff 0%, var(--primary-bg) 42%);
}

.light-mode .overview-card,
.light-mode .search-panel,
.light-mode .empty-state {
  background: rgba(255, 255, 255, 0.94);
  border-color: rgba(148, 163, 184, 0.18);
  box-shadow: 0 18rpx 48rpx -34rpx rgba(15, 23, 42, 0.35);
}

.light-mode .filter-chip:not(.active) {
  background: rgba(226, 232, 240, 0.72) !important;
  color: #64748b !important;
  border-color: transparent !important;
}

.light-mode .filter-chip.active {
  background: #2563eb !important;
  color: #ffffff !important;
  box-shadow: 0 12rpx 28rpx rgba(37, 99, 235, 0.24) !important;
}
</style>
