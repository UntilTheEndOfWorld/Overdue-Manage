package com.overdue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket配置类
 * 启用WebSocket支持并配置相关Bean
 */
@Slf4j
@Configuration
@EnableWebSocket
public class WebSocketConfig {

  /**
   * 启用WebSocket端点导出器
   * 自动注册所有@ServerEndpoint注解的类
   */
  @Bean
  public ServerEndpointExporter serverEndpointExporter() {
    log.info("WebSocket端点导出器已启用");
    return new ServerEndpointExporter();
  }
}
