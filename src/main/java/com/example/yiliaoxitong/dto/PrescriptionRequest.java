package com.example.yiliaoxitong.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 开处方请求：一次挂号生成一张处方，处方可包含多条药品明细。
 */
public class PrescriptionRequest {

    @NotNull(message = "挂号 ID 不能为空")
    private Long registrationId;

    @NotBlank(message = "诊断结果不能为空")
    private String diagnosis;

    @Valid
    /** 诊断记录允许不开药：明细可为空 */
    private List<PrescriptionItemRequest> items;

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public List<PrescriptionItemRequest> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItemRequest> items) {
        this.items = items;
    }
}
