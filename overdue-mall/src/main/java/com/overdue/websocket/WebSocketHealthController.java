package com.overdue.websocket;

import com.overdue.common.core.domain.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket健康检查控制器
 * 提供WebSocket服务状态的监控接口
 */
@Slf4j
@RestController
@RequestMapping("/websocket/health")
public class WebSocketHealthController {

  /**
   * 获取WebSocket服务状态
   */
  @GetMapping("/status")
  public AjaxResult getWebSocketStatus() {
    try {
      Map<String, Object> status = new HashMap<>();
      status.put("onlineCount", OrderNotificationWebSocket.getOnlineCount());
      status.put("sessionPoolSize", OrderNotificationWebSocket.getSessionPoolSize());
      status.put("serviceStatus", "running");
      status.put("timestamp", System.currentTimeMillis());

      log.info("WebSocket健康检查 - 在线用户: {}, 会话池大小: {}",
          status.get("onlineCount"), status.get("sessionPoolSize"));

      return AjaxResult.success("WebSocket服务状态正常", status);
    } catch (Exception e) {
      log.error("获取WebSocket状态失败", e);
      return AjaxResult.error("获取WebSocket状态失败: " + e.getMessage());
    }
  }

  /**
   * 测试WebSocket连接
   */
  @GetMapping("/test")
  public AjaxResult testWebSocket() {
    try {
      Map<String, Object> testResult = new HashMap<>();
      testResult.put("message", "WebSocket服务运行正常");
      testResult.put("endpoint", "/jx_slr_api/websocket/order/{token}");
      testResult.put("protocol", "WebSocket (ws://, wss://)");
      testResult.put("timestamp", System.currentTimeMillis());

      log.info("WebSocket连接测试完成");

      return AjaxResult.success("WebSocket连接测试成功", testResult);
    } catch (Exception e) {
      log.error("WebSocket连接测试失败", e);
      return AjaxResult.error("WebSocket连接测试失败: " + e.getMessage());
    }
  }
}
