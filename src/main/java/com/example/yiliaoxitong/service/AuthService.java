package com.example.yiliaoxitong.service;

import com.example.yiliaoxitong.dto.LoginRequest;
import com.example.yiliaoxitong.dto.LoginResponse;
import com.example.yiliaoxitong.security.TokenPayload;

/**
 * 认证服务：登录签发令牌、注销销毁令牌、读取当前登录载荷。
 */
public interface AuthService {

    LoginResponse login(LoginRequest request);

    void logout(String authorizationHeader);

    TokenPayload current(String authorizationHeader);
}
