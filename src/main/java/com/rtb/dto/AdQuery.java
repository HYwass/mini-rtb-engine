package com.rtb.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdQuery {

    @Min(1)
    private Integer advertiserId;

    @Min(1)
    @Max(3)
    private Integer status;

    @Size(max = 128)
    private String titleLike;

    @NotNull
    @Min(1)
    private Integer pageNum = 1;

    @NotNull
    @Min(1)
    @Max(200)
    private Integer pageSize = 20;

    public long getOffset() {
        return (pageNum.longValue() - 1) * pageSize;
    }
}
