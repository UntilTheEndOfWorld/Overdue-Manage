package com.overdue.h5.controller;

import com.overdue.common.constant.Constants;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.framework.config.LocalDataUtil;
import com.overdue.h5.domain.form.PointsExchangeForm;
import com.overdue.h5.domain.vo.PointsHistoryVO;
import com.overdue.h5.domain.vo.H5PointsProductVO;
import com.overdue.h5.domain.vo.UserPointsVO;
import com.overdue.h5.service.H5PointsService;
import com.overdue.manager.pms.domain.query.PointsProductQuery;
import com.overdue.manager.ums.domain.entity.Member;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * H5积分相关接口控制器
 * 
 * @author zcc
 * @date 2024-01-15
 */
@Api(description = "H5积分相关接口")
@RestController
@RequestMapping("/h5/points")
@Slf4j
public class H5PointsController {

  @Autowired
  private H5PointsService h5PointsService;

  @ApiOperation("获取用户积分信息")
  @GetMapping("/user-points")
  public ResponseEntity<UserPointsVO> getUserPoints() {
    try {
      Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
      if (member == null) {
        return ResponseEntity.badRequest().build();
      }

      UserPointsVO userPoints = h5PointsService.getUserPoints(member.getId());
      return ResponseEntity.ok(userPoints);
    } catch (Exception e) {
      log.error("获取用户积分信息失败", e);
      return ResponseEntity.badRequest().build();
    }
  }

  @ApiOperation("获取积分商品列表")
  @PostMapping("/products")
  public ResponseEntity<Page<H5PointsProductVO>> getPointsProducts(
      @RequestBody(required = false) PointsProductQuery query,
      Pageable pageable) {
    try {
      if (query == null) {
        query = new PointsProductQuery();
      }
      // 只查询已上架的商品
      query.setPublishStatus(1);

      Page<H5PointsProductVO> products = (Page<H5PointsProductVO>) h5PointsService.getPointsProducts(query, pageable);
      return ResponseEntity.ok(products);
    } catch (Exception e) {
      log.error("获取积分商品列表失败", e);
      return ResponseEntity.badRequest().build();
    }
  }

  @ApiOperation("积分兑换商品")
  @PostMapping("/exchange")
  public ResponseEntity<AjaxResult> exchangeProduct(@RequestBody PointsExchangeForm form) {
    try {
      Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
      if (member == null) {
        return ResponseEntity.ok(AjaxResult.error("用户未登录"));
      }

      AjaxResult result = h5PointsService.exchangeProduct(member.getId(), form);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("积分兑换商品失败", e);
      return ResponseEntity.ok(AjaxResult.error("兑换失败：" + e.getMessage()));
    }
  }

  @ApiOperation("获取积分明细")
  @PostMapping("/history")
  public ResponseEntity<Page<PointsHistoryVO>> getPointsHistory(
      @RequestBody(required = false) Pageable pageable) {
    try {
      Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
      if (member == null) {
        return ResponseEntity.badRequest().build();
      }

      if (pageable == null) {
        pageable = org.springframework.data.domain.PageRequest.of(0, 10);
      }

      Page<PointsHistoryVO> history = (Page<PointsHistoryVO>) h5PointsService.getPointsHistory(member.getId(),
          pageable);
      return ResponseEntity.ok(history);
    } catch (Exception e) {
      log.error("获取积分明细失败", e);
      return ResponseEntity.badRequest().build();
    }
  }

  @ApiOperation("获取轮播图积分商品")
  @GetMapping("/banner")
  public ResponseEntity<List<H5PointsProductVO>> getBannerPointsProducts() {
    try {
      List<H5PointsProductVO> bannerProducts = h5PointsService.getBannerPointsProducts();
      return ResponseEntity.ok(bannerProducts);
    } catch (Exception e) {
      log.error("获取轮播图积分商品失败", e);
      return ResponseEntity.badRequest().build();
    }
  }
}
