package com.rtb.service;

import com.rtb.entity.Ad;
import com.rtb.mapper.AdMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdAdminService {
    private final AdMapper adMapper;

    public AdAdminService(AdMapper adMapper) {
        this.adMapper = adMapper;
    }

    public Ad detail(Long id){
        Ad ad = adMapper.selectById(id);

        if (ad == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "广告不存在"
            );
        }

        return ad;
    }
}
