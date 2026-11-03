package com.rtb.mapper;

import com.rtb.entity.Ad;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdMapper {

    Ad selectById(@Param("id") Long id);
}
