package com.rtb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BidResponse {

    private String requestId;

    // 默认值false（后面会直接采用默认值）
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

    // 默认值false（后面会直接采用默认值）
    // 当前只有数据库连接失败会标记为降级
    private boolean degraded;

    /*
    NO_CANDIDATE	                没有符合条件的候选
    BUDGET_INSUFFICIENT	            候选都没有足够预算
    CANDIDATE_LOOKUP_UNAVAILABLE	候选查询因连接或超时问题无法完成
    */
    private String reason;
}
