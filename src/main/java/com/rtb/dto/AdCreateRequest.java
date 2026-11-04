package com.rtb.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AdCreateRequest {
    @NotNull
    @Min(1)
    private Integer advertiserId;

    @NotBlank
    @Size(max = 128)
    private String title;

    @NotBlank
    @Size(max = 255)
    private String creativeUrl;

    @NotBlank
    @Size(max = 32)
    private String categoryCode;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 6, fraction = 2)
    private BigDecimal bid;

    @NotNull
    @DecimalMin("1.00")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal dailyBudget;

    @NotEmpty
    @Valid
    private List<@NotNull TargetItem> targets;
}
