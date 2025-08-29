package org.wnn.core.idempotent;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

/**
 * 基于Redis实现的幂等性Token服务类，用于验证和消费幂等Token，防止接口重复提交。
 *
 * <p>该服务通过Redis存储Token状态，并使用Lua脚本保证操作的原子性，
 * 在高并发场景下确保同一Token只能被成功消费一次，从而实现接口的幂等性。
 *
 * @author NanNan wang
 * @version 1.0
 */
public class RedisIdempotentTokenService implements IdempotentTokenService{

    private final StringRedisTemplate redisTemplate;

    public RedisIdempotentTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private final String CHECK_TOKEN_SCRIPT =
            "local val = redis.call('get', KEYS[1]) " +
            "if not val then " +
            "    return -1 " + // 令牌不存在
            "end " +
            "if val == 'true' then " +
            "    return 0 " +
            "end " +
            "local ttl = redis.call('ttl', KEYS[1]) " + //获取剩余过期时间
            "if ttl == -1 then " +
            "    ttl = 5 " + // 处理永久有效情况，设置默认过期时间
            "end " +
            "if ttl > 0 then " +
            "    redis.call('set', KEYS[1], 'true', 'XX', 'EX', ttl) " + //-- 更新状态并保留过期时间
            "    return 1 " + //-- 令牌使用成功
            "else " +
            "    return -2 " + //-- 令牌已过期
            "end";

    @Override
    public int tryUseToken(String token) {
        // 构建Redis中存储Token的键（格式：idempotent:token:{token}）
        String redisKey = "idempotent:token:" + token;
        // Lua脚本：原子性检查并更新Token状态
        // 初始化Redis脚本对象，指定返回值类型为Long
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(CHECK_TOKEN_SCRIPT, Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(redisKey));
        return result == null ? -1 : result.intValue();
    }

}
