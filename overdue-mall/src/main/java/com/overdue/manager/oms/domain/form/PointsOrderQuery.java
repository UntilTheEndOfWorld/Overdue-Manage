package com.overdue.manager.oms.domain.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分兑换订单查询表单
 * 
 * @author zcc
 * @date 2024-01-15
 */
@Data
@ApiModel(description = "积分兑换订单查询表单")
public class PointsOrderQuery {

  @ApiModelProperty("订单编号")
  private String orderSn;

  @ApiModelProperty("用户ID")
  private Long memberId;

  @ApiModelProperty("用户昵称")
  private String memberUsername;

  @ApiModelProperty("积分商品ID")
  private Long pointsProductId;

  @ApiModelProperty("积分商品名称")
  private String pointsProductName;

  @ApiModelProperty("订单状态：0->待发货；1->已发货；2->已收货；3->已完成；4->已取消")
  private Integer status;

  @ApiModelProperty("收货人姓名")
  private String receiverName;

  @ApiModelProperty("收货人电话")
  private String receiverPhone;

  @ApiModelProperty("创建时间开始")
  private LocalDateTime createTimeStart;

  @ApiModelProperty("创建时间结束")
  private LocalDateTime createTimeEnd;
}
