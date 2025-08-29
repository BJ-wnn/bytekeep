package org.wnn.core.log;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlRunnable;
import org.slf4j.MDC;

import java.util.function.Supplier;

/**
 * @author NanNan Wang
 */
public class TraceContext {

    // 1. 存储traceId（使用TransmittableThreadLocal，支持线程池/异步线程透传）
    private static final TransmittableThreadLocal<String> TRACE_ID_THREAD_LOCAL = new TransmittableThreadLocal<>();
    private static final String TRACE_ID_KEY = "traceId";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    // 2. 设置traceId到上下文
    public static void setTraceId(String traceId) {
        TRACE_ID_THREAD_LOCAL.set(traceId);
        // 同时同步到MDC，保证日志打印正常
        MDC.put(TRACE_ID_KEY, traceId);
    }

    // 3. 从上下文获取traceId
    public static String getTraceId() {
        return TRACE_ID_THREAD_LOCAL.get();
    }

    // 4. 清除上下文（避免内存泄漏）
    public static void clear() {
        TRACE_ID_THREAD_LOCAL.remove();
        MDC.remove(TRACE_ID_KEY);
    }

    // 5. 包装Runnable，支持异步线程透传traceId
    public static Runnable wrap(Runnable runnable) {
        return TtlRunnable.get(runnable);
    }

    // 6. 包装Supplier，支持异步线程透传traceId
    // 可选: Supplier 封装（如果你需要 supplyAsync）
    public static <T> Supplier<T> wrap(Supplier<T> supplier) {
        return () -> {
            String traceId = getTraceId();
            if (traceId != null) MDC.put(TRACE_ID_KEY, traceId);
            try {
                return supplier.get();
            } finally {
                MDC.remove(TRACE_ID_KEY);
            }
        };
    }
}
