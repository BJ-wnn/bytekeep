package org.wnn.core.idempotent;

/**
 * 幂等性令牌服务接口，定义幂等性令牌的验证与使用规范，用于配合{@link IdempotentAspect}实现接口幂等性控制。
 * <p>
 * 该接口提供令牌的原子性验证逻辑，具体实现可基于不同存储介质（如Redis、本地缓存、数据库等），
 * 核心功能是判断令牌是否已被使用，从而防止重复提交。实现类需保证验证与标记令牌状态的操作原子性，
 * 避免并发场景下的幂等性失效。
 * </p>
 *
 * @author NanNan Wang
 * @see IdempotentAspect 依赖此接口实现幂等性校验的切面
 * @see Idempotent 标记需要幂等性校验的方法注解
 * @see IdempotentException 当令牌已被使用（重复提交）时抛出的异常
 */
public interface IdempotentTokenService {

    /**
     * 尝试使用幂等性令牌进行验证，判断当前请求是否为首次提交。
     * <p>
     * 方法执行逻辑：
     * <ul>
     *   <li>若令牌不存在（未被使用）：标记令牌为"已使用"状态，并返回true（允许继续处理请求）</li>
     *   <li>若令牌已存在（已被使用）：直接返回false（阻止重复处理请求）</li>
     * </ul>
     * 实现时需保证操作的原子性（如使用Redis的SET NX命令、数据库唯一索引等），防止并发场景下的判断误差。
     * </p>
     *
     * @param token 幂等性令牌值，通常由客户端生成（如UUID），并通过请求头传递
     * @return 1 表示令牌未被使用（首次提交）；0 表示令牌已被使用（重复提交）; -1：表示令牌非法
     */
    int tryUseToken(String token);

}
