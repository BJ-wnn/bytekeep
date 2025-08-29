package org.wnn.core.global.response.constant;

import org.wnn.core.global.response.IResultCode;

/**
 * @author NanNan Wang
 */
public enum ResultCode implements IResultCode {

    SUCCESS(200, "Success"),
    FAIL(500, "Internal Server Error"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Resource Not Found"),
    BAD_REQUEST(400, "Bad Request"),
    IDEMPOTENT_REJECTED(409,"重复提交"),
    MISSING_IDEMPOTENT_TOKEN(460, "缺少幂等性 Token"),
    ILLEGAL_IDEMPOTENT_TOKEN(461,"非法token"),

    // 业务错误 1000+
    USER_NOT_EXIST(1001, "User Not Found"),
    DATA_CONFLICT(1002, "Data Conflict");
    ;

    private final int statusCode;
    private final String message;

    ResultCode(int code, String message) {
        this.statusCode = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
