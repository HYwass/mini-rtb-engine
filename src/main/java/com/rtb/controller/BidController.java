package com.rtb.controller;

import com.rtb.common.ApiResponse;
import com.rtb.dto.BidRequest;
import com.rtb.dto.BidResponse;
import com.rtb.entity.Ad;
import com.rtb.service.BidService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BidController {

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    @PostMapping("/bid")
    public ApiResponse<BidResponse> bid(@Valid @RequestBody BidRequest req) {
        return ApiResponse.ok(bidService.bid(req));
    }

    @PostMapping("/bid/candidate")
    public ApiResponse<List<Ad>> fineCandidates(@Valid @RequestBody BidRequest req) {
        return ApiResponse.ok(bidService.findCandidates(req));
    }
}
