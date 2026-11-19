package com.example.yiliaoxitong.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 安全认证集成测试：JWT 签发、全局拦截、RBAC 越权拦截、注销销毁与字段脱敏。
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String login(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("username", username, "password", password));
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return node.path("data").path("token").asText();
    }

    @Test
    @DisplayName("三角色登录：签发令牌并返回角色")
    void loginShouldIssueTokenForEachRole() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("username", "admin", "password", "123456"));
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.expiresIn").value(7200));

        assertTrue(!login("doctor1", "123456").isBlank());
        assertTrue(!login("patient1", "123456").isBlank());
    }

    @Test
    @DisplayName("口令错误：401 与统一中文提示")
    void wrongPasswordShouldBeRejected() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("username", "admin", "password", "1234567"));
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("未登录访问患者档案：401")
    void protectedApiWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("未登录")));
    }

    @Test
    @DisplayName("篡改令牌：签名校验失败，401")
    void tamperedTokenShouldBeRejected() throws Exception {
        String token = login("admin", "123456");
        mockMvc.perform(get("/api/patients").header(HttpHeaders.AUTHORIZATION, "Bearer " + token + "x"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("RBAC：患者角色不能读取患者档案，403")
    void patientRoleShouldNotReadPatientRecords() throws Exception {
        String token = login("patient1", "123456");
        mockMvc.perform(get("/api/patients").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("无权访问该资源"));
    }

    @Test
    @DisplayName("RBAC：患者角色不能开处方，403")
    void patientRoleShouldNotCreatePrescription() throws Exception {
        String token = login("patient1", "123456");
        String body = objectMapper.writeValueAsString(Map.of("registrationId", 1, "diagnosis", "测试", "items", java.util.List.of()));
        mockMvc.perform(post("/api/prescriptions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("字段脱敏：医生看到打码的身份证与手机号，管理员看到完整值")
    void sensitiveFieldsShouldBeMaskedByRole() throws Exception {
        String doctorToken = login("doctor1", "123456");
        mockMvc.perform(get("/api/patients/1").header(HttpHeaders.AUTHORIZATION, "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.idCard").value("430102********1234"))
                .andExpect(jsonPath("$.data.phone").value("139****2001"));

        String adminToken = login("admin", "123456");
        mockMvc.perform(get("/api/patients/1").header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.idCard").value("430102199003031234"))
                .andExpect(jsonPath("$.data.phone").value("13900002001"));
    }

    @Test
    @DisplayName("注销：令牌进入黑名单，再次使用返回 401")
    void logoutShouldInvalidateToken() throws Exception {
        String token = login("patient1", "123456");
        mockMvc.perform(post("/api/auth/logout").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/prescriptions").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("公开目录：未登录也可查询科室与排班")
    void publicCatalogShouldBeReadableWithoutToken() throws Exception {
        mockMvc.perform(get("/api/departments").param("pageSize", "1"))
                .andExpect(status().isOk());
    }
}
