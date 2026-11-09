package com.rtb.service;

import com.rtb.dto.BidEvent;
import com.rtb.mapper.BillingMapper;
import com.rtb.mapper.HourlyStatMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Service
public class HourlyStatService {

    public static final String CONSUMER_GROUP = "learning-hourly";

    private static final ZoneId STAT_ZONE =
            ZoneId.of("Asia/Shanghai");

    private final BillingMapper billingMapper;
    private final HourlyStatMapper hourlyStatMapper;

    public HourlyStatService(
            BillingMapper billingMapper,
            HourlyStatMapper hourlyStatMapper) {

        this.billingMapper = billingMapper;
        this.hourlyStatMapper = hourlyStatMapper;
    }

    @Transactional
    public boolean process(BidEvent event) {
        validate(event);

        try {
            billingMapper.insertProcessed(
                    event.getEventId(),
                    CONSUMER_GROUP
            );
        } catch (DuplicateKeyException e) {
            return false;
        }

        // 没有成交：只记录已消费，不累加广告统计
        if (!event.isHasAd()) {
            return true;
        }

        LocalDateTime statHour = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(event.getOccurredAt()),
                STAT_ZONE
        ).truncatedTo(ChronoUnit.HOURS);

        hourlyStatMapper.accumulate(
                statHour,
                event.getAdId(),
                event.getPayPrice()
        );

        return true;
    }

    private void validate(BidEvent event) {
        if (event == null
                || event.getEventId() == null
                || event.getEventId().isBlank()) {
            throw new IllegalArgumentException("事件编号不能为空");
        }

        if (event.getOccurredAt() <= 0) {
            throw new IllegalArgumentException("事件时间无效");
        }

        if (event.isHasAd()
                && (event.getAdId() == null
                || event.getAdId() <= 0
                || event.getPayPrice() == null
                || event.getPayPrice().signum() <= 0)) {
            throw new IllegalArgumentException("成交事件缺少有效广告ID或金额");
        }
    }
}
