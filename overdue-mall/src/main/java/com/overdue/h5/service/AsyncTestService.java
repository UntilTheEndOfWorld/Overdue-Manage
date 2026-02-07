package com.overdue.h5.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncTestService {

  private static final Logger log = LoggerFactory.getLogger(AsyncTestService.class);

  /**
   * 测试异步方法
   */
  @Async("paymentAsyncExecutor")
  public CompletableFuture<String> testAsyncMethod(String message) {
    String traceId = MDC.get("traceId");
    String threadName = Thread.currentThread().getName();

    try {
      MDC.put("operation", "ASYNC_TEST");
      MDC.put("threadName", threadName);

      log.info("异步方法开始执行 - 消息: {}, 线程: {}, MDC上下文: {}",
          message, threadName, MDC.getCopyOfContextMap());

      // 模拟异步处理
      Thread.sleep(2000);

      String result = "异步处理完成: " + message;
      log.info("异步方法执行完成 - 结果: {}", result);

      return CompletableFuture.completedFuture(result);

    } catch (Exception e) {
      log.error("异步方法执行失败", e);
      return CompletableFuture.completedFuture("异步处理失败: " + e.getMessage());
    } finally {
      MDC.clear();
      if (traceId != null) {
        MDC.put("traceId", traceId);
      }
    }
  }

  /**
   * 测试MDC传递
   */
  public void testMDCPropagation() {
    MDC.put("traceId", "test-trace-123");
    MDC.put("userId", "test-user-456");

    log.info("主线程设置MDC - 上下文: {}", MDC.getCopyOfContextMap());

    // 调用异步方法
    testAsyncMethod("测试MDC传递").thenAccept(result -> {
      log.info("异步方法回调结果: {}", result);
    });

    log.info("主线程MDC上下文: {}", MDC.getCopyOfContextMap());
  }
}
