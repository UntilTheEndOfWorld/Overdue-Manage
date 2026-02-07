package com.overdue.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.overdue.common.constant.Constants;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.h5.domain.form.DealerDeliveryForm;
import com.overdue.manager.oms.domain.entity.Order;
import com.overdue.manager.oms.domain.entity.OrderItem;
import com.overdue.manager.oms.domain.entity.OrderOperateHistory;
import com.overdue.manager.oms.mapper.OrderItemMapper;
import com.overdue.manager.oms.mapper.OrderMapper;
import com.overdue.manager.oms.mapper.OrderOperateHistoryMapper;
import com.overdue.manager.ums.domain.entity.MemberWechat;
import com.overdue.manager.ums.mapper.MemberWechatMapper;
import com.overdue.wechat.WechatSubscribeMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 经销商订单服务
 */
@Service
@Slf4j
public class DealerOrderService {

  @Autowired
  private OrderMapper orderMapper;

  @Autowired
  private OrderItemMapper orderItemMapper;

  @Autowired
  private OrderOperateHistoryMapper orderOperateHistoryMapper;

  @Autowired
  private MemberWechatMapper memberWechatMapper;

  @Autowired
  private WechatSubscribeMessageService wechatSubscribeMessageService;

  /**
   * 获取经销商订单列表
   */
  public Page<Order> getDealerOrderPage(Long dealerId, Integer status, Pageable pageable) {
    QueryWrapper<Order> qw = new QueryWrapper<>();

    // 只查询已支付的订单（待发货、已发货、已完成）
    qw.in("status", Constants.OrderStatus.SEND, Constants.OrderStatus.GET, Constants.OrderStatus.CONFIRM);

    if (status != null) {
      qw.eq("status", status);
    }

    // 按创建时间倒序
    qw.orderByDesc("create_time");

    // 分页查询
    int offset = (int) pageable.getOffset();
    int pageSize = pageable.getPageSize();

    List<Order> orders = orderMapper.selectList(qw);
    int total = orders.size();

    // 手动分页
    int fromIndex = Math.min(offset, total);
    int toIndex = Math.min(offset + pageSize, total);
    List<Order> pageOrders = orders.subList(fromIndex, toIndex);

    return new PageImpl<>(pageOrders, pageable, total);
  }

  /**
   * 获取订单详情
   */
  public AjaxResult getDealerOrderDetail(Long orderId) {
    Order order = orderMapper.selectById(orderId);
    if (order == null) {
      return AjaxResult.error("订单不存在");
    }

    // 查询订单项
    QueryWrapper<OrderItem> itemQw = new QueryWrapper<>();
    itemQw.eq("order_id", orderId);
    List<OrderItem> orderItems = orderItemMapper.selectList(itemQw);

    // 查询订单操作历史
    QueryWrapper<OrderOperateHistory> historyQw = new QueryWrapper<>();
    historyQw.eq("order_id", orderId);
    historyQw.orderByDesc("create_time");
    List<OrderOperateHistory> histories = orderOperateHistoryMapper.selectList(historyQw);

    Map<String, Object> result = new HashMap<>();
    result.put("order", order);
    result.put("orderItems", orderItems);
    result.put("histories", histories);

    return AjaxResult.success(result);
  }

