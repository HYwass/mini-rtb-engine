package com.rtb.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtb.dto.BidEvent;
import com.rtb.service.BillingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BillingConsumer {

    private final ObjectMapper objectMapper;
    private final BillingService billingService;

    public BillingConsumer(
            ObjectMapper objectMapper,
            BillingService billingService) {

        this.objectMapper = objectMapper;
        this.billingService = billingService;
    }

    @KafkaListener(
            topics = "${rtb.kafka.topic}",
            groupId = BillingService.CONSUMER_GROUP,
            containerFactory = "billingKafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> record)
            throws JsonProcessingException {

        BidEvent event = objectMapper.readValue(
                record.value(),
                BidEvent.class
        );

        boolean processed = billingService.process(event);

        if (processed) {
            log.info(
                    "[learning-billing]计费事件处理完成，eventId={}, hasAd={}, partition={}, offset={}",
                    event.getEventId(),
                    event.isHasAd(),
                    record.partition(),
                    record.offset()
            );
        } else {
            log.info(
                    "[learning-billing]跳过重复计费事件，eventId={}, partition={}, offset={}",
                    event.getEventId(),
                    record.partition(),
                    record.offset()
            );
        }
    }
}
