package com.overdue.manager.pms.domain.vo;

import com.overdue.common.annotation.Excel;
import com.overdue.common.core.domain.BaseAudit;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 价格历史记录 数据视图对象
 * 
 * @author grocery
 * @date 2025-10-10
 */
@ApiModel(description = "价格历史记录视图对象")
@Data
public class PriceHistoryVO extends BaseAudit {

  @ApiModelProperty("主键ID")
  private Long id;

  @ApiModelProperty("商品ID")
  private Long productId;

  @ApiModelProperty("商品名称")
  private String productName;

  @ApiModelProperty("SKU ID")
  private Long skuId;

  @ApiModelProperty("SKU规格")
  private String skuSpec;

  @ApiModelProperty("原价格")
  private BigDecimal oldPrice;

  @ApiModelProperty("新价格")
  private BigDecimal newPrice;

  @ApiModelProperty("价格变动")
  private BigDecimal priceChange;

  @ApiModelProperty("变动类型：1-手动调价，2-批量调价，3-按比例调价")
  private Integer changeType;

  @ApiModelProperty("变动类型名称")
  private String changeTypeName;

  @ApiModelProperty("调价原因")
  private String changeReason;

  @ApiModelProperty("操作人ID")
  private Long operatorId;

  @ApiModelProperty("操作人姓名")
  private String operatorName;
}
