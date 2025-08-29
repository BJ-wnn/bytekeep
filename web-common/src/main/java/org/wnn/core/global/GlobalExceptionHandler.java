package org.wnn.core.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.wnn.core.idempotent.exception.IdempotentException;
import org.wnn.core.idempotent.exception.IllegalIdempotentTokenException;
import org.wnn.core.idempotent.exception.MissingIdempotentTokenException;
import org.wnn.core.global.response.constant.ResultCode;
import org.wnn.core.global.response.dto.CommonResponse;

/**
 * 全局异常处理器，统一捕获并处理应用中抛出的各类异常
 * <p>
 * 该类通过{@link RestControllerAdvice}注解实现全局异常拦截，针对不同类型的异常（如参数校验异常、系统异常等）
 * 提供统一的处理逻辑，将异常信息转换为标准化的响应格式返回给客户端，避免原始异常堆栈直接暴露，同时简化业务代码中的异常处理逻辑。
 * 注意： 业务异常由业务单独处理。
 * </p>
 * <p>
 * 核心功能包括：
 * <ul>
 *   <li>捕获Controller层抛出的所有未处理异常</li>
 *   <li>根据异常类型进行分类处理，返回对应的错误码和提示信息</li>
 *   <li>通过{@link Slf4j}记录异常详情，便于问题排查</li>
 *   <li>保证客户端始终收到结构化的响应，提升接口调用的友好性和一致性</li>
 * </ul>
 * </p>
 *
 * @author NanNan Wang
 * @see RestControllerAdvice 用于标识全局异常处理类，作用于所有@RestController
 * @see Slf4j 用于日志记录，输出异常详细信息
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有未明确指定的异常
     *
     * @param ex 发生的异常对象
     * @return 返回统一格式的错误响应
     */
    @ExceptionHandler(Exception.class)
    public CommonResponse<String> handleException(Exception ex) {
        log.error("系统异常: ", ex);
        return CommonResponse.fail(ResultCode.FAIL);
    }


    /**
     * 处理参数校验失败时抛出的异常（如使用 @Valid 注解校验失败）
     *
     * @param ex 参数校验异常对象
     * @return 返回统一格式的参数错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResponse<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("参数校验异常: ", ex);
        return CommonResponse.fail(ResultCode.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    public CommonResponse<String> handleBindException(BindException ex) {
        log.error("参数校验异常: ", ex);
        return CommonResponse.fail(ResultCode.BAD_REQUEST);
    }


    @ExceptionHandler(IdempotentException.class)
    public CommonResponse<String> handleIdempotentException(IdempotentException ex) {
        log.warn("幂等性异常: {}", ex.getMessage());
        return CommonResponse.fail(ResultCode.IDEMPOTENT_REJECTED);
    }

    @ExceptionHandler(MissingIdempotentTokenException.class)
    public CommonResponse<String> handleResponseStatusException(MissingIdempotentTokenException ex) {
        log.warn("缺少幂等性 Token: {}", ex.getMessage());
        return CommonResponse.fail(ResultCode.MISSING_IDEMPOTENT_TOKEN);
    }


    @ExceptionHandler(IllegalIdempotentTokenException.class)
    public CommonResponse<String> handleResponseStatusException(IllegalIdempotentTokenException ex) {
        log.warn("票据非法不存在，Token: {}", ex.getMessage());
        return CommonResponse.fail(ResultCode.ILLEGAL_IDEMPOTENT_TOKEN);
    }

}
