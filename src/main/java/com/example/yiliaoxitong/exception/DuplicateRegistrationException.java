package com.example.yiliaoxitong.exception;

/**
 * 重复挂号异常：同一患者对同一医生同一天重复挂号时抛出。
 */
public class DuplicateRegistrationException extends BusinessException {

    public DuplicateRegistrationException(String message) {
        super(400, message);
    }
}
