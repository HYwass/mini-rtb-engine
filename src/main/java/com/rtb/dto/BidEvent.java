package com.rtb.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BidEvent {

    private String eventId;

    private String requestId;

    private long occurredAt;

    private String region;

    private boolean hasAd;

    private int candidateCnt;

    private Long adId;

    private BigDecimal winBid;

    private BigDecimal payPrice;
}
