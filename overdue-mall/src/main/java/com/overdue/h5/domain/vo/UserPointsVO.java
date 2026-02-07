package com.overdue.h5.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户积分信息VO
 * 
 * @author zcc
 * @date 2024-01-15
 */
@Data
@ApiModel(value = "用户积分信息VO")
public class UserPointsVO {

  @ApiModelProperty("用户ID")
  private Long memberId;

  @ApiModelProperty("用户昵称")
  private String nickname;

  @ApiModelProperty("当前积分余额")
  private BigDecimal currentPoints;

  @ApiModelProperty("累计获得积分")
  private BigDecimal totalEarnedPoints;

  @ApiModelProperty("累计消费积分")
  private BigDecimal totalSpentPoints;

  @ApiModelProperty("积分等级")
  private String pointsLevel;

  @ApiModelProperty("等级描述")
  private String levelDescription;
}
