package com.example.yiliaoxitong.config;

import com.example.yiliaoxitong.common.Result;
import com.example.yiliaoxitong.security.AuthContext;
import com.example.yiliaoxitong.security.RequireRole;
import com.example.yiliaoxitong.security.TokenPayload;
import com.example.yiliaoxitong.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 全局认证拦截器：
 * 1. 解析 Authorization 头中的 Bearer 令牌，签名错误、过期、已注销一律 401；
 * 2. 未携带令牌时，仅放行科室、医生、排班、药品目录的 GET 公开查询；
 * 3. 把令牌载荷写入 AuthContext，供业务层做角色判断与字段脱敏；
 * 4. 读取 @RequireRole，角色不匹配返回 403。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);
    private static final String BEARER_PREFIX = "Bearer ";

    /** 公开只读路径：未登录也可查询的基础目录 */
    private static final List<String> PUBLIC_READ_PREFIXES = List.of(
            "/api/departments", "/api/doctors", "/api/schedules", "/api/drugs");

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(JwtUtils jwtUtils, ObjectMapper objectMapper) {
        this.jwtUtils = jwtUtils;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        TokenPayload payload = null;
        if (header != null && !header.isBlank()) {
            if (!header.startsWith(BEARER_PREFIX)) {
                return reject(response, 401, "令牌格式错误，应为 Bearer <token>");
            }
            try {
                payload = jwtUtils.parse(header.substring(BEARER_PREFIX.length()));
            } catch (IllegalArgumentException e) {
                log.warn("令牌校验失败：{} {}，原因：{}", request.getMethod(), request.getRequestURI(), e.getMessage());
                return reject(response, 401, e.getMessage());
            }
        }

        if (payload == null) {
            if (isPublicRead(request)) {
                return true;
            }
            return reject(response, 401, "未登录或登录已过期，请重新登录");
        }
        final TokenPayload principal = payload;
        AuthContext.set(principal);

        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole != null && Arrays.stream(requireRole.value())
                .noneMatch(role -> role.name().equalsIgnoreCase(principal.role()))) {
            log.warn("越权访问：user={}, role={}, uri={}", principal.username(), principal.role(), request.getRequestURI());
            return reject(response, 403, "无权访问该资源");
        }
        return true;
    }

    private boolean isPublicRead(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        return PUBLIC_READ_PREFIXES.stream().anyMatch(uri::startsWith);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private boolean reject(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(code, message)));
        return false;
    }
}
