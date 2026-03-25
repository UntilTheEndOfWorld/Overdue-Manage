package com.overdue.notify.reminder;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka 消费者：启用前请在 application.yml 设置 overdue.notify.consumer.kafka-enabled=true 并配置 spring.kafka.bootstrap-servers。
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "overdue.notify.consumer", name = "kafka-enabled", havingValue = "true")
public class OverdueReminderKafkaConsumer {

    @Autowired
    private OverdueReminderDeliveryService deliveryService;

    @KafkaListener(
        topics = "${overdue.notify.mq.topic:overdue-notify}",
        groupId = "${overdue.notify.kafka.group-id:overdue-reminder-consumer}"
    )
    public void listen(String payload) {
        try {
            OverdueReminderTaskMessage msg = JSON.parseObject(payload, OverdueReminderTaskMessage.class);
            deliveryService.deliver(msg);
        } catch (Exception e) {
            log.error("[reminder-kafka] deliver failed", e);
        }
    }
}
