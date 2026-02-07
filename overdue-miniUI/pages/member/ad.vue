<template>
  <view class="container" :class="themeClass">
    <view class="ad-content">
      <view class="ad-icon">📺</view>
      <view class="ad-title">观看广告获取额度</view>
      <view class="ad-desc">观看完整视频广告即可获得1次添加物品的额度</view>
      
      <view class="ad-video-area" v-if="!adFinished">
        <view class="ad-placeholder">
          <text class="ad-placeholder-text">广告视频区域</text>
          <text class="ad-placeholder-hint">（开发中：可对接微信激励视频广告）</text>
        </view>
      </view>

      <view class="ad-result" v-if="adFinished">
        <view class="result-icon">✅</view>
        <view class="result-text">恭喜您！获得1次添加物品的额度</view>
      </view>

      <button 
        class="btn-watch" 
        :disabled="adFinished || isWatching"
        @click="watchAd"
      >
        {{ adFinished ? '已完成' : isWatching ? '播放中...' : '开始观看' }}
      </button>

      <view class="ad-tips">
        <text class="tips-text">提示：观看完整视频后即可获得额度，可用于添加新物品</text>
      </view>
    </view>
  </view>
</template>

<script>
import memberUtil from '@/common/utils/member.js'
import themeMixin from '@/common/mixins/theme.js'

export default {
  mixins: [themeMixin],
  data() {
    return {
      isWatching: false,
      adFinished: false,
      adInstance: null
    }
  },
  onLoad() {
    this.initAd()
  },
  onUnload() {
    if (this.adInstance) {
      this.adInstance.destroy()
    }
  },
  methods: {
    initAd() {
      // 微信小程序激励视频广告初始化
      // #ifdef MP-WEIXIN
      // 实际项目中需要替换为真实的广告位ID
      // this.adInstance = wx.createRewardedVideoAd({
      //   adUnitId: 'your-ad-unit-id'
      // })
      // this.adInstance.onLoad(() => {
      //   console.log('广告加载成功')
      // })
      // this.adInstance.onError((err) => {
      //   console.error('广告加载失败', err)
      // })
      // #endif
    },
    watchAd() {
      // 模拟观看广告流程
      this.isWatching = true
      
      // 模拟广告播放（实际项目中需要调用真实广告API）
      setTimeout(() => {
        this.isWatching = false
        this.adFinished = true
        
        // 记录观看广告
        memberUtil.recordAdWatch()
        
        uni.showToast({
          title: '观看成功，获得1次额度',
          icon: 'success'
        })
      }, 3000)

      // 实际微信小程序代码示例（需要真实广告位ID）：
      // #ifdef MP-WEIXIN
      // if (this.adInstance) {
      //   this.adInstance.show()
      //     .then(() => {
      //       this.isWatching = true
      //     })
      //     .catch(err => {
      //       console.error('广告播放失败', err)
      //       uni.showToast({
      //         title: '广告加载失败，请稍后重试',
      //         icon: 'none'
      //       })
      //       this.isWatching = false
      //     })
      //   
      //   this.adInstance.onClose((res) => {
      //     this.isWatching = false
      //     if (res && res.isEnded) {
      //       // 用户完整观看了广告
      //       this.adFinished = true
      //       memberUtil.recordAdWatch()
      //       uni.showToast({
      //         title: '观看成功，获得1次额度',
      //         icon: 'success'
      //       })
      //     } else {
      //       // 用户提前关闭了广告
      //       uni.showToast({
      //         title: '请观看完整视频',
      //         icon: 'none'
      //       })
      //     }
      //   })
      // } else {
      //   // 模拟观看（开发测试用）
      //   this.isWatching = true
      //   setTimeout(() => {
      //     this.isWatching = false
      //     this.adFinished = true
      //     memberUtil.recordAdWatch()
      //     uni.showToast({
      //       title: '观看成功，获得1次额度',
      //       icon: 'success'
      //     })
      //   }, 3000)
      // }
      // #endif
      
      // #ifndef MP-WEIXIN
      // 非微信小程序环境，使用模拟
      // setTimeout(() => {
      //   this.isWatching = false
      //   this.adFinished = true
      //   memberUtil.recordAdWatch()
      //   uni.showToast({
      //     title: '观看成功，获得1次额度',
      //     icon: 'success'
      //   })
      // }, 3000)
      // #endif
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
  display: flex;
  align-items: center;
  justify-content: center;
}

.ad-content {
  width: 100%;
  max-width: 600rpx;
  text-align: center;
}

.ad-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
}

.ad-title {
  font-size: 40rpx;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 20rpx;
}

.ad-desc {
  font-size: 28rpx;
  color: var(--text-secondary);
  margin-bottom: 40rpx;
  line-height: 1.6;
}

.ad-video-area {
  width: 100%;
  height: 400rpx;
  background: var(--card-bg-solid);
  border-radius: 24rpx;
  margin-bottom: 40rpx;
  border: 2rpx solid var(--card-border);
  display: flex;
  align-items: center;
  justify-content: center;
}

.ad-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.ad-placeholder-text {
  font-size: 32rpx;
  color: var(--text-primary);
  font-weight: 600;
}

.ad-placeholder-hint {
  font-size: 24rpx;
  color: var(--text-tertiary);
}

.ad-result {
  background: var(--card-bg-solid);
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 40rpx;
  border: 2rpx solid var(--accent-green);
}

.result-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.result-text {
  font-size: 28rpx;
  color: var(--text-primary);
  font-weight: 600;
}

.btn-watch {
  width: 100%;
  padding: 28rpx;
  background: linear-gradient(135deg, var(--accent-green), #22c55e);
  color: white;
  border-radius: 24rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  margin-bottom: 30rpx;
}

.btn-watch:disabled {
  background: var(--text-tertiary);
  opacity: 0.5;
}

.ad-tips {
  padding: 20rpx;
  background: rgba(59, 130, 246, 0.1);
  border-radius: 16rpx;
}

.tips-text {
  font-size: 24rpx;
  color: var(--text-secondary);
  line-height: 1.6;
}
</style>
