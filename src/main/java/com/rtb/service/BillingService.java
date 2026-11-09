package com.rtb.service;

import com.rtb.dto.BidEvent;
import com.rtb.mapper.BillingMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingService {

    public static final String CONSUMER_GROUP =
            "learning-billing";

    private final BillingMapper billingMapper;

    public BillingService(BillingMapper billingMapper) {
        this.billingMapper = billingMapper;
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
            // 该消费者组已经处理过这个事件
            return false;
        }

        if (event.isHasAd()) {
            billingMapper.insertBilling(event);
        }

        return true;
    }

    private void validate(BidEvent event) {
        if (event.getEventId() == null
                || event.getEventId().isBlank()
                || event.getRequestId() == null
                || event.getRequestId().isBlank()) {
            throw new IllegalArgumentException("事件编号不能为空");
        }

        if (event.isHasAd()
                && (event.getAdId() == null
                || event.getPayPrice() == null
                || event.getPayPrice().signum() <= 0)) {
            throw new IllegalArgumentException("成交事件缺少有效广告ID或金额");
        }
    }
}
