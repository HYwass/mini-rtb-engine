package com.rtb.controller;


import com.rtb.common.ApiResponse;
import com.rtb.common.PageResult;
import com.rtb.dto.AdCreateRequest;
import com.rtb.dto.AdDetailResponse;
import com.rtb.dto.AdQuery;
import com.rtb.dto.AdUpdateRequest;
import com.rtb.entity.Ad;
import com.rtb.service.AdAdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/ad")
public class AdAdminController {

    private final AdAdminService adAdminService;

    public AdAdminController(AdAdminService adAdminService) {
        this.adAdminService = adAdminService;
    }

    @GetMapping("/{id}")
    public ApiResponse<AdDetailResponse> detail(@PathVariable("id") Long id){
        AdDetailResponse detail = adAdminService.detail(id);
        return ApiResponse.ok(detail);
    }

    @PostMapping
    public ApiResponse<Map<String, Long>> create(@Valid @RequestBody AdCreateRequest req){
        Long id = adAdminService.create(req);
        return ApiResponse.ok(Map.of("id", id));
    }

    @GetMapping
    public ApiResponse<PageResult<Ad>> page(@Valid @ModelAttribute AdQuery query){
        return ApiResponse.ok(adAdminService.page(query));
    }

    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody AdUpdateRequest req) {

        adAdminService.update(id, req);
        return ApiResponse.ok(Map.of("id", id, "updated", true));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Map<String, Object>> changeStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") Integer status) {

        adAdminService.changeStatus(id, status);
        return ApiResponse.ok(Map.of("id", id, "status", status));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Object>> delete(
            @PathVariable("id") Long id) {

        adAdminService.delete(id);
        return ApiResponse.ok(Map.of("id", id, "deleted", true));
    }
}
