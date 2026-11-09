package com.rtb.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HourlyStatResponse {

    private LocalDateTime statHour;

    private Long adId;

    private Long winCount;

    private BigDecimal spend;
}
