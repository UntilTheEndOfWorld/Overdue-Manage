package com.overdue.manager.pms.controller;

import com.github.pagehelper.Page;
import com.overdue.common.annotation.Log;
import com.overdue.common.core.controller.BaseController;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.common.enums.BusinessType;
import com.overdue.common.utils.SecurityUtils;
import com.overdue.manager.pms.domain.entity.PriceAdjustTask;
import com.overdue.manager.pms.domain.query.PriceAdjustTaskQuery;
import com.overdue.manager.pms.domain.vo.PriceAdjustTaskVO;
import com.overdue.manager.pms.service.PriceAdjustService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 批量调价Controller
 * 
 * @author grocery
 * @date 2025-10-10
 */
@Slf4j
@Api(description = "批量调价接口列表")
@RestController
@RequestMapping("/pms/priceAdjust")
public class PriceAdjustController extends BaseController {

  @Autowired
  private PriceAdjustService service;

  @ApiOperation("查询调价任务列表")
  @PreAuthorize("@ss.hasPermi('pms:priceAdjust:list')")
  @PostMapping("/list")
  public AjaxResult list(
      @RequestBody PriceAdjustTaskQuery query, Pageable page) {
    List<PriceAdjustTaskVO> list = service.selectVOList(query, page);
    return AjaxResult.success(new PageImpl<>(list, page, ((Page) list).getTotal()));
  }

  @ApiOperation("获取调价任务详细信息")
  @PreAuthorize("@ss.hasPermi('pms:priceAdjust:query')")
  @GetMapping(value = "/{id}")
  public AjaxResult getInfo(@PathVariable("id") Long id) {
    return AjaxResult.success(service.selectVOById(id));
  }

  @ApiOperation("批量调价")
  @PreAuthorize("@ss.hasPermi('pms:priceAdjust:batch')")
  @Log(title = "批量调价", businessType = BusinessType.UPDATE)
  @PostMapping("/batch")
  public AjaxResult batchAdjust(@RequestBody PriceAdjustTaskVO taskVO) {
    try {
      Long operatorId = SecurityUtils.getUserId();
      String operatorName = SecurityUtils.getUsername();
      int count = service.batchAdjustPrice(taskVO, operatorId, operatorName);
      return AjaxResult.success(count);
    } catch (Exception e) {
      log.error("批量调价失败", e);
      return AjaxResult.error("批量调价失败：" + e.getMessage());
    }
  }

  @ApiOperation("快速调价（单个商品或SKU）")
  @PreAuthorize("@ss.hasPermi('pms:priceAdjust:quick')")
  @Log(title = "快速调价", businessType = BusinessType.UPDATE)
  @PostMapping("/quick")
  public AjaxResult quickAdjust(
      @RequestParam(required = false) Long productId,
      @RequestParam(required = false) Long skuId,
      @RequestParam BigDecimal newPrice,
      @RequestParam(required = false) String reason) {
    try {
      Long operatorId = SecurityUtils.getUserId();
      String operatorName = SecurityUtils.getUsername();
      service.quickAdjustPrice(productId, skuId, newPrice, reason, operatorId, operatorName);
      return AjaxResult.success("调价成功");
    } catch (Exception e) {
      log.error("快速调价失败", e);
      return AjaxResult.error("调价失败：" + e.getMessage());
    }
  }
}
