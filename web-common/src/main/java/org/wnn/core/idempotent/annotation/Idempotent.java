package org.wnn.core.idempotent.annotation;

import java.lang.annotation.*;

/**
 * 用于标记方法需要进行幂等性校验的注解
 * <p>
 * 幂等性指多次调用同一接口时，产生的结果与单次调用一致，可有效防止因网络重试、前端重复提交等导致的重复处理问题。
 * 本注解通过要求请求携带特定HTTP头（默认"Idempotency-Key"）实现幂等性：
 * 1. 首次请求时，客户端生成唯一标识并放入指定HTTP头
 * 2. 服务端校验该标识，若未存在则处理请求并缓存标识
 * 3. 相同标识的重复请求在有效期内将直接返回首次处理结果，不重复执行业务逻辑
 * </p>
 *
 * @author NanNan Wang
 * @see java.lang.annotation.ElementType#METHOD 仅作用于方法
 * @see java.lang.annotation.RetentionPolicy#RUNTIME 运行时保留，便于AOP等机制解析处理
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * HTTP请求头中用于传递幂等性标识的键名
     * <p>客户端需将生成的唯一标识放入该请求头，服务端通过此键获取标识进行校验</p>
     *
     * @return 请求头名称，默认值为"Idempotency-Key"
     */
    String headerName() default "Idempotency-Key";

    /**
     * 幂等性标识的过期时间（单位：秒）
     * <p>超过该时间后，相同的幂等性标识可再次用于新的请求处理，避免标识永久占用资源</p>
     *
     * @return 过期时间，默认300秒（5分钟）
     */
    long expireSeconds() default 300;
}
