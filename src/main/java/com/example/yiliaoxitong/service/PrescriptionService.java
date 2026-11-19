package com.example.yiliaoxitong.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.yiliaoxitong.dto.PrescriptionRequest;
import com.example.yiliaoxitong.entity.Prescription;
import com.example.yiliaoxitong.entity.PrescriptionItem;

import java.util.List;

/**
 * 处方服务：开方事务（处方 + 明细 + 库存扣减免）与处方查询。
 */
public interface PrescriptionService {

    Prescription create(PrescriptionRequest request);

    IPage<Prescription> page(Long registrationId, Long patientId, Long doctorId, long pageNum, long pageSize);

    Prescription detail(Long id);

    List<PrescriptionItem> items(Long prescriptionId);

    /** 患者凭余额支付处方药费 */
    Prescription pay(Long patientId, Long prescriptionId);

    /** 处方应付总额 = 明细单价 × 数量之和 */
    java.math.BigDecimal totalAmount(Long prescriptionId);
}
