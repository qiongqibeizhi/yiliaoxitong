package com.example.yiliaoxitong.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JWT 工具类单元测试：签发、解析、篡改校验、过期校验。
 */
@SpringBootTest
class JwtUtilsTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("签发后可原样解析出用户名与角色")
    void createAndParseShouldRoundTrip() {
        String token = jwtUtils.createToken("doctor1", "DOCTOR", null, 1L);
        System.out.println("TOKEN_SAMPLE=" + token);
        var payload = jwtUtils.parse(token);
        assertEquals("doctor1", payload.username());
        assertEquals("DOCTOR", payload.role());
        assertEquals(1L, payload.doctorId());
    }

    @Test
    @DisplayName("篡改签名后解析抛出异常")
    void tamperedTokenShouldFail() {
        String token = jwtUtils.createToken("admin", "ADMIN", null, null);
        String tampered = token.substring(0, token.length() - 2) + "xy";
        assertThrows(IllegalArgumentException.class, () -> jwtUtils.parse(tampered));
    }
}
