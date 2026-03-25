package com.overdue.manager.item.domain.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新到期提醒设置
 *
 * @author overdue
 */
@Data
@ApiModel("到期提醒设置更新")
public class ReminderSettingsForm implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("提醒总开关 0关 1开")
    private String reminderEnabled;

    @ApiModelProperty("微信订阅消息 0关 1开")
    private String reminderSubscribe;

    @ApiModelProperty("短信提醒 0关 1开")
    private String reminderSms;

    @ApiModelProperty("邮件提醒 0关 1开")
    private String reminderEmail;

    @ApiModelProperty("联系手机号（可选，非空则更新 overdue_user.phone）")
    private String contactPhone;

    @ApiModelProperty("提醒邮箱（可选，非空则更新 overdue_user.email）")
    private String contactEmail;
}
