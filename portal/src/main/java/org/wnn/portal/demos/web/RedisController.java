package org.wnn.portal.demos.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wnn.portal.pub.service.RedisService;

import java.util.concurrent.TimeUnit;

/**
 * @author NanNan Wang
 */
@RestController
@RequestMapping("/redis")
@RequiredArgsConstructor
public class RedisController {

    private final RedisService redisService;

    /**
     * 设置缓存并指定过期时间
     */
    @PostMapping("/setWithExpire")
    public String setWithExpire(@RequestParam String key, @RequestParam String value,
                                @RequestParam long timeout) {
        redisService.set(key, value, timeout, TimeUnit.SECONDS);
        return "设置成功，" + timeout + "秒后过期";
    }



}

