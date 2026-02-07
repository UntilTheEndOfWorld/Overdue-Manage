package com.overdue.h5.controller;

import com.overdue.common.constant.Constants;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.framework.config.LocalDataUtil;
import com.overdue.h5.domain.form.PointsOrderCreateForm;
import com.overdue.manager.oms.domain.vo.PointsOrderVO;
import com.overdue.manager.oms.service.PointsOrderService;
import com.overdue.manager.ums.domain.entity.Member;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * H5积分兑换订单控制器
 * 
 * @author zcc
 * @date 2024-01-15
 */
@Api(description = "H5积分兑换订单接口")
@RestController
@RequestMapping("/h5/points-order")
@Slf4j
public class H5PointsOrderController {

  @Autowired
  private PointsOrderService pointsOrderService;

  @ApiOperation("创建积分兑换订单")
  @PostMapping("/create")
  public ResponseEntity<AjaxResult> createOrder(@RequestBody PointsOrderCreateForm form) {
    try {
      Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
      if (member == null) {
        return ResponseEntity.ok(AjaxResult.error("用户未登录"));
      }

      AjaxResult result = pointsOrderService.createPointsOrder(
          member.getId(),
          form.getPointsProductId(),
          form.getQuantity(),
          form.getAddressId(),
          form.getNote());

      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("创建积分兑换订单失败", e);
      return ResponseEntity.ok(AjaxResult.error("创建订单失败：" + e.getMessage()));
    }
  }

  @ApiOperation("获取用户积分兑换订单列表")
  @GetMapping("/list")
  public ResponseEntity<Page<PointsOrderVO>> getUserOrders(
      @RequestParam(required = false) Integer status,
      Pageable pageable) {
    try {
      Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
      if (member == null) {
        return ResponseEntity.badRequest().build();
      }

      // 构建查询条件
      com.overdue.manager.oms.domain.form.PointsOrderQuery query = new com.overdue.manager.oms.domain.form.PointsOrderQuery();
      query.setMemberId(member.getId());
      if (status != null) {
        query.setStatus(status);
      }

      PageImpl<PointsOrderVO> orders = pointsOrderService.selectList(query, pageable);
      return ResponseEntity.ok(orders);
    } catch (Exception e) {
      log.error("获取积分兑换订单列表失败", e);
      return ResponseEntity.badRequest().build();
    }
  }

  @ApiOperation("获取积分兑换订单详情")
  @GetMapping("/{id}")
  public ResponseEntity<PointsOrderVO> getOrderDetail(@PathVariable Long id) {
    try {
      Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
      if (member == null) {
        return ResponseEntity.badRequest().build();
      }

      PointsOrderVO order = pointsOrderService.selectById(id);
      if (order == null) {
        return ResponseEntity.badRequest().build();
      }

      // 检查订单是否属于当前用户
      if (!order.getMemberId().equals(member.getId())) {
        return ResponseEntity.badRequest().build();
      }

      return ResponseEntity.ok(order);
    } catch (Exception e) {
      log.error("获取积分兑换订单详情失败", e);
      return ResponseEntity.badRequest().build();
    }
  }

  @ApiOperation("确认收货")
  @PostMapping("/confirm-receive/{id}")
  public ResponseEntity<AjaxResult> confirmReceive(@PathVariable Long id) {
    try {
      Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
      if (member == null) {
        return ResponseEntity.ok(AjaxResult.error("用户未登录"));
      }

      AjaxResult result = pointsOrderService.confirmReceive(id, member.getId());
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("确认收货失败", e);
      return ResponseEntity.ok(AjaxResult.error("确认收货失败：" + e.getMessage()));
    }
  }
}
