package org.wnn.core;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wnn.core.log.WebHttpRequestLoggingFilter;
import org.wnn.core.log.TraceIdFilter;

/**
 * 请求日志过滤器与TraceId过滤器的自动配置类
 *
 * <p>该类基于Spring Boot自动配置机制，负责将{@link TraceIdFilter}和{@link WebHttpRequestLoggingFilter}
 * 注册到Spring容器中，并配置其执行顺序和拦截的URL模式。同时启用{@link WebHttpRequestLoggingFilter.LoggingProperties}
 * 配置属性绑定，支持通过外部配置自定义请求日志行为。
 *
 * @author NanNan Wang
 * @see WebHttpRequestLoggingFilter
 * @see TraceIdFilter
 * @see WebHttpRequestLoggingFilter.LoggingProperties
 */
@Configuration
@EnableConfigurationProperties(WebHttpRequestLoggingFilter.LoggingProperties.class)
public class LogFilterAutoConfiguration {

    private final WebHttpRequestLoggingFilter.LoggingProperties props;

    public LogFilterAutoConfiguration(WebHttpRequestLoggingFilter.LoggingProperties props) {
        this.props = props;
    }


    /**
     * 注册TraceId过滤器Bean
     *
     * <p>该方法创建{@link TraceIdFilter}的注册Bean，设置其执行顺序为1（优先级较高），
     * 确保在请求处理链路的早期生成并注入TraceId，以便后续日志能关联同一请求的追踪ID。
     * 拦截所有URL路径("/*")。
     *
     * @return 配置好的TraceIdFilter注册Bean
     */
    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter());
        registration.setOrder(1);  // 确保在 TraceIdFilter 之后
        registration.addUrlPatterns("/*");
        return registration;
    }

    /**
     * 注册请求日志过滤器Bean
     *
     * <p>该方法创建{@link WebHttpRequestLoggingFilter}的注册Bean，设置其执行顺序为2（在TraceIdFilter之后），
     * 确保日志中能包含已生成的TraceId。通过注入的{@link WebHttpRequestLoggingFilter.LoggingProperties}
     * 配置过滤器行为，拦截所有URL路径("/*")。
     *
     * @return 配置好的RequestLoggingFilter注册Bean
     */
    @Bean
    @ConditionalOnProperty(prefix = "logging.request", name = "enabled", havingValue = "true")
    public FilterRegistrationBean<WebHttpRequestLoggingFilter> requestLoggingFilter() {
        FilterRegistrationBean<WebHttpRequestLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new WebHttpRequestLoggingFilter(props));
        registration.setOrder(2);  // 确保在 TraceIdFilter 之后
        registration.addUrlPatterns("/*");
        return registration;
    }
}
