package com.art;

import com.art.annotation.Sensitive;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.util.List;

/**
 * 脱敏模块
 *
 * @author Luminous.X
 * @since 1.3.3
 */
public class SensitiveLogModule extends SimpleModule {

    @Override
    public void setupModule(SetupContext context) {
        context.addBeanSerializerModifier(new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(
                    SerializationConfig config,
                    BeanDescription beanDesc,
                    List<BeanPropertyWriter> beanProperties) {

                for (BeanPropertyWriter writer : beanProperties) {
                    Sensitive sensitive = writer.getAnnotation(Sensitive.class);
                    if (sensitive != null) {
                        writer.assignSerializer(new SensitiveMaskSerializer(sensitive));
                    }
                }
                return beanProperties;
            }
        });
    }
}