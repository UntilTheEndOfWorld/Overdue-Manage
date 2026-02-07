<template>
  <view class="container" :class="themeClass">

    <!-- 免费额度显示 -->
    <view class="quota-bar" v-if="!memberUtil.isMember()">
      <view class="quota-info">
        <text class="quota-text">{{ usedQuota }}/{{ FREE_QUOTA }}个免费额度</text>
      </view>
      <view class="quota-action" @click="goToUpgrade">
        <text class="quota-link">免费用户 • 升级会员无限制</text>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-container">
      <view class="stat-card">
        <view class="stat-value">{{ totalCount }}</view>
        <view class="stat-label">总物品数</view>
      </view>
      <view class="stat-card warning">
        <view class="stat-value">{{ expiringCount }}</view>
        <view class="stat-label">即将过期</view>
      </view>
      <view class="stat-card danger">
        <view class="stat-value">{{ expiredCount }}</view>
        <view class="stat-label">已过期</view>
      </view>
      <view class="stat-card shared">
        <view class="stat-value">{{ sharedCount }}</view>
        <view class="stat-label">共享空间</view>
      </view>
    </view>

    <!-- TAB切换 -->
    <view class="tab-container">
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'personal' }"
        @click="switchTab('personal')"
      >
        <text class="tab-icon">👤</text>
        <text class="tab-text">个人</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'shared' }"
        @click="switchTab('shared')"
      >
        <text class="tab-icon">👥</text>
        <text class="tab-text">共享空间</text>
      </view>
    </view>

    <!-- 个人TAB内容 -->
    <view v-if="activeTab === 'personal'" class="tab-content">
      <!-- 提醒区域 -->
      <view class="reminder-section" v-if="reminders.length > 0">
        <view class="reminder-header">
          <text class="icon">🔔</text>
          <text class="title">过期提醒</text>
        </view>
        <view class="reminder-list">
          <view 
            class="reminder-item" 
            :class="item.type"
            v-for="(item, index) in reminders" 
            :key="index"
            @click="handleReminderClick(item)"
          >
            <text>{{ item.text }}</text>
            <text class="reminder-action">{{ item.action }}</text>
          </view>
        </view>
      </view>

      <!-- 物品列表 -->
      <view class="section-title" v-if="recentItems.length > 0">
        <view class="title-with-icon">
          <text class="icon">📦</text>
          <text>我的物品</text>
        </view>
      </view>

      <view class="item-list" v-if="recentItems.length > 0">
        <item-card 
          v-for="item in recentItems" 
          :key="item.id"
          :item="item"
          @click="goToItemDetail"
          @edit="editItem"
          @log="showItemLogs"
        />
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="recentItems.length === 0 && reminders.length === 0">
        <text class="empty-icon">📦</text>
        <text class="empty-title">暂无物品</text>
        <text class="empty-desc">点击下方按钮添加您的第一件物品</text>
        <button class="empty-add-btn" @click="showAddItemModal">添加物品</button>
      </view>
    </view>

    <!-- 共享空间TAB内容 -->
    <view v-if="activeTab === 'shared'" class="tab-content">
      <!-- 共享空间列表 -->
      <view class="section-title" v-if="sharedSpaces.length > 0">
        <view class="title-with-icon">
          <text class="icon">👥</text>
          <text>共享空间</text>
        </view>
        <view class="add-btn" @click="goToCreateSpace">+</view>
      </view>

      <view class="space-list" v-if="sharedSpaces.length > 0">
        <view 
          class="space-card" 
          v-for="space in sharedSpaces" 
          :key="space.id"
          @click="goToSpaceDetail(space.id)"
        >
          <view class="space-header">
            <view class="space-icon">
              <text>{{ space.name.charAt(0) || '空' }}</text>
            </view>
            <view class="space-info">
              <text class="space-name">{{ space.name }}</text>
              <text class="space-desc">{{ space.memberCount || 0 }} 个成员 · {{ space.itemCount || 0 }} 件物品</text>
            </view>
            <view class="space-arrow">></view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="sharedSpaces.length === 0">
        <text class="empty-icon">👥</text>
        <text class="empty-title">暂无共享空间</text>
        <text class="empty-desc">创建或加入共享空间，与家人朋友共同管理物品</text>
        <button class="empty-add-btn" @click="goToCreateSpace">创建共享空间</button>
      </view>
    </view>

    <!-- 添加物品弹窗 -->
    <add-item-modal 
      :show="showAddModal" 
      @close="handleCloseModal"
      @success="handleAddSuccess"
    />
    
  </view>
