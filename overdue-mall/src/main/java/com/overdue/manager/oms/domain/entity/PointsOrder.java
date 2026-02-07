package com.overdue.manager.oms.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.overdue.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.overdue.common.core.domain.BaseAudit;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 积分兑换订单对象 oms_points_order
 * 
 * @author zcc
 * @date 2024-01-15
 */
@ApiModel(description = "积分兑换订单对象")
@Data
@TableName("oms_points_order")
public class PointsOrder extends BaseAudit {
  private static final long serialVersionUID = 1L;

  @ApiModelProperty("订单ID")
  private Long id;

  @ApiModelProperty("订单编号")
  @Excel(name = "订单编号")
  private String orderSn;

  @ApiModelProperty("用户ID")
  @Excel(name = "用户ID")
  private Long memberId;

  @ApiModelProperty("用户昵称")
  @Excel(name = "用户昵称")
  private String memberUsername;

  @ApiModelProperty("积分商品ID")
  @Excel(name = "积分商品ID")
  private Long pointsProductId;

  @ApiModelProperty("积分商品名称")
  @Excel(name = "积分商品名称")
  private String pointsProductName;

  @ApiModelProperty("兑换数量")
  @Excel(name = "兑换数量")
  private Integer quantity;

  @ApiModelProperty("消耗积分")
  @Excel(name = "消耗积分")
  private BigDecimal pointsUsed;

  @ApiModelProperty("订单状态：0->待发货；1->已发货；2->已收货；3->已完成；4->已取消")
  @Excel(name = "订单状态")
  private Integer status;

  @ApiModelProperty("收货人姓名")
  @Excel(name = "收货人姓名")
  private String receiverName;

  @ApiModelProperty("收货人电话")
  @Excel(name = "收货人电话")
  private String receiverPhone;

  @ApiModelProperty("收货人邮编")
  @Excel(name = "收货人邮编")
  private String receiverPostCode;

  @ApiModelProperty("省份/直辖市")
  @Excel(name = "省份/直辖市")
  private String receiverProvince;

  @ApiModelProperty("城市")
  @Excel(name = "城市")
  private String receiverCity;

  @ApiModelProperty("区")
  @Excel(name = "区")
  private String receiverDistrict;

  @ApiModelProperty("详细地址")
  @Excel(name = "详细地址")
  private String receiverDetailAddress;

  @ApiModelProperty("物流公司")
  @Excel(name = "物流公司")
  private String deliveryCompany;

  @ApiModelProperty("物流单号")
  @Excel(name = "物流单号")
  private String deliverySn;

  @ApiModelProperty("发货时间")
  @Excel(name = "发货时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime deliveryTime;

  @ApiModelProperty("确认收货时间")
  @Excel(name = "确认收货时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime receiveTime;

  @ApiModelProperty("订单备注")
  @Excel(name = "订单备注")
  private String note;

  @ApiModelProperty("商家备注")
  @Excel(name = "商家备注")
  private String merchantNote;

  @ApiModelProperty("确认收货状态：0->未确认；1->已确认")
  @Excel(name = "确认收货状态")
  private Integer confirmStatus;

  @ApiModelProperty("删除状态：0->未删除；1->已删除")
  @Excel(name = "删除状态")
  private Integer deleteStatus;
}
