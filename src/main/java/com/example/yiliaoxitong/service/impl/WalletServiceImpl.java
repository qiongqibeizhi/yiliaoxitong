package com.example.yiliaoxitong.service.impl;

import com.example.yiliaoxitong.dto.RechargeRequest;
import com.example.yiliaoxitong.exception.BusinessException;
import com.example.yiliaoxitong.mapper.PatientWalletMapper;
import com.example.yiliaoxitong.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 钱包服务实现。充值与扣款均为条件更新，并发下余额不会为负；
 * 沙箱环境统一付款密码 123456，不产生真实扣款。
 */
@Service
public class WalletServiceImpl implements WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletServiceImpl.class);
    private static final String SANDBOX_PAY_PASSWORD = "123456";

    private final PatientWalletMapper walletMapper;

    public WalletServiceImpl(PatientWalletMapper walletMapper) {
        this.walletMapper = walletMapper;
    }

    @Override
    public BigDecimal balanceOf(Long patientId) {
        BigDecimal balance = walletMapper.balanceOf(patientId);
        return balance == null ? BigDecimal.ZERO : balance;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal recharge(Long patientId, RechargeRequest request) {
        if (!SANDBOX_PAY_PASSWORD.equals(request.getPayPassword())) {
            throw new BusinessException("付款密码错误，请重新输入");
        }
        walletMapper.credit(patientId, request.getAmount());
        BigDecimal balance = walletMapper.balanceOf(patientId);
        log.info("钱包充值成功：patientId={}, amount={}, balance={}", patientId, request.getAmount(), balance);
        return balance;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deduct(Long patientId, BigDecimal amount) {
        int deducted = walletMapper.deduct(patientId, amount);
        if (deducted == 0) {
            throw new BusinessException(
                    "余额不足，需支付 " + amount + " 元，请先前往「我的钱包」充值");
        }
        log.info("钱包扣款成功：patientId={}, amount={}", patientId, amount);
    }
}
