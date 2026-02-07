package com.overdue.manager.pms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.overdue.common.utils.StringUtils;
import com.overdue.manager.pms.domain.entity.PriceHistory;
import com.overdue.manager.pms.domain.entity.Product;
import com.overdue.manager.pms.domain.entity.Sku;
import com.overdue.manager.pms.domain.query.PriceHistoryQuery;
import com.overdue.manager.pms.domain.vo.PriceHistoryVO;
import com.overdue.manager.pms.mapper.PriceHistoryMapper;
import com.overdue.manager.pms.mapper.ProductMapper;
import com.overdue.manager.pms.mapper.SkuMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 价格历史记录Service
 * 
 * @author grocery
 * @date 2025-10-10
 */
@Slf4j
@Service
public class PriceHistoryService {

  @Autowired
  private PriceHistoryMapper priceHistoryMapper;

  @Autowired
  private ProductMapper productMapper;

  @Autowired
  private SkuMapper skuMapper;

  /**
   * 查询价格历史记录列表
   */
  public List<PriceHistory> selectList(PriceHistoryQuery query, Pageable page) {
    if (page != null) {
      PageHelper.startPage(page.getPageNumber() + 1, page.getPageSize());
    }
    LambdaQueryWrapper<PriceHistory> wrapper = buildQueryWrapper(query);
    return priceHistoryMapper.selectList(wrapper);
  }

  /**
   * 查询价格历史记录列表（带详细信息）
   */
  public List<PriceHistoryVO> selectVOList(PriceHistoryQuery query, Pageable page) {
    List<PriceHistory> list = selectList(query, page);
    return list.stream().map(this::convertToVO).collect(Collectors.toList());
  }

  /**
   * 根据ID查询价格历史记录
   */
  public PriceHistory selectById(Long id) {
    return priceHistoryMapper.selectById(id);
  }

  /**
   * 根据ID查询价格历史记录（带详细信息）
   */
  public PriceHistoryVO selectVOById(Long id) {
    PriceHistory priceHistory = selectById(id);
    return priceHistory != null ? convertToVO(priceHistory) : null;
  }

  /**
   * 新增价格历史记录
   */
  @Transactional
  public int insert(PriceHistory priceHistory) {
    priceHistory.setCreateTime(LocalDateTime.now());
    return priceHistoryMapper.insert(priceHistory);
  }

  /**
   * 记录价格变动（通用方法）
   */
  @Transactional
  public void recordPriceChange(Long productId, Long skuId, BigDecimal oldPrice,
      BigDecimal newPrice, Integer changeType, String reason,
      Long operatorId, String operatorName) {
    PriceHistory history = new PriceHistory();
    history.setProductId(productId);
    history.setSkuId(skuId);
    history.setOldPrice(oldPrice);
    history.setNewPrice(newPrice);
    history.setPriceChange(newPrice.subtract(oldPrice));
    history.setChangeType(changeType);
    history.setChangeReason(reason);
    history.setOperatorId(operatorId);
    history.setOperatorName(operatorName);
    history.setCreateTime(LocalDateTime.now());

    priceHistoryMapper.insert(history);
    log.info("记录价格变动历史：商品ID={}, SKU ID={}, 原价={}, 新价={}, 变动={}",
        productId, skuId, oldPrice, newPrice, history.getPriceChange());
  }

  /**
   * 根据商品ID查询价格历史
   */
  public List<PriceHistoryVO> selectByProductId(Long productId) {
    LambdaQueryWrapper<PriceHistory> wrapper = Wrappers.lambdaQuery();
    wrapper.eq(PriceHistory::getProductId, productId);
    wrapper.orderByDesc(PriceHistory::getCreateTime);
    List<PriceHistory> list = priceHistoryMapper.selectList(wrapper);
    return list.stream().map(this::convertToVO).collect(Collectors.toList());
  }

  /**
   * 根据SKU ID查询价格历史
   */
  public List<PriceHistoryVO> selectBySkuId(Long skuId) {
    LambdaQueryWrapper<PriceHistory> wrapper = Wrappers.lambdaQuery();
    wrapper.eq(PriceHistory::getSkuId, skuId);
    wrapper.orderByDesc(PriceHistory::getCreateTime);
    List<PriceHistory> list = priceHistoryMapper.selectList(wrapper);
    return list.stream().map(this::convertToVO).collect(Collectors.toList());
  }

  /**
   * 构建查询条件
   */
  private LambdaQueryWrapper<PriceHistory> buildQueryWrapper(PriceHistoryQuery query) {
    LambdaQueryWrapper<PriceHistory> wrapper = Wrappers.lambdaQuery();
    if (query != null) {
      wrapper.eq(query.getProductId() != null, PriceHistory::getProductId, query.getProductId());
      wrapper.eq(query.getSkuId() != null, PriceHistory::getSkuId, query.getSkuId());
      wrapper.eq(query.getChangeType() != null, PriceHistory::getChangeType, query.getChangeType());
      wrapper.like(StringUtils.isNotEmpty(query.getOperatorName()),
          PriceHistory::getOperatorName, query.getOperatorName());
      wrapper.ge(StringUtils.isNotEmpty(query.getBeginTime()),
          PriceHistory::getCreateTime, query.getBeginTime());
      wrapper.le(StringUtils.isNotEmpty(query.getEndTime()),
          PriceHistory::getCreateTime, query.getEndTime());
    }
    wrapper.orderByDesc(PriceHistory::getCreateTime);
    return wrapper;
  }

  /**
   * 转换为VO对象
   */
  private PriceHistoryVO convertToVO(PriceHistory priceHistory) {
    PriceHistoryVO vo = new PriceHistoryVO();
    BeanUtils.copyProperties(priceHistory, vo);

    // 补充商品信息
    if (priceHistory.getProductId() != null) {
      Product product = productMapper.selectById(priceHistory.getProductId());
      if (product != null) {
        vo.setProductName(product.getName());
      }
    }

    // 补充SKU信息
    if (priceHistory.getSkuId() != null) {
      Sku sku = skuMapper.selectById(priceHistory.getSkuId());
      if (sku != null) {
        vo.setSkuSpec(sku.getSpData());
      }
    }

    // 变动类型名称
    if (priceHistory.getChangeType() != null) {
      switch (priceHistory.getChangeType()) {
        case 1:
          vo.setChangeTypeName("手动调价");
          break;
        case 2:
          vo.setChangeTypeName("批量调价");
          break;
        case 3:
          vo.setChangeTypeName("按比例调价");
          break;
        default:
          vo.setChangeTypeName("未知");
      }
    }

    return vo;
  }
}
