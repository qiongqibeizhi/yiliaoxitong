package com.example.yiliaoxitong.util;

import com.example.yiliaoxitong.security.TokenPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT 工具类（HS256）。为便于离线构建，签名与编码全部使用 JDK 自带能力实现，
 * 不依赖第三方 jjwt 库。
 *
 * <p>令牌结构为 Header.Payload.Signature 三段式，载荷携带用户名、角色、业务主体编号、
 * 签发时间、过期时间与 jti。注销时把 jti 放入内存黑名单直到其自然过期，
 * 实现“令牌销毁”，避免退出登录后旧令牌仍可继续访问。</p>
 */
@Component
public class JwtUtils {

    private static final String HMAC_ALG = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long expireMinutes;
    /** jti -> 过期时间戳，仅保存尚未过期的注销令牌 */
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public JwtUtils(ObjectMapper objectMapper,
                    @Value("${medical.jwt.secret:yiliaoxitong-jwt-secret-2026-change-me-in-production}") String secret,
                    @Value("${medical.jwt.expire-minutes:120}") long expireMinutes) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT 密钥长度至少 32 字节");
        }
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expireMinutes = expireMinutes;
    }

    public long getExpireSeconds() {
        return expireMinutes * 60;
    }

    /**
     * 签发令牌。
     */
    public String createToken(String username, String role, Long patientId, Long doctorId) {
        long now = Instant.now().getEpochSecond();
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", username);
        payload.put("role", role);
        if (patientId != null) {
            payload.put("pid", patientId);
        }
        if (doctorId != null) {
            payload.put("did", doctorId);
        }
        payload.put("iat", now);
        payload.put("exp", now + getExpireSeconds());
        payload.put("jti", UUID.randomUUID().toString().replace("-", ""));

        String head = encode(toJson(header).getBytes(StandardCharsets.UTF_8));
        String body = encode(toJson(payload).getBytes(StandardCharsets.UTF_8));
        String content = head + "." + body;
        return content + "." + sign(content);
    }

    /**
     * 校验并解析令牌：签名、过期时间、注销状态三重校验，任一不通过抛出 IllegalArgumentException。
     */
    public TokenPayload parse(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("令牌格式错误");
        }
        String content = parts[0] + "." + parts[1];
        if (!MessageDigest.isEqual(encode(signBytes(content)).getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("令牌签名校验失败");
        }
        Map<?, ?> claims;
        try {
            claims = objectMapper.readValue(decode(parts[1]), Map.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("令牌载荷解析失败");
        }
        long exp = ((Number) claims.get("exp")).longValue();
        if (exp <= Instant.now().getEpochSecond()) {
            throw new IllegalArgumentException("登录已过期，请重新登录");
        }
        String jti = String.valueOf(claims.get("jti"));
        Long revokedAt = blacklist.get(jti);
        if (revokedAt != null) {
            if (revokedAt <= Instant.now().getEpochSecond()) {
                blacklist.remove(jti);
            } else {
                throw new IllegalArgumentException("令牌已注销，请重新登录");
            }
        }
        return new TokenPayload(
                String.valueOf(claims.get("sub")),
                String.valueOf(claims.get("role")),
                claims.get("pid") == null ? null : ((Number) claims.get("pid")).longValue(),
                claims.get("did") == null ? null : ((Number) claims.get("did")).longValue(),
                jti,
                exp);
    }

    /**
     * 注销令牌：加入黑名单，直到自然过期。
     */
    public void invalidate(TokenPayload payload) {
        blacklist.put(payload.jti(), payload.expiresAt());
        cleanExpired();
    }

    private void cleanExpired() {
        long now = Instant.now().getEpochSecond();
        blacklist.entrySet().removeIf(e -> e.getValue() <= now);
    }

    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new IllegalStateException("令牌序列化失败", e);
        }
    }

    private String sign(String content) {
        return encode(signBytes(content));
    }

    private byte[] signBytes(String content) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(secret, HMAC_ALG));
            return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("令牌签名失败", e);
        }
    }

    private String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
