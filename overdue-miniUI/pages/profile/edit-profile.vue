<template>
  <view class="page" :class="themeClass">
    <view class="card">
      <view class="avatar-wrap">
        <!-- #ifdef MP-WEIXIN -->
        <button class="avatar-btn" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
          <image v-if="form.avatarUrl" class="avatar-img" :src="form.avatarUrl" mode="aspectFill" />
          <text v-else class="avatar-placeholder">点击设置头像</text>
        </button>
        <!-- #endif -->
        <!-- #ifndef MP-WEIXIN -->
        <view class="avatar-btn" @click="pickImage">
          <image v-if="form.avatarUrl" class="avatar-img" :src="form.avatarUrl" mode="aspectFill" />
          <text v-else class="avatar-placeholder">点击设置头像</text>
        </view>
        <!-- #endif -->
        <text class="avatar-tip">头像将上传至服务器</text>
      </view>

      <view class="field">
        <text class="label">昵称</text>
        <input class="input" v-model="form.nickname" maxlength="32" placeholder="请输入昵称" />
      </view>
      <view class="field">
        <text class="label">手机号</text>
        <input class="input" v-model="form.phone" type="number" maxlength="11" placeholder="11位手机号（选填）" />
      </view>
      <view class="field">
        <text class="label">邮箱</text>
        <input class="input" v-model="form.email" placeholder="邮箱（选填）" />
      </view>

      <button class="save" type="primary" @click="save">保存</button>
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
      form: {
        nickname: '',
        phone: '',
        email: '',
        avatarUrl: ''
      }
    }
  },
  onShow() {
    this.load()
  },
  methods: {
    async load() {
      try {
        const res = await api.getProfile()
        const p = res && res.data
        if (!p) return
        this.form = {
          nickname: p.nickname || '',
          phone: p.phone || '',
          email: p.email || '',
          avatarUrl: p.avatarUrl || ''
        }
      } catch (e) {
        console.error(e)
      }
    },
    async onChooseAvatar(e) {
      const path = e.detail.avatarUrl
      if (!path) return
      await this.uploadAndSet(path)
    },
    pickImage() {
      var self = this
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: function(res) {
          var p = res.tempFilePaths && res.tempFilePaths[0]
          if (p) self.uploadAndSet(p)
        }
      })
    },
    async uploadAndSet(localPath) {
      try {
        uni.showLoading({ title: '上传中' })
        const url = await api.uploadImage(localPath)
        await api.updateProfile({ avatarUrl: url })
        this.form.avatarUrl = url
        var old = storage.get('userInfo', {}) || {}
        storage.set(
          'userInfo',
          Object.assign({}, old, {
            avatarUrl: url,
            avatar: url
          })
        )
        uni.hideLoading()
        uni.showToast({ title: '头像已保存', icon: 'success' })
      } catch (err) {
        uni.hideLoading()
        uni.showToast({ title: (err && err.message) || '上传或保存失败', icon: 'none' })
      }
    },
    async save() {
      try {
        uni.showLoading({ title: '保存中' })
        const res = await api.updateProfile({
          nickname: this.form.nickname,
          phone: this.form.phone,
          email: this.form.email,
          avatarUrl: this.form.avatarUrl
        })
        uni.hideLoading()
        const p = res && res.data
        if (p) {
          var old = storage.get('userInfo', {}) || {}
          var merged = Object.assign({}, old, {
            nickName: p.nickname || old.nickName,
            nickname: p.nickname,
            avatarUrl: p.avatarUrl || old.avatarUrl,
            avatar: p.avatarUrl || old.avatar,
            phone: p.phone,
            email: p.email
          })
          storage.set('userInfo', merged)
        }
        uni.showToast({ title: '已保存', icon: 'success' })
        setTimeout(function() {
          uni.navigateBack()
        }, 1500)
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: (e && e.message) || '保存失败', icon: 'none' })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/common/style/common.scss';

.page {
  min-height: 100vh;
  padding: 32rpx;
  background: var(--primary-bg);
  box-sizing: border-box;
}

.card {
  background: var(--card-bg-solid);
  border-radius: 24rpx;
  padding: 32rpx;
  border: 2rpx solid var(--card-border);
}

.avatar-wrap {
  text-align: center;
  margin-bottom: 40rpx;
}

.avatar-btn {
  width: 180rpx;
  height: 180rpx;
  border-radius: 50%;
  padding: 0;
  margin: 0 auto;
  overflow: hidden;
  background: var(--secondary-bg);
  border: 2rpx solid var(--card-border);
  line-height: 180rpx;
}

.avatar-btn::after {
  border: none;
}

.avatar-img {
  width: 100%;
  height: 100%;
}

.avatar-placeholder {
  font-size: 24rpx;
  color: var(--text-secondary);
}

.avatar-tip {
  display: block;
  font-size: 22rpx;
  color: var(--text-tertiary);
  margin-top: 16rpx;
}

.field {
  margin-bottom: 28rpx;
}

.label {
  display: block;
  font-size: 26rpx;
  color: var(--text-secondary);
  margin-bottom: 12rpx;
}

.input {
  width: 100%;
  padding: 24rpx;
  background: var(--secondary-bg);
  border-radius: 16rpx;
  font-size: 28rpx;
  color: var(--text-primary);
  box-sizing: border-box;
}

.save {
  margin-top: 40rpx;
  background: linear-gradient(135deg, #2563eb, #0d9488) !important;
  color: #fff !important;
  border-radius: 16rpx !important;
}
</style>
