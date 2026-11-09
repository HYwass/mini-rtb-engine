package com.rtb.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BudgetService {
    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Shanghai");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.BASIC_ISO_DATE;

    private static final Duration KEY_TTL =
            Duration.ofDays(2);

    private final StringRedisTemplate redis;

    private final DefaultRedisScript<Long> deductScript;

    public BudgetService(StringRedisTemplate redis) {
        this.redis = redis;

        this.deductScript = new DefaultRedisScript<>();
        this.deductScript.setLocation(
                new ClassPathResource("lua/deduct-budget.lua")
        );
        this.deductScript.setResultType(Long.class);
    }


    public BigDecimal getOrInitBalance(
            Long adId,
            BigDecimal dailyBudget) {

        String key = budgetKey(adId, today());

        long budgetCents = toCents(dailyBudget);

        redis.opsForValue().setIfAbsent(
                key,
                Long.toString(budgetCents),
                KEY_TTL
        );

        String balance = redis.opsForValue().get(key);

        if (balance == null) {
            throw new IllegalStateException("预算初始化后读取失败");
        }

        return BigDecimal.valueOf(Long.parseLong(balance), 2);
    }


    public boolean deduct(
            Long adId,
            BigDecimal dailyBudget,
            BigDecimal amount) {

        long amountCents = toCents(amount);

        if (amountCents <= 0) {
            throw new IllegalArgumentException("扣费金额必须大于 0");
        }

        String date = today();

        List<String> keys = List.of(
                budgetKey(adId, date),
                spentKey(adId, date)
        );

        Long result = redis.execute(
                deductScript,
                keys,
                Long.toString(amountCents),
                Long.toString(toCents(dailyBudget)),
                Long.toString(KEY_TTL.toSeconds())
        );

        if (result == null) {
            throw new IllegalStateException("扣费脚本没有返回结果");
        }

        return result == 1L;
    }


    public BigDecimal getSpent(Long adId, LocalDate date) {
        String key = spentKey(
                adId,
                date.format(DATE_FORMAT)
        );

        String value = redis.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        return BigDecimal.valueOf(
                Long.parseLong(value),
                2
        );
    }


    private String today() {
        return LocalDate.now(BUSINESS_ZONE).format(DATE_FORMAT);
    }

    private String budgetKey(Long adId, String date) {
        return "budget:" + adId + ":" + date;
    }

    private String spentKey(Long adId, String date) {
        return "spent:" + adId + ":" + date;
    }

    private long toCents(BigDecimal yuan) {
        return yuan.movePointRight(2).longValueExact();
    }
}
