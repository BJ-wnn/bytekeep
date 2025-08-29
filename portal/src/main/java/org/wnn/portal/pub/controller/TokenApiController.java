package org.wnn.portal.pub.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.DigestUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wnn.core.global.response.annotation.ResponseAutoWrap;
import org.wnn.portal.pub.controller.req.IdempotentTokenCreateDTO;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;

/**
 * @author NanNan Wang
 */
@RestController
@RequestMapping("/api/public/token")
@RequiredArgsConstructor
@Api(value = "门户公共功能")
@ResponseAutoWrap
@Slf4j
public class TokenApiController {
    // 注意序列化问题，幂等要用 StringRedisTemplate，否则要自己实现 IdempotentTokenService
    private final StringRedisTemplate redisTemplate;

    @ApiOperation(
            value = "获取幂等性令牌",
            notes = "用于生成防止接口重复提交的幂等性令牌，适用于下单、支付、表单提交等需要确保操作唯一性的场景。" +
                    "使用方式：生成令牌后，需在后续请求的请求头或参数中携带该令牌，接口会校验令牌有效性。" +
                    "默认有效期30秒，若需自定义有效期，可通过请求参数指定。" +
                    "返回值为生成的令牌字符串，前端需临时存储（如localStorage）。",
            response = String.class,  // 明确返回数据类型（令牌为字符串）
            code = 200,  // 接口成功时的HTTP状态码（默认200，显式指定更清晰）
            tags = {"门户公共功能", "幂等性控制"}  // 补充标签，方便在Swagger文档中按功能归类
    )
    @PostMapping("/get-idempotent-token")
    public String generateToken(@Validated @RequestBody IdempotentTokenCreateDTO request) {

        // 1. token = interfaceName  + interfaceParamStr 生成参数哈希（直接内联，无需单独方法）
        String token = DigestUtils.md5DigestAsHex(Strings.concat(request.getInterfaceParamStr(),request.getInterfaceParamStr()).getBytes(StandardCharsets.UTF_8))
                .substring(8, 24); // 16位MD5哈希

        // 2. 生成Redis键
        String redisKey = String.format("idempotent:token:%s", token);

        // 3. 处理过期时间，默认5秒
        long expireSeconds = Optional.ofNullable(request.getExpireSeconds())
                .filter(seconds -> seconds > 0)
                .orElse(5L);
        log.debug("生成幂等性token，过期时间设置为: {}秒", expireSeconds); // 增加日志输出，便于调试过期时间问题

        // 4. Lua 脚本保证：如果不存在，才设置键值和过期时间，存在就不操作
        String luaScript =
                "if redis.call('exists', KEYS[1]) == 0 then " +
                        "  redis.call('set', KEYS[1], ARGV[1], 'EX', tonumber(ARGV[2])) " +
                        "  return 1 " +
                        "else " +
                        "  return 0 " +
                        "end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        redisTemplate.execute(redisScript,
                Collections.singletonList(redisKey),
                "false",
                String.valueOf(expireSeconds));

        return token;
    }
}
