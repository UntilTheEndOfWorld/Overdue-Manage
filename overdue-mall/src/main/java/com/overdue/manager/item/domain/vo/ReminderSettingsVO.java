package com.overdue.manager.item.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 到期提醒设置（小程序展示）
 *
 * @author overdue
 */
@Data
@ApiModel("到期提醒设置")
public class ReminderSettingsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("提醒总开关 0关 1开")
    private String reminderEnabled;

    @ApiModelProperty("微信订阅消息 0关 1开")
    private String reminderSubscribe;

    @ApiModelProperty("短信提醒 0关 1开")
    private String reminderSms;

    @ApiModelProperty("邮件提醒 0关 1开")
    private String reminderEmail;

    @ApiModelProperty("手机号脱敏展示")
    private String phoneMasked;

    @ApiModelProperty("是否已绑定手机号")
    private boolean phoneBound;

    @ApiModelProperty("邮箱脱敏展示")
    private String emailMasked;

    @ApiModelProperty("是否已填写邮箱")
    private boolean emailBound;
}
