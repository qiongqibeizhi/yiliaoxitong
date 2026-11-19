package com.example.yiliaoxitong.controller;

import com.example.yiliaoxitong.common.Result;
import com.example.yiliaoxitong.dto.RechargeRequest;
import com.example.yiliaoxitong.security.AuthContext;
import com.example.yiliaoxitong.security.RequireRole;
import com.example.yiliaoxitong.security.Role;
import com.example.yiliaoxitong.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 患者钱包接口：查余额、沙箱充值（自定义金额 + 付款密码）。
 * 患者的余额支付（购药/处方）从本钱包扣款。
 */
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    @RequireRole(Role.PATIENT)
    public Result<BigDecimal> balance() {
        return Result.ok(walletService.balanceOf(AuthContext.get().patientId()));
    }

    /** 沙箱充值：付款密码统一 123456，金额自定义（0.01 - 5000 元） */
    @PostMapping("/recharge")
    @RequireRole(Role.PATIENT)
    public Result<BigDecimal> recharge(@Valid @RequestBody RechargeRequest request) {
        return Result.ok(walletService.recharge(AuthContext.get().patientId(), request));
    }
}
