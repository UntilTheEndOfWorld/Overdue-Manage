package com.overdue.manager.pms.domain.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 价格历史记录查询对象
 * 
 * @author grocery
 * @date 2025-10-10
 */
@ApiModel(description = "价格历史记录查询对象")
@Data
public class PriceHistoryQuery {

  @ApiModelProperty("商品ID")
  private Long productId;

  @ApiModelProperty("SKU ID")
  private Long skuId;

  @ApiModelProperty("变动类型：1-手动调价，2-批量调价，3-按比例调价")
  private Integer changeType;

  @ApiModelProperty("操作人姓名")
  private String operatorName;

  @ApiModelProperty("开始时间")
  private String beginTime;

  @ApiModelProperty("结束时间")
  private String endTime;
}
