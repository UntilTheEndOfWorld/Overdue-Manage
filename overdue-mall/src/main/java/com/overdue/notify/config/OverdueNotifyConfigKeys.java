package com.overdue.notify.config;

/**
 * 到期提醒 / 通知相关 sys_config 键名（参数管理里可增改，此处为约定常量）。
 * <p>
 * 取值说明见 {@link OverdueNotifyConfigReader} 与 doc/overdue_notify_sys_config.sql。
 */
public final class OverdueNotifyConfigKeys {

    private OverdueNotifyConfigKeys() {
    }

    /**
     * 异步投递方式：direct | redis | rabbitmq | kafka | rocketmq | none
     */
    public static final String QUEUE_TYPE = "overdue.notify.queue.type";

    /**
     * Redis 队列键（queue.type=redis 时使用）
     */
    public static final String QUEUE_REDIS_KEY = "overdue.notify.queue.redis.key";

    /**
     * Kafka Topic / Rabbit 路由键 / RocketMQ Topic 名（rabbitmq|kafka|rocketmq 时使用）
     */
    public static final String MQ_TOPIC = "overdue.notify.mq.topic";

    /**
     * RabbitMQ 交换机（空字符串表示默认交换机，仅按 routingKey 投递）
     */
    public static final String MQ_RABBIT_EXCHANGE = "overdue.notify.mq.rabbit.exchange";

    /**
     * RocketMQ Tag（destination 为 topic:tag）
     */
    public static final String MQ_ROCKET_TAG = "overdue.notify.mq.rocket.tag";

    /** 阿里云短信模板 ID（到期提醒，变量需与代码中传入一致） */
    public static final String SMS_TEMPLATE_REMINDER = "overdue.notify.sms.template.reminder";

    /** 微信小程序订阅消息模板 ID（到期提醒） */
    public static final String WX_TEMPLATE_REMINDER = "overdue.notify.wx.template.reminder";

    /**
     * 短信通道：aliyun | noop（不发送）| log（仅打日志，用于联调）
     */
    public static final String SMS_PROVIDER = "overdue.notify.sms.provider";

    /**
     * 邮件通道：log | noop | smtp（预留，需再接入 JavaMail）
     */
    public static final String EMAIL_PROVIDER = "overdue.notify.email.provider";

    /**
     * 微信订阅消息：wechat（调微信 API）| noop | log
     */
    public static final String WX_SUBSCRIBE_PROVIDER = "overdue.notify.wx.subscribe.provider";
}
