package org.wnn.portal.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;

import com.alibaba.ttl.threadpool.TtlExecutors;

/**
 * @author NanNan Wang
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Bean("ttlTaskExecutor")
    public Executor ttlTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("async-");

        // TaskDecorator 复制 MDC（让日志打印 traceId）
        executor.setTaskDecorator(r -> {
            Map<String, String> parentMdc = MDC.getCopyOfContextMap();
            return () -> {
                Map<String, String> backup = MDC.getCopyOfContextMap();
                if (parentMdc != null) MDC.setContextMap(parentMdc); else MDC.clear();
                try {
                    r.run();
                } finally {
                    if (backup != null) MDC.setContextMap(backup); else MDC.clear();
                }
            };
        });

        executor.initialize();
        return TtlExecutors.getTtlExecutor(executor);
    }

    @Override
    public Executor getAsyncExecutor() {
        return ttlTaskExecutor();
    }

}
