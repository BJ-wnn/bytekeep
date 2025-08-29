//package org.wnn.business.feign;
//
//import feign.RequestInterceptor;
//import feign.RequestTemplate;
//import org.slf4j.MDC;
//import org.springframework.stereotype.Component;
//
///**
// * @author NanNan Wang
// */
//@Component
//public class FeignTraceIdInterceptor implements RequestInterceptor {
//
//    // 通常使用"traceId"作为头信息键名
//    private static final String TRACE_ID_HEADER = "traceId";
//
//    @Override
//    public void apply(RequestTemplate template) {
//        // 从MDC中获取当前线程的traceID
//        String traceId = MDC.get(TRACE_ID_HEADER);
//
//        // 如果存在traceID，则添加到请求头
//        if (traceId != null) {
//            template.header("X-Trace-Id", traceId);
//        }
//    }
//}
