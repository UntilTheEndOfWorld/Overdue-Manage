package com.overdue.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.overdue.h5.domain.form.CancelOrderForm;
import com.overdue.h5.service.H5OrderService;
import com.overdue.manager.oms.domain.entity.Order;
import com.overdue.manager.oms.mapper.OrderMapper;
import com.overdue.common.constant.Constants;
import com.overdue.common.core.redis.OrderCountdownService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单自动取消定时任务
 */
@Component
@Slf4j
public class OrderAutoCancelJob {

  @Autowired
  private H5OrderService h5OrderService;

  @Autowired
  private OrderMapper orderMapper;

  @Autowired
  private OrderCountdownService orderCountdownService;

  /**
   * 每分钟检查一次超时订单并自动取消
   * 使用Redis的过期机制，当订单倒计时到期时自动触发取消
   */
  @Scheduled(cron = "0 * * * * ?")
  public void autoCancelTimeoutOrders() {
    log.info("【自动取消超时订单任务开始】");

    try {
      // 获取所有倒计时中的订单key
      Collection<String> countdownKeys = orderCountdownService.getAllCountdownOrderKeys();

      if (countdownKeys.isEmpty()) {
        log.info("【自动取消超时订单任务】当前没有倒计时中的订单");
        return;
      }

      List<Long> timeoutOrderIds = new ArrayList<>();

      // 检查每个订单的倒计时状态
      for (String key : countdownKeys) {
        Long orderId = orderCountdownService.extractOrderIdFromKey(key);
        if (orderId != null) {
          // 检查订单是否还在倒计时中（Redis会自动处理过期）
          if (!orderCountdownService.isOrderInCountdown(orderId)) {
            // 订单已过期，需要取消
            timeoutOrderIds.add(orderId);
            log.info("发现超时订单，准备取消 - 订单ID: {}", orderId);
          }
        }
      }

      // 批量取消超时订单
      if (!timeoutOrderIds.isEmpty()) {
        // 验证订单状态，确保只取消待支付的订单
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", timeoutOrderIds);
        queryWrapper.eq("status", Constants.OrderStatus.NOTPAID);
        queryWrapper.eq("aftersale_status", 1);

        List<Order> validOrders = orderMapper.selectList(queryWrapper);
        List<Long> validOrderIds = validOrders.stream()
            .map(Order::getId)
            .collect(Collectors.toList());

        if (!validOrderIds.isEmpty()) {
          CancelOrderForm request = new CancelOrderForm();
          request.setIdList(validOrderIds);

          // 调用批量取消订单服务
          h5OrderService.orderBatchCancel(request, null);

          log.info("【自动取消超时订单任务完成】成功取消 {} 个超时订单: {}",
              validOrderIds.size(), validOrderIds);
        } else {
          log.info("【自动取消超时订单任务】没有找到有效的待支付订单");
        }
      } else {
        log.info("【自动取消超时订单任务】当前没有超时订单");
      }

    } catch (Exception e) {
      log.error("【自动取消超时订单任务异常】", e);
    }

    log.info("【自动取消超时订单任务结束】");
  }
}
