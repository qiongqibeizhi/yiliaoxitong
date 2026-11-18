package com.example.yiliaoxitong.security;

/**
 * 当前请求的登录用户上下文。由 AuthInterceptor 在进入 Controller 前写入、
 * 请求结束后清除，业务层通过它判断角色并决定是否脱敏。
 */
public final class AuthContext {

    private static final ThreadLocal<TokenPayload> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(TokenPayload payload) {
        HOLDER.set(payload);
    }

    public static TokenPayload get() {
        return HOLDER.get();
    }

    public static String currentRole() {
        TokenPayload payload = HOLDER.get();
        return payload == null ? null : payload.role();
    }

    public static boolean isAdmin() {
        return Role.ADMIN.name().equals(currentRole());
    }

    public static void clear() {
        HOLDER.remove();
    }
}
