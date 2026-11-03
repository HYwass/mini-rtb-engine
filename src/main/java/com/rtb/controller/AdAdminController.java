package com.rtb.controller;


import com.rtb.entity.Ad;
import com.rtb.service.AdAdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/ad")
public class AdAdminController {

    private final AdAdminService adAdminService;

    public AdAdminController(AdAdminService adAdminService) {
        this.adAdminService = adAdminService;
    }

    @GetMapping("/{id}")
    public Ad detail(@PathVariable("id") Long id){
        return adAdminService.detail(id);
    }
}
