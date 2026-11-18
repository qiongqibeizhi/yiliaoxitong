package com.example.yiliaoxitong.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.yiliaoxitong.common.Result;
import com.example.yiliaoxitong.security.RequireRole;
import com.example.yiliaoxitong.security.Role;
import com.example.yiliaoxitong.dto.RegistrationQuery;
import com.example.yiliaoxitong.dto.RegistrationRequest;
import com.example.yiliaoxitong.entity.Registration;
import com.example.yiliaoxitong.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 挂号接口：在线挂号（事务）、取消挂号、挂号记录查询。
 */
@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping
    @RequireRole({Role.ADMIN, Role.DOCTOR})
    public Result<IPage<Registration>> page(RegistrationQuery query) {
        return Result.ok(registrationService.page(query));
    }

    @GetMapping("/{id}")
    @RequireRole({Role.ADMIN, Role.DOCTOR})
    public Result<Registration> detail(@PathVariable Long id) {
        return Result.ok(registrationService.detail(id));
    }

    @PostMapping
    @RequireRole({Role.PATIENT, Role.ADMIN, Role.DOCTOR})
    public Result<Registration> register(@Valid @RequestBody RegistrationRequest request) {
        return Result.ok(registrationService.register(request));
    }

    @PutMapping("/{id}/cancel")
    @RequireRole({Role.PATIENT, Role.ADMIN})
    public Result<Void> cancel(@PathVariable Long id) {
        registrationService.cancel(id);
        return Result.ok();
    }
}
