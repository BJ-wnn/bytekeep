package org.wnn.core;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.wnn.core.global.*;

/**
 * @author NanNan Wang
 */
@Configuration
@ConditionalOnWebApplication // 仅在 Web 环境生效
@Import({
        GlobalExceptionHandler.class,
        GlobalResponseWrapperAdvice.class,
})
public class GlobalResponseAutoConfiguration {



}
