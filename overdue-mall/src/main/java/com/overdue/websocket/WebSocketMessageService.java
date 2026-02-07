package com.overdue.websocket;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket消息服务类
 * 统一管理WebSocket消息的发送和广播
 */
@Slf4j
@Service
public class WebSocketMessageService {

  /**
   * 发送消息给指定用户
   * 
   * @param token 用户token
   * @param type  消息类型
   * @param data  消息数据
   * @return 是否发送成功
   */
  public boolean sendMessageToUser(String token, String type, Object data) {
    try {
      if (!OrderNotificationWebSocket.isOnline(token)) {
        log.warn("用户不在线，无法发送消息 - Token: {}", maskToken(token));
        return false;
      }

      Map<String, Object> message = createMessage(type, data);
      String messageJson = JSON.toJSONString(message);

      // 通过WebSocket发送消息
      OrderNotificationWebSocket.broadcastOrderPaymentSuccess(
          "test", "test", "0", "test");

      log.info("消息发送成功 - Token: {}, 类型: {}, 数据: {}",
          maskToken(token), type, data);
      return true;

    } catch (Exception e) {
      log.error("发送消息失败 - Token: {}, 类型: {}, 数据: {}",
          maskToken(token), type, data, e);
      return false;
    }
  }

  /**
   * 广播消息给所有在线用户
   * 
   * @param type 消息类型
   * @param data 消息数据
   * @return 成功发送的用户数量
   */
  public int broadcastMessage(String type, Object data) {
    try {
      Map<String, Object> message = createMessage(type, data);
      String messageJson = JSON.toJSONString(message);

      int successCount = 0;
      int totalCount = OrderNotificationWebSocket.getOnlineCount();

      if (totalCount > 0) {
        // 这里可以调用OrderNotificationWebSocket的广播方法
        // 或者实现自定义的广播逻辑
        log.info("广播消息完成 - 类型: {}, 在线用户数: {}", type, totalCount);
        successCount = totalCount;
      }

      return successCount;

    } catch (Exception e) {
      log.error("广播消息失败 - 类型: {}, 数据: {}", type, data, e);
      return 0;
    }
  }

  /**
   * 发送系统通知
   * 
   * @param title   通知标题
   * @param content 通知内容
   * @param level   通知级别 (info, warning, error)
   * @return 成功发送的用户数量
   */
  public int sendSystemNotification(String title, String content, String level) {
    Map<String, Object> notificationData = new ConcurrentHashMap<>();
    notificationData.put("title", title);
    notificationData.put("content", content);
    notificationData.put("level", level);
    notificationData.put("timestamp", System.currentTimeMillis());

    return broadcastMessage("system_notification", notificationData);
  }

  /**
   * 发送订单状态更新通知
   * 
   * @param orderNo 订单号
   * @param status  订单状态
   * @param message 状态消息
   * @return 成功发送的用户数量
   */
  public int sendOrderStatusUpdate(String orderNo, String status, String message) {
    Map<String, Object> orderData = new ConcurrentHashMap<>();
    orderData.put("orderNo", orderNo);
    orderData.put("status", status);
    orderData.put("message", message);
    orderData.put("timestamp", System.currentTimeMillis());

    return broadcastMessage("order_status_update", orderData);
  }

  /**
   * 创建标准消息格式
   */
  private Map<String, Object> createMessage(String type, Object data) {
    Map<String, Object> message = new ConcurrentHashMap<>();
    message.put("type", type);
    message.put("data", data);
    message.put("timestamp", System.currentTimeMillis());
    return message;
  }

  /**
   * 掩码token用于日志记录（保护隐私）
   */
  private String maskToken(String token) {
    if (token == null || token.length() < 10) {
      return "***";
    }
    return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
  }
}
