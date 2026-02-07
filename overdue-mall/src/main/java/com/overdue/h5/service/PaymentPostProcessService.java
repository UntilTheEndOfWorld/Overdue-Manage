package com.overdue.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.overdue.common.core.redis.OrderCountdownService;
import com.overdue.manager.act.service.IntegralHistoryService;
import com.overdue.manager.oms.domain.entity.Order;
import com.overdue.manager.oms.domain.entity.OrderItem;
import com.overdue.manager.ums.domain.entity.Member;
import com.overdue.manager.oms.mapper.OrderItemMapper;
import com.overdue.manager.ums.mapper.MemberMapper;
import com.overdue.websocket.OrderNotificationWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class PaymentPostProcessService {

  private static final Logger log = LoggerFactory.getLogger(PaymentPostProcessService.class);

  @Autowired
  private OrderItemMapper orderItemMapper;

  @Autowired
  private MemberMapper memberMapper;

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
   * 异步处理WebSocket消息推送
   */
  @Async("paymentAsyncExecutor")
  public void handleWebSocketNotificationAsync(Order order) {
    String traceId = MDC.get("traceId");

    try {
      MDC.put("operation", "WEBSOCKET_NOTIFICATION");
      MDC.put("orderId", String.valueOf(order.getId()));
      MDC.put("orderSn", order.getOrderSn());

      log.info("开始异步推送WebSocket消息 - 订单ID: {}, 订单号: {}",
          order.getId(), order.getOrderSn());

      // 获取订单商品信息
      List<OrderItem> orderItems = orderItemMapper.selectList(
          new QueryWrapper<OrderItem>().eq("order_id", order.getId()));

      // 构建商品名称列表
      String productNames = orderItems.stream()
          .map(OrderItem::getProductName)
          .limit(3)
          .collect(Collectors.joining("、"));

      if (orderItems.size() > 3) {
        productNames += "等";
      }

      // 获取会员信息
      Member member = memberMapper.selectById(order.getMemberId());
      String memberName = member != null ? member.getNickname() : "未知用户";

      // 推送WebSocket消息
      OrderNotificationWebSocket.broadcastOrderPaymentSuccess(
          order.getOrderSn(), memberName, String.valueOf(order.getPayAmount()), productNames);

      log.info("WebSocket消息推送完成 - 订单号: {}, 客户: {}, 金额: {}元",
          order.getOrderSn(), memberName, order.getPayAmount());

    } catch (Exception e) {
      log.error("WebSocket消息推送失败 - 订单ID: {}, 订单号: {}",
          order.getId(), order.getOrderSn(), e);
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
          CompletableFuture.runAsync(() -> handleWebSocketNotificationAsync(order)),
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
