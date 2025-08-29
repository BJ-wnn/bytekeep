package org.wnn.core.feign;

import org.slf4j.MDC;

/**
 * 基于SLF4J MDC的TraceId提供器
 * 适用于使用Logback、Log4j等支持MDC的日志框架
 * @author NanNan Wang
 */
public class MdcTraceIdProvider implements TraceIdProvider {
    /**
     * MDC中存储TraceId的键名
     */
    private final String mdcKey;

    public MdcTraceIdProvider(String mdcKey) {
        this.mdcKey = mdcKey;
    }

    @Override
    public String getTraceId() {
        return MDC.get(mdcKey);
    }
}

