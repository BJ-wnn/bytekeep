package org.wnn.core.idempotent.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * 接口幂等性校验异常，用于在接口检测到重复提交或幂等性验证失败时抛出。
 * <p>
 * 该异常继承自{@link ResponseStatusException}，可携带HTTP状态码和详细错误信息，
 * 通常与{@link Idempotent}注解配合使用：当请求携带的幂等性标识（如Idempotency-Key）
 * 已存在且未过期时，由幂等性校验逻辑抛出，用于告知客户端当前请求为重复提交，避免重复执行业务逻辑。
 * </p>
 *
 * @author NanNan Wang
 * @see ResponseStatusException 用于携带HTTP状态码的异常基类
 * @see Idempotent 标记需要进行幂等性校验的接口注解
 */
public class IdempotentException extends ResponseStatusException {

    /**
     * 构造幂等性校验异常
     *
     * @param status HTTP状态码，通常使用409 Conflict（请求冲突）或429 Too Many Requests（请求过多）
     * @param reason 异常原因描述，建议包含幂等性校验失败的具体信息（如"重复提交，请稍后再试"）
     */
    public IdempotentException(HttpStatus status, String reason) {
        super(status, reason);
    }
}
