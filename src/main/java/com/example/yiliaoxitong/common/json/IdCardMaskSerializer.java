package com.example.yiliaoxitong.common.json;

import com.example.yiliaoxitong.security.AuthContext;
import com.example.yiliaoxitong.util.DesensitizeUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 身份证号脱敏序列化器：管理员可见完整号码，其他角色只看到前 6 位与后 4 位。
 */
@Component
public class IdCardMaskSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeString(AuthContext.isAdmin() ? value : DesensitizeUtils.maskIdCard(value));
    }
}
