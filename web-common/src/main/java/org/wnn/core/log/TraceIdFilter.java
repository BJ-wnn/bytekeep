package org.wnn.core.log;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * 分布式追踪过滤器，用于生成、透传和管理请求链路的唯一标识（traceId），支持分布式系统的日志关联与问题排查。
 * <p>
 * 该过滤器通过以下方式实现追踪功能：
 * <ul>
 *   <li>优先从请求头{@value #TRACE_ID_HEADER}获取已有的traceId（适用于微服务间调用，实现链路透传）</li>
 *   <li>若请求头中无traceId，则自动生成一个UUID作为新的traceId（去除横杠并转为大写，确保格式统一）</li>
 *   <li>将traceId存入MDC（Mapped Diagnostic Context），供日志框架在输出日志时自动包含该标识</li>
 *   <li>将traceId设置到响应头{@value #TRACE_ID_HEADER}，便于客户端获取并用于后续问题追踪</li>
 *   <li>请求处理完成后清除MDC中的traceId，避免内存泄漏</li>
 * </ul>
 * 标注{@link Order#value()}为1，确保在其他依赖traceId的过滤器（如日志记录过滤器）之前执行，保证全链路traceId的一致性。
 * </p>
 *
 * @author NanNan Wang
 * @see Filter 标准Servlet过滤器接口，用于拦截HTTP请求
 * @see MDC 日志诊断上下文，用于在多线程环境下传递上下文信息（如traceId）
 */
public class TraceIdFilter implements Filter {

    /**
     * MDC中存储traceId的键名，日志配置中可通过该键引用traceId（如%X{traceId}）
     */
    private static final String TRACE_ID_KEY = "traceId";

    /**
     * HTTP请求头和响应头中用于传递traceId的键名，客户端可通过该头传递或获取traceId
     */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";


    /**
     * 拦截HTTP请求，处理traceId的生成、透传与清理逻辑
     *
     * @param request  Servlet请求对象，用于获取请求头中的traceId
     * @param response Servlet响应对象，用于设置响应头中的traceId
     * @param chain    过滤器链，用于将请求传递给后续过滤器或处理器
     * @throws IOException      处理请求/响应流时发生的IO异常
     * @throws ServletException 处理请求时发生的Servlet异常
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 转换为HTTP请求/响应对象，以便操作请求头和响应头
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 1. 尝试从请求头获取已有traceId（微服务调用时透传上游服务的traceId）
        String traceId = httpRequest.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.trim().isEmpty()) {
            // 2. 若不存在则生成新的traceId（UUID去除横杠并大写，减少长度且格式统一）
            traceId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        }

        try {
            // 3. 将traceId存入MDC，供日志框架使用（如输出日志时自动带上该标识）
//            MDC.put(TRACE_ID_KEY, traceId);
            TraceContext.setTraceId(traceId);

            // 4. 将traceId设置到响应头，方便客户端获取并用于问题追踪
            httpResponse.setHeader(TRACE_ID_HEADER, traceId);

            // 5. 继续执行过滤器链，将请求传递给后续处理
            chain.doFilter(request, response);
        } finally {
            // 6. 无论请求处理是否成功，都清除MDC中的traceId，避免多线程环境下的上下文污染
//            MDC.remove(TRACE_ID_KEY);
            TraceContext.clear();
        }
    }
}
