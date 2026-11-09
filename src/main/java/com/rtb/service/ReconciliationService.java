package com.rtb.service;

import com.rtb.common.BizException;
import com.rtb.dto.ReconciliationQuery;
import com.rtb.dto.ReconciliationResponse;
import com.rtb.mapper.BillingMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class ReconciliationService {

    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Shanghai");

    private final BillingMapper billingMapper;
    private final BudgetService budgetService;

    public ReconciliationService(
            BillingMapper billingMapper,
            BudgetService budgetService) {

        this.billingMapper = billingMapper;
        this.budgetService = budgetService;
    }

    public ReconciliationResponse reconcile(
            ReconciliationQuery query) {

        LocalDate date = query.getDate();

        if (date.isAfter(LocalDate.now(BUSINESS_ZONE))) {
            throw new BizException(
                    400,
                    "不能查询未来日期"
            );
        }

        long startMillis = date
                .atStartOfDay(BUSINESS_ZONE)
                .toInstant()
                .toEpochMilli();

        long endMillis = date
                .plusDays(1)
                .atStartOfDay(BUSINESS_ZONE)
                .toInstant()
                .toEpochMilli();

        BigDecimal billedAmount = billingMapper.sumAmount(
                query.getAdId(),
                startMillis,
                endMillis
        );

        BigDecimal redisSpent;
        try {
            redisSpent = budgetService.getSpent(
                    query.getAdId(),
                    date
            );
        } catch (DataAccessException e) {
            throw new BizException(
                    503,
                    "Redis 暂时不可用，无法完成对账"
            );
        }

        ReconciliationResponse response =
                new ReconciliationResponse();

        response.setAdId(query.getAdId());
        response.setDate(date);
        response.setBilledAmount(billedAmount);

        if (redisSpent == null) {
            response.setStatus("REDIS_DATA_MISSING");
            return response;
        }

        BigDecimal difference =
                redisSpent.subtract(billedAmount);

        response.setRedisSpent(redisSpent);
        response.setDifference(difference);

        if (difference.compareTo(BigDecimal.ZERO) == 0) {
            response.setStatus("MATCHED");
        } else {
            response.setStatus("MISMATCH");
        }

        return response;
    }
}
