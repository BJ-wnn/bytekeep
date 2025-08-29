package org.wnn.core.log;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 全局请求日志记录过滤器，用于捕获并记录HTTP请求与响应的详细信息，支持敏感数据掩码和路径过滤。
 * <p>
 * 该过滤器继承自{@link OncePerRequestFilter}，确保每个请求仅被处理一次。主要功能包括：
 * <ul>
 *   <li>记录请求方法、URI、查询参数、客户端地址等基本信息</li>
 *   <li>捕获请求体（针对JSON类型的POST/PUT/PATCH请求）和响应体（可配置开关）</li>
 *   <li>生成或复用traceId用于分布式追踪，关联请求全链路</li>
 *   <li>对敏感数据（如password、token）进行掩码处理，避免日志泄露</li>
 *   <li>支持配置排除特定路径（如静态资源、Swagger文档）的日志记录</li>
 *   <li>记录请求处理耗时，便于性能分析</li>
 * </ul>
 * 日志输出格式为JSON，便于日志收集与分析系统解析。
 * </p>
 *
 * @author NanNan Wang
 * @see OncePerRequestFilter 确保过滤器对每个请求仅执行一次
 * @see ContentCachingRequestWrapper 用于缓存请求体，便于后续读取
 * @see ContentCachingResponseWrapper 用于缓存响应体，便于日志记录
 */
