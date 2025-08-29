package org.wnn.core.global.response;

/**
 * 结果码接口，用于定义统一的业务状态码规范。
 * 所有实现该接口的枚举或类都应提供状态码和对应的描述信息。
 *
 * @author NanNan Wang
 */
public interface IResultCode {
    /**
     * 获取状态码数值，通常用于标识请求结果（如 200 表示成功，400 表示参数错误等）
     *
     * @return 状态码整数值
     */
    int getCode();

    /**
     * 获取状态码对应的描述信息，用于返回给前端展示
     *
     * @return 状态描述字符串
     */
    String getMessage();
}
