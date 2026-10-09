/*
 *    Copyright 2024-2025, Warm-Flow (290631660@qq.com).
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
// Modified by Luminous.X on 2026.10.09
package org.dromara.warm.flow.orm.keygen;

import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.keygen.IKeyGenerator;
import com.mybatisflex.core.keygen.KeyGeneratorFactory;
import com.mybatisflex.core.keygen.impl.SnowFlakeIDKeyGenerator;
import com.mybatisflex.core.util.ConvertUtil;
import com.mybatisflex.core.util.StringUtil;
import org.dromara.warm.flow.core.invoker.FrameInvoker;
import org.dromara.warm.flow.core.keygen.KenGen;

/**
 * MybatisFlexIdGen
 *
 * @author warm
 */
public class MybatisFlexIdGen implements KenGen {

    /**
     * MyBatis-Flex 默认主键生成器（与框架默认的雪花算法保持一致）
     */
    private final IKeyGenerator defaultKeyGenerator = new SnowFlakeIDKeyGenerator();

    /**
     * 获取唯一ID
     *
     * @return id
     */
    @Override
    public synchronized long nextId() {
        return ConvertUtil.toLong(resolveKeyGenerator().generate(null, null));
    }

    /**
     * 解析 MyBatis-Flex 使用的主键生成器：
     * 优先使用容器中注册的生成器，其次使用全局配置中指定的生成器，最后回退到雪花算法
     *
     * @return 主键生成器
     */
    private IKeyGenerator resolveKeyGenerator() {
        IKeyGenerator bean = FrameInvoker.getBean(IKeyGenerator.class);
        if (bean != null) {
            return bean;
        }
        FlexGlobalConfig.KeyConfig keyConfig = FlexGlobalConfig.getDefaultConfig().getKeyConfig();
        if (keyConfig != null && StringUtil.hasText(keyConfig.getValue())) {
            IKeyGenerator keyGenerator = KeyGeneratorFactory.getKeyGenerator(keyConfig.getValue());
            if (keyGenerator != null) {
                return keyGenerator;
            }
        }
        return defaultKeyGenerator;
    }

}
