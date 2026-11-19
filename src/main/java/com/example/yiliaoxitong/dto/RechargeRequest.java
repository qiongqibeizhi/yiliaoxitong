package com.example.yiliaoxitong.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * 钱包充值请求：自定义金额 + 沙箱付款密码。
 */
public class RechargeRequest {

    @NotNull(message = "请填写充值金额")
    @DecimalMin(value = "0.01", message = "充值金额至少 0.01 元")
    @DecimalMax(value = "5000", message = "单笔充值不能超过 5000 元")
    private BigDecimal amount;

    @NotBlank(message = "请输入付款密码")
    @Pattern(regexp = "^\\d{6}$", message = "付款密码应为 6 位数字")
    private String payPassword;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPayPassword() {
        return payPassword;
    }

    public void setPayPassword(String payPassword) {
        this.payPassword = payPassword;
    }
}
