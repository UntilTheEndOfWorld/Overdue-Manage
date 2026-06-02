<template>
  <view class="container" :class="themeClass">
    <view class="form">
      <view class="form-group">
        <text class="label">空间名称 *</text>
        <input class="input" v-model="form.name" placeholder="例如：我家药箱、301室友冰箱" />
      </view>

      <view class="form-group">
        <text class="label">空间描述</text>
        <textarea class="textarea" v-model="form.description" placeholder="可选，描述这个共享空间的用途" />
      </view>

      <view class="btn-group">
        <button class="btn btn-secondary" @click="cancel">取消</button>
        <button class="btn btn-primary" @click="create">创建</button>
      </view>
    </view>
  </view>
</template>

<script>
import storage from '@/common/utils/storage.js'
import themeMixin from '@/common/mixins/theme.js'
import api from '@/common/utils/api.js'
import memberUtil from '@/common/utils/member.js'
import appConfig from '@/common/utils/appConfig.js'

export default {
  mixins: [themeMixin],
  data() {
    return {
      form: {
        name: '',
        description: ''
      },
      submitting: false
    }
  },
  onLoad() {
    appConfig.loadConfig(false)
  },
  methods: {
    async create() {
      if (!this.form.name) {
        uni.showToast({ title: '请输入空间名称', icon: 'none' })
        return
      }
      var spaces = storage.get('sharedSpaces', [])
      if (!memberUtil.canCreateSharedSpace(Array.isArray(spaces) ? spaces.length : 0)) {
        memberUtil.handleSharedSpaceQuotaExceeded()
        return
      }
      if (this.submitting) return
      this.submitting = true

      try {
        var res = await api.createSharedSpace({
          name: this.form.name,
          description: this.form.description
        })
        uni.showToast({ title: '创建成功', icon: 'success' })
        setTimeout(function() { uni.navigateBack() }, 1500)
      } catch (error) {
        console.error('创建共享空间失败:', error)
        uni.showToast({ title: error.message || '创建失败', icon: 'none' })
      } finally {
        this.submitting = false
      }
    },
    cancel() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/common/style/common.scss';

.container {
  padding: 40rpx;
  min-height: 100vh;
  background: var(--primary-bg);
  transition: background-color 0.3s ease;
}

.form {
  background: var(--card-bg-solid);
  border-radius: 20rpx;
  padding: 40rpx;
  border: 2rpx solid var(--card-border);
  box-shadow: 0 10rpx 40rpx var(--shadow-color);
  transition: all 0.3s ease;
}

.form-group {
  margin-bottom: 40rpx;
}

.label {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20rpx;
}

/* 名称输入框：原仅 padding 时视觉偏矮，固定高度约 80rpx 量级，此处整体加高一倍便于点击与阅读 */
.input {
  width: 100%;
  height: 160rpx;
  min-height: 160rpx;
  padding: 0 28rpx;
  line-height: 160rpx;
  background: var(--hover-bg);
  border: 2rpx solid var(--card-border);
  border-radius: 16rpx;
  font-size: 32rpx;
  color: var(--text-primary);
  transition: all 0.3s ease;
  box-sizing: border-box;
  margin: 0;
}

.input:focus {
  border-color: var(--accent-blue);
  background: var(--active-bg);
}

.textarea {
  width: 100%;
  padding: 24rpx;
  background: var(--hover-bg);
  border: 2rpx solid var(--card-border);
  border-radius: 12rpx;
  font-size: 32rpx;
  color: var(--text-primary);
  min-height: 200rpx;
  transition: all 0.3s ease;
  box-sizing: border-box;
  margin: 0;
}

.textarea:focus {
  border-color: var(--accent-blue);
  background: var(--active-bg);
}

.btn-group {
  display: flex;
  gap: 30rpx;
  margin-top: 60rpx;
}

.btn {
  flex: 1;
  padding: 28rpx;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.98);
  }
}

.btn-primary {
  background: linear-gradient(135deg, var(--accent-blue), var(--accent-teal));
  color: white;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3);
  
  &:active {
    box-shadow: 0 2rpx 8rpx rgba(59, 130, 246, 0.4);
  }
}

.btn-secondary {
  background: var(--hover-bg);
  color: var(--text-secondary);
  border: 2rpx solid var(--card-border);
  
  &:active {
    background: var(--active-bg);
  }
}

/* 浅色模式下的按钮增强对比度 */
.light-mode .btn-secondary {
  background: #f1f5f9 !important;
  border: 2rpx solid rgba(59, 130, 246, 0.3) !important;
  color: #475569 !important;
  
  &:active {
    background: #e2e8f0 !important;
  }
}

.light-mode .btn-primary {
  background: linear-gradient(135deg, #3b82f6, #14b8a6) !important;
  box-shadow: 0 4rpx 16rpx rgba(59, 130, 246, 0.4) !important;
}
</style>
