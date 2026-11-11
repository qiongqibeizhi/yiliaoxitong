package com.example.yiliaoxitong.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 让 JSON 响应头显式带上 charset=UTF-8。
 * 中文版浏览器（默认 GBK）在缺少 charset 时会把 UTF-8 的 JSON 按 GBK 解码，出现乱码。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        MediaType jsonUtf8 = new MediaType("application", "json", StandardCharsets.UTF_8);
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                jacksonConverter.setSupportedMediaTypes(List.of(jsonUtf8));
            }
        }
    }
}
