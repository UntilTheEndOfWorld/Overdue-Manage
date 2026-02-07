package com.overdue.manager.pms.domain.vo;

import com.overdue.common.annotation.Excel;
import com.overdue.common.core.domain.BaseAudit;
import com.overdue.common.core.domain.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 积分商品SKU信息 数据视图对象
 * 
 * @author zcc
 */
@Data
public class PointsProductSkuVO extends BaseAudit {
  /** ID */
  private Long id;
  /** PRODUCT_ID */
  @Excel(name = "PRODUCT_ID")
  private Long productId;
  /** sku编码 */
  @Excel(name = "sku编码")
  private String outSkuId;
  /** 所需积分 */
  @Excel(name = "所需积分")
  private BigDecimal points;
  /** 原价（用于显示价值感） */
  @Excel(name = "原价")
  private BigDecimal originalPrice;
  /** 展示图片 */
  @Excel(name = "展示图片")
  private String pic;
  /** 商品销售属性，json格式 */
  @Excel(name = "商品销售属性，json格式")
  private String spData;
  /** 库存数 */
  @Excel(name = "库存数")
  private Integer stock;
  /** 已兑换数量 */
  @Excel(name = "已兑换数量")
  private Integer exchangedCount;
  /** 兑换限制：每人限兑数量，0表示不限制 */
  @Excel(name = "兑换限制")
  private Integer exchangeLimit;
  /** SKU描述 */
  @Excel(name = "SKU描述")
  private String description;

  /** 删除标志（0代表存在 2代表删除） */
  @Excel(name = "删除标志")
  private String delFlag;
}
