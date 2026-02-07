package com.overdue.websocket;

import com.alibaba.fastjson.JSON;
import com.overdue.common.core.domain.model.LoginUser;
import com.overdue.framework.web.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单通知WebSocket服务
 * 用于向WEB管理系统推送订单相关消息
 * 
 * 连接地址: wss://lycc.ltd/jx_slr_api/websocket/order/{token}
 * 认证方式: JWT Token
 * 消息格式: JSON
 */
@Slf4j
@Component
@ServerEndpoint("/jx_slr_api/websocket/order/{token}")
public class OrderNotificationWebSocket {

  private static TokenService tokenService;

  // 在线连接数
  private static final AtomicInteger onlineCount = new AtomicInteger(0);

  /**
   * 设置TokenService（由Spring容器注入）
   */
  @Autowired
  public void setTokenService(TokenService tokenService) {
    OrderNotificationWebSocket.tokenService = tokenService;
  }

  // 连接池，key为token，value为WebSocket会话
  private static final Map<String, Session> sessionPool = new ConcurrentHashMap<>();

  /**
   * 连接建立成功调用的方法
   */
  @OnOpen
  public void onOpen(Session session, @PathParam("token") String token) {
    log.info("尝试建立WebSocket连接 - Token: {}", maskToken(token));

    try {
      // 启用Spring Bean自动装配
      SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

      // 验证token
      if (tokenService == null) {
        log.error("TokenService未初始化，无法验证token");
        session.close();
        return;
      }

      LoginUser loginUser = tokenService.getLoginUser(token);
      if (loginUser == null) {
        log.warn("WebSocket连接失败：无效的token - {}", maskToken(token));
        session.close();
        return;
      }

      // 保存会话
      sessionPool.put(token, session);
      onlineCount.incrementAndGet();

      log.info("WebSocket连接建立成功 - 用户ID: {}, 用户名: {}, 当前在线数: {}, 会话ID: {}",
          loginUser.getUserId(),
          loginUser.getUsername(),
          onlineCount.get(),
          session.getId());

      // 发送连接成功消息
      sendMessage(session, JSON.toJSONString(createMessage("connect", "连接成功")));

    } catch (Exception e) {
      log.error("WebSocket连接建立失败 - Token: {}", maskToken(token), e);
      try {
        session.close();
      } catch (IOException ex) {
        log.error("关闭WebSocket连接失败", ex);
      }
    }
  }

  /**
   * 连接关闭调用的方法
   */
  @OnClose
  public void onClose(@PathParam("token") String token) {
    Session session = sessionPool.remove(token);
    if (session != null) {
      onlineCount.decrementAndGet();
      log.info("WebSocket连接关闭 - Token: {}, 会话ID: {}, 当前在线数: {}",
          maskToken(token), session.getId(), onlineCount.get());
    }
  }

  /**
   * 收到客户端消息后调用的方法
   */
  @OnMessage
  public void onMessage(String message, @PathParam("token") String token) {
    log.debug("收到WebSocket消息 - Token: {}, 消息: {}", maskToken(token), message);

    try {
      // 解析消息
      Map<String, Object> messageMap = JSON.parseObject(message, Map.class);
      String type = (String) messageMap.get("type");

      if ("ping".equals(type)) {
        // 心跳响应
        Session session = sessionPool.get(token);
        if (session != null && session.isOpen()) {
          sendMessage(session, JSON.toJSONString(createMessage("pong", "pong")));
          log.debug("发送心跳响应 - Token: {}", maskToken(token));
        }
      } else if ("status".equals(type)) {
        // 状态查询
        Session session = sessionPool.get(token);
        if (session != null && session.isOpen()) {
          Map<String, Object> statusData = new ConcurrentHashMap<>();
          statusData.put("onlineCount", onlineCount.get());
          statusData.put("sessionPoolSize", sessionPool.size());
          statusData.put("sessionId", session.getId());
          sendMessage(session, JSON.toJSONString(createMessage("status", statusData)));
        }
      }
    } catch (Exception e) {
      log.error("处理WebSocket消息失败 - Token: {}, 消息: {}", maskToken(token), message, e);
    }
  }

