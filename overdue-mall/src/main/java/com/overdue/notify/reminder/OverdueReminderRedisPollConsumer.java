package com.overdue.notify.reminder;

import com.alibaba.fastjson.JSON;
import com.overdue.common.core.redis.RedisCache;
import com.overdue.notify.config.OverdueNotifyConfigReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Redis 列表消费者：与 queue.type=redis 配合，RPOP 后投递短信/邮件。
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "overdue.notify.consumer", name = "redis-poll-enabled", havingValue = "true",
    matchIfMissing = true)
public class OverdueReminderRedisPollConsumer {

    private static final int MAX_BATCH = 100;

    @Autowired
    private OverdueNotifyConfigReader configReader;

    @Autowired(required = false)
    private RedisCache redisCache;

    @Autowired
    private OverdueReminderDeliveryService deliveryService;

    @Scheduled(fixedDelayString = "${overdue.notify.consumer.redis-poll-ms:5000}")
    public void poll() {
        if (!"redis".equals(configReader.getQueueType())) {
            return;
        }
        if (redisCache == null) {
            return;
        }
        String key = configReader.getQueueRedisKey();
        for (int i = 0; i < MAX_BATCH; i++) {
            String json = redisCache.rightPopCacheList(key);
            if (json == null) {
                break;
            }
            try {
                OverdueReminderTaskMessage msg = JSON.parseObject(json, OverdueReminderTaskMessage.class);
                deliveryService.deliver(msg);
            } catch (Exception e) {
                log.error("[reminder-redis] deliver failed payload={}", json, e);
            }
        }
    }
}
