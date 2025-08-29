package org.wnn.core.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * @author NanNan Wang
 */
public class TraceIdFeignInterceptor implements RequestInterceptor {

    private final TraceIdProperties properties;
    private final TraceIdProvider traceIdProvider;

    public TraceIdFeignInterceptor(TraceIdProperties properties, TraceIdProvider traceIdProvider) {
        this.properties = properties;
        this.traceIdProvider = traceIdProvider;
    }

    @Override
    public void apply(RequestTemplate template) {
        // 如果未启用或没有TraceId，则不添加
        if (!properties.isEnabled()) {
            return;
        }

        String traceId = traceIdProvider.getTraceId();
        if (traceId != null && !traceId.isEmpty()) {
            template.header(properties.getHeaderName(), traceId);
        }
    }

}
