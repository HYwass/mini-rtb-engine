package com.rtb.service;

import com.rtb.common.BizException;
import com.rtb.dto.HourlyStatQuery;
import com.rtb.dto.HourlyStatResponse;
import com.rtb.mapper.HourlyStatMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class HourlyStatQueryService {

    private final HourlyStatMapper hourlyStatMapper;

    public HourlyStatQueryService(
            HourlyStatMapper hourlyStatMapper) {

        this.hourlyStatMapper = hourlyStatMapper;
    }

    public List<HourlyStatResponse> query(HourlyStatQuery query) {
        LocalDateTime start = query.getStartTime();
        LocalDateTime end = query.getEndTime();

        if (!start.isBefore(end)) {
            throw new BizException(
                    400,
                    "开始时间必须早于结束时间"
            );
        }

        if (!start.equals(start.truncatedTo(ChronoUnit.HOURS))
                || !end.equals(end.truncatedTo(ChronoUnit.HOURS))) {
            throw new BizException(
                    400,
                    "开始时间和结束时间必须为整点"
            );
        }

        long hours = ChronoUnit.HOURS.between(start, end);

        if (hours > 31L * 24) {
            throw new BizException(
                    400,
                    "查询时间范围不能超过31天"
            );
        }

        return hourlyStatMapper.selectHourly(query);
    }
}
