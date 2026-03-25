package com.overdue.manager.item.domain.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新个人资料
 *
 * @author overdue
 */
@Data
@ApiModel("更新用户资料")
public class OverdueProfileForm implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("头像URL（上传后地址）")
    private String avatarUrl;
}
