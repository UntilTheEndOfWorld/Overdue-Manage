package com.overdue.manager.oms.domain.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 积分兑换订单发货表单
 * 
 * @author zcc
 * @date 2024-01-15
 */
@Data
@ApiModel(description = "积分兑换订单发货表单")
public class DeliverPointsOrderForm {

  @ApiModelProperty(value = "订单ID", required = true)
  @NotNull(message = "订单ID不能为空")
  private Long orderId;

  @ApiModelProperty(value = "物流公司", required = true)
  @NotNull(message = "物流公司不能为空")
  private String expressName;

  @ApiModelProperty(value = "物流单号", required = true)
  @NotNull(message = "物流单号不能为空")
  private String expressSn;

  @ApiModelProperty("商家备注")
  private String merchantNote;
}
