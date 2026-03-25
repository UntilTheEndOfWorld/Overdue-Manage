package com.overdue.manager.item.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息表 overdue_user
 * <p>不继承 BaseEntity，避免 {@code params} Map 被 MyBatis 当作表字段插入。</p>
 *
 * @author overdue
 */
@ApiModel(description = "用户信息表")
@Data
@TableName("overdue_user")
public class OverdueUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
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

    @ApiModelProperty("提醒邮箱")
    private String email;

    @ApiModelProperty("提醒总开关 0关 1开")
    private String reminderEnabled;

    @ApiModelProperty("微信订阅消息 0关 1开")
    private String reminderSubscribe;

    @ApiModelProperty("短信提醒 0关 1开")
    private String reminderSms;

    @ApiModelProperty("邮件提醒 0关 1开")
    private String reminderEmail;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
