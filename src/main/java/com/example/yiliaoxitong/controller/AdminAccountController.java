package com.example.yiliaoxitong.controller;

import com.example.yiliaoxitong.common.Result;
import com.example.yiliaoxitong.dto.AdminAccountRequest;
import com.example.yiliaoxitong.dto.DoctorAccountRequest;
import com.example.yiliaoxitong.entity.SysUser;
import com.example.yiliaoxitong.security.RequireRole;
import com.example.yiliaoxitong.security.Role;
import com.example.yiliaoxitong.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 管理后台账号管理：医生/管理员账号由管理员统一开设 */
@RestController
@RequestMapping("/api/admin/accounts")
@RequireRole(Role.ADMIN)
public class AdminAccountController {

    private final AccountService accountService;

    public AdminAccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public Result<List<SysUser>> list(@RequestParam(required = false) String role) {
        return Result.ok(accountService.listAccounts(role));
    }

    @PostMapping("/doctor")
    public Result<SysUser> createDoctor(@Valid @RequestBody DoctorAccountRequest request) {
        return Result.ok(accountService.createDoctorAccount(request));
    }

    @PostMapping("/admin")
    public Result<SysUser> createAdmin(@Valid @RequestBody AdminAccountRequest request) {
        return Result.ok(accountService.createAdminAccount(request));
    }
}
