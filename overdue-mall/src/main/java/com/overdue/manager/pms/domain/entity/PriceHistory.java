package com.overdue.manager.pms.domain.entity;

import java.math.BigDecimal;
import com.overdue.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.overdue.common.core.domain.BaseAudit;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

/**
 * 价格历史记录对象 pms_price_history
 * 
 * @author grocery
 * @date 2025-10-10
 */
@ApiModel(description = "价格历史记录对象")
@Data
@TableName("pms_price_history")
public class PriceHistory extends BaseAudit {
  private static final long serialVersionUID = 1L;

  @ApiModelProperty("主键ID")
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  @ApiModelProperty("商品ID")
  @Excel(name = "商品ID")
  private Long productId;

  @ApiModelProperty("SKU ID")
  @Excel(name = "SKU ID")
  private Long skuId;

  @ApiModelProperty("原价格")
  @Excel(name = "原价格")
  private BigDecimal oldPrice;

  @ApiModelProperty("新价格")
  @Excel(name = "新价格")
  private BigDecimal newPrice;

  @ApiModelProperty("价格变动")
  @Excel(name = "价格变动")
  private BigDecimal priceChange;

  @ApiModelProperty("变动类型：1-手动调价，2-批量调价，3-按比例调价")
  @Excel(name = "变动类型", readConverterExp = "1=手动调价,2=批量调价,3=按比例调价")
  private Integer changeType;

  @ApiModelProperty("调价原因")
  @Excel(name = "调价原因")
  private String changeReason;

  @ApiModelProperty("操作人ID")
  @Excel(name = "操作人ID")
  private Long operatorId;

  @ApiModelProperty("操作人姓名")
  @Excel(name = "操作人姓名")
  private String operatorName;
}
