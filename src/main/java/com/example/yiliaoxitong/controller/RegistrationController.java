package com.example.yiliaoxitong.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.yiliaoxitong.common.Result;
import com.example.yiliaoxitong.exception.BusinessException;
import com.example.yiliaoxitong.security.AuthContext;
import com.example.yiliaoxitong.security.RequireRole;
import com.example.yiliaoxitong.security.Role;
import com.example.yiliaoxitong.security.TokenPayload;
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
 * 挂号接口：在线挂号（事务）、取消挂号、完成就诊、挂号记录查询。
 *
 * <p>数据权限：医生只能查询/处理自己接诊的挂号记录，患者只能查看本人挂号，
 * 管理员可见全部记录。同一患者同一天可挂不同科室、不同医生的号，
 * 仅限制同一天重复挂同一位医生。</p>
 */
@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping
    @RequireRole({Role.ADMIN, Role.DOCTOR, Role.PATIENT})
    public Result<IPage<Registration>> page(RegistrationQuery query) {
        applyDataScope(query);
        return Result.ok(registrationService.page(query));
    }

    @GetMapping("/{id}")
    @RequireRole({Role.ADMIN, Role.DOCTOR, Role.PATIENT})
    public Result<Registration> detail(@PathVariable Long id) {
        Registration registration = registrationService.detail(id);
        checkOwnership(registration);
        return Result.ok(registration);
    }

    @PostMapping
    @RequireRole({Role.PATIENT, Role.ADMIN, Role.DOCTOR})
    public Result<Registration> register(@Valid @RequestBody RegistrationRequest request) {
        return Result.ok(registrationService.register(request));
    }

    @PutMapping("/{id}/cancel")
    @RequireRole({Role.PATIENT, Role.ADMIN})
    public Result<Void> cancel(@PathVariable Long id) {
        checkOwnership(registrationService.detail(id));
        registrationService.cancel(id);
        return Result.ok();
    }

    /** 医生接诊完成：将待就诊记录标记为已完成，作为对患者病症的处理闭环 */
    @PutMapping("/{id}/complete")
    @RequireRole({Role.DOCTOR, Role.ADMIN})
    public Result<Void> complete(@PathVariable Long id) {
        checkOwnership(registrationService.detail(id));
        registrationService.complete(id);
        return Result.ok();
    }

    /** 按角色收窄查询范围：医生只看自己的号、患者只看本人的号 */
    private void applyDataScope(RegistrationQuery query) {
        TokenPayload user = AuthContext.get();
        if (user == null) {
            return;
        }
        if (Role.DOCTOR.name().equals(user.role()) && user.doctorId() != null) {
            query.setDoctorId(user.doctorId());
        } else if (Role.PATIENT.name().equals(user.role()) && user.patientId() != null) {
            query.setPatientId(user.patientId());
        }
    }

    /** 非 admin 访问他人数据时直接拒绝 */
    private void checkOwnership(Registration registration) {
        TokenPayload user = AuthContext.get();
        if (user == null || Role.ADMIN.name().equals(user.role())) {
            return;
        }
        if (Role.DOCTOR.name().equals(user.role())
                && user.doctorId() != null && !user.doctorId().equals(registration.getDoctorId())) {
            throw new BusinessException("只能查看本人接诊的挂号记录");
        }
        if (Role.PATIENT.name().equals(user.role())
                && user.patientId() != null && !user.patientId().equals(registration.getPatientId())) {
            throw new BusinessException("只能查看本人的挂号记录");
        }
    }
}
