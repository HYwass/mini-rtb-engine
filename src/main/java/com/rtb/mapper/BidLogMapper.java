package com.rtb.mapper;

import com.rtb.entity.BidLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BidLogMapper {

    int batchInsert(@Param("logs") List<BidLog> bidLog);
}
