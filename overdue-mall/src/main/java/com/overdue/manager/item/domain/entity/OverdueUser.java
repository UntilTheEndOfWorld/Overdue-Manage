package com.overdue.manager.item.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.overdue.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 用户信息表 overdue_user
 * 
 * @author overdue
 */
@ApiModel(description = "用户信息表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("overdue_user")
public class OverdueUser extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户ID")
    private Long id;

    @ApiModelProperty("微信OpenID")
    private String openid;

    @ApiModelProperty("微信UnionID")
    private String unionid;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("头像URL")
    private String avatarUrl;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("性别0-未知，1-男，2-女")
    private Integer gender;

    @ApiModelProperty("国家")
    private String country;

    @ApiModelProperty("省份")
    private String province;

    @ApiModelProperty("城市")
    private String city;

    @ApiModelProperty("状态（0-正常 1-停用）")
    private String status;
}
