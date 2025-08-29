package org.wnn.core.idempotent;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.wnn.core.idempotent.annotation.Idempotent;
import org.wnn.core.idempotent.exception.IdempotentException;
import org.wnn.core.idempotent.exception.IllegalIdempotentTokenException;
import org.wnn.core.idempotent.exception.MissingIdempotentTokenException;

import javax.servlet.http.HttpServletRequest;

/**
 * 接口幂等性校验切面，用于拦截标记了{@link Idempotent}注解的方法，实现请求幂等性控制。
 * <p>
 * 该切面通过AOP环绕通知（@Around）实现对目标方法的拦截，核心校验流程如下：
 * <ol>
 *   <li>从请求头中获取{@link Idempotent#headerName()}指定的幂等性令牌（如Idempotency-Key）</li>
 *   <li>若令牌不存在或为空，抛出{@link MissingIdempotentTokenException}</li>
 *   <li>调用{@link IdempotentTokenService#tryUseToken(String)}验证令牌有效性：
 *     <ul>
 *       <li>验证通过（令牌未被使用过）：允许目标方法执行，并标记令牌为已使用</li>
 *       <li>验证失败（令牌已被使用）：抛出{@link IdempotentException}阻止重复执行</li>
 *     </ul>
 *   </li>
 * </ol>
 * 切面仅在Spring容器中存在{@link IdempotentTokenService}接口的实现类时生效（通过{@link ConditionalOnBean}控制），
 * 确保幂等性校验逻辑可灵活扩展（如基于Redis、本地缓存等实现令牌管理）。
 * </p>
 *
 * @author NanNan Wang
 * @see Aspect AOP切面标识，用于定义切面逻辑
 * @see Idempotent 标记需要进行幂等性校验的方法注解
 * @see IdempotentTokenService 幂等性令牌管理服务接口，提供令牌验证与使用功能
 * @see MissingIdempotentTokenException 当请求头中缺少幂等性令牌时抛出的异常
 * @see IdempotentException 当检测到重复提交（令牌已被使用）时抛出的异常
 * @see ConditionalOnBean 条件注解，确保仅在存在IdempotentTokenService实现时生效
 */
@Aspect
@RequiredArgsConstructor
public class IdempotentAspect {

    private final ObjectFactory<HttpServletRequest> requestFactory;
    private final IdempotentTokenService idempotentTokenService;


    /**
     * 环绕拦截带有{@link Idempotent}注解的方法，执行幂等性校验逻辑
     *
     * @param joinPoint AOP连接点，用于获取目标方法信息并执行目标方法
     * @param idempotent 方法上的{@link Idempotent}注解实例，包含幂等性配置（如令牌请求头名、过期时间）
     * @return 目标方法的执行结果
     * @throws Throwable 目标方法执行过程中抛出的异常，或幂等性校验失败时抛出的{@link MissingIdempotentTokenException}、{@link IdempotentException}
     */
    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        HttpServletRequest request = requestFactory.getObject();
        // 从注解中获取幂等性令牌的请求头名称
        String headerName = idempotent.headerName();
        // 从请求头中获取令牌值
        String token = request.getHeader(headerName);

        // 校验令牌是否存在
        if (token == null || token.trim().isEmpty()) {
            throw new MissingIdempotentTokenException(HttpStatus.BAD_REQUEST, "缺少幂等性Token");
        }

        final int tokenStatus = idempotentTokenService.tryUseToken(token);
        // 调用令牌服务验证并使用令牌，若验证失败（已被使用）则抛出重复提交异常
        if ( tokenStatus == 0) {
            throw new IdempotentException(HttpStatus.CONFLICT, "重复提交");
        } else if (tokenStatus == -1) {
            throw new IllegalIdempotentTokenException(HttpStatus.BAD_REQUEST, "幂等性Token失效");
        }

        // 验证通过，执行目标方法
        return joinPoint.proceed();
    }

}
