package com.overdue.manager.pms.controller;

import com.github.pagehelper.Page;
import com.overdue.common.core.controller.BaseController;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.manager.pms.domain.query.PriceHistoryQuery;
import com.overdue.manager.pms.domain.vo.PriceHistoryVO;
import com.overdue.manager.pms.service.PriceHistoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 价格历史记录Controller
 * 
 * @author grocery
 * @date 2025-10-10
 */
@Api(description = "价格历史记录接口列表")
@RestController
@RequestMapping("/pms/priceHistory")
public class PriceHistoryController extends BaseController {

  @Autowired
  private PriceHistoryService service;

  @ApiOperation("查询价格历史记录列表")
  @PreAuthorize("@ss.hasPermi('pms:priceHistory:list')")
  @PostMapping("/list")
  public AjaxResult list(
      @RequestBody PriceHistoryQuery query, Pageable page) {
    List<PriceHistoryVO> list = service.selectVOList(query, page);
    return AjaxResult.success(new PageImpl<>(list, page, ((Page) list).getTotal()));
  }

  @ApiOperation("获取价格历史记录详细信息")
  @PreAuthorize("@ss.hasPermi('pms:priceHistory:query')")
  @GetMapping(value = "/{id}")
  public AjaxResult getInfo(@PathVariable("id") Long id) {
    return AjaxResult.success(service.selectVOById(id));
  }

  @ApiOperation("根据商品ID查询价格历史")
  @PreAuthorize("@ss.hasPermi('pms:priceHistory:query')")
  @GetMapping("/product/{productId}")
  public AjaxResult getByProductId(@PathVariable("productId") Long productId) {
    return AjaxResult.success(service.selectByProductId(productId));
  }

  @ApiOperation("根据SKU ID查询价格历史")
  @PreAuthorize("@ss.hasPermi('pms:priceHistory:query')")
  @GetMapping("/sku/{skuId}")
  public AjaxResult getBySkuId(@PathVariable("skuId") Long skuId) {
    return AjaxResult.success(service.selectBySkuId(skuId));
  }
}
