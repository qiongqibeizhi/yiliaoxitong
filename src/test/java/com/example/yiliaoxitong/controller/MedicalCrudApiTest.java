package com.example.yiliaoxitong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 科室 / 医生 / 患者 CRUD 接口的分层集成测试。
 * 使用 H2 内存库（MODE=MySQL）执行，测试方法级事务自动回滚。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MedicalCrudApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String withCheckCode(String first17) {
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] codes = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (first17.charAt(i) - '0') * weights[i];
        }
        return first17 + codes[sum % 11];
    }

    @Test
    void jsonResponseShouldDeclareUtf8Charset() throws Exception {
        mockMvc.perform(get("/api/departments").param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Content-Type", org.hamcrest.Matchers.containsString("charset=UTF-8")));
    }

    @Test
    void departmentPageQueryShouldReturnSeedData() throws Exception {
        mockMvc.perform(get("/api/departments").param("pageNum", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records[0].deptName").value("内科"));
    }

    @Test
    void departmentCrudShouldWorkEndToEnd() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("deptCode", "TEST01");
        body.put("deptName", "测试科室");
        body.put("location", "测试楼1层");
        body.put("phone", "0731-88889999");
        body.put("status", 1);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/departments").param("name", "测试科室"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].deptCode").value("TEST01"));

        body.put("deptName", "测试科室（已改名）");
        mockMvc.perform(put("/api/departments/{id}", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(delete("/api/departments/{id}", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/departments/{id}", 3L))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void duplicateDeptCodeShouldBeRejected() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("deptCode", "NK");
        body.put("deptName", "重复编码科室");
        body.put("status", 1);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("科室编码已存在")));
    }

    @Test
    void deleteDepartmentWithDoctorShouldBeRejected() throws Exception {
        mockMvc.perform(delete("/api/departments/{id}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("不能删除")));
    }

    @Test
    void patientWithIllegalPhoneShouldBeRejected() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("patientNo", "P9001");
        body.put("name", "测试患者");
        body.put("gender", "男");
        body.put("idCard", withCheckCode("43010219900303123"));
        body.put("phone", "12345678901");

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("手机号格式不正确")));
    }

    @Test
    void patientWithWrongIdCardCheckCodeShouldBeRejected() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("patientNo", "P9002");
        body.put("name", "测试患者");
        body.put("gender", "女");
        body.put("idCard", "430102199003031230");
        body.put("phone", "13900009999");

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("身份证号校验失败")));
    }

    @Test
    void patientCreateShouldFillBirthdayFromIdCard() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("patientNo", "P9003");
        body.put("name", "测试患者");
        body.put("gender", "女");
        body.put("idCard", withCheckCode("43010219900303123"));
        body.put("phone", "13900008888");

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/patients").param("keyword", "P9003"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].birthDate").value("1990-03-03"));
    }

    @Test
    void doctorPageQueryShouldFilterByDepartment() throws Exception {
        mockMvc.perform(get("/api/doctors").param("departmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].name").value("张明"));

        mockMvc.perform(get("/api/doctors").param("departmentId", "2"))
                .andExpect(jsonPath("$.data.total").value(0));
    }
}
