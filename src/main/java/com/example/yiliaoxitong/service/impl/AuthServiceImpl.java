package com.example.yiliaoxitong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.yiliaoxitong.dto.LoginRequest;
import com.example.yiliaoxitong.dto.RegisterRequest;
import com.example.yiliaoxitong.dto.LoginResponse;
import com.example.yiliaoxitong.entity.Patient;
import com.example.yiliaoxitong.entity.SysUser;
import com.example.yiliaoxitong.mapper.PatientMapper;
import com.example.yiliaoxitong.mapper.PatientWalletMapper;
import com.example.yiliaoxitong.exception.BusinessException;
import com.example.yiliaoxitong.mapper.SysUserMapper;
import com.example.yiliaoxitong.security.TokenPayload;
import com.example.yiliaoxitong.service.AuthService;
import com.example.yiliaoxitong.util.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /** GB 11643 校验位算法：加权因子 + 模 11 映射 */
    private static boolean validIdCard(String idCard) {
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        String codes = "10X98765432";
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (idCard.charAt(i) - '0') * weights[i];
        }
        return codes.charAt(sum % 11) == Character.toUpperCase(idCard.charAt(17));
    }

    private final SysUserMapper userMapper;
    private final PatientMapper patientMapper;
    private final PatientWalletMapper walletMapper;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(SysUserMapper userMapper, PatientMapper patientMapper,
                           PatientWalletMapper walletMapper, JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.patientMapper = patientMapper;
        this.walletMapper = walletMapper;
        this.jwtUtils = jwtUtils;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request) {
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (exists > 0) {
            throw new BusinessException("用户名已被占用，请换一个用户名");
        }
        Long idCardUsed = patientMapper.selectCount(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getIdCard, request.getIdCard()));
        if (idCardUsed > 0) {
            throw new BusinessException("该身份证号已建档，请直接登录");
        }

        // 建患者档案：身份证校验位/出生日期校验 + 由身份证解析补齐出生日期
        Patient patient = new Patient();
        patient.setPatientNo("P" + String.format("%010d", Math.abs(System.nanoTime() % 10000000000L)));
        patient.setName(request.getName());
        patient.setGender(request.getGender());
        patient.setIdCard(request.getIdCard());
        patient.setPhone(request.getPhone());
        patient.setAddress(request.getAddress());
        java.time.LocalDate birthday;
        try {
            birthday = java.time.LocalDate.parse(
                    request.getIdCard().substring(6, 14), java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (java.time.format.DateTimeParseException e) {
            throw new BusinessException("身份证号无效：出生日期段不合法，请检查号码是否正确");
        }
        if (java.time.LocalDate.now().isBefore(birthday)) {
            throw new BusinessException("身份证号出生日期无效，请检查号码是否正确");
        }
        if (!validIdCard(request.getIdCard())) {
            throw new BusinessException("身份证号校验位不正确，请核对号码是否输错");
        }
        patient.setBirthDate(birthday);
        if (patientMapper.insert(patient) != 1) {
            throw new BusinessException("建档失败，请稍后重试");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(sha256(request.getPassword()));
        user.setRole("PATIENT");
        user.setRealName(request.getName());
        user.setPatientId(patient.getId());
        user.setStatus(1);
        if (userMapper.insert(user) != 1) {
            throw new BusinessException("开通账号失败，请稍后重试");
        }
        // 注册即开户，初始余额 0
        var wallet = new com.example.yiliaoxitong.entity.PatientWallet();
        wallet.setPatientId(patient.getId());
        wallet.setBalance(java.math.BigDecimal.ZERO);
        walletMapper.insert(wallet);
        log.info("患者注册成功：username={}, patientId={}", user.getUsername(), patient.getId());

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
        return response;
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
