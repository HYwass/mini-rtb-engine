package com.rtb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BidRequest {

    @NotBlank
    @Size(max = 32)
    private String region;

    // 例如 25-30；可不传
    private String ageBucket;

    // 可不传；传入值要和 ad_target 中的值一致
    @Pattern(regexp = "male|female|unknown")
    private String gender;

    // 可不传
    private String categoryCode;
}
