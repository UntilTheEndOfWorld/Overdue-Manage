package com.overdue.h5.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 积分历史记录VO
 * 
 * @author zcc
 * @date 2024-01-15
 */
@Data
@ApiModel(value = "积分历史记录VO")
public class PointsHistoryVO {

  @ApiModelProperty("记录ID")
  private Long id;

  @ApiModelProperty("用户ID")
  private Long memberId;

  @ApiModelProperty("积分变化类型：1-获得，2-消费")
  private Integer changeType;

  @ApiModelProperty("积分变化类型名称")
  private String changeTypeName;

  @ApiModelProperty("积分变化数量")
  private BigDecimal changeCount;

  @ApiModelProperty("变化后积分余额")
  private BigDecimal currentPoints;

  @ApiModelProperty("积分来源：1-购物获得，2-签到获得，3-评价获得，4-兑换消费，5-系统调整")
  private Integer sourceType;

  @ApiModelProperty("积分来源名称")
  private String sourceTypeName;

  @ApiModelProperty("关联订单ID")
  private Long orderId;

  @ApiModelProperty("关联订单号")
  private String orderSn;

  @ApiModelProperty("关联积分商品ID")
  private Long pointsProductId;

  @ApiModelProperty("积分商品名称")
  private String pointsProductName;

  @ApiModelProperty("备注说明")
  private String note;

  @ApiModelProperty("创建时间")
  private LocalDateTime createTime;
}
