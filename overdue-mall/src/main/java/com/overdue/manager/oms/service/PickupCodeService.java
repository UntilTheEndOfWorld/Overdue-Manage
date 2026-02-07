package com.overdue.manager.oms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 取件码生成服务
 * 
 * @author zcc
 */
@Service
@Slf4j
public class PickupCodeService {

  @Autowired
  private RedisTemplate<Object, Object> redisTemplate;

  private static final String PICKUP_CODE_COUNTER_KEY = "pickup_code_counter";
  private static final String PICKUP_CODE_PREFIX = "A";

  /**
   * 生成取件码
   * 格式：A + 4位数字，从0000开始递增
   * 
   * @return 取件码
   */
  public String generatePickupCode() {
    try {
      // 使用Redis原子操作获取并递增计数器
      Long counter = redisTemplate.opsForValue().increment(PICKUP_CODE_COUNTER_KEY);

      // 如果计数器不存在，设置为1
      if (counter == null || counter == 1) {
        redisTemplate.opsForValue().set(PICKUP_CODE_COUNTER_KEY, 1L, 365, TimeUnit.DAYS);
        counter = 1L;
      }

      // 生成4位数字，不足4位前面补0
      String numberStr = String.format("%04d", counter);

      // 组合取件码：A + 4位数字
      String pickupCode = PICKUP_CODE_PREFIX + numberStr;

      log.info("生成取件码成功: {}, 计数器: {}", pickupCode, counter);

      return pickupCode;

    } catch (Exception e) {
      log.error("生成取件码失败", e);
      // 如果Redis操作失败，使用时间戳作为备选方案
      return generateFallbackPickupCode();
    }
  }

  /**
   * 生成备选取件码（当Redis不可用时使用）
   * 
   * @return 取件码
   */
  private String generateFallbackPickupCode() {
    // 使用当前时间戳的后4位作为备选方案
    long timestamp = System.currentTimeMillis();
    String numberStr = String.format("%04d", timestamp % 10000);
    String pickupCode = PICKUP_CODE_PREFIX + numberStr;

    log.warn("使用备选方案生成取件码: {}", pickupCode);

    return pickupCode;
  }

  /**
   * 验证取件码格式
   * 
   * @param pickupCode 取件码
   * @return 是否有效
   */
  public boolean isValidPickupCode(String pickupCode) {
    if (pickupCode == null || pickupCode.length() != 5) {
      return false;
    }

    // 检查前缀是否为A
    if (!pickupCode.startsWith(PICKUP_CODE_PREFIX)) {
      return false;
    }

    // 检查后4位是否为数字
    try {
      String numberStr = pickupCode.substring(1);
      Integer.parseInt(numberStr);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * 获取当前计数器值
   * 
   * @return 计数器值
   */
  public Long getCurrentCounter() {
    try {
      Object value = redisTemplate.opsForValue().get(PICKUP_CODE_COUNTER_KEY);
      return value != null ? (Long) value : 0L;
    } catch (Exception e) {
      log.error("获取取件码计数器失败", e);
      return 0L;
    }
  }
}
