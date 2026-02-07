package com.overdue.manager.pms.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.overdue.common.utils.StringUtils;
import com.overdue.manager.pms.domain.entity.PriceAdjustTask;
import com.overdue.manager.pms.domain.entity.Product;
import com.overdue.manager.pms.domain.entity.Sku;
import com.overdue.manager.pms.domain.query.PriceAdjustTaskQuery;
import com.overdue.manager.pms.domain.vo.PriceAdjustTaskVO;
import com.overdue.manager.pms.mapper.PriceAdjustTaskMapper;
import com.overdue.manager.pms.mapper.ProductMapper;
import com.overdue.manager.pms.mapper.SkuMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 批量调价Service
 * 
 * @author grocery
 * @date 2025-10-10
 */
@Slf4j
@Service
public class PriceAdjustService {

  @Autowired
  private PriceAdjustTaskMapper taskMapper;

  @Autowired
  private ProductMapper productMapper;

  @Autowired
  private SkuMapper skuMapper;

  @Autowired
  private PriceHistoryService priceHistoryService;

  /**
   * 查询调价任务列表
   */
  public List<PriceAdjustTask> selectList(PriceAdjustTaskQuery query, Pageable page) {
    if (page != null) {
      PageHelper.startPage(page.getPageNumber() + 1, page.getPageSize());
    }
    LambdaQueryWrapper<PriceAdjustTask> wrapper = buildQueryWrapper(query);
    return taskMapper.selectList(wrapper);
  }

  /**
   * 查询调价任务列表（带详细信息）
   */
  public List<PriceAdjustTaskVO> selectVOList(PriceAdjustTaskQuery query, Pageable page) {
    List<PriceAdjustTask> list = selectList(query, page);
    return list.stream().map(this::convertToVO).collect(Collectors.toList());
  }

  /**
   * 根据ID查询调价任务
   */
  public PriceAdjustTask selectById(Long id) {
    return taskMapper.selectById(id);
  }

  /**
   * 根据ID查询调价任务（带详细信息）
   */
  public PriceAdjustTaskVO selectVOById(Long id) {
    PriceAdjustTask task = selectById(id);
    return task != null ? convertToVO(task) : null;
  }

  /**
   * 新增调价任务
   */
  @Transactional
  public int insert(PriceAdjustTask task) {
    task.setStatus(0); // 待执行
    task.setCreateTime(LocalDateTime.now());
    return taskMapper.insert(task);
  }

  /**
   * 批量调价（主要方法）
   */
  @Transactional
  public int batchAdjustPrice(PriceAdjustTaskVO taskVO, Long operatorId, String operatorName) {
    log.info("开始执行批量调价任务：{}", JSONUtil.toJsonStr(taskVO));

    // 创建任务记录
    PriceAdjustTask task = new PriceAdjustTask();
    BeanUtils.copyProperties(taskVO, task);
    task.setStatus(1); // 执行中
    task.setExecuteTime(LocalDateTime.now());
    task.setOperatorId(operatorId);
    task.setOperatorName(operatorName);
    task.setCreateTime(LocalDateTime.now());
    taskMapper.insert(task);

    int affectCount = 0;
    try {
      // 根据目标类型获取需要调价的商品
      List<Long> productIds = getTargetProductIds(taskVO);

      for (Long productId : productIds) {
        // 调整商品价格
        Product product = productMapper.selectById(productId);
        if (product != null && product.getPrice() != null) {
          BigDecimal oldPrice = product.getPrice();
          BigDecimal newPrice = calculateNewPrice(oldPrice, taskVO);

          if (newPrice.compareTo(BigDecimal.ZERO) > 0) {
            product.setPrice(newPrice);
            product.setUpdateTime(LocalDateTime.now());
            productMapper.updateById(product);

            // 记录价格历史
            priceHistoryService.recordPriceChange(
                productId, null, oldPrice, newPrice,
                2, taskVO.getReason(), operatorId, operatorName);
            affectCount++;
          }
        }

        // 调整SKU价格
        LambdaQueryWrapper<Sku> skuWrapper = Wrappers.lambdaQuery();
        skuWrapper.eq(Sku::getProductId, productId);
        List<Sku> skuList = skuMapper.selectList(skuWrapper);

        for (Sku sku : skuList) {
          if (sku.getPrice() != null) {
            BigDecimal oldSkuPrice = sku.getPrice();
            BigDecimal newSkuPrice = calculateNewPrice(oldSkuPrice, taskVO);

            if (newSkuPrice.compareTo(BigDecimal.ZERO) > 0) {
              sku.setPrice(newSkuPrice);
              sku.setUpdateTime(LocalDateTime.now());
              skuMapper.updateById(sku);

              // 记录SKU价格历史
              priceHistoryService.recordPriceChange(
                  productId, sku.getId(), oldSkuPrice, newSkuPrice,
                  2, taskVO.getReason(), operatorId, operatorName);
            }
          }
        }
      }

      // 更新任务状态为已完成
      task.setStatus(2);
      task.setAffectCount(affectCount);
      task.setUpdateTime(LocalDateTime.now());
      taskMapper.updateById(task);

      log.info("批量调价任务执行完成，影响商品数：{}", affectCount);
      return affectCount;

    } catch (Exception e) {
      log.error("批量调价任务执行失败", e);
      // 更新任务状态为失败
      task.setStatus(3);
      task.setUpdateTime(LocalDateTime.now());
      taskMapper.updateById(task);
      throw new RuntimeException("批量调价失败：" + e.getMessage());
    }
  }

