package org.wnn.core.idempotent.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * 非法幂等性令牌异常，用于在幂等性校验中检测到令牌格式不合法、为空或不符合约定规则时抛出。
 * <p>
 * 该异常继承自{@link ResponseStatusException}，携带HTTP状态码和具体错误信息，主要用于：
 * <ul>
 *   <li>校验请求头中幂等性令牌（如Idempotency-Key）为空的场景</li>
 *   <li>检测令牌格式不符合预设规则（如非UUID、长度不合法等）的情况</li>
 *   <li>作为幂等性校验流程中"令牌合法性验证失败"的专用异常</li>
 * </ul>
 * 通常与{@link Idempotent}注解及幂等性验证拦截器配合使用，为客户端提供明确的参数错误提示。
 * </p>
 *
 * @author NanNan Wang
 * @see ResponseStatusException 携带HTTP状态码的异常基类
 * @see Idempotent 标记需要幂等性校验的接口注解
 * @see IdempotentException 幂等性校验相关的重复提交异常
 */
public class IllegalIdempotentTokenException extends ResponseStatusException {


    /**
     * 构造非法幂等性令牌异常
     *
     * @param status HTTP状态码，通常使用400 Bad Request（请求参数错误）
     * @param reason 异常原因描述，建议明确提示令牌非法的具体原因（如"幂等性令牌格式错误，需为UUID"）
     */
    public IllegalIdempotentTokenException(HttpStatus status, String reason) {
        super(status, reason);
    }
}
