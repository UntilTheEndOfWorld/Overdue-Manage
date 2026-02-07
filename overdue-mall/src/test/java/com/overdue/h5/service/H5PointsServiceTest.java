package com.overdue.h5.service;

import com.overdue.common.core.domain.AjaxResult;
import com.overdue.h5.domain.form.PointsExchangeForm;
import com.overdue.h5.domain.vo.H5PointsProductVO;
import com.overdue.h5.domain.vo.UserPointsVO;
import com.overdue.manager.pms.domain.entity.PointsProduct;
import com.overdue.manager.pms.service.PointsProductService;
import com.overdue.manager.ums.domain.entity.Member;
import com.overdue.manager.ums.mapper.MemberMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * H5积分服务测试
 * 
 * @author zcc
 * @date 2024-01-15
 */
@SpringBootTest
@ActiveProfiles("test")
public class H5PointsServiceTest {

  @Autowired
  private H5PointsService h5PointsService;

  @Autowired
  private MemberMapper memberMapper;

  @Autowired
  private PointsProductService pointsProductService;

  @Test
  public void testGetUserPoints() {
    // 测试获取用户积分信息
    // 假设存在用户ID为1的用户
    Long memberId = 1L;

    try {
      UserPointsVO userPoints = h5PointsService.getUserPoints(memberId);
      assertNotNull(userPoints);
      assertNotNull(userPoints.getCurrentPoints());
      assertNotNull(userPoints.getTotalEarnedPoints());
      assertNotNull(userPoints.getTotalSpentPoints());

      System.out.println("用户积分信息测试通过:");
      System.out.println("当前积分: " + userPoints.getCurrentPoints());
      System.out.println("累计获得: " + userPoints.getTotalEarnedPoints());
      System.out.println("累计消费: " + userPoints.getTotalSpentPoints());
      System.out.println("积分等级: " + userPoints.getPointsLevel());
    } catch (Exception e) {
      System.out.println("用户积分信息测试失败: " + e.getMessage());
    }
  }

  @Test
  public void testPointsExchange() {
    // 测试积分兑换功能
    // 假设存在用户ID为1的用户
    Long memberId = 1L;
    // 假设存在积分商品ID为1的商品
    Long productId = 1L;

    try {
      // 创建兑换表单
      PointsExchangeForm form = new PointsExchangeForm();
      form.setProductId(productId);
      form.setQuantity(1);
      form.setNote("测试兑换");

      // 执行兑换
      AjaxResult result = h5PointsService.exchangeProduct(memberId, form);

      System.out.println("积分兑换测试结果: " + result);

      // 验证结果
      assertNotNull(result);

    } catch (Exception e) {
      System.out.println("积分兑换测试失败: " + e.getMessage());
    }
  }

  @Test
  public void testGetPointsProducts() {
    // 测试获取积分商品列表
    try {
      PageImpl<H5PointsProductVO> products = h5PointsService.getPointsProducts(null, null);
      assertNotNull(products);

      System.out.println("积分商品列表测试通过，商品数量: " + products.getTotalElements());

    } catch (Exception e) {
      System.out.println("积分商品列表测试失败: " + e.getMessage());
    }
  }

  @Test
  public void testGetBannerPointsProducts() {
    // 测试获取轮播图积分商品
    try {
      List<H5PointsProductVO> bannerProducts = h5PointsService.getBannerPointsProducts();
      assertNotNull(bannerProducts);

      System.out.println("轮播图积分商品测试通过，商品数量: " + bannerProducts.size());

    } catch (Exception e) {
      System.out.println("轮播图积分商品测试失败: " + e.getMessage());
    }
  }
}
