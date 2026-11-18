package com.example.yiliaoxitong.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.yiliaoxitong.common.Result;
import com.example.yiliaoxitong.security.RequireRole;
import com.example.yiliaoxitong.security.Role;
import com.example.yiliaoxitong.dto.DoctorQuery;
import com.example.yiliaoxitong.entity.Doctor;
import com.example.yiliaoxitong.service.DoctorService;
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
 * 医生管理接口：支持姓名/工号模糊 + 科室 + 职称 + 状态组合分页查询。
 */
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public Result<IPage<Doctor>> page(DoctorQuery query) {
        return Result.ok(doctorService.pageQuery(query));
    }

    @GetMapping("/{id}")
    public Result<Doctor> detail(@PathVariable Long id) {
        return Result.ok(doctorService.getById(id));
    }

    @PostMapping
    @RequireRole(Role.ADMIN)
    public Result<Void> create(@Valid @RequestBody Doctor doctor) {
        doctorService.create(doctor);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @RequireRole(Role.ADMIN)
    public Result<Void> modify(@PathVariable Long id, @Valid @RequestBody Doctor doctor) {
        doctorService.modify(id, doctor);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireRole(Role.ADMIN)
    public Result<Void> remove(@PathVariable Long id) {
        doctorService.remove(id);
        return Result.ok();
    }
}
