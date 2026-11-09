package com.rtb.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtb.dto.BidEvent;
import com.rtb.service.HourlyStatService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HourlyStatConsumer {

    private final ObjectMapper objectMapper;
    private final HourlyStatService hourlyStatService;

    public HourlyStatConsumer(
            ObjectMapper objectMapper,
            HourlyStatService hourlyStatService) {

        this.objectMapper = objectMapper;
        this.hourlyStatService = hourlyStatService;
    }

    @KafkaListener(
            topics = "${rtb.kafka.topic}",
            groupId = HourlyStatService.CONSUMER_GROUP,
            containerFactory = "hourlyKafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> record)
            throws JsonProcessingException {

        BidEvent event = objectMapper.readValue(
                record.value(),
                BidEvent.class
        );

        boolean processed = hourlyStatService.process(event);

        if (processed) {
            log.info(
                    "[learning-hourly]统计事件处理完成，"
                            + "eventId={}, hasAd={}, adId={}, "
                            + "partition={}, offset={}",
                    event.getEventId(),
                    event.isHasAd(),
                    event.getAdId(),
                    record.partition(),
                    record.offset()
            );
        } else {
            log.info(
                    "[learning-hourly]跳过重复统计，eventId={}",
                    event.getEventId()
            );
        }
    }
}
