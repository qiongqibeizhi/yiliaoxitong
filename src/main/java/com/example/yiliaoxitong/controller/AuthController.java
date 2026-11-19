package com.example.yiliaoxitong.controller;

import com.example.yiliaoxitong.common.Result;
import com.example.yiliaoxitong.dto.LoginRequest;
import com.example.yiliaoxitong.dto.RegisterRequest;
import com.example.yiliaoxitong.dto.LoginResponse;
import com.example.yiliaoxitong.security.TokenPayload;
import com.example.yiliaoxitong.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：登录签发令牌、注销销毁令牌、查询当前登录信息。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    /** 患者自助注册：建档 + 开通账号，注册成功直接返回登录令牌 */
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(authService.register(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        authService.logout(authorization);
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<TokenPayload> me(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return Result.ok(authService.current(authorization));
    }
}
