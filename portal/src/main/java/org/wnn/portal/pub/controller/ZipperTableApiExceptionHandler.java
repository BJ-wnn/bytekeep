package org.wnn.portal.pub.controller;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.wnn.core.global.response.IResultCode;
import org.wnn.core.global.response.dto.CommonResponse;

/**
 * @author NanNan Wang
 */
@RestControllerAdvice(assignableTypes = {ZipperTableApiController.class})
public class ZipperTableApiExceptionHandler {

    /**
     * 处理参数非法异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public CommonResponse<String> handleIllegalArgument(IllegalArgumentException exception) {
        return new CommonResponse(ZipperResultCode.ILLEGAL_EFFECTIVE_DATE.getCode(),exception.getMessage(),null);
    }

    enum ZipperResultCode implements IResultCode {

        ILLEGAL_EFFECTIVE_DATE(1101,"拉链表生效日期传入的参数不正确");

        private int code;
        private String message;

        ZipperResultCode(int i, String s) {
            this.code = i;
            this.message = s;
        }


        @Override
        public int getCode() {
            return this.code;
        }

        @Override
        public String getMessage() {
            return this.message;
        }
    }


}
