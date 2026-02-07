package com.overdue.h5.controller;

import com.overdue.common.core.domain.AjaxResult;
import com.overdue.common.constant.Constants;
import com.overdue.framework.config.LocalDataUtil;
import com.overdue.h5.domain.form.DealerDeliveryForm;
import com.overdue.h5.service.DealerOrderService;
import com.overdue.manager.ums.domain.entity.Member;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 经销商订单管理控制器
 * 用于经销商查看订单、发货等操作
 */
@Api(description = "经销商订单管理接口")
@RestController
@RequestMapping("/h5/dealer/order")
@Slf4j
public class DealerOrderController {

  @Autowired
  private DealerOrderService dealerOrderService;

  @ApiOperation("经销商订单列表")
  @GetMapping("/page")
  public ResponseEntity<Page> page(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) Integer status) {

    Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);

    // 检查是否为经销商
    if (member.getIsDealer() == null || member.getIsDealer() != 1) {
      return ResponseEntity.ok(Page.empty());
    }

    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(dealerOrderService.getDealerOrderPage(member.getId(), status, pageable));
  }

  @ApiOperation("经销商订单详情")
  @GetMapping("/{orderId}")
  public ResponseEntity<AjaxResult> detail(@PathVariable Long orderId) {
    Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);

    // 检查是否为经销商
    if (member.getIsDealer() == null || member.getIsDealer() != 1) {
      return ResponseEntity.ok(AjaxResult.error("无权限访问"));
    }

    return ResponseEntity.ok(dealerOrderService.getDealerOrderDetail(orderId));
  }

  @ApiOperation("经销商发货")
  @PostMapping("/delivery")
  public ResponseEntity<AjaxResult> delivery(@RequestBody DealerDeliveryForm form) {
    Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);

    // 检查是否为经销商
    if (member.getIsDealer() == null || member.getIsDealer() != 1) {
      return ResponseEntity.ok(AjaxResult.error("无权限操作"));
    }

    log.info("经销商发货 - 经销商ID: {}, 订单ID: {}, 物流公司: {}, 物流单号: {}",
        member.getId(), form.getOrderId(), form.getDeliveryCompany(), form.getDeliverySn());

    return ResponseEntity.ok(dealerOrderService.deliverOrder(form, member.getId()));
  }

  @ApiOperation("经销商订单统计")
  @GetMapping("/statistics")
  public ResponseEntity<AjaxResult> statistics() {
    Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);

    // 检查是否为经销商
    if (member.getIsDealer() == null || member.getIsDealer() != 1) {
      return ResponseEntity.ok(AjaxResult.error("无权限访问"));
    }

    return ResponseEntity.ok(dealerOrderService.getDealerOrderStatistics(member.getId()));
  }

  @ApiOperation("经销商添加订单备注")
  @PostMapping("/note")
  public ResponseEntity<AjaxResult> addNote(
      @RequestParam Long orderId,
      @RequestParam String note) {

    Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);

    // 检查是否为经销商
    if (member.getIsDealer() == null || member.getIsDealer() != 1) {
      return ResponseEntity.ok(AjaxResult.error("无权限操作"));
    }

    return ResponseEntity.ok(dealerOrderService.addOrderNote(orderId, note, member.getId()));
  }
}
