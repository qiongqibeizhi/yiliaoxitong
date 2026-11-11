package com.example.yiliaoxitong.exception;

/**
 * 业务异常：用于抛出可被前端直接展示的友好提示。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
