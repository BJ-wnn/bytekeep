package org.wnn.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.wnn.core.idempotent.IdempotentAspect;
import org.wnn.core.idempotent.IdempotentTokenService;
import org.wnn.core.idempotent.RedisIdempotentTokenService;

import javax.servlet.http.HttpServletRequest;


/**
 * 幂等性切面的自动配置类
 *
 * <p>该类基于Spring Boot自动配置机制，实现幂等性相关组件的自动装配。
 * 仅当项目中存在{@link StringRedisTemplate}类且容器中已注册{@link StringRedisTemplate} Bean时生效，
 * 主要负责配置{@link IdempotentTokenService}和{@link IdempotentAspect}相关Bean。
 *
 * @author NanNan Wang
 * @see StringRedisTemplate
 * @see IdempotentTokenService
 * @see RedisIdempotentTokenService
 * @see IdempotentAspect
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)  // 限制仅Servlet Web环境生效
@ConditionalOnClass({StringRedisTemplate.class, RedisConnectionFactory.class}) // 确保Redis核心类存在
public class IdempotentAspectAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    /**
     * 创建基于Redis的幂等性令牌服务Bean
     *
     * <p>该方法仅在容器中不存在{@link IdempotentTokenService}类型Bean时生效，
     * 通过注入{@link StringRedisTemplate}实例化{@link RedisIdempotentTokenService}，
     * 提供基于Redis的令牌生成、验证和清理功能，用于实现接口幂等性控制。
     *
     * @param redisTemplate Redis操作模板，用于令牌的存储与管理
     * @return 基于Redis的IdempotentTokenService实现类实例
     */
    @Bean
    @ConditionalOnMissingBean(IdempotentTokenService.class)
    public RedisIdempotentTokenService redisIdempotentTokenService(StringRedisTemplate redisTemplate) {
        return new RedisIdempotentTokenService(redisTemplate);
    }

    /**
     * 创建幂等性切面Bean
     *
     * <p>该方法在容器中存在{@link IdempotentTokenService}且不存在{@link IdempotentAspect}时生效，
     * 实例化{@link IdempotentAspect}并注入HTTP请求工厂和幂等性令牌服务，
     * 用于拦截带有幂等性注解的方法，实现请求幂等性校验逻辑。
     *
     * @param requestFactory HTTP请求对象的工厂类，用于获取当前请求上下文
     * @param idempotentTokenService 幂等性令牌服务，提供令牌相关操作
     * @return 幂等性切面实例，用于处理幂等性注解逻辑
     */
    @Bean
    @ConditionalOnBean(IdempotentTokenService.class)
    @ConditionalOnMissingBean(IdempotentAspect.class)
    public IdempotentAspect idempotentAspect(ObjectFactory<HttpServletRequest> requestFactory,
                                             IdempotentTokenService idempotentTokenService) {
        return new IdempotentAspect(requestFactory, idempotentTokenService);
    }
}
