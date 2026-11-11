package com.example.yiliaoxitong.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.yiliaoxitong.common.Result;
import com.example.yiliaoxitong.dto.PatientQuery;
import com.example.yiliaoxitong.entity.Patient;
import com.example.yiliaoxitong.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 患者管理接口：身份证、手机号格式校验在入参阶段完成。
 */
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public Result<IPage<Patient>> page(PatientQuery query) {
        return Result.ok(patientService.pageQuery(query));
    }

    @GetMapping("/{id}")
    public Result<Patient> detail(@PathVariable Long id) {
        return Result.ok(patientService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody Patient patient) {
        patientService.create(patient);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> modify(@PathVariable Long id, @Valid @RequestBody Patient patient) {
        patientService.modify(id, patient);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        patientService.remove(id);
        return Result.ok();
    }
}
