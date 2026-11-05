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

    private final BudgetService budgetService;

    private final BidLogService bidLogService;

    private static final BigDecimal MIN_INCREMENT =
            new BigDecimal("0.01");

    public BidService(
            AdMapper adMapper,
            BudgetService budgetService,
            BidLogService bidLogService) {

        this.adMapper = adMapper;
        this.budgetService = budgetService;
        this.bidLogService = bidLogService;
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

        for (int i = 0; i < candidates.size(); i++) {
            Ad candidate = candidates.get(i);

            // 没有下一名时，按自身出价计算
            BigDecimal payPrice = candidate.getBid();

            if (i + 1 < candidates.size()) {
                BigDecimal nextBid = candidates.get(i + 1).getBid();

                payPrice = nextBid
                        .add(MIN_INCREMENT)
                        .min(candidate.getBid());
            }

            boolean deducted = budgetService.deduct(
                    candidate.getId(),
                    candidate.getDailyBudget(),
                    payPrice
            );

            if (!deducted) {
                // 当前广告余额不足，继续尝试下一名
                continue;
            }

            BidResponse.Winner winner = new BidResponse.Winner();
            winner.setAdId(candidate.getId());
            winner.setTitle(candidate.getTitle());
            winner.setCreativeUrl(candidate.getCreativeUrl());
            winner.setBid(candidate.getBid());

            response.setHasAd(true);
            response.setWinner(winner);
            response.setPayPrice(payPrice);

            bidLogService.record(req, response);

            return response;
        }

        // 没有候选，或者所有候选都没有足够余额
        response.setHasAd(false);

        bidLogService.record(req, response);

        return response;
    }
}
