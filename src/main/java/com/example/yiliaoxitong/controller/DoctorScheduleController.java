package com.example.yiliaoxitong.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.yiliaoxitong.common.Result;
import com.example.yiliaoxitong.security.RequireRole;
import com.example.yiliaoxitong.security.Role;
import com.example.yiliaoxitong.dto.ScheduleQuery;
import com.example.yiliaoxitong.entity.DoctorSchedule;
import com.example.yiliaoxitong.service.DoctorScheduleService;
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
 * 医生排班接口：分页查询、详情、新增、编辑、删除。
 */
@RestController
@RequestMapping("/api/schedules")
public class DoctorScheduleController {

    private final DoctorScheduleService scheduleService;

    public DoctorScheduleController(DoctorScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public Result<IPage<DoctorSchedule>> page(ScheduleQuery query) {
        return Result.ok(scheduleService.page(query));
    }

    @GetMapping("/{id}")
    public Result<DoctorSchedule> detail(@PathVariable Long id) {
        return Result.ok(scheduleService.detail(id));
    }

    @PostMapping
    @RequireRole(Role.ADMIN)
    public Result<DoctorSchedule> create(@Valid @RequestBody DoctorSchedule schedule) {
        return Result.ok(scheduleService.create(schedule));
    }

    @PutMapping("/{id}")
    @RequireRole(Role.ADMIN)
    public Result<DoctorSchedule> modify(@PathVariable Long id, @Valid @RequestBody DoctorSchedule schedule) {
        return Result.ok(scheduleService.update(id, schedule));
    }

    @DeleteMapping("/{id}")
    @RequireRole(Role.ADMIN)
    public Result<Void> remove(@PathVariable Long id) {
        scheduleService.delete(id);
        return Result.ok();
    }
}
