package com.rtb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReconciliationResponse {

    private Long adId;

    private LocalDate date;

    private BigDecimal redisSpent;

    private BigDecimal billedAmount;

    private BigDecimal difference;

    private String status;
}
