package com.overdue.manager.pms.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.overdue.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.overdue.common.core.domain.BaseAudit;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

/**
 * 批量调价任务对象 pms_price_adjust_task
 * 
 * @author grocery
 * @date 2025-10-10
 */
@ApiModel(description = "批量调价任务对象")
@Data
@TableName("pms_price_adjust_task")
public class PriceAdjustTask extends BaseAudit {
  private static final long serialVersionUID = 1L;

  @ApiModelProperty("主键ID")
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  @ApiModelProperty("任务名称")
  @Excel(name = "任务名称")
  private String taskName;

  @ApiModelProperty("调价类型：1-按固定金额，2-按百分比，3-按成本加价率")
  @Excel(name = "调价类型", readConverterExp = "1=按固定金额,2=按百分比,3=按成本加价率")
  private Integer adjustType;

  @ApiModelProperty("调价值（金额或百分比）")
  @Excel(name = "调价值")
  private BigDecimal adjustValue;

  @ApiModelProperty("目标类型：1-指定商品，2-指定分类，3-全部商品")
  @Excel(name = "目标类型", readConverterExp = "1=指定商品,2=指定分类,3=全部商品")
  private Integer targetType;

  @ApiModelProperty("目标ID列表（JSON数组）")
  @Excel(name = "目标ID列表")
  private String targetIds;

  @ApiModelProperty("影响商品数量")
  @Excel(name = "影响商品数量")
  private Integer affectCount;

  @ApiModelProperty("状态：0-待执行，1-执行中，2-已完成，3-失败")
  @Excel(name = "状态", readConverterExp = "0=待执行,1=执行中,2=已完成,3=失败")
  private Integer status;

  @ApiModelProperty("执行时间")
  @Excel(name = "执行时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime executeTime;

  @ApiModelProperty("调价原因")
  @Excel(name = "调价原因")
  private String reason;

  @ApiModelProperty("操作人ID")
  @Excel(name = "操作人ID")
  private Long operatorId;

  @ApiModelProperty("操作人姓名")
  @Excel(name = "操作人姓名")
  private String operatorName;
}
