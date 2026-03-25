<template>
  <view class="page" :class="themeClass">
    <view v-if="loadError" class="card error-card">
      <text class="error-msg">资料加载失败，请检查网络</text>
      <button class="retry" type="default" @click="load">重试</button>
    </view>

    <view v-else class="card">
      <view class="avatar-wrap">
        <image v-if="profile.avatarUrl" class="avatar-img" :src="profile.avatarUrl" mode="aspectFill" />
        <text v-else class="avatar-placeholder">{{ (profile.nickname || 'U').charAt(0) }}</text>
      </view>

      <view class="row">
        <text class="label">用户 ID</text>
        <text class="value">{{ displayId }}</text>
      </view>
      <view class="row">
        <text class="label">昵称</text>
        <text class="value">{{ profile.nickname || '—' }}</text>
      </view>
      <view class="row">
        <text class="label">手机号</text>
        <text class="value">{{ profile.phone || '—' }}</text>
      </view>
      <view class="row">
        <text class="label">邮箱</text>
        <text class="value">{{ profile.email || '—' }}</text>
      </view>

      <button class="edit-btn" type="primary" @click="goEdit">编辑资料</button>
    </view>
  </view>
</template>

<script>
import themeMixin from '@/common/mixins/theme.js'
import api from '@/common/utils/api.js'
import storage from '@/common/utils/storage.js'

export default {
  mixins: [themeMixin],
  data() {
    return {
      profile: {},
      loadError: false
    }
  },
  computed: {
    displayId() {
      var id = this.profile && this.profile.id
      if (id !== undefined && id !== null && id !== '') return String(id)
      var u = storage.get('userInfo', {}) || {}
      if (u.userId !== undefined && u.userId !== null) return String(u.userId)
      return '—'
    }
  },
  onShow() {
    this.load()
  },
  methods: {
    async load() {
      this.loadError = false
      try {
        const res = await api.getProfile()
        const p = res && res.data
        if (p) {
          this.profile = {
            id: p.id,
            nickname: p.nickname || '',
            phone: p.phone || '',
            email: p.email || '',
            avatarUrl: p.avatarUrl || ''
          }
        } else {
          this.loadError = true
        }
      } catch (e) {
        console.error(e)
        this.loadError = true
      }
    },
    goEdit() {
      uni.navigateTo({ url: '/pages/profile/edit-profile' })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/common/style/common.scss';

.page {
  min-height: 100vh;
  padding: 32rpx;
  box-sizing: border-box;
}

.card {
  background: var(--card-bg-solid);
  border-radius: 24rpx;
  padding: 40rpx 32rpx;
  border: 2rpx solid var(--card-border);
}

.error-card {
  text-align: center;
}

.error-msg {
  display: block;
  font-size: 28rpx;
  color: var(--text-secondary);
  margin-bottom: 24rpx;
}

.retry {
  margin: 0 auto;
}

.avatar-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 40rpx;
}

.avatar-img {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: var(--hover-bg);
}

.avatar-placeholder {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: var(--hover-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  color: var(--text-secondary);
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24rpx 0;
  border-bottom: 1rpx solid var(--card-border);
  font-size: 28rpx;
}

.row:last-of-type {
  border-bottom: none;
}

.label {
  color: var(--text-secondary);
  flex-shrink: 0;
  margin-right: 24rpx;
}

.value {
  color: var(--text-primary);
  text-align: right;
  word-break: break-all;
}

.edit-btn {
  margin-top: 48rpx;
  width: 100%;
}
</style>
