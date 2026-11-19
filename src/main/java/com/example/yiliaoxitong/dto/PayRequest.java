package com.example.yiliaoxitong.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 虚拟收银台支付请求。沙箱环境统一支付密码为 123456，
 * 支付渠道支持微信支付 / 支付宝 / 银行卡。
 */
public class PayRequest {

    @NotBlank(message = "请输入付款密码")
    @Pattern(regexp = "^\\d{6}$", message = "付款密码应为 6 位数字")
    private String payPassword;

    /** 支付渠道：WECHAT / ALIPAY / BANK，默认微信支付 */
    @Pattern(regexp = "^(WECHAT|ALIPAY|BANK)$", message = "支付渠道仅支持 WECHAT / ALIPAY / BANK")
    private String payChannel = "WECHAT";

    /** 银行卡支付时的卡号（沙箱环境仅做格式校验） */
    @Pattern(regexp = "^$|^\\d{13,19}$", message = "银行卡号应为 13-19 位数字")
    private String bankCardNo;

    public String getPayPassword() {
        return payPassword;
    }

    public void setPayPassword(String payPassword) {
        this.payPassword = payPassword;
    }

    public String getPayChannel() {
        return payChannel;
    }

    public void setPayChannel(String payChannel) {
        this.payChannel = payChannel;
    }

    public String getBankCardNo() {
        return bankCardNo;
    }

    public void setBankCardNo(String bankCardNo) {
        this.bankCardNo = bankCardNo;
    }
}
