package com.rtb.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtb.dto.BidEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BidEventDebugConsumer {

    private final ObjectMapper objectMapper;

    public BidEventDebugConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${rtb.kafka.topic}",
            groupId = "learning-bid-debug"
    )
    public void consume(ConsumerRecord<String, String> record)
            throws JsonProcessingException {

        BidEvent event = objectMapper.readValue(
                record.value(),
                BidEvent.class
        );

        log.info(
                "[learning-bid-debug]收到竞价事件，eventId={}, hasAd={}, adId={}, payPrice={}, partition={}, offset={}",
                event.getEventId(),
                event.isHasAd(),
                event.getAdId(),
                event.getPayPrice(),
                record.partition(),
                record.offset()
        );
    }
}
