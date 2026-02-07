package com.overdue.manager.oms.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.overdue.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 积分兑换订单VO
 * 
 * @author zcc
 * @date 2024-01-15
 */
@Data
@ApiModel(description = "积分兑换订单VO")
public class PointsOrderVO {

  @ApiModelProperty("订单ID")
  private Long id;

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

  @ApiModelProperty("积分商品图片")
  private String pointsProductPic;

  @ApiModelProperty("兑换数量")
  private Integer quantity;

  @ApiModelProperty("消耗积分")
  private BigDecimal pointsUsed;

  @ApiModelProperty("订单状态：0->待发货；1->已发货；2->已收货；3->已完成；4->已取消")
  private Integer status;

  @ApiModelProperty("订单状态名称")
  private String statusName;

  @ApiModelProperty("收货人姓名")
  private String receiverName;

  @ApiModelProperty("收货人电话")
  private String receiverPhone;

  @ApiModelProperty("收货人邮编")
  private String receiverPostCode;

  @ApiModelProperty("省份/直辖市")
  private String receiverProvince;

  @ApiModelProperty("城市")
  private String receiverCity;

  @ApiModelProperty("区")
  private String receiverDistrict;

  @ApiModelProperty("详细地址")
  private String receiverDetailAddress;

  @ApiModelProperty("完整收货地址")
  private String fullAddress;

  @ApiModelProperty("物流公司")
  private String deliveryCompany;

  @ApiModelProperty("物流单号")
  private String deliverySn;

  @ApiModelProperty("发货时间")
  private LocalDateTime deliveryTime;

  @ApiModelProperty("确认收货时间")
  private LocalDateTime receiveTime;

  @ApiModelProperty("订单备注")
  private String note;

  @ApiModelProperty("商家备注")
  private String merchantNote;

  @ApiModelProperty("确认收货状态：0->未确认；1->已确认")
  private Integer confirmStatus;

  @ApiModelProperty("删除状态：0->未删除；1->已删除")
  private Integer deleteStatus;

  @ApiModelProperty("创建时间")
  private LocalDateTime createTime;

  @ApiModelProperty("更新时间")
  private LocalDateTime updateTime;
}
