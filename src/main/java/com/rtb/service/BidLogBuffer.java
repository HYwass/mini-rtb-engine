package com.rtb.service;

import com.rtb.entity.BidLog;
import com.rtb.mapper.BidLogMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Component
public class BidLogBuffer {

    private static final int QUEUE_CAPACITY = 10000;
    private static final int BATCH_SIZE = 200;

    private final BlockingQueue<BidLog> queue =
            new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    private final ScheduledExecutorService worker =
            Executors.newSingleThreadScheduledExecutor(task -> {
                Thread thread = new Thread(task, "bid-log-writer");
                thread.setDaemon(true);
                return thread;
            });

    private final BidLogMapper bidLogMapper;

    private boolean accepting = true;

    public BidLogBuffer(BidLogMapper bidLogMapper) {
        this.bidLogMapper = bidLogMapper;
    }

    @PostConstruct
    public void start() {
        worker.scheduleWithFixedDelay(
                this::flushBatch,
                200,
                200,
                TimeUnit.MILLISECONDS
        );
    }

    public synchronized boolean offer(BidLog detail) {
        if (!accepting) {
            return false;
        }

        return queue.offer(detail);
    }

    private void flushBatch() {
        List<BidLog> batch = new ArrayList<>(BATCH_SIZE);

        queue.drainTo(batch, BATCH_SIZE);

        if (batch.isEmpty()) {
            return;
        }

        try {
            bidLogMapper.batchInsert(batch);

            log.debug(
                    "竞价明细批量写入完成，batchSize={}, queueSize={}",
                    batch.size(),
                    queue.size()
            );
        } catch (RuntimeException e) {
            log.error(
                    "竞价明细批量写入异常，放弃本批重试，batchSize={}, firstRequestId={}",
                    batch.size(),
                    batch.get(0).getRequestId(),
                    e
            );
        }
    }

    private void drainRemaining() {
        while (!Thread.currentThread().isInterrupted()
                && !queue.isEmpty()) {
            flushBatch();
        }
    }

    @PreDestroy
    public void close() {
        synchronized (this) {
            accepting = false;
        }

        // 与正常批量写入使用同一个线程，避免关闭时并行刷盘
        worker.execute(this::drainRemaining);
        worker.shutdown();

        try {
            if (!worker.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn(
                        "等待明细写入结束超时，queueSize={}，仍可能存在正在写入的批次",
                        queue.size()
                );

                worker.shutdownNow();
            }
        } catch (InterruptedException e) {
            worker.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
