package com.overdue.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.h5.domain.form.PointsExchangeForm;
import com.overdue.h5.domain.vo.PointsHistoryVO;
import com.overdue.h5.domain.vo.H5PointsProductVO;
import com.overdue.h5.domain.vo.UserPointsVO;
import com.overdue.manager.pms.convert.PointsProductConvert;
import com.overdue.manager.pms.domain.entity.PointsProduct;
import com.overdue.manager.pms.domain.query.PointsProductQuery;
import com.overdue.manager.pms.service.PointsProductService;
import com.overdue.manager.oms.service.PointsOrderService;
import com.overdue.manager.ums.domain.entity.Member;
import com.overdue.manager.act.domain.entity.IntegralHistory;
import com.overdue.manager.act.mapper.IntegralHistoryMapper;
import com.overdue.manager.ums.mapper.MemberMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * H5积分服务
 * 
 * @author zcc
 * @date 2024-01-15
 */
@Service
@Slf4j
public class H5PointsService {

  @Autowired
  private MemberMapper memberMapper;

  @Autowired
  private IntegralHistoryMapper integralHistoryMapper;

  @Autowired
  private PointsProductService pointsProductService;

  @Autowired
  private PointsOrderService pointsOrderService;

  @Autowired
  private PointsProductConvert pointsProductConvert;

