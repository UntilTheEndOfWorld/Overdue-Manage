package com.overdue.manager.pms.domain.entity;

import java.math.BigDecimal;
import com.overdue.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.overdue.common.core.domain.BaseEntity;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 积分商品SKU信息对象 pms_points_product_sku
 * 
 * @author zcc
 */
@ApiModel(description = "积分商品SKU信息对象")
@Data
@TableName("pms_points_product_sku")
public class PointsProductSku extends BaseEntity {
  private static final long serialVersionUID = 1L;

  @ApiModelProperty("ID")
  private Long id;

  @ApiModelProperty("PRODUCT_ID")
  @Excel(name = "PRODUCT_ID")
  private Long productId;

  @ApiModelProperty("sku编码")
  @Excel(name = "sku编码")
  private String outSkuId;

  @ApiModelProperty("所需积分")
  @Excel(name = "所需积分")
  private BigDecimal points;

  @ApiModelProperty("原价（用于显示价值感）")
  @Excel(name = "原价")
  private BigDecimal originalPrice;

  @ApiModelProperty("展示图片")
  @Excel(name = "展示图片")
  private String pic;

  @ApiModelProperty("商品销售属性，json格式")
  @Excel(name = "商品销售属性，json格式")
  private String spData;

  @ApiModelProperty("库存数")
  @Excel(name = "库存数")
  private Integer stock;

  @ApiModelProperty("已兑换数量")
  @Excel(name = "已兑换数量")
  private Integer exchangedCount;

  @ApiModelProperty("兑换限制：每人限兑数量，0表示不限制")
  @Excel(name = "兑换限制")
  private Integer exchangeLimit;

  @ApiModelProperty("SKU描述")
  @Excel(name = "SKU描述")
  private String description;

  @ApiModelProperty("删除标志（0代表存在 2代表删除）")
  @Excel(name = "删除标志")
  private String delFlag;
}
