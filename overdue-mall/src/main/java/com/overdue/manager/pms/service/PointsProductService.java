package com.overdue.manager.pms.service;

import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.overdue.manager.pms.convert.PointsProductConvert;
import com.overdue.manager.pms.domain.entity.PointsProductSku;
import com.overdue.manager.pms.mapper.BrandMapper;
import com.overdue.manager.pms.mapper.PointsProductSkuMapper;
import com.overdue.manager.pms.domain.vo.PointsProductVO;
import com.github.pagehelper.PageHelper;
import com.overdue.common.utils.MemberSecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.overdue.manager.pms.mapper.PointsProductMapper;
import com.overdue.manager.pms.domain.entity.PointsProduct;
import com.overdue.manager.pms.domain.query.PointsProductQuery;
import org.springframework.transaction.annotation.Transactional;

/**
 * 积分商品信息Service业务层处理
 *
 * @author zcc
 */
@Service
@Slf4j
public class PointsProductService {
  @Autowired
  private PointsProductMapper pointsProductMapper;

  @Autowired
  private PointsProductSkuMapper pointsProductSkuMapper;

  @Autowired
  private BrandMapper brandMapper;

  @Autowired
  private PointsProductConvert convert;

  /**
   * 查询积分商品信息
   *
   * @param id 积分商品信息主键
   * @return 积分商品信息
   */
  public PointsProductVO selectById(Long id) {
    PointsProduct pointsProduct = pointsProductMapper.selectById(id);
    PointsProductVO pointsProductVO = convert.do2vo(pointsProduct);
    QueryWrapper<PointsProductSku> qw = new QueryWrapper<>();
    qw.eq("product_id", pointsProduct.getId());
    List<PointsProductSku> skus = pointsProductSkuMapper.selectList(qw);
    pointsProductVO.setSkuList(skus);
    return pointsProductVO;
  }

  /**
   * 查询积分商品信息列表
   *
   * @param query 查询条件
   * @param page  分页条件
   * @return 积分商品信息
   */
  public List<PointsProduct> selectList(PointsProductQuery query, Pageable page) {
    if (page != null) {
      PageHelper.startPage(page.getPageNumber() + 1, page.getPageSize());
    }
    QueryWrapper<PointsProduct> qw = new QueryWrapper<>();
    if (StringUtils.isNoneEmpty(query.getOrderField())) {
      // 将Java属性名转换为数据库字段名
      String dbField = convertToDbField(query.getOrderField());
      if (StringUtils.isNotEmpty(query.getOrderSort()) && "desc".equalsIgnoreCase(query.getOrderSort())) {
        qw.orderByDesc(dbField);
      } else {
        qw.orderByAsc(dbField);
      }
    } else {
      qw.orderByDesc("publish_status");
      qw.orderByAsc("sort");
    }
    Long categoryId = query.getCategoryId();
    if (categoryId != null) {
      qw.eq("category_id", categoryId);
    }
    Integer publishStatus = query.getPublishStatus();
    if (publishStatus != null) {
      qw.eq("publish_status", publishStatus);
    }
    Integer productType = query.getProductType();
    if (productType != null) {
      qw.eq("product_type", productType);
    }
    Integer isBanner = query.getIsBanner();
    if (isBanner != null) {
      qw.eq("is_banner", isBanner);
    }
    Integer isLimitedTime = query.getIsLimitedTime();
    if (isLimitedTime != null) {
      qw.eq("is_limited_time", isLimitedTime);
    }
    String search = query.getSearch();
    if (StringUtils.isNoneEmpty(search)) {
      qw.like("name", "%".concat(query.getSearch().trim()).concat("%"));
    }
    if (CollectionUtil.isNotEmpty(query.getExcludeProductIds())) {
      qw.notIn("id", query.getExcludeProductIds());
    }
    if (CollectionUtil.isNotEmpty(query.getIds())) {
      qw.in("id", query.getIds());
    }
    return pointsProductMapper.selectList(qw);
  }

  /**
   * 将Java属性名转换为数据库字段名
   */
  private String convertToDbField(String javaField) {
    switch (javaField) {
      case "createTime":
        return "create_time";
      case "updateTime":
        return "update_time";
      case "publishStatus":
        return "publish_status";
      case "outProductId":
        return "out_product_id";
      case "albumPics":
        return "album_pics";
      case "detailHtml":
        return "detail_html";
      case "detailMobileHtml":
        return "detail_mobile_html";
      case "brandName":
        return "brand_name";
      case "productCategoryName":
        return "product_category_name";
      case "productAttr":
        return "product_attr";
      case "isBanner":
        return "is_banner";
      case "bannerTitle":
        return "banner_title";
      case "supportExpress":
        return "support_express";
      case "productType":
        return "product_type";
      case "exchangeLimit":
        return "exchange_limit";
      case "totalStock":
        return "total_stock";
      case "exchangedCount":
        return "exchanged_count";
      case "activityStartTime":
        return "activity_start_time";
      case "activityEndTime":
        return "activity_end_time";
      case "isLimitedTime":
        return "is_limited_time";
      default:
        return javaField.toLowerCase();
    }
  }

