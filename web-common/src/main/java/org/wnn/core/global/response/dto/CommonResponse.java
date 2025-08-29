package org.wnn.core.global.response.dto;

import lombok.Data;
import org.wnn.core.global.response.IResultCode;
import org.wnn.core.global.response.constant.ResultCode;

/**
 * 通用响应类，用于封装返回给前端的数据格式
 *
 * @author NanNan Wang
 */
@Data
public class CommonResponse<T> {
    /**
     * 响应状态码，表示操作结果的状态
     */
    private int code;

    /**
     * 响应消息，描述操作结果的简要信息
     */
    private String message;

    /**
     * 响应数据，泛型类型，表示实际返回的数据
     */
    private T data;

    public CommonResponse() {
    }

    public CommonResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }


    /**
     * 通用构建方法，根据指定的结果码和数据生成响应对象。
     *
     * @param code 结果码实现类
     * @param data 返回的数据
     * @return 构建好的 CommonResponse 对象
     */
    public static <T> CommonResponse<T> of(IResultCode code, T data) {
        return new CommonResponse<>(code.getCode(), code.getMessage(), data);
    }

    /**
     * 快捷方法：构建一个表示“成功”的响应，并携带返回数据。
     *
     * @param data 成功时返回的数据
     * @return 构建好的成功响应对象
     */
    public static <T> CommonResponse<T> success(T data) {
        return of(ResultCode.SUCCESS, data);
    }

    /**
     * 快捷方法：构建一个无返回数据的“成功”响应。
     *
     * @return 构建好的成功响应对象
     */
    public static CommonResponse<Void> success() {
        return of(ResultCode.SUCCESS, null);
    }

    /**
     * 快捷方法：构建一个表示“失败”的响应。
     *
     * @param code 失败原因对应的结果码
     * @return 构建好的失败响应对象
     */
    public static <T> CommonResponse<T> fail(IResultCode code) {
        return of(code, null);
    }
}
