package com.art.config;

import com.art.common.BaseEntity;
import com.art.utils.SecurityUtil;
import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import com.mybatisflex.core.FlexGlobalConfig;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Flex 全局自动填充配置类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Configuration
public class FlexAutoFillConfiguration {

    /**
     * 构造函数 - 配置自动填充监听器
     */
    public FlexAutoFillConfiguration() {
        // 创建监听器
        MyInsertListener insertListener = new MyInsertListener();
        MyUpdateListener updateListener = new MyUpdateListener();

        // BaseEntity启用
        FlexGlobalConfig config = FlexGlobalConfig.getDefaultConfig();
        config.registerInsertListener(insertListener, BaseEntity.class);
        config.registerUpdateListener(updateListener, BaseEntity.class);
    }

    /**
     * 自定义插入监听器
     */
    static class MyInsertListener implements InsertListener {

        @Override
        public void onInsert(Object entity) {
            if (entity instanceof BaseEntity baseEntity) {
                baseEntity.setCreateId(SecurityUtil.getUserId());
                baseEntity.setCreateTime(LocalDateTime.now());
                baseEntity.setDeleteFlag(0);
            }
        }
    }

    /**
     * 自定义更新监听器
     */
    static class MyUpdateListener implements UpdateListener {

        @Override
        public void onUpdate(Object entity) {
            if (entity instanceof BaseEntity baseEntity) {
                baseEntity.setUpdateId(SecurityUtil.getUserId());
                baseEntity.setUpdateTime(LocalDateTime.now());
            }
        }
    }
}
