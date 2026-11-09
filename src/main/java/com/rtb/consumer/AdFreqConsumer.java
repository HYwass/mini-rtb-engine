package com.rtb.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtb.dto.BidEvent;
import com.rtb.service.AdFreqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdFreqConsumer {

    private final ObjectMapper objectMapper;
    private final AdFreqService adFreqService;

    public AdFreqConsumer(
            ObjectMapper objectMapper,
            AdFreqService adFreqService) {

        this.objectMapper = objectMapper;
        this.adFreqService = adFreqService;
    }

    @KafkaListener(
            topics = "${rtb.kafka.topic}",
            groupId = AdFreqService.CONSUMER_GROUP,
            containerFactory = "freqKafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> record)
            throws JsonProcessingException {

        BidEvent event = objectMapper.readValue(
                record.value(),
                BidEvent.class
        );

        boolean processed = adFreqService.process(event);

        if (processed) {
            log.info(
                    "[learning-freq]频次事件处理完成，"
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
                    "[learning-freq]跳过重复频次统计，eventId={}",
                    event.getEventId()
            );
        }
    }
}
