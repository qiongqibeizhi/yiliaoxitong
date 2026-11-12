package com.example.yiliaoxitong.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 处方明细请求：指定药品与数量，系统据此扣减库存。
 */
public class PrescriptionItemRequest {

    @NotNull(message = "药品 ID 不能为空")
    private Long drugId;

    @NotNull(message = "药品数量不能为空")
    @Min(value = 1, message = "药品数量至少为 1")
    private Integer quantity;

    private String dosage;

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

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }
}
