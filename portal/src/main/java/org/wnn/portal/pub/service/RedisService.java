package org.wnn.portal.pub.service;

import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * Redis 服务类，封装 Redis 的基本操作
 *
 * @author NanNan Wang
 */
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置缓存
     * @param key 键
     * @param value 值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }


    /**
     * 设置缓存并指定过期时间
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }


    /**
     * 原子性地设置键值对，仅当键不存在时生效，并为键设置过期时间
     *
     * <p>该方法对应Redis的SET命令的NX（Only set the key if it does NOT exist）和EX/PX（过期时间）组合功能，
     * 操作具有原子性，可用于实现分布式锁、防止重复创建等场景。</p>
     *
     * <p>行为说明：
     * <ul>
     *   <li>若键（key）不存在，则设置键值对（key -> value），并指定过期时间（timeout），单位由unit指定</li>
     *   <li>若键（key）已存在，则不执行任何操作，保持原有的键值和过期时间</li>
     * </ul>
     * </p>
     *
     * @param key 要设置的Redis键（非空）
     * @param value 要设置的值（若为null，可能会被Redis序列化器处理为"null"字符串，具体取决于RedisTemplate的配置）
     * @param timeout 键的过期时间（必须大于0，否则可能导致键永久有效）
     * @param unit 过期时间的单位（如TimeUnit.SECONDS表示秒，非空）
     *
     * @return 是否设置成功
     */
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value,timeout, unit);
    }

    /**
     * 获取缓存
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     * @param key 键
     * @return 是否删除成功
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 判断 key 是否存在
     * @param key 键
     * @return 是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     * @param key 键
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否设置成功
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }


}
