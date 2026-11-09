package com.rtb.service;

import com.rtb.common.BizException;
import com.rtb.dto.BidRequest;
import com.rtb.dto.BidResponse;
import com.rtb.entity.Ad;
import com.rtb.mapper.AdMapper;
import com.rtb.mq.BidEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class BidService {

    private final AdMapper adMapper;

    private final BudgetService budgetService;

    private final BidLogService bidLogService;

    private final BidEventPublisher bidEventPublisher;

    private static final BigDecimal MIN_INCREMENT =
            new BigDecimal("0.01");

    public BidService(
            AdMapper adMapper,
            BudgetService budgetService,
            BidLogService bidLogService,
            BidEventPublisher bidEventPublisher) {

        this.adMapper = adMapper;
        this.budgetService = budgetService;
        this.bidLogService = bidLogService;
        this.bidEventPublisher = bidEventPublisher;
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

        BidResponse response = new BidResponse();
        response.setRequestId(UUID.randomUUID().toString());

        List<Ad> candidates;

        try {
            candidates = findCandidates(req);
        } catch (DataAccessException e) {
            if (!isCandidateLookupUnavailable(e)) {
                throw e;
            }

            log.error(
                    "候选查询不可用，requestId={}",
                    response.getRequestId(),
                    e
            );

            response.setDegraded(true);
            response.setReason("CANDIDATE_LOOKUP_UNAVAILABLE");

            return finish(req, response);
        }

        response.setCandidateCnt(candidates.size());

        if (candidates.isEmpty()) {
            response.setReason("NO_CANDIDATE");
            return finish(req, response);
        }

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

            boolean deducted;

            try {
                deducted = budgetService.deduct(
                        candidate.getId(),
                        candidate.getDailyBudget(),
                        payPrice
                );
            } catch (DataAccessException e) {
                log.error(
                        "扣费操作异常，结果未确认，"
                                + "requestId={}, adId={}, amount={}",
                        response.getRequestId(),
                        candidate.getId(),
                        payPrice,
                        e
                );

                throw new BizException(
                        503,
                        "扣费结果未确认，请勿自动重试；requestId="
                                + response.getRequestId()
                );
            }

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

            return finish(req, response);
        }

        response.setReason("BUDGET_INSUFFICIENT");

        return finish(req, response);
    }

    private boolean isCandidateLookupUnavailable(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof DataAccessResourceFailureException
                    || cause instanceof QueryTimeoutException) {
                return true;
            }
        }
        return false;
    }

    private BidResponse finish(
            BidRequest req,
            BidResponse response) {

        bidLogService.record(req, response);
        bidEventPublisher.publish(req, response);

        return response;
    }
}
