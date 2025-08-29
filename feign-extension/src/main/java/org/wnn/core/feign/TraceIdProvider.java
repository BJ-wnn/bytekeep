package org.wnn.core.feign;


/**
 * TraceId提供器接口，定义获取TraceId的规范
 * 外部项目可实现此接口自定义TraceId获取方式
 * @author NanNan Wang
 */
public interface TraceIdProvider {
    /**
     * 获取当前上下文的TraceId
     * @return TraceId，如果不存在返回null
     */
    String getTraceId();
}
