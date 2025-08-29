package org.wnn.core.global;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.wnn.core.global.response.constant.ResultCode;
import org.wnn.core.global.response.annotation.ResponseAutoWrap;
import org.wnn.core.global.response.annotation.SkipResponseAutoWrap;
import org.wnn.core.global.response.dto.CommonResponse;

/**
 * 全局响应结果封装处理器，用于统一Controller层接口的返回格式
 * <p>
 * 该类通过{@link ControllerAdvice}实现全局拦截，结合{@link ResponseBodyAdvice}接口对Controller返回的响应体进行后置处理。
 * 核心特性是：仅对被{@link ResponseAutoWrapResponse}注解标记的接口生效，将其返回结果封装为标准化的响应结构（通常包含状态码、消息、数据等字段）。
 * </p>
 * <p>
 * 工作原理：
 * <ol>
 *   <li>拦截所有Controller方法的返回结果（在响应体写入前触发）</li>
 *   <li>通过{@link #supports(MethodParameter, Class)}方法判断当前接口是否被{@link ResponseAutoWrapResponse}标记</li>
 *   <li>若需要封装，则通过{@link #beforeBodyWrite(Object, MethodParameter, MediaType, Class, ServerHttpRequest, ServerHttpResponse)}方法将原始结果转换为统一响应格式</li>
 * </ol>
 * 此设计既保证了响应格式的一致性，又通过注解实现了灵活控制，避免对不需要封装的接口（如文件下载、特定协议接口）造成干扰。
 * </p>
 *
 * @author NanNan Wang
 * @see ControllerAdvice 用于标识全局控制器增强类，作用于所有Controller
 * @see ResponseBodyAdvice 用于对响应体进行后置处理的接口
 * @see ResponseAutoWrapResponse 用于标记需要进行响应封装的接口的注解
 */
@ControllerAdvice
public class GlobalResponseWrapperAdvice implements ResponseBodyAdvice<Object> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 如果标记了 @SkipAutoWrapResponse，直接跳过封装
        if (AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), SkipResponseAutoWrap.class) ||
                returnType.hasMethodAnnotation(SkipResponseAutoWrap.class)) {
            return false;
        }

        // 只有标记了 @AutoWrapResponse 的才进行封装
        return AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), ResponseAutoWrap.class) ||
                returnType.hasMethodAnnotation(ResponseAutoWrap.class);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        // 如果已经是封装后的格式，直接返回
        if (body instanceof CommonResponse || body instanceof ResponseEntity) {
            CommonResponse<?> apiResponse = (CommonResponse<?>) body;
            MDC.put("bizCode", String.valueOf(apiResponse.getCode()));
            return body;
        }

        // Spring MVC 对 String 类型的返回值会直接处理为 StringHttpMessageConverter，导致你包装成对象会报错。
        if (body instanceof String) {
            try {
                return objectMapper.writeValueAsString(CommonResponse.success(body));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("String 类型序列化失败", e);
            }
        }
        MDC.put("bizCode", String.valueOf(ResultCode.SUCCESS.getCode()));
        return CommonResponse.success(body);
    }
}