  /**
   * 快速调价（单个商品或SKU）
   */
  @Transactional
  public void quickAdjustPrice(Long productId, Long skuId, BigDecimal newPrice,
      String reason, Long operatorId, String operatorName) {
    if (skuId != null) {
      // 调整SKU价格
      Sku sku = skuMapper.selectById(skuId);
      if (sku != null) {
        BigDecimal oldPrice = sku.getPrice();
        sku.setPrice(newPrice);
        sku.setUpdateTime(LocalDateTime.now());
        skuMapper.updateById(sku);

        priceHistoryService.recordPriceChange(
            sku.getProductId(), skuId, oldPrice, newPrice,
            1, reason, operatorId, operatorName);
      }
    } else if (productId != null) {
      // 调整商品价格
      Product product = productMapper.selectById(productId);
      if (product != null) {
        BigDecimal oldPrice = product.getPrice();
        product.setPrice(newPrice);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);

        priceHistoryService.recordPriceChange(
            productId, null, oldPrice, newPrice,
            1, reason, operatorId, operatorName);
      }
    }
  }

  /**
   * 根据目标类型获取商品ID列表
   */
  private List<Long> getTargetProductIds(PriceAdjustTaskVO taskVO) {
    LambdaQueryWrapper<Product> wrapper = Wrappers.lambdaQuery();

    if (taskVO.getTargetType() == 1) {
      // 指定商品
      List<Long> targetIds = JSONUtil.toList(taskVO.getTargetIds(), Long.class);
      wrapper.in(Product::getId, targetIds);
    } else if (taskVO.getTargetType() == 2) {
      // 指定分类
      List<Long> categoryIds = JSONUtil.toList(taskVO.getTargetIds(), Long.class);
      wrapper.in(Product::getCategoryId, categoryIds);
    }
    // targetType == 3 表示全部商品，不需要额外条件

    List<Product> products = productMapper.selectList(wrapper);
    return products.stream().map(Product::getId).collect(Collectors.toList());
  }

  /**
   * 计算新价格
   */
  private BigDecimal calculateNewPrice(BigDecimal oldPrice, PriceAdjustTaskVO taskVO) {
    BigDecimal newPrice;

    switch (taskVO.getAdjustType()) {
      case 1:
        // 按固定金额调整
        newPrice = oldPrice.add(taskVO.getAdjustValue());
        break;
      case 2:
        // 按百分比调整
        BigDecimal rate = taskVO.getAdjustValue().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal change = oldPrice.multiply(rate);
        newPrice = oldPrice.add(change);
        break;
      case 3:
        // 按成本加价率（需要商品有成本价）
        // 这里简化处理，如果没有成本价则按百分比调整
        BigDecimal profitRate = taskVO.getAdjustValue().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        newPrice = oldPrice.multiply(BigDecimal.ONE.add(profitRate));
        break;
      default:
        newPrice = oldPrice;
    }

    // 保留两位小数
    return newPrice.setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * 构建查询条件
   */
  private LambdaQueryWrapper<PriceAdjustTask> buildQueryWrapper(PriceAdjustTaskQuery query) {
    LambdaQueryWrapper<PriceAdjustTask> wrapper = Wrappers.lambdaQuery();
    if (query != null) {
      wrapper.like(StringUtils.isNotEmpty(query.getTaskName()),
          PriceAdjustTask::getTaskName, query.getTaskName());
      wrapper.eq(query.getAdjustType() != null,
          PriceAdjustTask::getAdjustType, query.getAdjustType());
      wrapper.eq(query.getStatus() != null,
          PriceAdjustTask::getStatus, query.getStatus());
      wrapper.like(StringUtils.isNotEmpty(query.getOperatorName()),
          PriceAdjustTask::getOperatorName, query.getOperatorName());
      wrapper.ge(StringUtils.isNotEmpty(query.getBeginTime()),
          PriceAdjustTask::getCreateTime, query.getBeginTime());
      wrapper.le(StringUtils.isNotEmpty(query.getEndTime()),
          PriceAdjustTask::getCreateTime, query.getEndTime());
    }
    wrapper.orderByDesc(PriceAdjustTask::getCreateTime);
    return wrapper;
  }

  /**
   * 转换为VO对象
   */
  private PriceAdjustTaskVO convertToVO(PriceAdjustTask task) {
    PriceAdjustTaskVO vo = new PriceAdjustTaskVO();
    BeanUtils.copyProperties(task, vo);

    // 调价类型名称
    if (task.getAdjustType() != null) {
      switch (task.getAdjustType()) {
        case 1:
          vo.setAdjustTypeName("按固定金额");
          break;
        case 2:
          vo.setAdjustTypeName("按百分比");
          break;
        case 3:
          vo.setAdjustTypeName("按成本加价率");
          break;
      }
    }

    // 目标类型名称
    if (task.getTargetType() != null) {
      switch (task.getTargetType()) {
        case 1:
          vo.setTargetTypeName("指定商品");
          break;
        case 2:
          vo.setTargetTypeName("指定分类");
          break;
        case 3:
          vo.setTargetTypeName("全部商品");
          break;
      }
    }

    // 状态名称
    if (task.getStatus() != null) {
      switch (task.getStatus()) {
        case 0:
          vo.setStatusName("待执行");
          break;
        case 1:
          vo.setStatusName("执行中");
          break;
        case 2:
          vo.setStatusName("已完成");
          break;
        case 3:
          vo.setStatusName("失败");
          break;
      }
    }

    return vo;
  }
}
