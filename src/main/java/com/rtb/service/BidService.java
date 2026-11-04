package com.rtb.service;

import com.rtb.dto.BidRequest;
import com.rtb.dto.BidResponse;
import com.rtb.entity.Ad;
import com.rtb.mapper.AdMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class BidService {

    private final AdMapper adMapper;

    private static final BigDecimal MIN_INCREMENT =
            new BigDecimal("0.01");

    public BidService(AdMapper adMapper) {
        this.adMapper = adMapper;
    }

    public List<Ad> findCandidates(BidRequest req) {
        return adMapper.findCandidates(
                req.getRegion(),
                req.getAgeBucket(),
                req.getGender(),
                req.getCategoryCode()
        );
    }


    public BidResponse bid(BidRequest req) {
        List<Ad> candidates = findCandidates(req);

        BidResponse response = new BidResponse();
        response.setRequestId(UUID.randomUUID().toString());
        response.setCandidateCnt(candidates.size());

        // 没有候选广告
        if (candidates.isEmpty()) {
            response.setHasAd(false);
            return response;
        }

        // SQL 已经按照出价降序排列
        Ad winnerAd = candidates.get(0);

        // 只有一条候选时，按自身出价计算
        BigDecimal payPrice = winnerAd.getBid();

        // 至少两条候选时，按第二名出价加一分钱计算
        if (candidates.size() >= 2) {
            BigDecimal secondBid = candidates.get(1).getBid();

            payPrice = secondBid
                    .add(MIN_INCREMENT)
                    .min(winnerAd.getBid());
        }

        BidResponse.Winner winner = new BidResponse.Winner();
        winner.setAdId(winnerAd.getId());
        winner.setTitle(winnerAd.getTitle());
        winner.setCreativeUrl(winnerAd.getCreativeUrl());
        winner.setBid(winnerAd.getBid());

        response.setHasAd(true);
        response.setWinner(winner);
        response.setPayPrice(payPrice);

        return response;
    }
}
