package com.overdue.manager.pms.domain.vo;

import com.overdue.common.annotation.Excel;
import com.overdue.common.core.domain.BaseAudit;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 批量调价任务 数据视图对象
 * 
 * @author grocery
 * @date 2025-10-10
 */
@ApiModel(description = "批量调价任务视图对象")
@Data
public class PriceAdjustTaskVO extends BaseAudit {

  @ApiModelProperty("主键ID")
  private Long id;

  @ApiModelProperty("任务名称")
  private String taskName;

  @ApiModelProperty("调价类型：1-按固定金额，2-按百分比，3-按成本加价率")
  private Integer adjustType;

  @ApiModelProperty("调价类型名称")
  private String adjustTypeName;

  @ApiModelProperty("调价值（金额或百分比）")
  private BigDecimal adjustValue;

  @ApiModelProperty("目标类型：1-指定商品，2-指定分类，3-全部商品")
  private Integer targetType;

  @ApiModelProperty("目标类型名称")
  private String targetTypeName;

  @ApiModelProperty("目标ID列表（JSON数组）")
  private String targetIds;

  @ApiModelProperty("影响商品数量")
  private Integer affectCount;

  @ApiModelProperty("状态：0-待执行，1-执行中，2-已完成，3-失败")
  private Integer status;

  @ApiModelProperty("状态名称")
  private String statusName;

  @ApiModelProperty("执行时间")
  private LocalDateTime executeTime;

  @ApiModelProperty("调价原因")
  private String reason;

  @ApiModelProperty("操作人ID")
  private Long operatorId;

  @ApiModelProperty("操作人姓名")
  private String operatorName;
}
