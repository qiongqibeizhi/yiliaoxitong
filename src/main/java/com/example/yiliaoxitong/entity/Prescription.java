package com.example.yiliaoxitong.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 电子处方实体，对应 prescription 表。
 */
@TableName("prescription")
public class Prescription {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("presc_no")
    private String prescNo;

    @TableField("registration_id")
    private Long registrationId;

    @TableField("patient_id")
    private Long patientId;

    @TableField("doctor_id")
    private Long doctorId;

    private String diagnosis;

    @TableField("presc_time")
    private LocalDateTime prescTime;

    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 出诊医生姓名（联查字段，不入库） */
    @TableField(exist = false)
    private String doctorName;

    /** 是否需要支付（含药品明细时为 true，联查字段） */
    @TableField(exist = false)
    private Boolean needPay;

    public Boolean getNeedPay() {
        return needPay;
    }

    public void setNeedPay(Boolean needPay) {
        this.needPay = needPay;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPrescNo() {
        return prescNo;
    }

    public void setPrescNo(String prescNo) {
        this.prescNo = prescNo;
    }

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public LocalDateTime getPrescTime() {
        return prescTime;
    }

    public void setPrescTime(LocalDateTime prescTime) {
        this.prescTime = prescTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
