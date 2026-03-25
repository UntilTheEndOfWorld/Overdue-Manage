<template>
  <view class="container" :class="themeClass">
    <!-- 搜索框 -->
    <view class="search-bar">
      <input 
        class="search-input" 
        v-model="searchKeyword" 
        placeholder="搜索空间名称..."
        @input="onSearch"
      />
    </view>

    <!-- 标题和添加按钮 -->
    <view class="section-header">
      <text class="section-title">空间列表</text>
      <view class="add-container" @click="createSpace">
        <view class="add-btn">+</view>
        <text class="add-text">新增空间</text>
      </view>
    </view>

    <view class="space-list">
      <view 
        class="space-card" 
        v-for="space in filteredSpaces" 
        :key="space.id"
        @click="goToDetail(space.id)"
      >
        <view class="space-header">
          <text class="space-name">{{ space.name }}</text>
          <text class="space-role">{{ getRoleText(space.role) }}</text>
        </view>
        <view class="space-info">
          <text>成员：{{ space.memberCount }} 人</text>
          <text>物品：{{ space.itemCount }} 件</text>
        </view>
      </view>
    </view>

    <view class="empty-state" v-if="filteredSpaces.length === 0">
      <text>暂无共享空间</text>
      <button class="btn btn-primary" @click="createSpace">创建共享空间</button>
    </view>
    
  </view>
</template>

<script>
import storage from '@/common/utils/storage.js'
import themeMixin from '@/common/mixins/theme.js'
import api from '@/common/utils/api.js'
import { isLoggedIn } from '@/common/utils/auth.js'

export default {
  mixins: [themeMixin],
  data() {
    return {
      spaces: [],
      searchKeyword: '',
      loading: false
    }
  },
  computed: {
    filteredSpaces() {
      if (!this.searchKeyword) {
        return this.spaces
      }
      var keyword = this.searchKeyword.toLowerCase()
      return this.spaces.filter(function(space) {
        return space.name && space.name.toLowerCase().indexOf(keyword) !== -1
      })
    }
  },
  onLoad() {
    this.loadSpaces()
  },
  onShow() {
    this.loadSpaces()
  },
  methods: {
    async loadSpaces() {
      if (!isLoggedIn()) return
      if (this.loading) return
      this.loading = true
      try {
        var res = await api.getSharedSpaces()
        if (res && res.code === 200 && res.data) {
          this.spaces = Array.isArray(res.data) ? res.data : (res.data.rows || res.data.list || [])
          storage.set('sharedSpaces', this.spaces)
        }
      } catch (error) {
        console.error('加载共享空间列表失败:', error)
        this.spaces = storage.get('sharedSpaces', [])
      } finally {
        this.loading = false
      }
    },
    onSearch() {
      // 搜索逻辑已在computed中处理
    },
    createSpace() {
      uni.navigateTo({
        url: '/pages/shared/create'
      })
    },
    goToDetail(spaceId) {
      // 雪花 ID 超出 JS 安全整数时必须用字符串传递，勿经 Number 转换
      var id = spaceId != null && spaceId !== '' ? String(spaceId) : ''
      if (!id) return
      uni.navigateTo({
        url: '/pages/shared/detail?id=' + encodeURIComponent(id)
      })
    },
    getRoleText(role) {
      const roleMap = {
        'creator': '创建者',
        'admin': '管理员',
        'member': '成员'
      }
      return roleMap[role] || '成员'
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

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
}

.section-title {
  font-size: 36rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.add-container {
  display: flex;
  align-items: center;
  gap: 16rpx;
  cursor: pointer;
}

.add-btn {
  width: 80rpx;
  height: 80rpx;
  background: linear-gradient(135deg, var(--accent-blue), var(--accent-teal));
  color: white;
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

.add-text {
  font-size: 28rpx;
  color: var(--text-primary);
  font-weight: 500;
}

.space-list {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.space-card {
  @extend .glass-card;
  background: var(--card-bg);
  padding: 40rpx;
  position: relative;
  overflow: hidden;
  
  /* 左侧渐变装饰条 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 8rpx;
    height: 100%;
    background: linear-gradient(180deg, var(--accent-purple), var(--accent-blue));
    border-radius: 32rpx 0 0 32rpx;
    transition: width 0.3s ease;
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

.space-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.space-name {
  font-size: 36rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.space-role {
  padding: 8rpx 20rpx;
  background: var(--hover-bg);
  color: var(--accent-blue);
  border-radius: 20rpx;
  font-size: 24rpx;
  border: 2rpx solid var(--card-border);
}

.space-info {
  display: flex;
  gap: 40rpx;
  font-size: 28rpx;
  color: var(--text-secondary);
}

.empty-state {
  text-align: center;
  padding: 100rpx 0;
  color: var(--text-secondary);
  font-size: 32rpx;
}

.btn {
  margin-top: 40rpx;
  padding: 28rpx;
  border-radius: 12rpx;
  font-size: 32rpx;
  border: none;
}

.btn-primary {
  background: linear-gradient(135deg, var(--accent-blue), var(--accent-teal));
  color: white;
  box-shadow: 0 8rpx 20rpx rgba(59, 130, 246, 0.3);
  transition: all 0.3s ease;
  
  &:active {
    transform: translateY(2rpx);
  }
}

/* 浅色模式下的添加按钮 */
.light-mode .add-btn {
  background: linear-gradient(135deg, #3b82f6, #14b8a6) !important;
  box-shadow: 0 8rpx 20rpx rgba(59, 130, 246, 0.3) !important;
}

/* 浅色模式下的按钮 */
.light-mode .btn-primary {
  background: linear-gradient(135deg, #3b82f6, #14b8a6) !important;
  box-shadow: 0 8rpx 20rpx rgba(59, 130, 246, 0.3) !important;
}
</style>
