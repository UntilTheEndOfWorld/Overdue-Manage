package com.overdue.h5.domain.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;

/**
 * 积分兑换表单
 * 
 * @author zcc
 * @date 2024-01-15
 */
@Data
@ApiModel(value = "积分兑换表单")
public class PointsExchangeForm {

  @ApiModelProperty(value = "积分商品ID", required = true)
  @NotNull(message = "商品ID不能为空")
  private Long productId;

  @ApiModelProperty(value = "兑换数量", required = true)
  @NotNull(message = "兑换数量不能为空")
  @Min(value = 1, message = "兑换数量必须大于0")
  private Integer quantity = 1;

  @ApiModelProperty(value = "收货地址ID（实物商品需要）")
  private Long addressId;

  @ApiModelProperty(value = "备注")
  private String note;
}
