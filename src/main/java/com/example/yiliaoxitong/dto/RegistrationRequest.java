package com.example.yiliaoxitong.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 挂号请求：患者选择某个排班（医生 + 日期 + 时段）完成挂号。
 */
public class RegistrationRequest {

    @NotNull(message = "患者 ID 不能为空")
    private Long patientId;

    @NotNull(message = "排班 ID 不能为空")
    private Long scheduleId;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }
}
