package com.rtb.controller;

import com.rtb.common.ApiResponse;
import com.rtb.dto.HourlyStatQuery;
import com.rtb.dto.HourlyStatResponse;
import com.rtb.service.HourlyStatQueryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/stats")
public class StatsAdminController {

    private final HourlyStatQueryService hourlyStatQueryService;

    public StatsAdminController(
            HourlyStatQueryService hourlyStatQueryService) {

        this.hourlyStatQueryService = hourlyStatQueryService;
    }

    @GetMapping("/hourly")
    public ApiResponse<List<HourlyStatResponse>> hourly(
            @Valid @ModelAttribute HourlyStatQuery query) {

        return ApiResponse.ok(
                hourlyStatQueryService.query(query)
        );
    }
}
