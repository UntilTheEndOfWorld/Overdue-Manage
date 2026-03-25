package com.overdue.notify.config;

import com.overdue.common.utils.StringUtils;
import com.overdue.system.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 从 sys_config 读取通知策略，缺省值保证未配置时仍可运行（偏保守：直发、阿里云短信、微信真发）。
 */
@Service
public class OverdueNotifyConfigReader {

    public static final String DEFAULT_QUEUE_TYPE = "direct";
    public static final String DEFAULT_QUEUE_REDIS_KEY = "overdue:notify:queue";
    public static final String DEFAULT_MQ_TOPIC = "overdue-notify";
    public static final String DEFAULT_RABBIT_EXCHANGE = "";
    public static final String DEFAULT_ROCKET_TAG = "notify";
    public static final String DEFAULT_SMS_PROVIDER = "aliyun";
    public static final String DEFAULT_EMAIL_PROVIDER = "log";
    public static final String DEFAULT_WX_PROVIDER = "wechat";

    @Autowired
    private ISysConfigService sysConfigService;

    public String getQueueType() {
        return normalize(getRaw(OverdueNotifyConfigKeys.QUEUE_TYPE), DEFAULT_QUEUE_TYPE);
    }

    public String getQueueRedisKey() {
        String v = getRaw(OverdueNotifyConfigKeys.QUEUE_REDIS_KEY);
        return StringUtils.isNotEmpty(v) ? v.trim() : DEFAULT_QUEUE_REDIS_KEY;
    }

    /** Kafka Topic / Rabbit routing key / RocketMQ topic 名 */
    public String getMqTopic() {
        String v = getRaw(OverdueNotifyConfigKeys.MQ_TOPIC);
        return StringUtils.isNotEmpty(v) ? v.trim() : DEFAULT_MQ_TOPIC;
    }

    public String getRabbitExchange() {
        String v = getRaw(OverdueNotifyConfigKeys.MQ_RABBIT_EXCHANGE);
        if (v == null) {
            return DEFAULT_RABBIT_EXCHANGE;
        }
        return v.trim();
    }

    public String getRocketTag() {
        String v = getRaw(OverdueNotifyConfigKeys.MQ_ROCKET_TAG);
        return StringUtils.isNotEmpty(v) ? v.trim() : DEFAULT_ROCKET_TAG;
    }

    /** 未配置则不发短信（避免误用默认模板） */
    public String getSmsReminderTemplateId() {
        String v = getRaw(OverdueNotifyConfigKeys.SMS_TEMPLATE_REMINDER);
        return v == null ? "" : v.trim();
    }

    /** 未配置则不发订阅消息 */
    public String getWxReminderTemplateId() {
        String v = getRaw(OverdueNotifyConfigKeys.WX_TEMPLATE_REMINDER);
        return v == null ? "" : v.trim();
    }

    public String getSmsProvider() {
        return normalize(getRaw(OverdueNotifyConfigKeys.SMS_PROVIDER), DEFAULT_SMS_PROVIDER);
    }

    public String getEmailProvider() {
        return normalize(getRaw(OverdueNotifyConfigKeys.EMAIL_PROVIDER), DEFAULT_EMAIL_PROVIDER);
    }

    public String getWxSubscribeProvider() {
        return normalize(getRaw(OverdueNotifyConfigKeys.WX_SUBSCRIBE_PROVIDER), DEFAULT_WX_PROVIDER);
    }

    private String getRaw(String key) {
        try {
            return sysConfigService.selectConfigByKey(key);
        } catch (Exception e) {
            return null;
        }
    }

    private static String normalize(String value, String defaultVal) {
        if (StringUtils.isEmpty(value)) {
            return defaultVal;
        }
        return value.trim().toLowerCase();
    }
}
