package com.example.yiliaoxitong.exception;

/**
 * 库存不足异常：处方中任一药品库存不足时抛出，整张处方回滚。
 */
public class StockNotEnoughException extends BusinessException {

    public StockNotEnoughException(String message) {
        super(400, message);
    }
}
