package com.rtb.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtb.dto.BidEvent;
import com.rtb.dto.BidRequest;
import com.rtb.dto.BidResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BidEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    private final String topic;

    public BidEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${rtb.kafka.topic}") String topic) {

        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    public void publish(BidRequest req, BidResponse response) {
        BidEvent event = new BidEvent();

        event.setEventId(response.getRequestId());
        event.setRequestId(response.getRequestId());
        event.setOccurredAt(System.currentTimeMillis());
        event.setRegion(req.getRegion());
        event.setHasAd(response.isHasAd());
        event.setCandidateCnt(response.getCandidateCnt());

        if (response.getWinner() != null) {
            event.setAdId(response.getWinner().getAdId());
            event.setWinBid(response.getWinner().getBid());
            event.setPayPrice(response.getPayPrice());
        }

        try {
            String key = event.getAdId() == null
                    ? event.getRequestId()
                    : event.getAdId().toString();

            String value = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(topic, key, value)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error(
                                    "[publisher]竞价事件发送失败，eventId={}",
                                    event.getEventId(),
                                    error
                            );
                            return;
                        }

                        log.info(
                                "[publisher]竞价事件发送成功，eventId={}, partition={}, offset={}",
                                event.getEventId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    });
        } catch (Exception e) {
            log.error(
                    "竞价事件序列化或发送调用失败，eventId={}",
                    event.getEventId(),
                    e
            );
        }
    }
}