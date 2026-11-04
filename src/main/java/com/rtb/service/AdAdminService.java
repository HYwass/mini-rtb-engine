package com.rtb.service;

import com.rtb.common.BizException;
import com.rtb.common.PageResult;
import com.rtb.dto.AdCreateRequest;
import com.rtb.dto.AdDetailResponse;
import com.rtb.dto.AdQuery;
import com.rtb.dto.AdUpdateRequest;
import com.rtb.dto.TargetItem;
import com.rtb.entity.Ad;
import com.rtb.mapper.AdMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdAdminService {
    private final AdMapper adMapper;

    public AdAdminService(AdMapper adMapper) {
        this.adMapper = adMapper;
    }

    public AdDetailResponse detail(Long id){
        Ad ad = adMapper.selectById(id);

        if (ad == null) {
            throw BizException.notFound("广告不存在: " + id);
        }

        List<TargetItem> targets = adMapper.listTargetsByAdId(id);

        return new AdDetailResponse(ad, targets);
    }

    @Transactional
    public Long create(AdCreateRequest req){
        Ad ad = new Ad();
        ad.setAdvertiserId(req.getAdvertiserId());
        ad.setTitle(req.getTitle());
        ad.setCreativeUrl(req.getCreativeUrl());
        ad.setCategoryCode(req.getCategoryCode());
        ad.setBid(req.getBid());
        ad.setDailyBudget(req.getDailyBudget());
        ad.setStatus(1);

        adMapper.insertAd(ad);

        adMapper.batchInsertTargets(
                ad.getId(),
                req.getTargets()
        );

        return ad.getId();
    }

    public PageResult<Ad> page(AdQuery query){
        long total = adMapper.countBy(query);

        List<Ad> list;
        if (total == 0){
            list = List.of();
        } else {
            list = adMapper.selectPage(query);
        }

        return new PageResult<>(
                total,
                query.getPageNum(),
                query.getPageSize(),
                list
        );
    }



    private Ad mustGet(Long id) {
        Ad ad = adMapper.selectById(id);
        if (ad == null) {
            throw BizException.notFound("广告不存在: " + id);
        }
        return ad;
    }

    @Transactional
    public void update(Long id, AdUpdateRequest req) {
        mustGet(id);

        Ad patch = new Ad();
        patch.setId(id);
        patch.setTitle(req.getTitle());
        patch.setCreativeUrl(req.getCreativeUrl());
        patch.setCategoryCode(req.getCategoryCode());
        patch.setBid(req.getBid());
        patch.setDailyBudget(req.getDailyBudget());

        adMapper.updateAd(patch);

        adMapper.deleteTargetsByAdId(id);
        adMapper.batchInsertTargets(id, req.getTargets());
    }

    @Transactional
    public void delete(Long id) {
        mustGet(id);
        adMapper.deleteTargetsByAdId(id);
        adMapper.deleteAd(id);
    }

    @Transactional
    public void changeStatus(Long id, Integer status) {
        if (status == null || (status != 1 && status != 2)) {
            throw new BizException(400, "status 只能为 1 或 2");
        }

        mustGet(id);
        adMapper.updateStatus(id, status);
    }
}
