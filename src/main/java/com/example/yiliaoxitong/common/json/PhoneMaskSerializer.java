package com.example.yiliaoxitong.common.json;

import com.example.yiliaoxitong.security.AuthContext;
import com.example.yiliaoxitong.util.DesensitizeUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 手机号脱敏序列化器：管理员可见完整号码，其他角色只看到前 3 位与后 4 位。
 */
public class PhoneMaskSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeString(AuthContext.isAdmin() ? value : DesensitizeUtils.maskPhone(value));
    }
}