public class WebHttpRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger("reqLog");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;
    private final ObjectMapper objectMapper;
    private final LoggingProperties props;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public WebHttpRequestLoggingFilter(LoggingProperties props) {
        this.props = props;
        this.objectMapper = Jackson2ObjectMapperBuilder.json()
                .modules(new JavaTimeModule())
                .build();
    }

    /**
     * 核心过滤方法，实现请求日志的捕获与记录逻辑
     *
     * @param request 原始HTTP请求对象
     * @param response 原始HTTP响应对象
     * @param filterChain 过滤器链，用于继续处理请求
     * @throws ServletException 处理请求时发生的Servlet异常
     * @throws IOException 处理请求/响应流时发生的IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();

        // 检查请求是否需要跳过日志记录
        if (shouldSkipLogging(request)) {
            filterChain.doFilter(request, response);
            MDC.remove("bizCode");  // 清理MDC中的业务编码，防止内存泄漏
            return;
        }

        // 包装请求（针对JSON类型的写操作请求）和响应（用于缓存响应体）
        HttpServletRequest wrappedRequest = wrapRequest(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            // 计算请求耗时并记录日志
            long duration = System.currentTimeMillis() - start;
            logRequest(wrappedRequest, wrappedResponse, duration);
            // 将缓存的响应体写回原始响应，避免客户端无法获取响应
            wrappedResponse.copyBodyToResponse();
            MDC.remove("bizCode");  // 清理MDC中的业务编码，防止内存泄漏
        }
    }

    /**
     * 包装请求对象，对JSON类型的写操作请求使用内容缓存包装器
     *
     * @param request 原始HTTP请求
     * @return 包装后的请求对象：对于POST/PUT/PATCH且Content-Type为JSON的请求，返回{@link ContentCachingRequestWrapper}；否则返回原始请求
     */
    private HttpServletRequest wrapRequest(HttpServletRequest request) {
        String method = request.getMethod();
        String contentType = request.getContentType();
        if ((HttpMethod.POST.matches(method) || HttpMethod.PUT.matches(method) || HttpMethod.PATCH.matches(method)) &&
                MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(contentType)) {
            return new ContentCachingRequestWrapper(request);
        }
        return request;
    }


    /**
     * 记录请求与响应的详细信息到日志
     *
     * @param request 可能被包装的请求对象
     * @param response 缓存响应体的响应包装器
     * @param duration 请求处理耗时（毫秒）
     */
    private void logRequest(HttpServletRequest request, ContentCachingResponseWrapper response, long duration) {
        LogEntry log = new LogEntry();

        // 获取或生成traceId，用于分布式追踪
        String traceId = MDC.get("traceId");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
            MDC.put("traceId", traceId);
        }

        // 填充日志基本信息
        log.setTraceId(traceId);
        log.setTimestamp(FORMATTER.format(LocalDateTime.now().atZone(ZoneOffset.UTC)));
        log.setMethod(request.getMethod());
        log.setUri(request.getRequestURI());
        log.setQueryString(request.getQueryString());
        log.setRemoteAddr(request.getRemoteAddr());
        log.setDuration(duration);

        // 记录响应状态码（优先使用业务自定义编码）
        String bizCode = MDC.get("bizCode");
        if (bizCode != null) {
            try {
                log.setResponseCode(Integer.parseInt(bizCode));
            } catch (NumberFormatException e) {
                log.setResponseCode(response.getStatus());
            }
        } else {
            log.setResponseCode(response.getStatus());
        }

        // 记录请求参数或请求体
        if (request instanceof ContentCachingRequestWrapper && props.isLogRequestBody()) {
            ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
            byte[] content = wrapper.getContentAsByteArray();

            String payload;
            try {
                String contentType = Optional.ofNullable(wrapper.getContentType()).orElse("").toLowerCase();
                boolean isTextContent = contentType.contains("json") || contentType.contains("xml")
                        || contentType.contains("html") || contentType.contains("text") || contentType.isEmpty();

                if (isTextContent) {
                    payload = new String(content, wrapper.getCharacterEncoding());

                    int maxLen = props.getMaxRequestLength(); // 你可以新增一个配置项
                    if (payload.length() > maxLen) {
                        payload = payload.substring(0, maxLen) +
                                String.format("...(truncated, totalLength=%d)", payload.length());
                    }
                } else {
                    payload = String.format("[binary content, length=%d bytes]", content.length);
                }
            } catch (UnsupportedEncodingException e) {
                payload = "[unreadable payload]";
            }

            log.setPayload(maskSensitiveData(payload));

        } else if(props.isLogRequestParams()) {
            // 对于非JSON请求，记录表单参数
            String args = request.getParameterMap().entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
                    .collect(Collectors.joining("&"));

            int maxLen = props.getMaxRequestLength();
            if (args.length() > maxLen) {
                args = args.substring(0, maxLen) +
                        String.format("...(truncated, totalLength=%d)", args.length());
            }
            log.setArgs(args);
        }

        // 若启用响应体日志，记录响应内容（截断超长内容）
        if (props.isLogResponseBody()) {
            byte[] body = response.getContentAsByteArray();

            String contentType = Optional.ofNullable(response.getContentType()).orElse("").toLowerCase();
            String responseBody;

            // 判断是否是文本内容
            boolean isTextContent = contentType.contains("json") || contentType.contains("xml")
                    || contentType.contains("html") || contentType.contains("text");

            if (isTextContent) {
                // 按UTF-8安全解码
                responseBody = new String(body, StandardCharsets.UTF_8);

                // 截断逻辑
                int maxLen = props.getMaxResponseLength();
                if (responseBody.length() > maxLen) {
                    responseBody = responseBody.substring(0, maxLen) +
                            String.format("...(truncated, totalLength=%d)", responseBody.length());
                }
            } else {
                // 非文本直接提示，不输出原内容
                responseBody = String.format("[binary content, length=%d bytes]", body.length);
            }

            log.setResponseBody(maskSensitiveData(responseBody));
        }
        try {
            if(log.getResponseCode() != 200 || duration > props.getMaxResponseTimeMillis()) {
                logger.info(objectMapper.writeValueAsString(log));
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize request log", e);
        }
    }


    /**
     * 对日志中的敏感数据进行掩码处理
     *
     * @param text 原始文本（请求体或响应体JSON字符串）
     * @return 处理后的文本，其中password和token字段的值被替换为"****"
     */
    private String maskSensitiveData(String text) {
        return text
                .replaceAll("\"password\"\\s*:\\s*\".*?\"", "\"password\":\"****\"")
                .replaceAll("\"token\"\\s*:\\s*\".*?\"", "\"token\":\"****\"");
    }


    /**
     * 判断是否需要跳过当前请求的日志记录
     *
     * @param request 当前HTTP请求
     * @return true表示跳过日志记录（如文件上传、排除路径的请求）；false表示需要记录
     */
    private boolean shouldSkipLogging(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contentType = request.getContentType();

        if ((contentType != null && contentType.contains(MediaType.MULTIPART_FORM_DATA_VALUE)) ||
                (contentType != null && contentType.equals(MediaType.APPLICATION_OCTET_STREAM_VALUE))) {
            return true;
        }

        // 为了排除一些静态资源请求、swagger等非业务请求
        for (String pattern : props.getExcludePaths()) {
            if (pathMatcher.match(pattern, uri)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 日志条目数据模型，用于封装请求日志的所有字段
     */
    @Data
    public static class LogEntry {
        @JsonProperty("@timestamp")
        private String timestamp; // 日志记录时间（UTC时区，ISO格式）
        private String traceId;   // 分布式追踪ID，用于关联请求链路
        private String method;    // HTTP请求方法（GET/POST等）
        private String uri;       // 请求URI路径
        private String queryString; // URL查询参数
        private String remoteAddr; // 客户端IP地址
        private String args;      // 非JSON请求的表单参数
        private String payload;   // JSON请求的请求体（已掩码敏感数据）
        private int responseCode; // 响应状态码（业务编码或HTTP状态码）
        private String responseBody; // 响应体（已掩码敏感数据，可配置开关）
        private long duration;    // 请求处理耗时（毫秒）
    }

    /**
     * 请求日志配置属性类，与配置文件中"logging.request"前缀的配置绑定
     */
    @ConfigurationProperties(prefix = "logging.request")
    @Data
    public static class LoggingProperties {
        private boolean enabled = false; // 是否启用请求日志记录
        private boolean logResponseBody = false; // 是否启用响应体日志记录
        private boolean logRequestBody = false; // 是否启用请求体日志记录
        private boolean logRequestParams = false; // 是否启用请求参数日志记录
        private int maxResponseLength = 2000;  // 响应体日志的最大长度（超出部分截断）
        private int maxRequestLength = 2000; // 请求体日志的最大长度（超出部分截断，默认2000字符）
        private long maxResponseTimeMillis = 2000; // 最大响应时间（毫秒）：超过此时间的请求才记录日志
        private List<String> excludePaths = new ArrayList<>();  // 排除日志记录的路径列表（支持Ant风格表达式）
    }
}
