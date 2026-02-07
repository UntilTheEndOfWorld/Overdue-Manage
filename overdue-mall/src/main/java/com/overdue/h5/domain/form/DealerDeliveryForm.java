package com.overdue.h5.domain.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 经销商发货表单
 */
@ApiModel("经销商发货表单")
@Data
public class DealerDeliveryForm {

  @ApiModelProperty("订单ID")
  @NotNull(message = "订单ID不能为空")
  private Long orderId;

  @ApiModelProperty("物流公司")
  private String deliveryCompany;

  @ApiModelProperty("物流单号")
  private String deliverySn;

  @ApiModelProperty("备注")
  private String note;
}
