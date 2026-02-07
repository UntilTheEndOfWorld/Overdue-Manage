package com.overdue.manager.oms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 同城配送号码生成服务
 * 
 * @author zcc
 */
@Service
@Slf4j
public class LocalDeliveryCodeService {

  @Autowired
  private RedisTemplate<Object, Object> redisTemplate;

  private static final String LOCAL_DELIVERY_CODE_COUNTER_KEY = "local_delivery_code_counter";
  private static final String LOCAL_DELIVERY_CODE_PREFIX = "T";

  /**
   * 生成同城配送号码
   * 格式：T + 4位数字，从0000开始递增
   * 
   * @return 同城配送号码
   */
  public String generateLocalDeliveryCode() {
    try {
      // 使用Redis原子操作获取并递增计数器
      Long counter = redisTemplate.opsForValue().increment(LOCAL_DELIVERY_CODE_COUNTER_KEY);

      // 如果计数器不存在，设置为1
      if (counter == null || counter == 1) {
        redisTemplate.opsForValue().set(LOCAL_DELIVERY_CODE_COUNTER_KEY, 1L, 365, TimeUnit.DAYS);
        counter = 1L;
      }

      // 生成4位数字，不足4位前面补0
      String numberStr = String.format("%04d", counter);

      // 组合同城配送号码：T + 4位数字
      String localDeliveryCode = LOCAL_DELIVERY_CODE_PREFIX + numberStr;

      log.info("生成同城配送号码成功: {}, 计数器: {}", localDeliveryCode, counter);

      return localDeliveryCode;

    } catch (Exception e) {
      log.error("生成同城配送号码失败", e);
      // 如果Redis操作失败，使用时间戳作为备选方案
      return generateFallbackLocalDeliveryCode();
    }
  }

  /**
   * 生成备选同城配送号码（当Redis不可用时使用）
   * 
   * @return 同城配送号码
   */
  private String generateFallbackLocalDeliveryCode() {
    // 使用当前时间戳的后4位作为备选方案
    long timestamp = System.currentTimeMillis();
    String numberStr = String.format("%04d", timestamp % 10000);
    String localDeliveryCode = LOCAL_DELIVERY_CODE_PREFIX + numberStr;

    log.warn("使用备选方案生成同城配送号码: {}", localDeliveryCode);

    return localDeliveryCode;
  }

  /**
   * 验证同城配送号码格式
   * 
   * @param localDeliveryCode 同城配送号码
   * @return 是否有效
   */
  public boolean isValidLocalDeliveryCode(String localDeliveryCode) {
    if (localDeliveryCode == null || localDeliveryCode.length() != 5) {
      return false;
    }

    // 检查前缀是否为T
    if (!localDeliveryCode.startsWith(LOCAL_DELIVERY_CODE_PREFIX)) {
      return false;
    }

    // 检查后4位是否为数字
    try {
      String numberStr = localDeliveryCode.substring(1);
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
      Object value = redisTemplate.opsForValue().get(LOCAL_DELIVERY_CODE_COUNTER_KEY);
      return value != null ? (Long) value : 0L;
    } catch (Exception e) {
      log.error("获取同城配送号码计数器失败", e);
      return 0L;
    }
  }
}