package com.overdue.notify.facade;

import com.alibaba.fastjson.JSON;
import com.overdue.common.core.redis.RedisCache;
import com.overdue.notify.config.OverdueNotifyConfigReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 按 sys_config {@code overdue.notify.queue.type} 选择异步投递方式。
 * <ul>
 *   <li>direct：不排队，由调用方同步调用各 Facade</li>
 *   <li>redis：Redis 列表 LPUSH</li>
 *   <li>rabbitmq：需配置 spring.rabbitmq.*，默认交换机 + routingKey={@link OverdueNotifyConfigReader#getMqTopic()}</li>
 *   <li>kafka：需配置 spring.kafka.bootstrap-servers，Topic={@link OverdueNotifyConfigReader#getMqTopic()}</li>
 *   <li>rocketmq：需配置 rocketmq.name-server，destination=topic:tag</li>
 *   <li>none：不写队列</li>
 * </ul>
 */
@Service
@Slf4j
public class OverdueNotifyAsyncFacade {

    private static final int KAFKA_SEND_TIMEOUT_SEC = 10;

    @Autowired
    private OverdueNotifyConfigReader configReader;

    @Autowired(required = false)
    private RedisCache redisCache;

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;

    public boolean isDirectMode() {
        return "direct".equals(configReader.getQueueType());
    }

    public boolean isNoneMode() {
        return "none".equals(configReader.getQueueType());
    }

    /**
     * 是否使用任一种消息中间件或 Redis 入队（非 direct/none）。
     */
    public boolean isEnqueueMode() {
        String t = configReader.getQueueType();
        return "redis".equals(t) || "rabbitmq".equals(t) || "kafka".equals(t) || "rocketmq".equals(t);
    }

    /**
     * 将通知任务序列化为 JSON 入队；仅当 queue.type 为 redis/rabbitmq/kafka/rocketmq 时返回 true 表示投递成功。
     */
    public boolean enqueue(Object taskPayload) {
        String json = JSON.toJSONString(taskPayload);
        String type = configReader.getQueueType();
        switch (type) {
            case "redis":
                return enqueueRedis(json);
            case "rabbitmq":
                return enqueueRabbit(json);
            case "kafka":
                return enqueueKafka(json);
            case "rocketmq":
                return enqueueRocket(json);
            default:
                return false;
        }
    }

    private boolean enqueueRedis(String json) {
        if (redisCache == null) {
            log.warn("[notify-queue] RedisCache missing, cannot enqueue");
            return false;
        }
        String key = configReader.getQueueRedisKey();
        try {
            long len = redisCache.leftPushCacheList(key, json);
            log.debug("[notify-queue] redis LPUSH {} size~{}", key, len);
            return true;
        } catch (Exception e) {
            log.error("[notify-queue] redis enqueue failed", e);
            return false;
        }
    }

    private boolean enqueueRabbit(String json) {
        if (rabbitTemplate == null) {
            log.warn("[notify-queue] RabbitTemplate missing (check spring.rabbitmq.*), cannot enqueue");
            return false;
        }
        String routingKey = configReader.getMqTopic();
        String exchange = configReader.getRabbitExchange();
        try {
            if (exchange == null || exchange.isEmpty()) {
                rabbitTemplate.convertAndSend(routingKey, json);
            } else {
                rabbitTemplate.convertAndSend(exchange, routingKey, json);
            }
            log.debug("[notify-queue] rabbitmq sent exchange={} routingKey={}", exchange, routingKey);
            return true;
        } catch (Exception e) {
            log.error("[notify-queue] rabbitmq enqueue failed", e);
            return false;
        }
    }

    private boolean enqueueKafka(String json) {
        if (kafkaTemplate == null) {
            log.warn("[notify-queue] KafkaTemplate missing (check spring.kafka.bootstrap-servers), cannot enqueue");
            return false;
        }
        String topic = configReader.getMqTopic();
        try {
            ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, json);
            future.get(KAFKA_SEND_TIMEOUT_SEC, TimeUnit.SECONDS);
            log.debug("[notify-queue] kafka sent topic={}", topic);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[notify-queue] kafka enqueue interrupted", e);
            return false;
        } catch (ExecutionException | TimeoutException e) {
            log.error("[notify-queue] kafka enqueue failed", e);
            return false;
        }
    }

    private boolean enqueueRocket(String json) {
        if (rocketMQTemplate == null) {
            log.warn("[notify-queue] RocketMQTemplate missing (check rocketmq.name-server), cannot enqueue");
            return false;
        }
        String topic = configReader.getMqTopic();
        String tag = configReader.getRocketTag();
        String destination = topic + ":" + tag;
        try {
            rocketMQTemplate.syncSend(destination, json);
            log.debug("[notify-queue] rocketmq sent destination={}", destination);
            return true;
        } catch (Exception e) {
            log.error("[notify-queue] rocketmq enqueue failed", e);
            return false;
        }
    }
}
