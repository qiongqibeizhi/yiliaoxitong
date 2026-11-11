package com.example.yiliaoxitong.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 启动真实内嵌 Tomcat（随机端口），验证 JSON 响应头包含 charset=UTF-8，
 * 防止中文版浏览器按 GBK 解码造成乱码。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JsonCharsetTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void jsonResponseHeaderShouldContainUtf8Charset() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/departments?pageSize=1", String.class);
        HttpHeaders headers = response.getHeaders();
        String contentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);
        assertTrue(contentType != null && contentType.toUpperCase().contains("CHARSET=UTF-8"),
                "响应头应包含 charset=UTF-8，实际为：" + contentType);
        assertTrue(response.getBody() != null && response.getBody().contains("内科"),
                "响应体中中文应正常（UTF-8 解码后）");
    }
}
