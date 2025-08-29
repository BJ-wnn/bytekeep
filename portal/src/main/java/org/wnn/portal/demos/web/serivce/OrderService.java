package org.wnn.portal.demos.web.serivce;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.wnn.core.log.TraceContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author NanNan Wang
 */
@Service
@Slf4j
public class OrderService {


    // 直接使用 @Async，自动透传 traceId
    @Async("ttlTaskExecutor")
    public void handleOrderAsync(Long orderId) {
        log.info("异步处理订单：{}，traceId={}", orderId, TraceContext.getTraceId());
    }

    // 如果需要 CompletableFuture，显式传入 ttlExecutor
    @Autowired
    @Qualifier(value = "ttlTaskExecutor")
    private Executor ttlExecutor;

    public CompletableFuture<Void> handleOrderWithCF(Long orderId) {
        return CompletableFuture.runAsync(() -> {
            log.info("CF 处理订单：{}，traceId={}", orderId, TraceContext.getTraceId());
        }, ttlExecutor);
    }
}
