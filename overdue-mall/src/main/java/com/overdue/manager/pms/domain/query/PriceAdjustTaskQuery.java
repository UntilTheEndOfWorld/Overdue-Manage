package com.overdue.manager.pms.domain.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 批量调价任务查询对象
 * 
 * @author grocery
 * @date 2025-10-10
 */
@ApiModel(description = "批量调价任务查询对象")
@Data
public class PriceAdjustTaskQuery {

  @ApiModelProperty("任务名称")
  private String taskName;

  @ApiModelProperty("调价类型：1-按固定金额，2-按百分比，3-按成本加价率")
  private Integer adjustType;

  @ApiModelProperty("状态：0-待执行，1-执行中，2-已完成，3-失败")
  private Integer status;

  @ApiModelProperty("操作人姓名")
  private String operatorName;

  @ApiModelProperty("开始时间")
  private String beginTime;

  @ApiModelProperty("结束时间")
  private String endTime;
}
