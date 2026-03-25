<template>
  <view class="page" :class="themeClass">
    <view class="section">
      <view class="section-title">到期提醒</view>
      <view class="row row-master">
        <view class="row-text">
          <text class="label">开启提醒</text>
          <text class="hint">关闭后不会通过任何渠道发送到期通知</text>
        </view>
        <switch :checked="form.reminderEnabled === '1'" color="#3b82f6" @change="onMasterChange" />
      </view>
    </view>

    <view class="section" :class="{ disabled: form.reminderEnabled !== '1' }">
      <view class="section-title">提醒方式</view>

      <view class="row">
        <view class="row-text">
          <text class="label">微信订阅消息</text>
          <text class="hint">需在小程序内授权；到期任务由服务端发送</text>
        </view>
        <switch
          :checked="form.reminderSubscribe === '1'"
          color="#3b82f6"
          :disabled="form.reminderEnabled !== '1'"
          @change="onFlagChange('reminderSubscribe', $event)"
        />
      </view>

      <!-- #ifdef MP-WEIXIN -->
      <button
        v-if="form.reminderEnabled === '1' && form.reminderSubscribe === '1'"
        class="sub-btn"
        type="default"
        @click="requestSubscribeTpl"
      >
        订阅消息授权（微信）
      </button>
      <!-- #endif -->

      <view class="row">
        <view class="row-text">
          <text class="label">短信提醒</text>
          <text class="hint">需填写已验证手机号（与账号绑定）</text>
        </view>
        <switch
          :checked="form.reminderSms === '1'"
          color="#3b82f6"
          :disabled="form.reminderEnabled !== '1'"
          @change="onFlagChange('reminderSms', $event)"
        />
      </view>

      <view class="field" v-if="form.reminderEnabled === '1'">
        <text class="field-label">手机号</text>
        <text v-if="phoneMasked" class="masked">{{ phoneMasked }}</text>
        <input
          v-model="form.contactPhone"
          class="field-input"
          type="number"
          maxlength="11"
          placeholder="填写或修改11位手机号"
        />
      </view>

      <view class="row">
        <view class="row-text">
          <text class="label">邮件提醒</text>
          <text class="hint">填写邮箱后可用于邮件通知</text>
        </view>
        <switch
          :checked="form.reminderEmail === '1'"
          color="#3b82f6"
          :disabled="form.reminderEnabled !== '1'"
          @change="onFlagChange('reminderEmail', $event)"
        />
      </view>

      <view class="field" v-if="form.reminderEnabled === '1'">
        <text class="field-label">邮箱</text>
        <text v-if="emailMasked" class="masked">{{ emailMasked }}</text>
        <input
          v-model="form.contactEmail"
          class="field-input"
          type="text"
          placeholder="填写提醒邮箱"
        />
      </view>
    </view>

    <view class="footer">
      <button class="save-btn" type="primary" @click="save">保存设置</button>
    </view>
  </view>
</template>

<script>
import themeMixin from '@/common/mixins/theme.js'
import api from '@/common/utils/api.js'

export default {
  mixins: [themeMixin],
  data() {
    return {
      phoneMasked: '',
      emailMasked: '',
      form: {
        reminderEnabled: '0',
        reminderSubscribe: '0',
        reminderSms: '0',
        reminderEmail: '0',
        contactPhone: '',
        contactEmail: ''
      }
    }
  },
  onShow() {
    this.load()
  },
  methods: {
    async load() {
      try {
        const res = await api.getReminderSettings()
        const d = (res && res.data) || res
        if (!d) return
        this.phoneMasked = d.phoneMasked || ''
        this.emailMasked = d.emailMasked || ''
        this.form.reminderEnabled = d.reminderEnabled === '1' ? '1' : '0'
        this.form.reminderSubscribe = d.reminderSubscribe === '1' ? '1' : '0'
        this.form.reminderSms = d.reminderSms === '1' ? '1' : '0'
        this.form.reminderEmail = d.reminderEmail === '1' ? '1' : '0'
        this.form.contactPhone = ''
        this.form.contactEmail = ''
      } catch (e) {
        console.error(e)
        uni.showToast({ title: '加载失败', icon: 'none' })
      }
    },
    onMasterChange(e) {
      this.form.reminderEnabled = e.detail.value ? '1' : '0'
    },
    onFlagChange(key, e) {
      this.form[key] = e.detail.value ? '1' : '0'
    },
    requestSubscribeTpl() {
      // #ifdef MP-WEIXIN
      const tmplIds = []
      if (!tmplIds.length) {
        uni.showModal({
          title: '提示',
          content: '请先在微信公众平台配置订阅消息模板，并将模板 ID 填入 reminder-settings.vue 的 tmplIds',
          showCancel: false
        })
        return
      }
      uni.requestSubscribeMessage({
        tmplIds: tmplIds,
        success: function(res) {
          console.log('subscribe', res)
          uni.showToast({ title: '已处理', icon: 'none' })
        },
        fail: function(err) {
          console.warn(err)
          uni.showToast({ title: '订阅失败或未配置模板', icon: 'none' })
        }
      })
      // #endif
    },
    async save() {
      try {
        uni.showLoading({ title: '保存中' })
        const payload = {
          reminderEnabled: this.form.reminderEnabled,
          reminderSubscribe: this.form.reminderSubscribe,
          reminderSms: this.form.reminderSms,
          reminderEmail: this.form.reminderEmail
        }
        if (this.form.contactPhone && String(this.form.contactPhone).trim() !== '') {
          payload.contactPhone = String(this.form.contactPhone).trim()
        }
        if (this.form.contactEmail && String(this.form.contactEmail).trim() !== '') {
          payload.contactEmail = String(this.form.contactEmail).trim()
        }
        const res = await api.updateReminderSettings(payload)
        uni.hideLoading()
        const d = (res && res.data) || res
        if (d && d.phoneMasked !== undefined) {
          this.phoneMasked = d.phoneMasked || ''
          this.emailMasked = d.emailMasked || ''
        }
        uni.showToast({ title: '已保存', icon: 'success' })
        this.load()
      } catch (e) {
        uni.hideLoading()
        var msg = (e && e.message) || '保存失败'
        uni.showToast({ title: msg, icon: 'none' })
      }
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 24rpx 32rpx 120rpx;
  box-sizing: border-box;
  background: #f8fafc;
}
.section {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(15, 23, 42, 0.06);
}
.section.disabled {
  opacity: 0.55;
}
.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 20rpx;
}
.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f1f5f9;
}
.row:last-child {
  border-bottom: none;
}
.row-master {
  padding-top: 0;
}
.row-text {
  flex: 1;
  padding-right: 24rpx;
}
.label {
  display: block;
  font-size: 30rpx;
  color: #334155;
}
.hint {
  display: block;
  font-size: 22rpx;
  color: #94a3b8;
  margin-top: 8rpx;
  line-height: 1.4;
}
.field {
  margin-top: 16rpx;
}
.field-label {
  font-size: 24rpx;
  color: #64748b;
  display: block;
  margin-bottom: 8rpx;
}
.masked {
  display: block;
  font-size: 26rpx;
  color: #475569;
  margin-bottom: 12rpx;
}
.field-input {
  background: #f1f5f9;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  font-size: 28rpx;
}
.sub-btn {
  margin: 16rpx 0 8rpx;
  font-size: 24rpx;
}
.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 16rpx 32rpx 32rpx;
  background: linear-gradient(to top, #f8fafc 80%, transparent);
}
.save-btn {
  background: #3b82f6 !important;
  border-radius: 16rpx !important;
  font-size: 30rpx !important;
}
</style>
