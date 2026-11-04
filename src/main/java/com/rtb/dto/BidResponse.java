package com.rtb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BidResponse {

    private String requestId;

    private boolean hasAd;

    private int candidateCnt;

    private Winner winner;

    private BigDecimal payPrice;

    @Data
    public static class Winner {

        private Long adId;

        private String title;

        private String creativeUrl;

        private BigDecimal bid;
    }
}
