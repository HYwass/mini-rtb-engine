package com.rtb.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BidLog {

    private Long id;

    private String requestId;

    private String region;

    private String ageBucket;

    private String gender;

    private String categoryCode;

    private Integer candidateCnt;

    private Boolean hasAd;

    private Long adId;

    private BigDecimal winBid;

    private BigDecimal payPrice;

    private LocalDateTime bidAt;
}
