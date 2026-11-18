package com.example.yiliaoxitong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.yiliaoxitong.dto.LoginRequest;
import com.example.yiliaoxitong.dto.LoginResponse;
import com.example.yiliaoxitong.entity.SysUser;
import com.example.yiliaoxitong.exception.BusinessException;
import com.example.yiliaoxitong.mapper.SysUserMapper;
import com.example.yiliaoxitong.security.TokenPayload;
import com.example.yiliaoxitong.service.AuthService;
import com.example.yiliaoxitong.util.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * 认证服务实现：口令以 SHA-256 摘要比较，登录签发 JWT，注销把令牌加入黑名单。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final SysUserMapper userMapper;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(SysUserMapper userMapper, JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!MessageDigest.isEqual(sha256(request.getPassword()).getBytes(StandardCharsets.UTF_8),
                user.getPassword().getBytes(StandardCharsets.UTF_8))) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        String token = jwtUtils.createToken(user.getUsername(), user.getRole(),
                user.getPatientId(), user.getDoctorId());
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setExpiresIn(jwtUtils.getExpireSeconds());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setRealName(user.getRealName());
        response.setPatientId(user.getPatientId());
        response.setDoctorId(user.getDoctorId());
        log.info("登录成功：username={}, role={}", user.getUsername(), user.getRole());
        return response;
    }

    @Override
    public void logout(String authorizationHeader) {
        TokenPayload payload = current(authorizationHeader);
        jwtUtils.invalidate(payload);
        log.info("注销登录：username={}", payload.username());
    }

    @Override
    public TokenPayload current(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(401, "未登录或登录已过期，请重新登录");
        }
        try {
            return jwtUtils.parse(authorizationHeader.substring(BEARER_PREFIX.length()));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(401, e.getMessage());
        }
    }

    static String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("口令摘要失败", e);
        }
    }
}
