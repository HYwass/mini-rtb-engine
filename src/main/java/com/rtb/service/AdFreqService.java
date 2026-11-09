package com.rtb.service;

import com.rtb.dto.BidEvent;
import com.rtb.mapper.AdFreqMapper;
import com.rtb.mapper.BillingMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdFreqService {

    public static final String CONSUMER_GROUP = "learning-freq";

    private final BillingMapper billingMapper;
    private final AdFreqMapper adFreqMapper;

    public AdFreqService(
            BillingMapper billingMapper,
            AdFreqMapper adFreqMapper) {

        this.billingMapper = billingMapper;
        this.adFreqMapper = adFreqMapper;
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

        if (event.isHasAd()) {
            adFreqMapper.increment(event.getAdId());
        }

        return true;
    }

    private void validate(BidEvent event) {
        if (event == null
                || event.getEventId() == null
                || event.getEventId().isBlank()) {
            throw new IllegalArgumentException("事件编号不能为空");
        }

        if (event.isHasAd()
                && (event.getAdId() == null
                || event.getAdId() <= 0)) {
            throw new IllegalArgumentException("成交事件缺少有效广告ID");
        }
    }
}
