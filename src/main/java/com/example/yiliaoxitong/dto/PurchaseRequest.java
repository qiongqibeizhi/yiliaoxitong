package com.example.yiliaoxitong.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 患者单独购药请求。
 */
public class PurchaseRequest {

    @NotNull(message = "请选择要购买的药品")
    private Long drugId;

    @NotNull(message = "请填写购买数量")
    @Min(value = 1, message = "购买数量至少为 1")
    @Max(value = 999, message = "单笔购买数量不能超过 999")
    private Integer quantity;

    /** 管理员代客下单时指定患者编号；患者下单时忽略并取自登录令牌 */
    private Long patientId;

    public Long getDrugId() {
        return drugId;
    }

    public void setDrugId(Long drugId) {
        this.drugId = drugId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }
}
