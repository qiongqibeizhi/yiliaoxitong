package com.example.yiliaoxitong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 患者钱包账户，对应 patient_wallet 表。一人一户，余额不允许为负；
 * 充值与支付均通过条件更新保证并发安全。
 */
@TableName("patient_wallet")
public class PatientWallet {

    /** 一人一户，主键即患者编号 */
    @TableId(value = "patient_id", type = IdType.INPUT)
    private Long patientId;

    private BigDecimal balance;

    @TableField("update_time")
    private LocalDateTime updateTime;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
