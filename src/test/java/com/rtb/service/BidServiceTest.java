package com.rtb.service;

import com.rtb.dto.BidRequest;
import com.rtb.dto.BidResponse;
import com.rtb.mapper.AdMapper;
import com.rtb.mq.BidEventPublisher;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.exceptions.PersistenceException;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BidServiceTest {

    private final AdMapper mapper = mock(AdMapper.class);
    private final BudgetService budget = mock(BudgetService.class);
    private final BidLogService logs = mock(BidLogService.class);
    private final BidEventPublisher publisher = mock(BidEventPublisher.class);
    private final BidService service = new BidService(mapper, budget, logs, publisher);

    @Test
    void wrappedConnectionFailureReturnsDegradedResultWithoutDeducting() {
        var failure = new MyBatisSystemException(new PersistenceException(
                new CannotGetJdbcConnectionException("connection failed", new SQLException())));
        when(mapper.findCandidates(null, null, null, null)).thenThrow(failure);

        BidRequest request = new BidRequest();
        BidResponse response = service.bid(request);

        assertTrue(response.isDegraded());
        assertFalse(response.isHasAd());
        assertEquals("CANDIDATE_LOOKUP_UNAVAILABLE", response.getReason());
        verifyNoInteractions(budget);
        verify(logs).record(request, response);
        verify(publisher).publish(request, response);
    }

    @Test
    void directQueryTimeoutStillDegrades() {
        when(mapper.findCandidates(null, null, null, null))
                .thenThrow(new QueryTimeoutException("query timeout"));

        assertTrue(service.bid(new BidRequest()).isDegraded());
        verifyNoInteractions(budget);
    }

    @Test
    void wrappedBindingErrorIsNotHiddenByDegradation() {
        var failure = new MyBatisSystemException(new BindingException("missing parameter"));
        when(mapper.findCandidates(null, null, null, null)).thenThrow(failure);

        assertSame(failure, assertThrows(MyBatisSystemException.class,
                () -> service.bid(new BidRequest())));
        verifyNoInteractions(budget, logs, publisher);
    }
}
