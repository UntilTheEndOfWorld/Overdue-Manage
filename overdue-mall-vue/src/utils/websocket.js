/**
 * WebSocket连接管理工具
 * 用于接收后端推送的订单消息
 */
class WebSocketManager {
  constructor() {
    this.ws = null;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectInterval = 3000;
    this.heartbeatInterval = 30000; // 30秒心跳
    this.heartbeatTimer = null;
    this.isConnected = false;
    this.messageHandlers = [];
    this.token = null;
  }

  /**
   * 初始化WebSocket连接
   * @param {string} token 用户登录token
   */
  init(token) {
    if (!token) {
      console.warn('WebSocket初始化失败：token为空');
      return;
    }
    
    this.token = token;
    this.connect();
  }

  /**
   * 建立WebSocket连接
   */
  connect() {
    try {
      const wsUrl = this.getWebSocketUrl();
      console.log('尝试连接WebSocket:', wsUrl);
      
      this.ws = new WebSocket(wsUrl);
      this.setupEventHandlers();
      
      // 设置连接超时
      setTimeout(() => {
        if (this.ws && this.ws.readyState === WebSocket.CONNECTING) {
          console.warn('WebSocket连接超时，关闭连接');
          this.ws.close();
        }
      }, 10000); // 10秒超时
      
    } catch (error) {
      console.error('WebSocket连接失败:', error);
      this.scheduleReconnect();
    }
  }

  /**
   * 获取WebSocket连接URL
   */
  getWebSocketUrl() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;
    return `${protocol}//${host}/jx_slr_api/websocket/order/${this.token}`;
  }

  /**
   * 设置事件处理器
   */
  setupEventHandlers() {
    this.ws.onopen = () => {
      console.log('WebSocket连接已建立');
      this.isConnected = true;
      this.reconnectAttempts = 0;
      this.startHeartbeat();
    };

    this.ws.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data);
        this.handleMessage(message);
      } catch (error) {
        console.error('WebSocket消息解析失败:', error);
      }
    };

    this.ws.onclose = () => {
      console.log('WebSocket连接已关闭');
      this.isConnected = false;
      this.stopHeartbeat();
      this.scheduleReconnect();
    };

    this.ws.onerror = (error) => {
      console.error('WebSocket错误:', error);
      console.error('WebSocket状态:', this.ws.readyState);
      console.error('WebSocket URL:', this.ws.url);
      this.isConnected = false;
    };
  }

  /**
   * 处理接收到的消息
   */
  handleMessage(message) {
    console.log('收到WebSocket消息:', message);
    
    // 触发所有注册的消息处理器
    this.messageHandlers.forEach(handler => {
      try {
        handler(message);
      } catch (error) {
        console.error('消息处理器执行失败:', error);
      }
    });
  }

  /**
   * 注册消息处理器
   */
  onMessage(handler) {
    this.messageHandlers.push(handler);
  }

  /**
   * 移除消息处理器
   */
  offMessage(handler) {
    const index = this.messageHandlers.indexOf(handler);
    if (index > -1) {
      this.messageHandlers.splice(index, 1);
    }
  }

  /**
   * 发送消息
   */
  send(message) {
    if (this.isConnected && this.ws) {
      this.ws.send(JSON.stringify(message));
    } else {
      console.warn('WebSocket未连接，无法发送消息');
    }
  }

  /**
   * 开始心跳检测
   */
  startHeartbeat() {
    this.heartbeatTimer = setInterval(() => {
      if (this.isConnected) {
        this.send({ type: 'ping' });
      }
    }, this.heartbeatInterval);
  }

  /**
   * 停止心跳检测
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  /**
   * 断开连接
   */
  disconnect() {
    this.stopHeartbeat();
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
    this.isConnected = false;
  }

  /**
   * 重连机制
   */
  scheduleReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
      setTimeout(() => {
        this.connect();
      }, this.reconnectInterval);
    } else {
      console.error('WebSocket重连失败，已达到最大重试次数');
    }
  }

  /**
   * 获取连接状态
   */
  getConnectionStatus() {
    return {
      isConnected: this.isConnected,
      reconnectAttempts: this.reconnectAttempts,
      maxReconnectAttempts: this.maxReconnectAttempts
    };
  }
}

// 创建单例实例
const webSocketManager = new WebSocketManager();

export default webSocketManager;
