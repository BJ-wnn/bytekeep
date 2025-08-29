package org.wnn.core.feign;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TraceId配置属性，支持外部项目通过配置文件自定义
 * @author NanNan Wang
 */
@ConfigurationProperties(prefix = "feign.trace")
public class TraceIdProperties {

    /**
     * TraceId在Header中的键名
     */
    private String headerName = "X-Trace-Id";

    /**
     * TraceId在MDC中的键名
     */
    private String traceIdKey = "traceId";

    /**
     * 是否启用TraceId传递
     */
    private boolean enabled = true;

    // getter和setter
    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTraceIdKey() {
        return traceIdKey;
    }

    public void setTraceIdKey(String traceIdKey) {
        this.traceIdKey = traceIdKey;
    }
}
