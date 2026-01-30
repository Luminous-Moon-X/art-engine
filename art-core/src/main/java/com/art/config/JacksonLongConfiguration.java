package com.art.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.NumberSerializer;

import java.io.IOException;

/**
 * Jackson Long 配置
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@JacksonStdImpl
public class JacksonLongConfiguration extends NumberSerializer {
    /**
     * 浏览器能识别的整数范围
     */
    private static final long MAX_SAFE_INTEGER = 9007199254740991L;
    private static final long MIN_SAFE_INTEGER = -9007199254740991L;
    /**
     * 静态实例
     */
    public static final JacksonLongConfiguration INSTANCE = new JacksonLongConfiguration(Number.class);

    public JacksonLongConfiguration(Class<? extends Number> rawType) {
        super(rawType);
    }

    /**
     * 序列化
     *
     * @param value    数值
     * @param gen      生成器
     * @param provider 提供者
     * @throws IOException 异常
     */
    @Override
    public void serialize(Number value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // 超出范围 序列化位字符串
        if (value.longValue() > MIN_SAFE_INTEGER && value.longValue() < MAX_SAFE_INTEGER) {
            super.serialize(value, gen, provider);
        } else {
            gen.writeString(value.toString());
        }
    }
}