</template>

<script>
import storage from '@/common/utils/storage.js'
import ItemCard from '@/components/item-card/item-card.vue'
import AddItemModal from '@/components/add-item-modal/add-item-modal.vue'
import themeMixin from '@/common/mixins/theme.js'
import memberUtil from '@/common/utils/member.js'

export default {
  mixins: [themeMixin],
  components: {
    ItemCard,
    'add-item-modal': AddItemModal
  },
  data() {
    return {
      memberUtil,
      FREE_QUOTA: 5,
      usedQuota: 0,
      userInfo: {},
      totalCount: 0,
      expiringCount: 0,
      expiredCount: 0,
      sharedCount: 0,
      reminders: [],
      recentItems: [],
      // 是否使用模拟数据（对接接口后改为 false）
      useMockData: true,
      // 是否显示添加物品弹窗
      showAddModal: false,
      // 当前激活的TAB：'personal' 或 'shared'
      activeTab: 'personal',
      // 共享空间列表
      sharedSpaces: []
    }
  },
  onLoad() {
    this.checkLogin()
  },
  onShow() {
    this.loadData()
  },
  methods: {
    checkLogin() {
      const userInfo = storage.get('userInfo')
      if (!userInfo || !userInfo.openid) {
        // 未登录，尝试微信登录
        this.wxLogin()
      } else {
        this.loadData()
      }
    },
    wxLogin() {
      uni.login({
        provider: 'weixin',
        success: (res) => {
          // 获取用户信息
          uni.getUserProfile({
            desc: '用于完善用户资料',
            success: (userRes) => {
              // 保存用户信息
              const userInfo = {
                openid: res.code, // 实际应该从后端获取
                nickName: userRes.userInfo.nickName,
                avatarUrl: userRes.userInfo.avatarUrl
              }
              storage.set('userInfo', userInfo)
              this.loadData()
            },
            fail: () => {
              // 用户拒绝授权，使用默认信息
              const userInfo = {
                openid: res.code,
                nickName: '用户',
                avatarUrl: ''
              }
              storage.set('userInfo', userInfo)
              this.loadData()
            }
          })
        },
        fail: () => {
          uni.showToast({ title: '登录失败', icon: 'none' })
        }
      })
    },
    loadData() {
      // ========== 模拟数据开始 ==========
      // TODO: 对接接口后，将 useMockData 改为 false，并取消注释下面的接口调用代码
      
      if (this.useMockData) {
        // 模拟用户信息
        const mockUserInfo = {
          openid: 'mock_openid_123',
          nickName: '张三',
          avatarUrl: ''
        }
        
        // 模拟物品数据
        const now = new Date()
        const mockItems = [
          {
            id: '1',
            name: '纯牛奶',
            category: '食品',
            purchaseDate: this.formatDateForMock(new Date(now.getTime() - 5 * 24 * 60 * 60 * 1000)),
            productionDate: this.formatDateForMock(new Date(now.getTime() - 10 * 24 * 60 * 60 * 1000)),
            shelfLife: 30,
            unit: '天',
            expiryDate: this.formatDateForMock(new Date(now.getTime() + 3 * 24 * 60 * 60 * 1000)), // 3天后过期
            createdAt: new Date().toISOString()
          },
          {
            id: '2',
            name: '维生素C片',
            category: '药品',
            purchaseDate: this.formatDateForMock(new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)),
            productionDate: this.formatDateForMock(new Date(now.getTime() - 60 * 24 * 60 * 60 * 1000)),
            shelfLife: 365,
            unit: '天',
            expiryDate: this.formatDateForMock(new Date(now.getTime() + 45 * 24 * 60 * 60 * 1000)), // 45天后过期
            createdAt: new Date().toISOString()
          },
          {
            id: '3',
            name: '番茄酱',
            category: '食品',
            purchaseDate: this.formatDateForMock(new Date(now.getTime() - 60 * 24 * 60 * 60 * 1000)),
            productionDate: this.formatDateForMock(new Date(now.getTime() - 90 * 24 * 60 * 60 * 1000)),
            shelfLife: 180,
            unit: '天',
            expiryDate: this.formatDateForMock(new Date(now.getTime() - 5 * 24 * 60 * 60 * 1000)), // 已过期5天
            createdAt: new Date().toISOString()
          },
          {
            id: '4',
            name: '止痛药',
            category: '药品',
            purchaseDate: this.formatDateForMock(new Date(now.getTime() - 20 * 24 * 60 * 60 * 1000)),
            productionDate: this.formatDateForMock(new Date(now.getTime() - 40 * 24 * 60 * 60 * 1000)),
            shelfLife: 730,
            unit: '天',
            expiryDate: this.formatDateForMock(new Date(now.getTime() + 120 * 24 * 60 * 60 * 1000)), // 120天后过期
            createdAt: new Date().toISOString()
          },
          {
            id: '5',
            name: '酸奶',
            category: '食品',
            purchaseDate: this.formatDateForMock(new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000)),
            productionDate: this.formatDateForMock(new Date(now.getTime() - 5 * 24 * 60 * 60 * 1000)),
            shelfLife: 14,
            unit: '天',
            expiryDate: this.formatDateForMock(new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000)), // 7天后过期
            createdAt: new Date().toISOString()
          },
          {
            id: '6',
            name: '面包',
            category: '食品',
            purchaseDate: this.formatDateForMock(new Date(now.getTime() - 1 * 24 * 60 * 60 * 1000)),
            productionDate: this.formatDateForMock(new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000)),
            shelfLife: 5,
            unit: '天',
            expiryDate: this.formatDateForMock(new Date(now.getTime() + 10 * 24 * 60 * 60 * 1000)), // 10天后过期
            createdAt: new Date().toISOString()
          }
        ]
        
        // 如果没有存储数据，使用模拟数据
        let items = storage.get('personalItems', [])
        if (items.length === 0) {
          items = mockItems
          storage.set('personalItems', items)
        }
        
        // 如果没有用户信息，使用模拟数据
        let userInfo = storage.get('userInfo', {})
        if (!userInfo || !userInfo.nickName) {
          userInfo = mockUserInfo
          storage.set('userInfo', userInfo)
        }
        this.userInfo = userInfo
        
        // 模拟共享空间数量
        this.sharedCount = 2
        
        this.calculateStats(items)
        // 加载共享空间列表
        this.loadSharedSpaces()
        return
      }
      
      // ========== 模拟数据结束 ==========
      
      // ========== 接口调用代码（对接接口后取消注释） ==========
      // try {
      //   // 获取用户信息
      //   this.userInfo = await api.getUserInfo()
      //   
      //   // 获取物品列表
      //   const items = await api.getPersonalItems()
      //   
      //   // 获取共享空间数量
      //   this.sharedCount = await api.getSharedSpacesCount()
      //   
      //   // 计算统计数据
      //   this.calculateStats(items)
      // } catch (error) {
      //   console.error('加载数据失败:', error)
      //   uni.showToast({ title: '加载数据失败', icon: 'none' })
      // }
      // ========== 接口调用代码结束 ==========
      
      // 如果未使用模拟数据且没有接口，则从存储中加载
      const items = storage.get('personalItems', [])
      this.userInfo = storage.get('userInfo', {})
      this.sharedCount = 0
      this.calculateStats(items)
      // 加载共享空间列表
      this.loadSharedSpaces()
      // 更新额度信息
      this.updateQuota()
    },
    // 更新额度信息
    updateQuota() {
      this.usedQuota = memberUtil.getUsedQuota()
    },
    // 计算统计数据
    calculateStats(items) {
      const now = new Date()
      const sevenDaysLater = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000)
      
      this.totalCount = items.length
      
      // 计算过期数量
      this.expiredCount = items.filter(item => new Date(item.expiryDate) <= now).length
      
      // 计算即将过期数量
      this.expiringCount = items.filter(item => {
        const expiry = new Date(item.expiryDate)
        return expiry > now && expiry <= sevenDaysLater
      }).length

      // 生成提醒
      this.reminders = []
      const expired = items.filter(item => new Date(item.expiryDate) <= now)
      const expiring = items.filter(item => {
        const expiry = new Date(item.expiryDate)
        return expiry > now && expiry <= sevenDaysLater
      })
      
      if (expired.length > 0) {
        this.reminders.push({ 
          text: `有 ${expired.length} 件物品已过期`, 
          action: '立即处理',
          type: 'danger'
        })
      }
      if (expiring.length > 0) {
        this.reminders.push({ 
          text: `有 ${expiring.length} 件物品即将过期`, 
          action: '7天内',
          type: 'warning'
        })
      }
      
      // 加载最近物品（按过期时间排序，取前5个）
      this.recentItems = items
        .sort((a, b) => new Date(a.expiryDate) - new Date(b.expiryDate))
        .slice(0, 5)
    },
    // 格式化日期为 YYYY-MM-DD 格式（用于模拟数据）
    formatDateForMock(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    goToItemDetail(item) {
      uni.navigateTo({
        url: `/pages/personal/edit-item?id=${item.id}`
      })
    },
    editItem(item) {
      uni.navigateTo({
        url: `/pages/personal/edit-item?id=${item.id}`
      })
    },
    // 显示添加物品弹窗
    showAddItemModal() {
      // 检查额度
      if (!memberUtil.canAddItemWithAd()) {
        // 额度已用完，跳转到升级页面
        uni.navigateTo({
          url: '/pages/member/upgrade'
        })
        return
      }
      this.showAddModal = true
    },
    // 关闭添加物品弹窗
    handleCloseModal() {
      this.showAddModal = false
    },
    // 切换TAB
    switchTab(tab) {
      this.activeTab = tab
      if (tab === 'shared') {
        this.loadSharedSpaces()
      }
    },
    // 加载共享空间列表
    loadSharedSpaces() {
      // ========== 模拟数据开始 ==========
      if (this.useMockData) {
        // 模拟共享空间数据
        const mockSpaces = [
          {
            id: '1',
            name: '家庭药箱',
            memberCount: 3,
            itemCount: 8,
            role: 'creator'
          },
          {
            id: '2',
            name: '厨房食品',
            memberCount: 2,
            itemCount: 12,
            role: 'member'
          }
        ]
        
        // 优先使用存储数据
        let spaces = storage.get('sharedSpaces', [])
        if (spaces.length === 0) {
          spaces = mockSpaces
          storage.set('sharedSpaces', spaces)
        }
        this.sharedSpaces = spaces
        return
      }
      // ========== 模拟数据结束 ==========
      
      // TODO: 对接接口后，使用以下代码
      // const spaces = await api.getSharedSpaces()
      // this.sharedSpaces = spaces
      
      // 如果未使用模拟数据，从存储中加载
      this.sharedSpaces = storage.get('sharedSpaces', [])
    },
    // 跳转到创建共享空间
    goToCreateSpace() {
      uni.navigateTo({
        url: '/pages/shared/create'
      })
    },
    // 跳转到共享空间详情
    goToSpaceDetail(spaceId) {
      uni.navigateTo({
        url: `/pages/shared/detail?id=${spaceId}`
      })
    },
    // 添加物品成功回调
    handleAddSuccess(item) {
      // 刷新数据
      this.loadData()
      // 关闭弹窗
      this.showAddModal = false
      // 更新额度
      this.updateQuota()
    },
    // 跳转到升级页面
    goToUpgrade() {
      uni.navigateTo({
        url: '/pages/member/upgrade'
      })
    },
    // 处理提醒项点击
    handleReminderClick(item) {
      // 如果是过期提醒，跳转到个人物品页面查看过期物品
      if (item.type === 'danger') {
        uni.switchTab({
          url: '/pages/personal/personal'
        })
      } else if (item.type === 'warning') {
        // 如果是即将过期提醒，也跳转到个人物品页面
        uni.switchTab({
          url: '/pages/personal/personal'
        })
      }
    },
    goToPersonal() {
      uni.switchTab({
        url: '/pages/personal/personal'
      })
    },
    goToShared() {
      uni.switchTab({
        url: '/pages/shared/list'
      })
    },
    // 显示物品日志
    showItemLogs(item) {
      uni.navigateTo({
        url: `/pages/personal/logs?itemId=${item.id}`
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
  padding-bottom: 200rpx; /* 为底部导航留出空间 */
  background: var(--primary-bg);
  transition: background-color 0.3s ease;
}

/* 免费额度显示栏 - 高级样式 */
.quota-bar {
  @extend .glass-card;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 36rpx;
  margin-bottom: 30rpx;
  background: var(--card-bg);
  position: relative;
  overflow: hidden;
  
  /* 左侧渐变装饰 */
  &::after {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 6rpx;
    background: linear-gradient(180deg, var(--accent-blue), var(--accent-teal));
    border-radius: 24rpx 0 0 24rpx;
  }
}

.quota-info {
  flex: 1;
}

.quota-text {
  font-size: 28rpx;
  color: var(--text-primary);
  font-weight: 600;
}

.quota-action {
  flex-shrink: 0;
}

.quota-link {
  font-size: 24rpx;
  color: var(--accent-blue);
  text-decoration: underline;
}

.light-mode .quota-link {
  color: #3b82f6;
}


.title {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.title .icon {
  width: 80rpx;
  height: 80rpx;
  background: linear-gradient(135deg, $accent-blue, $accent-teal);
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
}

.title .text {
  font-size: 56rpx;
  font-weight: 600;
  background: linear-gradient(135deg, var(--accent-blue), var(--accent-teal));
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 2rpx;
}

.subtitle {
  font-size: 28rpx;
  color: var(--text-secondary);
  margin-top: 20rpx;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: var(--hover-bg);
  padding: 20rpx 30rpx;
  border-radius: 100rpx;
  border: 2rpx solid var(--card-border);
  transition: all 0.3s ease;
}

.user-icon {
  width: 60rpx;
  height: 60rpx;
  background: linear-gradient(135deg, #8b5cf6, $accent-blue);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 600;
  border: 2rpx solid rgba(255, 255, 255, 0.1);
}

.user-name {
  font-size: 28rpx;
  color: $text-primary;
}

/* 统计卡片区域 */
.stats-container {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.stat-card {
  @extend .glass-card;
  flex: 1;
  padding: 36rpx 24rpx;
  background: var(--card-bg);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 160rpx;
  cursor: pointer;
  
  &:active {
    transform: translateY(2rpx) scale(0.98);
  }
  
  /* 顶部渐变条 - 高级样式 */
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 6rpx;
    background: linear-gradient(90deg, var(--accent-blue), var(--accent-teal));
    border-radius: 32rpx 32rpx 0 0;
    opacity: 0;
    transition: opacity 0.3s ease;
  }
  
  /* 默认状态 - 蓝色渐变 */
  &::after {
    opacity: 1;
  }
  
  /* 即将过期 - 琥珀色渐变 */
  &.warning::after {
    background: linear-gradient(90deg, var(--accent-amber), #f97316);
    opacity: 1;
  }
  
  /* 已过期 - 玫瑰红渐变 */
  &.danger::after {
    background: linear-gradient(90deg, var(--accent-rose), #db2777);
    opacity: 1;
  }
  
  /* 共享空间 - 紫色渐变 */
  &.shared::after {
    background: linear-gradient(90deg, var(--accent-purple), #a855f7);
    opacity: 1;
  }
  
  /* 微妙的内发光效果 */
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(
      circle,
      rgba(37, 99, 235, 0.08) 0%,
      transparent 70%
    );
    pointer-events: none;
    opacity: 0;
    transition: opacity 0.3s ease;
  }
  
  &:active::before {
    opacity: 1;
  }
}

.stat-value {
  font-size: 52rpx;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1.2;
  margin-bottom: 12rpx;
  letter-spacing: -1rpx;
  background: linear-gradient(135deg, var(--text-primary), var(--text-secondary));
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 24rpx;
  font-weight: 500;
  line-height: 1.3;
  text-align: center;
}

/* 浅色模式下的统计卡片 - 高级样式 */
.light-mode .stat-card {
  background: rgba(255, 255, 255, 0.9) !important;
  border: 2rpx solid rgba(37, 99, 235, 0.15) !important;
  box-shadow: 
    0 8rpx 32rpx -8rpx rgba(0, 0, 0, 0.08),
    0 0 0 1rpx rgba(255, 255, 255, 0.8) inset !important;
}

/* TAB切换容器 - 高级玻璃态样式 */
.tab-container {
  display: flex;
  gap: 16rpx;
  margin-bottom: 30rpx;
  background: var(--card-bg);
  border-radius: 40rpx;
  padding: 8rpx;
  border: 2rpx solid var(--card-border);
  backdrop-filter: blur(24rpx) saturate(180%);
  -webkit-backdrop-filter: blur(24rpx) saturate(180%);
  box-shadow: 
    0 8rpx 32rpx -8rpx var(--shadow-color),
    0 0 0 1rpx rgba(255, 255, 255, 0.05) inset;
  position: sticky;
  top: 20rpx;
  z-index: 10;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 24rpx;
  border-radius: 32rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  
  &:active {
    transform: scale(0.98);
  }
  
  &.active {
    background: linear-gradient(135deg, var(--accent-blue), var(--accent-teal));
    box-shadow: 
      0 4rpx 16rpx -4rpx var(--glow-blue),
      0 2rpx 8rpx -2rpx var(--shadow-color);
    
    .tab-text {
      color: #ffffff;
      font-weight: 600;
    }
    
    .tab-icon {
      filter: brightness(1.2);
    }
    
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
      border-radius: 32rpx 32rpx 0 0;
    }
  }
}

.tab-icon {
  font-size: 36rpx;
}

.tab-text {
  font-size: 32rpx;
  font-weight: 500;
  color: var(--text-secondary);
  transition: color 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-item.active .tab-text {
  color: var(--text-primary);
  font-weight: 600;
}

.tab-content {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 共享空间卡片 */
.space-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.space-card {
  @extend .glass-card;
  padding: 36rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: var(--card-bg-solid);
  
  &:active {
    transform: translateY(-5rpx);
    border-color: var(--accent-blue);
  }
}

.space-header {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.space-icon {
  width: 100rpx;
  height: 100rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #8b5cf6, $accent-blue);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  font-weight: 600;
  color: white;
  flex-shrink: 0;
}

.space-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.space-name {
  font-size: 36rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.space-desc {
  font-size: 26rpx;
  color: var(--text-secondary);
}

.space-arrow {
  font-size: 32rpx;
  color: var(--text-secondary);
  flex-shrink: 0;
}

/* 提醒区域 */
.reminder-section {
  @extend .glass-card;
  padding: 48rpx;
  margin-bottom: 60rpx;
  background: var(--card-bg-solid);
}

.reminder-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 40rpx;
}

.reminder-header .icon {
  font-size: 48rpx;
  color: $accent-rose;
}

.reminder-header .title {
  font-size: 48rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.reminder-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.reminder-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  background: var(--hover-bg);
  border-radius: 24rpx;
  border: 2rpx solid var(--card-border);
  font-size: 28rpx;
  color: var(--text-primary);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active {
    background: var(--active-bg);
    transform: translateX(10rpx);
  }
  
  &.warning {
    border-left: 10rpx solid var(--accent-amber);
  }
  
  &.danger {
    border-left: 10rpx solid var(--accent-rose);
  }
}

.reminder-action {
  color: var(--accent-teal);
  font-size: 24rpx;
  font-weight: 500;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
}

.title-with-icon {
  display: flex;
  align-items: center;
  gap: 20rpx;
  font-size: 48rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.title-with-icon .icon {
  font-size: 48rpx;
  color: $accent-teal;
}

.add-btn {
  background-color: #4a69bd;
  color: white;
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  box-shadow: 0 8rpx 20rpx rgba(74, 105, 189, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active {
    transform: scale(0.95);
  }
}

.item-list {
  margin-bottom: 30rpx;
}

.empty-state {
  text-align: center;
  padding: 160rpx 40rpx;
  color: var(--text-secondary);
}

.empty-icon {
  display: block;
  font-size: 240rpx;
  margin-bottom: 40rpx;
  opacity: 0.3;
}

.empty-title {
  display: block;
  font-size: 48rpx;
  font-weight: 600;
  margin-bottom: 20rpx;
  color: var(--text-primary);
}

.empty-desc {
  display: block;
  font-size: 28rpx;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 60rpx;
}

.empty-add-btn {
  margin-top: 40rpx;
  padding: 28rpx 80rpx;
  background: linear-gradient(135deg, $accent-blue, $accent-teal);
  color: white;
  border-radius: 50rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  box-shadow: 0 16rpx 40rpx rgba(59, 130, 246, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active {
    transform: scale(0.95);
  }
}

/* 浅色模式下的添加按钮 */
.light-mode .add-btn {
  background: linear-gradient(135deg, #3b82f6, #14b8a6) !important;
  box-shadow: 0 8rpx 20rpx rgba(59, 130, 246, 0.3) !important;
}

/* 浅色模式下的空状态按钮 */
.light-mode .empty-add-btn {
  background: linear-gradient(135deg, #3b82f6, #14b8a6) !important;
  box-shadow: 0 16rpx 40rpx rgba(59, 130, 246, 0.3) !important;
}
</style>
