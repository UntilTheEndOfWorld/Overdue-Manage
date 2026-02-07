package com.overdue.manager.pms.controller;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.overdue.common.annotation.Log;
import com.overdue.common.core.controller.BaseController;
import com.overdue.common.enums.BusinessType;
import com.overdue.manager.pms.convert.PointsProductConvert;
import com.overdue.manager.pms.domain.entity.PointsProduct;
import com.overdue.manager.pms.domain.query.PointsProductQuery;
import com.overdue.manager.pms.service.PointsProductService;
import com.overdue.manager.pms.domain.vo.PointsProductVO;
import com.overdue.common.utils.poi.ExcelUtil;

/**
 * 积分商品信息Controller
 * 
 * @author zcc
 * @date 2024-01-15
 */
@Api(description = "积分商品信息接口列表")
@RestController
@RequestMapping("/pms/points-product")
public class PointsProductController extends BaseController {
  @Autowired
  private PointsProductService service;
  @Autowired
  private PointsProductConvert convert;

  @ApiOperation("查询积分商品信息列表")
  @PreAuthorize("@ss.hasPermi('pms:points-product:list')")
  @PostMapping("/list")
  public ResponseEntity<Page<PointsProduct>> list(@RequestBody PointsProductQuery query, Pageable page) {
    List<PointsProduct> list = service.selectList(query, page);
    return ResponseEntity.ok(new PageImpl<>(list, page, ((com.github.pagehelper.Page) list).getTotal()));
  }

  @ApiOperation("导出积分商品信息列表")
  @PreAuthorize("@ss.hasPermi('pms:points-product:export')")
  @Log(title = "积分商品信息", businessType = BusinessType.EXPORT)
  @GetMapping("/export")
  public ResponseEntity<String> export(PointsProductQuery query) {
    List<PointsProduct> list = service.selectList(query, null);
    ExcelUtil<PointsProductVO> util = new ExcelUtil<>(PointsProductVO.class);
    return ResponseEntity.ok(util.writeExcel(convert.dos2vos(list), "积分商品信息数据"));
  }

  @ApiOperation("获取积分商品信息详细信息")
  @PreAuthorize("@ss.hasPermi('pms:points-product:query')")
  @GetMapping(value = "/{id}")
  public ResponseEntity<PointsProductVO> getInfo(@PathVariable("id") Long id) {
    return ResponseEntity.ok(service.selectById(id));
  }

  @ApiOperation("新增积分商品信息")
  @PreAuthorize("@ss.hasPermi('pms:points-product:add')")
  @Log(title = "积分商品信息", businessType = BusinessType.INSERT)
  @PostMapping
  public ResponseEntity<Integer> add(@RequestBody PointsProductVO pointsProduct) {
    return ResponseEntity.ok(service.insert(pointsProduct));
  }

  @ApiOperation("修改积分商品信息")
  @PreAuthorize("@ss.hasPermi('pms:points-product:edit')")
  @Log(title = "积分商品信息", businessType = BusinessType.UPDATE)
  @PutMapping
  public ResponseEntity<Integer> edit(@RequestBody PointsProductVO pointsProduct) {
    return ResponseEntity.ok(service.update(pointsProduct));
  }

  @ApiOperation("删除积分商品信息")
  @PreAuthorize("@ss.hasPermi('pms:points-product:remove')")
  @Log(title = "积分商品信息", businessType = BusinessType.DELETE)
  @DeleteMapping("/{id}")
  public ResponseEntity<Integer> remove(@PathVariable Long id) {
    return ResponseEntity.ok(service.deleteById(id));
  }

  @ApiOperation("批量删除积分商品信息")
  @PreAuthorize("@ss.hasPermi('pms:points-product:remove')")
  @Log(title = "积分商品信息", businessType = BusinessType.DELETE)
  @DeleteMapping("/batch")
  public ResponseEntity<Integer> removeBatch(@RequestBody List<Long> ids) {
    return ResponseEntity.ok(service.deleteByIds(ids));
  }

  @ApiOperation("查询轮播图积分商品列表")
  @PreAuthorize("@ss.hasPermi('pms:points-product:list')")
  @GetMapping("/banner")
  public ResponseEntity<List<PointsProduct>> getBannerProducts() {
    List<PointsProduct> list = service.selectBannerProducts(10);
    return ResponseEntity.ok(list);
  }
}
