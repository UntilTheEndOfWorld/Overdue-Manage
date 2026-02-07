package com.overdue.h5.domain.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 微信用户信息登录表单
 */
@Data
@ApiModel("微信用户信息登录表单")
public class WechatUserInfoLoginForm {

    @ApiModelProperty("微信登录code")
    @NotBlank(message = "微信登录code不能为空")
    private String code;

    @ApiModelProperty("用户昵称")
    private String nickname;

    @ApiModelProperty("用户头像")
    private String avatarUrl;

    @ApiModelProperty("用户性别")
    private Integer gender;

    @ApiModelProperty("用户国家")
    private String country;

    @ApiModelProperty("用户省份")
    private String province;

    @ApiModelProperty("用户城市")
    private String city;
}
