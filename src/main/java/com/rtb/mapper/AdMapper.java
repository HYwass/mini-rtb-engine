package com.rtb.mapper;

import com.rtb.dto.AdQuery;
import com.rtb.dto.TargetItem;
import com.rtb.entity.Ad;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdMapper {

    Ad selectById(@Param("id") Long id);

    int insertAd(Ad ad);

    int batchInsertTargets(
            @Param("adId") Long adId,
            @Param("targets") List<TargetItem> targets
    );

    List<TargetItem> listTargetsByAdId(@Param("adId") Long adId);

    long countBy(@Param("q") AdQuery query);

    List<Ad> selectPage(@Param("q") AdQuery query);

    int updateAd(Ad ad);

    int deleteTargetsByAdId(@Param("adId")  Long adId);

    int deleteAd(@Param("id") Long id);

    int updateStatus(
            @Param("id") Long id,
            @Param("status") Integer status
    );


    List<Ad> findCandidates(
            @Param("region") String region,
            @Param("ageBucket") String ageBucket,
            @Param("gender") String gender,
            @Param("categoryCode") String categoryCode
    );
}
