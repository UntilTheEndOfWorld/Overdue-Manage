package com.overdue.manager.item.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 到期提醒投递记录（去重）
 */
@Data
@TableName("overdue_reminder_sent")
public class OverdueReminderSent implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long itemId;
    /** T3=到期前3天，T0=到期当天 */
    private String phase;
    private LocalDate remindDate;
    private LocalDateTime createTime;
}
