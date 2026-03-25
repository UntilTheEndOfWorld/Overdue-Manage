package com.overdue.notify.reminder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 到期提醒任务（入队 JSON，消费者反序列化后投递短信/邮件/订阅消息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverdueReminderTaskMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long itemId;
    private String itemName;
    /** yyyy-MM-dd */
    private String expiryDate;
    /**
     * T3=到期前第3天（即 expiry 为今天+3 天）<br>
     * T0=到期当天（expiry 为今天）
     */
    private String phase;
    private String phone;
    private String email;
    private String openid;
    /** 用户开关 reminderEnabled */
    private boolean reminderEnabled;
    private boolean wantSms;
    private boolean wantEmail;
    private boolean wantWx;
}
