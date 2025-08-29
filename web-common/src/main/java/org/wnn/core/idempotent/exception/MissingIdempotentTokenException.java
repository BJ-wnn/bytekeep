package org.wnn.core.idempotent.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * 缺少幂等性令牌异常，用于在幂等性校验中检测到请求未携带必要的幂等性令牌时抛出。
 * <p>
 * 该异常继承自{@link ResponseStatusException}，携带HTTP状态码和具体错误信息，主要应用场景包括：
 * <ul>
 *   <li>请求头中未包含幂等性令牌字段（如{@code Idempotency-Key}）</li>
 *   <li>标记了{@link Idempotent}注解的接口必须携带幂等性令牌，但请求中未提供</li>
 *   <li>作为幂等性校验流程中"令牌缺失"场景的专用异常，与令牌格式错误、重复提交等异常区分</li>
 * </ul>
 * 抛出此异常时，通常提示客户端需在请求头中添加合法的幂等性令牌，以满足接口的幂等性要求。
 * </p>
 *
 * @author NanNan Wang
 * @see ResponseStatusException 携带HTTP状态码的异常基类
 * @see Idempotent 标记需要幂等性校验的接口注解
 * @see IdempotentException 幂等性校验中的重复提交异常
 * @see IllegalIdempotentTokenException 幂等性令牌格式非法的异常
 */
public class MissingIdempotentTokenException extends ResponseStatusException {

    /**
     * 构造缺少幂等性令牌异常
     *
     * @param status HTTP状态码，通常使用400 Bad Request（请求参数错误）
     * @param reason 异常原因描述，建议明确提示缺少的令牌字段（如"请求头中缺少必要的Idempotency-Key"）
     */
    public MissingIdempotentTokenException(HttpStatus status, String reason) {
        super(status, reason);
    }
}