  /**
   * 经销商发货
   */
  @Transactional
  public AjaxResult deliverOrder(DealerDeliveryForm form, Long dealerId) {
    try {
      Order order = orderMapper.selectById(form.getOrderId());
      if (order == null) {
        return AjaxResult.error("订单不存在");
      }

      // 检查订单状态，只有待发货状态才能发货
      if (!Constants.OrderStatus.SEND.equals(order.getStatus())) {
        return AjaxResult.error("订单状态不正确，无法发货");
      }

      // 更新订单状态
      LocalDateTime now = LocalDateTime.now();
      order.setStatus(Constants.OrderStatus.GET); // 已发货
      order.setDeliveryCompany(form.getDeliveryCompany());
      order.setDeliverySn(form.getDeliverySn());
      order.setDeliveryTime(now);
      order.setUpdateTime(now);
      order.setUpdateBy(dealerId);

      if (form.getNote() != null && !form.getNote().isEmpty()) {
        order.setMerchantNote(form.getNote());
      }

      int rows = orderMapper.updateById(order);
      if (rows < 1) {
        return AjaxResult.error("发货失败");
      }

      // 添加订单操作历史
      OrderOperateHistory history = new OrderOperateHistory();
      history.setOrderId(order.getId());
      history.setOrderSn(order.getOrderSn());
      history.setOperateMan("经销商ID:" + dealerId);
      history.setOrderStatus(Constants.OrderStatus.GET);
      history.setCreateTime(now);
      history.setCreateBy(dealerId);
      orderOperateHistoryMapper.insert(history);

      // 发送发货通知给用户
      try {
        MemberWechat memberWechat = memberWechatMapper.selectByMemberId(order.getMemberId());
        if (memberWechat != null && memberWechat.getRoutineOpenid() != null) {
          wechatSubscribeMessageService.sendDeliveryNotify(
              memberWechat.getRoutineOpenid(),
              order.getOrderSn(),
              form.getDeliveryCompany(),
              form.getDeliverySn());
          log.info("已发送发货通知给用户 - 订单ID: {}, 用户ID: {}", order.getId(), order.getMemberId());
        }
      } catch (Exception e) {
        log.error("发送发货通知失败 - 订单ID: {}", order.getId(), e);
      }

      log.info("经销商发货成功 - 经销商ID: {}, 订单ID: {}, 订单号: {}",
          dealerId, order.getId(), order.getOrderSn());

      return AjaxResult.success("发货成功");

    } catch (Exception e) {
      log.error("经销商发货失败", e);
      return AjaxResult.error("发货失败：" + e.getMessage());
    }
  }

  /**
   * 获取经销商订单统计
   */
  public AjaxResult getDealerOrderStatistics(Long dealerId) {
    try {
      Map<String, Object> statistics = new HashMap<>();

      // 待发货订单数
      QueryWrapper<Order> pendingQw = new QueryWrapper<>();
      pendingQw.eq("status", Constants.OrderStatus.SEND);
      long pendingCount = orderMapper.selectCount(pendingQw);
      statistics.put("pendingDelivery", pendingCount);

      // 已发货订单数
      QueryWrapper<Order> deliveredQw = new QueryWrapper<>();
      deliveredQw.eq("status", Constants.OrderStatus.GET);
      long deliveredCount = orderMapper.selectCount(deliveredQw);
      statistics.put("delivered", deliveredCount);

      // 已完成订单数
      QueryWrapper<Order> completedQw = new QueryWrapper<>();
      completedQw.eq("status", Constants.OrderStatus.CONFIRM);
      long completedCount = orderMapper.selectCount(completedQw);
      statistics.put("completed", completedCount);

      // 今日新订单数
      QueryWrapper<Order> todayQw = new QueryWrapper<>();
      todayQw.eq("status", Constants.OrderStatus.SEND);
      todayQw.apply("DATE(create_time) = CURDATE()");
      long todayCount = orderMapper.selectCount(todayQw);
      statistics.put("todayNew", todayCount);

      return AjaxResult.success(statistics);

    } catch (Exception e) {
      log.error("获取经销商订单统计失败", e);
      return AjaxResult.error("获取统计数据失败");
    }
  }

  /**
   * 添加订单备注
   */
  @Transactional
  public AjaxResult addOrderNote(Long orderId, String note, Long dealerId) {
    try {
      Order order = orderMapper.selectById(orderId);
      if (order == null) {
        return AjaxResult.error("订单不存在");
      }

      UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
      updateWrapper.eq("id", orderId);
      updateWrapper.set("merchant_note", note);
      updateWrapper.set("update_time", LocalDateTime.now());
      updateWrapper.set("update_by", dealerId);

      int rows = orderMapper.update(null, updateWrapper);
      if (rows < 1) {
        return AjaxResult.error("添加备注失败");
      }

      return AjaxResult.success("添加备注成功");

    } catch (Exception e) {
      log.error("添加订单备注失败", e);
      return AjaxResult.error("添加备注失败：" + e.getMessage());
    }
  }
}