  /**
   * 获取用户积分信息
   */
  public UserPointsVO getUserPoints(Long memberId) {
    Member member = memberMapper.selectById(memberId);
    if (member == null) {
      throw new RuntimeException("用户不存在");
    }

    UserPointsVO userPoints = new UserPointsVO();
    userPoints.setMemberId(memberId);
    userPoints.setNickname(member.getNickname());
    userPoints.setCurrentPoints(member.getIntegral());

    // 计算累计获得积分
    QueryWrapper<IntegralHistory> earnWrapper = new QueryWrapper<>();
    earnWrapper.eq("member_id", memberId);
    // 1-收入
    earnWrapper.eq("op_type", 1);
    List<IntegralHistory> earnHistory = integralHistoryMapper.selectList(earnWrapper);
    BigDecimal totalEarned = earnHistory.stream()
        .map(IntegralHistory::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    userPoints.setTotalEarnedPoints(totalEarned);

    // 计算累计消费积分
    QueryWrapper<IntegralHistory> spendWrapper = new QueryWrapper<>();
    spendWrapper.eq("member_id", memberId);
    // 2-支出
    spendWrapper.eq("op_type", 2);
    List<IntegralHistory> spendHistory = integralHistoryMapper.selectList(spendWrapper);
    BigDecimal totalSpent = spendHistory.stream()
        .map(IntegralHistory::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    userPoints.setTotalSpentPoints(totalSpent);

    // 设置积分等级（简单实现）
    BigDecimal currentPoints = member.getIntegral();
    if (currentPoints.compareTo(new BigDecimal("10000")) >= 0) {
      userPoints.setPointsLevel("钻石会员");
      userPoints.setLevelDescription("享受最高等级优惠");
    } else if (currentPoints.compareTo(new BigDecimal("5000")) >= 0) {
      userPoints.setPointsLevel("黄金会员");
      userPoints.setLevelDescription("享受高级会员优惠");
    } else if (currentPoints.compareTo(new BigDecimal("1000")) >= 0) {
      userPoints.setPointsLevel("白银会员");
      userPoints.setLevelDescription("享受会员优惠");
    } else {
      userPoints.setPointsLevel("普通会员");
      userPoints.setLevelDescription("享受基础会员服务");
    }

    return userPoints;
  }

  /**
   * 获取积分商品列表
   */
  public PageImpl<H5PointsProductVO> getPointsProducts(PointsProductQuery query, Pageable pageable) {
    List<PointsProduct> products = pointsProductService.selectList(query, pageable);

    List<H5PointsProductVO> productVOs = products.stream()
        .map(this::convertToPointsProductVO)
        .collect(Collectors.toList());

    return new PageImpl<>(productVOs, pageable, ((com.github.pagehelper.Page) products).getTotal());
  }

  /**
   * 积分兑换商品（创建订单）
   */
  @Transactional
  public AjaxResult exchangeProduct(Long memberId, PointsExchangeForm form) {
    try {
      // 调用积分兑换订单服务创建订单
      AjaxResult result = pointsOrderService.createPointsOrder(
          memberId,
          form.getProductId(),
          form.getQuantity(),
          form.getAddressId(),
          form.getNote());

      if ((Integer) result.get(AjaxResult.CODE_TAG) == 200) {
        // 记录积分消费历史
        com.overdue.manager.pms.domain.vo.PointsProductVO product = pointsProductService
            .selectById(form.getProductId());
        BigDecimal requiredPoints = product.getPoints().multiply(new BigDecimal(form.getQuantity()));

        IntegralHistory history = new IntegralHistory();
        history.setMemberId(memberId);
        // 2-支出
        history.setOpType(2);
        // 22-兑换消费
        history.setSubOpType(22);
        history.setAmount(requiredPoints);
        // 积分兑换订单ID
        history.setOrderId((Long) result.get(AjaxResult.DATA_TAG));
        history.setCreateTime(LocalDateTime.now());
        integralHistoryMapper.insert(history);

        log.info("用户{}成功创建积分兑换订单，消耗积分{}", memberId, requiredPoints);
      }

      return result;

    } catch (Exception e) {
      log.error("积分兑换失败", e);
      return AjaxResult.error("兑换失败：" + e.getMessage());
    }
  }

  /**
   * 获取积分明细
   */
  public PageImpl<PointsHistoryVO> getPointsHistory(Long memberId, Pageable pageable) {
    QueryWrapper<IntegralHistory> wrapper = new QueryWrapper<>();
    wrapper.eq("member_id", memberId);
    wrapper.orderByDesc("create_time");

    Page<IntegralHistory> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
    Page<IntegralHistory> historyPage = integralHistoryMapper.selectPage(page, wrapper);

    List<PointsHistoryVO> historyVOs = historyPage.getRecords().stream()
        .map(this::convertToPointsHistoryVO)
        .collect(Collectors.toList());

    return new PageImpl<>(historyVOs, pageable, historyPage.getTotal());
  }

  /**
   * 获取轮播图积分商品
   */
  public List<H5PointsProductVO> getBannerPointsProducts() {
    PointsProductQuery query = new PointsProductQuery();
    // 1-是轮播
    query.setIsBanner(1);
    // 1-上架
    query.setPublishStatus(1);

    List<PointsProduct> products = pointsProductService.selectList(query, null);

    return products.stream()
        // 限制10个
        .limit(10)
        .map(this::convertToPointsProductVO)
        .collect(Collectors.toList());
  }

  /**
   * 转换为积分商品VO
   */
  private H5PointsProductVO convertToPointsProductVO(PointsProduct product) {
    H5PointsProductVO vo = new H5PointsProductVO();
    vo.setId(product.getId());
    vo.setName(product.getName());
    vo.setPic(product.getPic());
    vo.setAlbumPics(product.getAlbumPics());
    vo.setPoints(product.getPoints());
    vo.setOriginalPrice(product.getOriginalPrice());
    vo.setProductType(product.getProductType());
    vo.setProductTypeName(getProductTypeName(product.getProductType()));
    vo.setTotalStock(product.getTotalStock());
    vo.setExchangedCount(product.getExchangedCount());
    vo.setExchangeLimit(product.getExchangeLimit());
    vo.setDescription(product.getDescription());
    vo.setDetailHtml(product.getDetailHtml());
    vo.setDetailMobileHtml(product.getDetailMobileHtml());
    vo.setIsBanner(product.getIsBanner());
    vo.setBannerTitle(product.getBannerTitle());
    vo.setIsLimitedTime(product.getIsLimitedTime());
    vo.setActivityStartTime(product.getActivityStartTime());
    vo.setActivityEndTime(product.getActivityEndTime());
    vo.setPublishStatus(product.getPublishStatus());
    vo.setSort(product.getSort());
    vo.setHotness(product.getHotness());
    vo.setCreateTime(product.getCreateTime());
    vo.setCategoryName(product.getProductCategoryName());
    vo.setBrandName(product.getBrandName());

    return vo;
  }

  /**
   * 转换为积分历史VO
   */
  private PointsHistoryVO convertToPointsHistoryVO(IntegralHistory history) {
    PointsHistoryVO vo = new PointsHistoryVO();
    vo.setId(history.getId());
    vo.setMemberId(history.getMemberId());
    vo.setChangeType(history.getOpType());
    vo.setChangeTypeName(getChangeTypeName(history.getOpType()));
    vo.setChangeCount(history.getAmount());
    // 当前积分需要单独计算
    vo.setCurrentPoints(null);
    vo.setSourceType(history.getSubOpType());
    vo.setSourceTypeName(getSourceTypeName(history.getSubOpType()));
    vo.setOrderId(history.getOrderId());
    // 积分历史表没有订单号字段
    vo.setOrderSn(null);
    // 积分历史表没有积分商品ID字段
    vo.setPointsProductId(null);
    // 积分历史表没有积分商品名称字段
    vo.setPointsProductName(null);
    // 使用来源类型作为备注
    vo.setNote(getSourceTypeName(history.getSubOpType()));
    vo.setCreateTime(history.getCreateTime());

    return vo;
  }

  /**
   * 获取商品类型名称
   */
  private String getProductTypeName(Integer productType) {
    if (productType == null)
      return "";
    switch (productType) {
      case 1:
        return "实物商品";
      case 2:
        return "虚拟商品";
      case 3:
        return "优惠券";
      case 4:
        return "服务";
      default:
        return "未知类型";
    }
  }

  /**
   * 获取积分变化类型名称
   */
  private String getChangeTypeName(Integer changeType) {
    if (changeType == null)
      return "";
    switch (changeType) {
      case 1:
        return "获得";
      case 2:
        return "消费";
      default:
        return "未知";
    }
  }

  /**
   * 获取积分来源类型名称
   */
  private String getSourceTypeName(Integer subOpType) {
    if (subOpType == null)
      return "";
    switch (subOpType) {
      case 11:
        return "签到获得";
      case 12:
        return "购物获得";
      case 21:
        return "退款扣除积分";
      case 22:
        return "兑换消费";
      default:
        return "其他";
    }
  }
}
