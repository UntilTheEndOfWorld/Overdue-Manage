package com.overdue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * WebSocket消息配置类
 * 专门处理WebSocket相关的消息调度，避免与Spring Boot默认调度器冲突
 */
@Configuration
public class WebSocketMessageConfig {

  /**
   * 配置WebSocket专用的TaskScheduler
   * 解决WebSocket与Spring调度的冲突
   */
  @Bean("webSocketTaskScheduler")
  public TaskScheduler webSocketTaskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(3);
    scheduler.setThreadNamePrefix("websocket-msg-");
    scheduler.setWaitForTasksToCompleteOnShutdown(true);
    scheduler.setAwaitTerminationSeconds(10);
    scheduler.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
    return scheduler;
  }
}
