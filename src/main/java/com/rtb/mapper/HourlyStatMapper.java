package com.rtb.mapper;

import com.rtb.dto.HourlyStatQuery;
import com.rtb.dto.HourlyStatResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HourlyStatMapper {

    int accumulate(
            @Param("statHour") LocalDateTime statHour,
            @Param("adId") Long adId,
            @Param("amount") BigDecimal amount
    );

    List<HourlyStatResponse> selectHourly(
            @Param("q") HourlyStatQuery query
    );
}
