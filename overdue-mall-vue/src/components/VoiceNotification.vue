<template>
  <div class="voice-notification">
    <!-- 语音播报控制面板 -->
    <div class="voice-controls" v-if="showControls">
      <el-button 
        :type="isEnabled ? 'success' : 'info'" 
        size="small"
        @click="toggleVoice"
        :icon="isEnabled ? 'el-icon-bell' : 'el-icon-bell'"
      >
        {{ isEnabled ? '语音播报已开启' : '语音播报已关闭' }}
      </el-button>
      
      <el-button 
        type="warning" 
        size="small"
        @click="showSettings = true"
        icon="el-icon-setting"
      >
        设置
      </el-button>
    </div>

    <!-- 设置对话框 -->
    <el-dialog 
      title="语音播报设置" 
      :visible.sync="showSettings" 
      width="500px"
    >
      <el-form :model="settings" label-width="120px">
        <el-form-item label="播报音量">
          <el-slider 
            v-model="settings.volume" 
            :min="0" 
            :max="1" 
            :step="0.1"
            show-input
          />
        </el-form-item>
        
        <el-form-item label="播报语速">
          <el-slider 
            v-model="settings.rate" 
            :min="0.5" 
            :max="2" 
            :step="0.1"
            show-input
          />
        </el-form-item>
        
        <el-form-item label="播报音调">
          <el-slider 
            v-model="settings.pitch" 
            :min="0.5" 
            :max="2" 
            :step="0.1"
            show-input
          />
        </el-form-item>
      </el-form>
      
      <div slot="footer">
        <el-button @click="showSettings = false">取消</el-button>
        <el-button type="primary" @click="saveSettings">保存</el-button>
      </div>
    </el-dialog>

    <!-- 隐藏的音频元素 -->
    <audio 
      ref="audioElement" 
      preload="auto" 
      :muted="true"
      style="display: none;"
    />
  </div>
</template>

<script>
import webSocketManager from '@/utils/websocket';

export default {
  name: 'VoiceNotification',
  props: {
    showControls: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      isEnabled: true,
      showSettings: false,
      settings: {
        volume: 0.8,
        rate: 1.0,
        pitch: 1.0
      },
      speechSynthesis: null,
      supportedFeatures: {
        webSpeech: false,
        audioFiles: true
      }
    };
  },
  mounted() {
    this.initAudio();
    this.initWebSocket();
    this.loadSettings();
  },
  beforeDestroy() {
    this.cleanup();
  },
  methods: {
    // 初始化音频功能
    initAudio() {
      // 检查Web Speech API支持
      if ('speechSynthesis' in window) {
        this.supportedFeatures.webSpeech = true;
        this.speechSynthesis = window.speechSynthesis;
      }
    },

    // 初始化WebSocket
    initWebSocket() {
      // 注册消息处理器
      webSocketManager.onMessage(this.handleWebSocketMessage);
    },

    // 处理WebSocket消息
    handleWebSocketMessage(message) {
      if (!this.isEnabled) return;
      
      // 根据消息类型决定是否播报
      if (this.shouldPlayVoice(message)) {
        this.playVoiceNotification(message);
      }
    },

    // 判断是否应该播放语音
    shouldPlayVoice(message) {
      // 订单支付成功消息
      return message.type === 'order_payment_success';
    },

    // 播放语音提醒
    playVoiceNotification(message) {
      // 优先使用音频文件播放
      this.playAudioFile(message);
    },

    // 使用Web Speech API播报
    playSpeechSynthesis(message) {
      if (this.speechSynthesis) {
        const utterance = new SpeechSynthesisUtterance();
        utterance.text = this.formatMessageText(message);
        utterance.volume = this.settings.volume;
        utterance.rate = this.settings.rate;
        utterance.pitch = this.settings.pitch;
        utterance.lang = 'zh-CN';
        
        this.speechSynthesis.speak(utterance);
      }
    },

    // 播放音频文件
    playAudioFile(message) {
      const audio = this.$refs.audioElement;
      if (audio) {
        // 根据消息类型选择不同的音频文件
        const audioFile = this.getAudioFileByType(message.type);
        if (audioFile) {
          // 停止当前播放
          audio.pause();
          audio.currentTime = 0;
          
          // 设置新的音频源
          audio.src = audioFile;
          audio.volume = this.settings.volume;
          audio.muted = false;
          
          // 加载并播放音频
          audio.load();
          
          // 等待音频可以播放时开始播放
          const playAudio = () => {
            audio.play().catch(error => {
              console.error('音频播放失败:', error);
            });
          };
          
          if (audio.readyState >= 3) {
            // 音频已经加载完成，直接播放
            playAudio();
          } else {
            // 等待音频加载完成
            audio.addEventListener('canplay', playAudio, { once: true });
          }
        }
      }
    },

    // 根据消息类型获取音频文件
    getAudioFileByType(type) {
      const audioFiles = {
        order_payment_success: 'https://slrxp.oss-cn-shenzhen.aliyuncs.com/shanliren-mall/MP3/order-success.mp3'
      };
      return audioFiles[type];
    },

    // 格式化消息文本
    formatMessageText(message) {
      if (message.type === 'order_payment_success') {
        const data = message.data;
        return `新订单支付成功！订单号：${data.orderNo}，客户：${data.memberName}，金额：${data.amount}元`;
      }
      return message.data?.message || '您有新的消息';
    },

    // 切换语音播报开关
    toggleVoice() {
      this.isEnabled = !this.isEnabled;
      this.saveSettings();
      
      if (this.isEnabled) {
        this.$message.success('语音播报已开启');
      } else {
        this.$message.info('语音播报已关闭');
      }
    },


    // 保存设置
    saveSettings() {
      localStorage.setItem('voiceNotificationSettings', JSON.stringify({
        isEnabled: this.isEnabled,
        settings: this.settings
      }));
      this.showSettings = false;
      this.$message.success('设置已保存');
    },

    // 加载设置
    loadSettings() {
      try {
        const saved = localStorage.getItem('voiceNotificationSettings');
        if (saved) {
          const data = JSON.parse(saved);
          this.isEnabled = data.isEnabled !== undefined ? data.isEnabled : true;
          this.settings = { ...this.settings, ...data.settings };
        }
      } catch (error) {
        console.error('加载设置失败:', error);
      }
    },

    // 清理资源
    cleanup() {
      if (this.speechSynthesis) {
        this.speechSynthesis.cancel();
      }
      // 移除WebSocket消息处理器
      webSocketManager.offMessage(this.handleWebSocketMessage);
    }
  }
};
</script>

<style scoped>
.voice-notification {
  display: inline-block;
}

.voice-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}

.voice-controls .el-button {
  margin: 0;
}
</style>
