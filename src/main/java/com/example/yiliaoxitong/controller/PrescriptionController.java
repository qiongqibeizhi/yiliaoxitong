package com.example.yiliaoxitong.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.yiliaoxitong.common.Result;
import com.example.yiliaoxitong.security.RequireRole;
import com.example.yiliaoxitong.security.Role;
import com.example.yiliaoxitong.dto.PrescriptionRequest;
import com.example.yiliaoxitong.entity.Prescription;
import com.example.yiliaoxitong.entity.PrescriptionItem;
import com.example.yiliaoxitong.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 电子处方接口：开方（扣库存事务）、处方查询、处方明细查询。
 */
@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @GetMapping
    @RequireRole({Role.DOCTOR, Role.ADMIN})
    public Result<IPage<Prescription>> page(@RequestParam(required = false) Long registrationId,
                                            @RequestParam(required = false) Long patientId,
                                            @RequestParam(defaultValue = "1") long pageNum,
                                            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(prescriptionService.page(registrationId, patientId, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    @RequireRole({Role.DOCTOR, Role.ADMIN})
    public Result<Prescription> detail(@PathVariable Long id) {
        return Result.ok(prescriptionService.detail(id));
    }

    @GetMapping("/{id}/items")
    @RequireRole({Role.DOCTOR, Role.ADMIN})
    public Result<List<PrescriptionItem>> items(@PathVariable Long id) {
        return Result.ok(prescriptionService.items(id));
    }

    @PostMapping
    @RequireRole({Role.DOCTOR, Role.ADMIN})
    public Result<Prescription> create(@Valid @RequestBody PrescriptionRequest request) {
        return Result.ok(prescriptionService.create(request));
    }
}
