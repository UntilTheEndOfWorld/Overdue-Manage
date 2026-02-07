package com.overdue.common.core.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * 订单倒计时服务
 * 用于管理未支付订单的倒计时
 */
@Service
@Slf4j
public class OrderCountdownService {

  @Autowired
  private RedisCache redisCache;

  // Redis key前缀
  private static final String ORDER_COUNTDOWN_PREFIX = "order:countdown:";

  // 订单支付超时时间（分钟）
  private static final int ORDER_PAYMENT_TIMEOUT_MINUTES = 15;

  /**
   * 设置订单倒计时
   * 
   * @param orderId 订单ID
   * @param orderSn 订单号
   */
  public void setOrderCountdown(Long orderId, String orderSn) {
    String key = ORDER_COUNTDOWN_PREFIX + orderId;
    String value = orderSn + ":" + System.currentTimeMillis();

    // 设置15分钟倒计时
    redisCache.setCacheObject(key, value, ORDER_PAYMENT_TIMEOUT_MINUTES, TimeUnit.MINUTES);
    log.info("设置订单倒计时 - 订单ID: {}, 订单号: {}, 超时时间: {}分钟", orderId, orderSn, ORDER_PAYMENT_TIMEOUT_MINUTES);
  }

  /**
   * 获取订单倒计时剩余时间（秒）
   * 
   * @param orderId 订单ID
   * @return 剩余秒数，-1表示不存在或已过期
   */
  public long getOrderCountdownRemaining(Long orderId) {
    String key = ORDER_COUNTDOWN_PREFIX + orderId;
    Long ttl = redisCache.getExpire(key, TimeUnit.SECONDS);
    return ttl != null ? ttl : -1;
  }

  /**
   * 获取订单倒计时信息
   * 
   * @param orderId 订单ID
   * @return 订单信息字符串，格式：orderSn:timestamp
   */
  public String getOrderCountdownInfo(Long orderId) {
    String key = ORDER_COUNTDOWN_PREFIX + orderId;
    return redisCache.getCacheObject(key);
  }

  /**
   * 移除订单倒计时（支付成功时调用）
   * 
   * @param orderId 订单ID
   */
  public void removeOrderCountdown(Long orderId) {
    String key = ORDER_COUNTDOWN_PREFIX + orderId;
    redisCache.deleteObject(key);
    log.info("移除订单倒计时 - 订单ID: {}", orderId);
  }

  /**
   * 检查订单是否在倒计时中
   * 
   * @param orderId 订单ID
   * @return true表示在倒计时中，false表示不存在或已过期
   */
  public boolean isOrderInCountdown(Long orderId) {
    String key = ORDER_COUNTDOWN_PREFIX + orderId;
    return redisCache.hasKey(key);
  }

  /**
   * 获取所有倒计时中的订单ID
   * 
   * @return 订单ID列表
   */
  public Collection<String> getAllCountdownOrderKeys() {
    return redisCache.keys(ORDER_COUNTDOWN_PREFIX + "*");
  }

  /**
   * 从Redis key中提取订单ID
   * 
   * @param key Redis key
   * @return 订单ID
   */
  public Long extractOrderIdFromKey(String key) {
    if (key.startsWith(ORDER_COUNTDOWN_PREFIX)) {
      String orderIdStr = key.substring(ORDER_COUNTDOWN_PREFIX.length());
      try {
        return Long.parseLong(orderIdStr);
      } catch (NumberFormatException e) {
        log.warn("无法解析订单ID: {}", orderIdStr);
        return null;
      }
    }
    return null;
  }
}
