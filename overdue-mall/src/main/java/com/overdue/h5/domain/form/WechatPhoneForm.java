package com.overdue.h5.domain.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 微信手机号表单
 */
@Data
@ApiModel("微信手机号表单")
public class WechatPhoneForm {

  @ApiModelProperty("微信登录code")
  @NotBlank(message = "微信登录code不能为空")
  private String code;

  @ApiModelProperty("手机号加密数据")
  @NotBlank(message = "手机号加密数据不能为空")
  private String encryptedData;

  @ApiModelProperty("加密算法的初始向量")
  @NotBlank(message = "加密算法的初始向量不能为空")
  private String iv;
}
