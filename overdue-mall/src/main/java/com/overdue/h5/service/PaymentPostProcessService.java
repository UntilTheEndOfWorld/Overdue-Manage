package com.overdue.h5.service;

import com.overdue.common.core.redis.OrderCountdownService;
import com.overdue.manager.act.service.IntegralHistoryService;
import com.overdue.manager.oms.domain.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Service
public class PaymentPostProcessService {

  private static final Logger log = LoggerFactory.getLogger(PaymentPostProcessService.class);

  @Autowired
  private IntegralHistoryService integralHistoryService;

  @Autowired
  private OrderCountdownService orderCountdownService;

  @Autowired
  private VoiceNotificationService voiceNotificationService;

  /**
   * 异步处理积分增加
   */
  @Async("paymentAsyncExecutor")
  public void handleIntegralAsync(Long orderId, BigDecimal payAmount, Long memberId) {
    String traceId = MDC.get("traceId");
    String orderSn = MDC.get("orderSn");

    try {
      MDC.put("operation", "INTEGRAL_PROCESSING");
      MDC.put("orderId", String.valueOf(orderId));
      MDC.put("orderSn", orderSn);

      log.info("开始异步处理积分增加 - 订单ID: {}, 订单号: {}, 支付金额: {}",
          orderId, orderSn, payAmount);

      integralHistoryService.handleIntegral(orderId, payAmount, memberId);

      log.info("积分增加处理完成 - 订单ID: {}, 订单号: {}", orderId, orderSn);

    } catch (Exception e) {
      log.error("积分增加处理失败 - 订单ID: {}, 订单号: {}", orderId, orderSn, e);
    } finally {
      MDC.clear();
      if (traceId != null) {
        MDC.put("traceId", traceId);
      }
    }
  }

  /**
   * 异步处理订单倒计时移除
   */
  @Async("paymentAsyncExecutor")
  public void handleOrderCountdownAsync(Long orderId, String orderSn) {
    String traceId = MDC.get("traceId");

    try {
      MDC.put("operation", "COUNTDOWN_REMOVAL");
      MDC.put("orderId", String.valueOf(orderId));
      MDC.put("orderSn", orderSn);

      log.info("开始异步移除订单倒计时 - 订单ID: {}, 订单号: {}", orderId, orderSn);

      orderCountdownService.removeOrderCountdown(orderId);

      log.info("订单倒计时移除完成 - 订单ID: {}, 订单号: {}", orderId, orderSn);

    } catch (Exception e) {
      log.error("订单倒计时移除失败 - 订单ID: {}, 订单号: {}", orderId, orderSn, e);
    } finally {
      MDC.clear();
      if (traceId != null) {
        MDC.put("traceId", traceId);
      }
    }
  }

  /**
   * 异步处理语音播报
   */
  @Async("paymentAsyncExecutor")
  public void handleVoiceNotificationAsync(Order order) {
    String traceId = MDC.get("traceId");

    try {
      MDC.put("operation", "VOICE_NOTIFICATION");
      MDC.put("orderId", String.valueOf(order.getId()));
      MDC.put("orderSn", order.getOrderSn());

      log.info("开始异步处理语音播报 - 订单ID: {}, 订单号: {}",
          order.getId(), order.getOrderSn());

      // 调用语音播报服务
      voiceNotificationService.broadcastPaymentSuccess(order);

      log.info("语音播报处理完成 - 订单ID: {}, 订单号: {}", order.getId(), order.getOrderSn());

    } catch (Exception e) {
      log.error("语音播报处理失败 - 订单ID: {}, 订单号: {}",
          order.getId(), order.getOrderSn(), e);
    } finally {
      MDC.clear();
      if (traceId != null) {
        MDC.put("traceId", traceId);
      }
    }
  }

  /**
   * 异步处理所有支付后操作
   */
  @Async("paymentAsyncExecutor")
  public void handleAllPostPaymentOperationsAsync(Order order) {
    String traceId = MDC.get("traceId");

    try {
      MDC.put("operation", "POST_PAYMENT_PROCESSING");
      MDC.put("orderId", String.valueOf(order.getId()));
      MDC.put("orderSn", order.getOrderSn());

      log.info("开始异步处理所有支付后操作 - 订单ID: {}, 订单号: {}",
          order.getId(), order.getOrderSn());

      // 并行处理各项操作
      CompletableFuture.allOf(
          CompletableFuture
              .runAsync(() -> handleIntegralAsync(order.getId(), order.getPayAmount(), order.getMemberId())),
          CompletableFuture.runAsync(() -> handleOrderCountdownAsync(order.getId(), order.getOrderSn())),
          CompletableFuture.runAsync(() -> handleVoiceNotificationAsync(order))).join();

      log.info("所有支付后操作处理完成 - 订单ID: {}, 订单号: {}",
          order.getId(), order.getOrderSn());

    } catch (Exception e) {
      log.error("支付后操作处理失败 - 订单ID: {}, 订单号: {}",
          order.getId(), order.getOrderSn(), e);
    } finally {
      MDC.clear();
      if (traceId != null) {
        MDC.put("traceId", traceId);
      }
    }
  }
}
