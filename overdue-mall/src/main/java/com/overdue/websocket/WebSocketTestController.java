package com.overdue.websocket;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * WebSocket测试控制器
 * 用于测试WebSocket服务是否正常运行
 */
@RestController
@RequestMapping("/jx_slr_api/websocket")
public class WebSocketTestController {

  /**
   * 测试WebSocket服务状态
   */
  @GetMapping("/test")
  public String testWebSocket() {
    int onlineCount = OrderNotificationWebSocket.getOnlineCount();
    int sessionPoolSize = OrderNotificationWebSocket.getSessionPoolSize();

    return String.format("WebSocket服务正常运行 - 在线连接数: %d, 会话池大小: %d",
        onlineCount, sessionPoolSize);
  }

  /**
   * 获取WebSocket服务状态
   */
  @GetMapping("/status")
  public Object getWebSocketStatus() {
    return new Object() {
      public final int onlineCount = OrderNotificationWebSocket.getOnlineCount();
      public final int sessionPoolSize = OrderNotificationWebSocket.getSessionPoolSize();
      public final String status = "running";
      public final String timestamp = String.valueOf(System.currentTimeMillis());
    };
  }
}
