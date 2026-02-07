package com.overdue.h5.service;

import com.overdue.manager.oms.domain.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class VoiceNotificationService {

  private static final Logger log = LoggerFactory.getLogger(VoiceNotificationService.class);

  /**
   * 播报支付成功消息
   */
  public void broadcastPaymentSuccess(Order order) {
    try {
      MDC.put("operation", "VOICE_BROADCAST");
      MDC.put("orderId", String.valueOf(order.getId()));
      MDC.put("orderSn", order.getOrderSn());

      log.info("开始语音播报 - 订单号: {}", order.getOrderSn());

      // 这里实现具体的语音播报逻辑
      // 可以是调用TTS服务、播放音频文件等

      // 模拟语音播报
      Thread.sleep(1000);

      log.info("语音播报完成 - 订单号: {}", order.getOrderSn());

    } catch (Exception e) {
      log.error("语音播报失败 - 订单号: {}", order.getOrderSn(), e);
    }
  }
}