  /**
   * 发生错误时调用
   */
  @OnError
  public void onError(Session session, Throwable error) {
    log.error("WebSocket发生错误 - 会话ID: {}, 错误: {}", session.getId(), error.getMessage(), error);
  }

  /**
   * 发送消息给指定会话
   */
  private void sendMessage(Session session, String message) {
    try {
      if (session != null && session.isOpen()) {
        session.getBasicRemote().sendText(message);
        log.debug("WebSocket消息发送成功 - 会话ID: {}, 消息: {}", session.getId(), message);
      } else {
        log.warn("WebSocket会话无效或已关闭 - 会话ID: {}", session != null ? session.getId() : "null");
      }
    } catch (IOException e) {
      log.error("发送WebSocket消息失败 - 会话ID: {}", session != null ? session.getId() : "null", e);
    }
  }

  /**
   * 创建消息对象
   */
  private Map<String, Object> createMessage(String type, Object data) {
    Map<String, Object> message = new ConcurrentHashMap<>();
    message.put("type", type);
    message.put("data", data);
    message.put("timestamp", System.currentTimeMillis());
    return message;
  }

  /**
   * 广播订单支付成功消息
   * 在支付回调成功后调用此方法
   */
  public static void broadcastOrderPaymentSuccess(String orderNo, String memberName,
      String amount, String productNames) {
    Map<String, Object> message = new ConcurrentHashMap<>();
    message.put("type", "order_payment_success");
    message.put("data", new ConcurrentHashMap<String, Object>() {
      {
        put("orderNo", orderNo);
        put("memberName", memberName);
        put("amount", amount);
        put("productNames", productNames);
        put("message", String.format("新订单支付成功！订单号：%s，客户：%s，金额：%s元",
            orderNo, memberName, amount));
      }
    });
    message.put("timestamp", System.currentTimeMillis());

    String messageJson = JSON.toJSONString(message);
    int successCount = 0;
    int totalCount = 0;

    // 广播给所有在线用户
    for (Session session : sessionPool.values()) {
      totalCount++;
      try {
        if (session.isOpen()) {
          session.getBasicRemote().sendText(messageJson);
          successCount++;
        }
      } catch (IOException e) {
        log.error("广播订单支付成功消息失败 - 会话ID: {}", session.getId(), e);
      }
    }

    log.info("广播订单支付成功消息完成 - 订单号: {}, 成功发送: {}/{}, 在线用户数: {}",
        orderNo, successCount, totalCount, onlineCount.get());
  }

  /**
   * 获取当前在线连接数
   */
  public static int getOnlineCount() {
    return onlineCount.get();
  }

  /**
   * 获取当前连接池大小
   */
  public static int getSessionPoolSize() {
    return sessionPool.size();
  }

  /**
   * 获取指定token的会话
   */
  public static Session getSession(String token) {
    return sessionPool.get(token);
  }

  /**
   * 检查指定token是否在线
   */
  public static boolean isOnline(String token) {
    Session session = sessionPool.get(token);
    return session != null && session.isOpen();
  }

  /**
   * 断开指定用户的连接
   */
  public static void disconnectUser(String token) {
    Session session = sessionPool.remove(token);
    if (session != null) {
      try {
        session.close();
        onlineCount.decrementAndGet();
        log.info("主动断开用户连接 - Token: {}, 会话ID: {}", maskToken(token), session.getId());
      } catch (IOException e) {
        log.error("断开用户连接失败 - Token: {}", maskToken(token), e);
      }
    }
  }

  /**
   * 掩码token用于日志记录（保护隐私）
   */
  private static String maskToken(String token) {
    if (token == null || token.length() < 10) {
      return "***";
    }
    return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
  }
}
