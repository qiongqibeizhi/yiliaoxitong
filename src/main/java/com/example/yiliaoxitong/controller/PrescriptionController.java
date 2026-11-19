package com.example.yiliaoxitong.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.yiliaoxitong.common.Result;
import com.example.yiliaoxitong.entity.Prescription;
import com.example.yiliaoxitong.entity.PrescriptionItem;
import com.example.yiliaoxitong.exception.BusinessException;
import com.example.yiliaoxitong.security.AuthContext;
import com.example.yiliaoxitong.security.RequireRole;
import com.example.yiliaoxitong.security.Role;
import com.example.yiliaoxitong.security.TokenPayload;
import com.example.yiliaoxitong.dto.PrescriptionRequest;
import com.example.yiliaoxitong.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 电子处方接口：开方（扣库存事务）、处方查询、处方明细查询。
 *
 * <p>数据权限：医生只能查看/处理本人开具的处方并只能为本人接诊的挂号开方，
 * 患者只能查看本人的处方（作为单独购药的依据），管理员可见全部。</p>
 */
@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @GetMapping
    @RequireRole({Role.DOCTOR, Role.ADMIN, Role.PATIENT})
    public Result<IPage<Prescription>> page(@RequestParam(required = false) Long registrationId,
                                            @RequestParam(required = false) Long patientId,
                                            @RequestParam(defaultValue = "1") long pageNum,
                                            @RequestParam(defaultValue = "10") long pageSize) {
        TokenPayload user = AuthContext.get();
        Long doctorId = null;
        if (user != null) {
            if (Role.DOCTOR.name().equals(user.role()) && user.doctorId() != null) {
                doctorId = user.doctorId();
            } else if (Role.PATIENT.name().equals(user.role()) && user.patientId() != null) {
                patientId = user.patientId();
            }
        }
        return Result.ok(prescriptionService.page(registrationId, patientId, doctorId, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    @RequireRole({Role.DOCTOR, Role.ADMIN, Role.PATIENT})
    public Result<Prescription> detail(@PathVariable Long id) {
        Prescription prescription = prescriptionService.detail(id);
        checkOwnership(prescription);
        return Result.ok(prescription);
    }

    @GetMapping("/{id}/items")
    @RequireRole({Role.DOCTOR, Role.ADMIN, Role.PATIENT})
    public Result<List<PrescriptionItem>> items(@PathVariable Long id) {
        checkOwnership(prescriptionService.detail(id));
        return Result.ok(prescriptionService.items(id));
    }

    @PostMapping
    @RequireRole({Role.DOCTOR, Role.ADMIN})
    public Result<Prescription> create(@Valid @RequestBody PrescriptionRequest request) {
        return Result.ok(prescriptionService.create(request));
    }

    /** 处方应付金额（明细单价 × 数量之和） */
    @GetMapping("/{id}/amount")
    @RequireRole({Role.DOCTOR, Role.ADMIN, Role.PATIENT})
    public Result<java.math.BigDecimal> amount(@PathVariable Long id) {
        checkOwnership(prescriptionService.detail(id));
        return Result.ok(prescriptionService.totalAmount(id));
    }

    /** 患者凭余额支付处方药费：医生开单后才能付款 */
    @PutMapping("/{id}/pay")
    @RequireRole(Role.PATIENT)
    public Result<Prescription> pay(@PathVariable Long id) {
        return Result.ok(prescriptionService.pay(AuthContext.get().patientId(), id));
    }

    /** 非 admin 访问他人处方时直接拒绝 */
    private void checkOwnership(Prescription prescription) {
        TokenPayload user = AuthContext.get();
        if (user == null || Role.ADMIN.name().equals(user.role())) {
            return;
        }
        if (Role.DOCTOR.name().equals(user.role())
                && user.doctorId() != null && !user.doctorId().equals(prescription.getDoctorId())) {
            throw new BusinessException("只能查看本人开具的处方");
        }
        if (Role.PATIENT.name().equals(user.role())
                && user.patientId() != null && !user.patientId().equals(prescription.getPatientId())) {
            throw new BusinessException("只能查看本人的处方");
        }
    }
}