  /**
   * 新增积分商品信息
   *
   * @param pointsProductVO 积分商品信息
   * @return 结果
   */
  @Transactional
  public int insert(PointsProductVO pointsProductVO) {
    PointsProduct pointsProduct = convert.vo2do(pointsProductVO);
    pointsProduct.setCreateTime(LocalDateTime.now());
    List<PointsProductSku> skuList = pointsProductVO.getSkuList();
    pointsProductMapper.insert(pointsProduct);
    if (skuList != null) {
      skuList.forEach(sku -> {
        sku.setProductId(pointsProduct.getId());
        sku.setCreateTime(LocalDateTime.now());
        pointsProductSkuMapper.insert(sku);
      });
    }
    return 1;
  }

  /**
   * 修改积分商品信息
   *
   * @param pointsProductVO 积分商品信息
   * @return 结果
   */
  @Transactional
  public int update(PointsProductVO pointsProductVO) {
    PointsProduct dbPointsProduct = pointsProductMapper.selectById(pointsProductVO.getId());
    List<Long> idList = pointsProductVO.getSkuList().stream().filter(it -> it.getId() != null).map(it -> it.getId())
        .collect(Collectors.toList());
    if (dbPointsProduct == null) {
      return 0;
    }
    Long userId = MemberSecurityUtils.getMemberId();
    PointsProduct pointsProduct = convert.vo2do(pointsProductVO);
    List<PointsProductSku> skuList = pointsProductVO.getSkuList();
    pointsProduct.setUpdateBy(userId);
    pointsProduct.setUpdateTime(LocalDateTime.now());
    pointsProductMapper.updateById(pointsProduct);
    // 查找库中所有的sku
    Map<String, Object> map = new HashMap<>();
    map.put("product_id", pointsProduct.getId());
    Map<Long, PointsProductSku> skuMap = pointsProductSkuMapper.selectByMap(map).stream()
        .collect(Collectors.toMap(it -> it.getId(), it -> it, (v1, v2) -> v2));
    // 针对已有的进行编辑
    List<PointsProductSku> updateList = pointsProductVO.getSkuList().stream().filter(it -> it.getId() != null)
        .collect(Collectors.toList());
    if (!CollectionUtil.isEmpty(updateList)) {
      updateList.forEach(sku -> {
        sku.setUpdateBy(userId);
        sku.setUpdateTime(LocalDateTime.now());
        pointsProductSkuMapper.updateById(sku);
      });
    }
    // 针对新增的进行插入
    List<PointsProductSku> insertList = pointsProductVO.getSkuList().stream().filter(it -> it.getId() == null)
        .collect(Collectors.toList());
    if (!CollectionUtil.isEmpty(insertList)) {
      insertList.forEach(sku -> {
        sku.setProductId(pointsProduct.getId());
        sku.setCreateBy(userId);
        sku.setCreateTime(LocalDateTime.now());
        pointsProductSkuMapper.insert(sku);
      });
    }
    // 针对删除的进行删除
    List<Long> deleteList = skuMap.keySet().stream().filter(it -> !idList.contains(it))
        .collect(Collectors.toList());
    if (!CollectionUtil.isEmpty(deleteList)) {
      pointsProductSkuMapper.deleteBatchIds(deleteList);
    }
    return 1;
  }

  /**
   * 批量删除积分商品信息
   *
   * @param ids 需要删除的积分商品信息主键集合
   * @return 结果
   */
  @Transactional
  public int deleteByIds(List<Long> ids) {
    // 先删除SKU
    QueryWrapper<PointsProductSku> skuWrapper = new QueryWrapper<>();
    skuWrapper.in("product_id", ids);
    pointsProductSkuMapper.delete(skuWrapper);
    // 再删除商品
    return pointsProductMapper.deleteBatchIds(ids);
  }

  /**
   * 删除积分商品信息信息
   *
   * @param id 积分商品信息主键
   * @return 结果
   */
  @Transactional
  public int deleteById(Long id) {
    // 先删除SKU
    QueryWrapper<PointsProductSku> skuWrapper = new QueryWrapper<>();
    skuWrapper.eq("product_id", id);
    pointsProductSkuMapper.delete(skuWrapper);
    // 再删除商品
    return pointsProductMapper.deleteById(id);
  }

  /**
   * 增加积分商品热度值
   *
   * @param productId 商品ID
   * @param quantity  增加的数量
   * @return 结果
   */
  public int increaseHotness(Long productId, Integer quantity) {
    return pointsProductMapper.increaseHotness(productId, quantity);
  }

  /**
   * 查询轮播图积分商品列表
   *
   * @param limit 限制数量
   * @return 轮播图积分商品集合
   */
  public List<PointsProduct> selectBannerProducts(Integer limit) {
    return pointsProductMapper.selectBannerProducts(limit);
  }

  /**
   * 增加兑换数量
   *
   * @param productId 商品ID
   * @param quantity  增加的数量
   * @return 结果
   */
  public int increaseExchangedCount(Long productId, Integer quantity) {
    return pointsProductMapper.increaseExchangedCount(productId, quantity);
  }
}
