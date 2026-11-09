package com.rtb.mapper;

import com.rtb.dto.BidEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface BillingMapper {

    int insertProcessed(
            @Param("eventId") String eventId,
            @Param("consumerGroup") String consumerGroup
    );

    int insertBilling(@Param("event") BidEvent event);

    BigDecimal sumAmount(
            @Param("adId") Long adId,
            @Param("startMillis") long startMillis,
            @Param("endMillis") long endMillis
    );
}
