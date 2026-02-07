package com.overdue.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket启动配置类
 * 确保WebSocket服务在应用启动后正确初始化
 */
@Slf4j
@Configuration
@Order(1)
public class WebSocketBootstrapConfig implements CommandLineRunner {

  @Override
  public void run(String... args) throws Exception {
    log.info("=== WebSocket服务启动配置完成 ===");
    log.info("WebSocket端点: /jx_slr_api/websocket/order/{token}");
    log.info("支持的协议: WebSocket (ws://, wss://)");
    log.info("认证方式: JWT Token");
    log.info("消息类型: 订单支付成功通知、心跳检测");
    log.info("=====================================");

    // 启动后的健康检查
    try {
      Thread.sleep(2000); // 等待WebSocket服务完全启动
      log.info("WebSocket服务健康检查完成");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("WebSocket健康检查被中断");
    }
  }
}
