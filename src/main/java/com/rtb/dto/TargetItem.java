package com.rtb.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TargetItem {

    @NotNull
    @Min(1)
    @Max(3)
    private Integer dimType;

    @NotBlank
    @Size(max = 64)
    private String dimValue;
}
