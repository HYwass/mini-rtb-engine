package com.rtb.service;

import com.rtb.dto.BidRequest;
import com.rtb.dto.BidResponse;
import com.rtb.entity.BidLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
public class BidLogService {

    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Shanghai");

    private final BidLogBuffer bidLogBuffer;

    public BidLogService(BidLogBuffer bidLogBuffer) {
        this.bidLogBuffer = bidLogBuffer;
    }

    public void record(BidRequest req, BidResponse response) {
        BidLog detail = new BidLog();

        detail.setRequestId(response.getRequestId());

        detail.setRegion(req.getRegion());
        detail.setAgeBucket(req.getAgeBucket());
        detail.setGender(req.getGender());
        detail.setCategoryCode(req.getCategoryCode());

        detail.setCandidateCnt(response.getCandidateCnt());
        detail.setHasAd(response.isHasAd());

        detail.setBidAt(LocalDateTime.now(BUSINESS_ZONE));

        if (response.getWinner() != null) {
            detail.setAdId(response.getWinner().getAdId());
            detail.setWinBid(response.getWinner().getBid());
            detail.setPayPrice(response.getPayPrice());
        }

        boolean accepted = bidLogBuffer.offer(detail);

        if (!accepted) {
            log.warn(
                    "竞价明细未入队，队列已满或应用正在关闭，requestId={}",
                    detail.getRequestId()
            );
        }
    }
}
