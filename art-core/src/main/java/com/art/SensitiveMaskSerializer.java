package com.art;

import com.art.annotation.Sensitive;
import com.art.utils.SensitiveUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 脱敏序列化器
 *
 * @author Luminous.X
 * @since 1.3.3
 */
public class SensitiveMaskSerializer extends JsonSerializer<Object> {

    private final Sensitive sensitive;

    public SensitiveMaskSerializer(Sensitive sensitive) {
        this.sensitive = sensitive;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        String masked = SensitiveUtil.mask(
                value.toString(),
                sensitive.strategy(),
                sensitive.prefixLen(),
                sensitive.suffixLen(),
                sensitive.maskChar()
        );
        gen.writeString(masked);
    }
}
