package com.overdue.manager.oms.controller;

import com.overdue.common.annotation.Log;
import com.overdue.common.core.controller.BaseController;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.common.enums.BusinessType;
import com.overdue.manager.oms.domain.form.DeliverPointsOrderForm;
import com.overdue.manager.oms.domain.form.PointsOrderQuery;
import com.overdue.manager.oms.domain.vo.PointsOrderVO;
import com.overdue.manager.oms.service.PointsOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 积分兑换订单控制器
 * 
 * @author zcc
 * @date 2024-01-15
 */
@Api(description = "积分兑换订单接口")
@RestController
@RequestMapping("/oms/points-order")
public class PointsOrderController extends BaseController {

  @Autowired
  private PointsOrderService pointsOrderService;

  @ApiOperation("查询积分兑换订单列表")
  @PreAuthorize("@ss.hasPermi('oms:points-order:list')")
  @PostMapping("/list")
  public ResponseEntity<PageImpl<PointsOrderVO>> list(@RequestBody PointsOrderQuery query, Pageable pageable) {
    return ResponseEntity.ok(pointsOrderService.selectList(query, pageable));
  }

  @ApiOperation("获取积分兑换订单详细信息")
  @PreAuthorize("@ss.hasPermi('oms:points-order:query')")
  @GetMapping(value = "/{id}")
  public ResponseEntity<PointsOrderVO> getInfo(@PathVariable("id") Long id) {
    return ResponseEntity.ok(pointsOrderService.selectById(id));
  }

  @ApiOperation("积分兑换订单发货")
  @PreAuthorize("@ss.hasPermi('oms:points-order:deliver')")
  @Log(title = "积分兑换订单", businessType = BusinessType.UPDATE)
  @PostMapping("/deliver")
  public ResponseEntity<AjaxResult> deliver(@RequestBody DeliverPointsOrderForm form) {
    return ResponseEntity.ok(pointsOrderService.deliverOrder(form, getUserId()));
  }

  @ApiOperation("完成积分兑换订单")
  @PreAuthorize("@ss.hasPermi('oms:points-order:complete')")
  @Log(title = "积分兑换订单", businessType = BusinessType.UPDATE)
  @PostMapping("/complete/{id}")
  public ResponseEntity<AjaxResult> complete(@PathVariable Long id) {
    return ResponseEntity.ok(pointsOrderService.completeOrder(id, getUserId()));
  }
}
