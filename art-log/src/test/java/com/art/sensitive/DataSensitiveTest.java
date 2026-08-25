package com.art.sensitive;

import com.art.config.ApiLogConfig;
import com.art.utils.SensitiveUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据脱敏测试
 *
 * @author Luminous.X
 * @since 1.3.3
 */
public class DataSensitiveTest {
    /**
     * 测试实体字段数据脱敏
     *
     * @throws JsonProcessingException 测试异常
     */
    @Test
    public void testSensitive() throws JsonProcessingException {
        UserTest userTest = new UserTest();
        userTest.setPassword("A123456789");
        userTest.setPhone("13800138000");
        userTest.setEmail("690278565@qq.com");
        userTest.setCustomSensitive("1234567890");
        userTest.setIdCard("370982192004065478");
        userTest.setBankCardId("6217554602905641785");
        userTest.setCreateTime(LocalDateTime.now());
        UserTest userTest1 = SensitiveUtil.maskClass(userTest, UserTest.class);
        System.out.println(userTest1.toString());
    }

    /**
     * 测试集合字段数据脱敏
     *
     * @throws JsonProcessingException 测试异常
     */
    @Test
    public void testListSensitive() throws JsonProcessingException {
        List<UserTest> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UserTest userTest = new UserTest();
            userTest.setPassword("A123456789");
            userTest.setPhone("13800138000");
            userTest.setEmail("690278565@qq.com");
            userTest.setCustomSensitive("1234567890");
            userTest.setIdCard("370982192004065478");
            userTest.setBankCardId("6217554602905641785");
            userTest.setCreateTime(LocalDateTime.now());
            list.add(userTest);
        }

        ObjectMapper objectMapper = ApiLogConfig.apiLogObjectMapper();
        String s = objectMapper.writeValueAsString(list);
        System.out.println(s);
    }
}
