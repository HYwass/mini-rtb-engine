package com.rtb.controller;


import com.rtb.common.ApiResponse;
import com.rtb.common.BizException;
import com.rtb.entity.Ad;
import com.rtb.mapper.AdMapper;
import com.rtb.service.BudgetService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/admin/ad")
public class BudgetAdminController {
    private final AdMapper adMapper;
    private final BudgetService budgetService;

    public BudgetAdminController(
            AdMapper adMapper,
            BudgetService budgetService) {

        this.adMapper = adMapper;
        this.budgetService = budgetService;
    }

    @PostMapping("/{id}/budget/init")
    public ApiResponse<Map<String, Object>> init(
            @PathVariable("id") Long id) {

        Ad ad = adMapper.selectById(id);

        if (ad == null) {
            throw BizException.notFound("广告不存在: " + id);
        }

        BigDecimal remainingBudget =
                budgetService.getOrInitBalance(
                        ad.getId(),
                        ad.getDailyBudget()
                );

        return ApiResponse.ok(Map.of(
                "adId", id,
                "remainingBudget", remainingBudget
        ));
    }
}
