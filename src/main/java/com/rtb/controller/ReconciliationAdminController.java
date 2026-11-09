package com.rtb.controller;

import com.rtb.common.ApiResponse;
import com.rtb.dto.ReconciliationQuery;
import com.rtb.dto.ReconciliationResponse;
import com.rtb.service.ReconciliationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/reconciliation")
public class ReconciliationAdminController {

    private final ReconciliationService reconciliationService;

    public ReconciliationAdminController(
            ReconciliationService reconciliationService) {

        this.reconciliationService = reconciliationService;
    }

    @GetMapping
    public ApiResponse<ReconciliationResponse> reconcile(
            @Valid @ModelAttribute ReconciliationQuery query) {

        return ApiResponse.ok(
                reconciliationService.reconcile(query)
        );
    }
}
