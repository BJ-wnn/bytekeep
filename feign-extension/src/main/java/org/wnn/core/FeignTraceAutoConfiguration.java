package org.wnn.core;


import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import feign.RequestInterceptor;
import org.wnn.core.feign.MdcTraceIdProvider;
import org.wnn.core.feign.TraceIdFeignInterceptor;
import org.wnn.core.feign.TraceIdProperties;
import org.wnn.core.feign.TraceIdProvider;

/**
 * @author NanNan Wang
 */
@Configuration
@ConditionalOnClass(RequestInterceptor.class) // 当Feign的RequestInterceptor存在时才生效
@EnableConfigurationProperties(TraceIdProperties.class) // 启用配置属性
public class FeignTraceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean // 如果外部项目没有自定义TraceIdProvider，则使用默认实现
    public TraceIdProvider traceIdProvider(TraceIdProperties properties) {
        // 默认使用与Header同名的MDC键
        return new MdcTraceIdProvider(properties.getTraceIdKey());
    }

    @Bean
    @ConditionalOnMissingBean // 如果外部项目没有自定义拦截器，则使用默认实现
    public TraceIdFeignInterceptor traceIdFeignInterceptor(
            TraceIdProperties properties,
            TraceIdProvider traceIdProvider) {
        return new TraceIdFeignInterceptor(properties, traceIdProvider);
    }
}
