package com.overdue.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.slf4j.MDC;
import java.util.Map;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean("paymentAsyncExecutor")
  public Executor paymentAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(20);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("payment-async-");
    executor.setTaskDecorator(new MDCTaskDecorator());
    executor.initialize();
    return executor;
  }

  /**
   * MDC任务装饰器，用于在异步线程中传递MDC上下文
   */
  public static class MDCTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
      Map<String, String> contextMap = MDC.getCopyOfContextMap();
      return () -> {
        try {
          if (contextMap != null) {
            MDC.setContextMap(contextMap);
          }
          runnable.run();
        } finally {
          MDC.clear();
        }
      };
    }
  }
}
