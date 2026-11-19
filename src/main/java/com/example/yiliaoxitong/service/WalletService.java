package com.example.yiliaoxitong.service;

import com.example.yiliaoxitong.dto.RechargeRequest;

import java.math.BigDecimal;

/**
 * 患者钱包服务：余额查询、沙箱充值、余额扣减（购药/处方付款共用）。
 */
public interface WalletService {

    BigDecimal balanceOf(Long patientId);

    BigDecimal recharge(Long patientId, RechargeRequest request);

    /** 余额扣减，余额不足抛出业务异常 */
    void deduct(Long patientId, BigDecimal amount);
}
