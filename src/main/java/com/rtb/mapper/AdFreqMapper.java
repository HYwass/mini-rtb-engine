package com.rtb.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdFreqMapper {

    int increment(@Param("adId") Long adId);
}
