package com.rtb.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class HourlyKafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object>
    hourlyKafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> consumerFactory,
            KafkaTemplate<String, String> kafkaTemplate) {

        var factory =
                new ConcurrentKafkaListenerContainerFactory<Object, Object>();

        // 先应用 application.yml 中的 Kafka 消费配置
        configurer.configure(factory, consumerFactory);

        // 重试耗尽后，把失败消息发送到计费死信 Topic
        var recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(
                        record.topic() + ".hourly.DLT",
                        record.partition()
                )
        );

        // 死信发送失败时必须报错，不能当成已经妥善处理
        recoverer.setFailIfSendResultIsError(true);

        // 间隔 1 秒，最多重试两次：总共尝试三次
        var errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(1000L, 2L)
        );

        // 这些错误重复执行通常也不会变好，直接进入死信处理
        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                JsonProcessingException.class
        );

        errorHandler.setRetryListeners(
                (record, exception, deliveryAttempt) ->
                        log.warn(
                                "[learning-hourly]消费失败，"
                                        + "topic={}, partition={}, offset={}, "
                                        + "attempt={}, error={}",
                                record.topic(),
                                record.partition(),
                                record.offset(),
                                deliveryAttempt,
                                exception.getMessage()
                        )
        );

        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}
