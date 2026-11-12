package com.example.yiliaoxitong.exception;

/**
 * 号源已满异常：当前排班剩余号源为 0 时抛出，事务回滚，患者收到友好提示。
 */
public class NoSlotAvailableException extends BusinessException {

    public NoSlotAvailableException(String message) {
        super(400, message);
    }
}
