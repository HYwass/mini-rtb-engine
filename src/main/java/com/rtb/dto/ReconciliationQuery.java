package com.rtb.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class ReconciliationQuery {

    @NotNull(message = "广告ID不能为空")
    @Min(value = 1, message = "广告ID必须大于0")
    private Long adId;

    @NotNull(message = "日期不能为空")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;
}
