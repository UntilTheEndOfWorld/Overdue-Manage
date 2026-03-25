package com.overdue.notify.reminder;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 消费者：启用前设置 overdue.notify.consumer.rabbit-enabled=true，并创建与 overdue.notify.mq.topic 同名的队列（默认交换机绑定）。
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "overdue.notify.consumer", name = "rabbit-enabled", havingValue = "true")
public class OverdueReminderRabbitConsumer {

    @Autowired
    private OverdueReminderDeliveryService deliveryService;

    @RabbitListener(queues = "${overdue.notify.mq.topic:overdue-notify}")
    public void listen(String body) {
        try {
            OverdueReminderTaskMessage msg = JSON.parseObject(body, OverdueReminderTaskMessage.class);
            deliveryService.deliver(msg);
        } catch (Exception e) {
            log.error("[reminder-rabbit] deliver failed", e);
        }
    }
}
