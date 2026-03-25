package com.overdue.notify.reminder;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * RocketMQ 消费者：启用前设置 overdue.notify.consumer.rocket-enabled=true 与 rocketmq.name-server；订阅 topic 与生产者一致，tag 默认 *。
 */
@Service
@Slf4j
@ConditionalOnProperty(prefix = "overdue.notify.consumer", name = "rocket-enabled", havingValue = "true")
@RocketMQMessageListener(
    topic = "${overdue.notify.mq.topic:overdue-notify}",
    consumerGroup = "${overdue.notify.rocket.consumer-group:overdue-reminder-consumer}",
    selectorExpression = "*"
)
public class OverdueReminderRocketConsumer implements RocketMQListener<String> {

    @Autowired
    private OverdueReminderDeliveryService deliveryService;

    @Override
    public void onMessage(String message) {
        try {
            OverdueReminderTaskMessage msg = JSON.parseObject(message, OverdueReminderTaskMessage.class);
            deliveryService.deliver(msg);
        } catch (Exception e) {
            log.error("[reminder-rocket] deliver failed", e);
        }
    }
}
