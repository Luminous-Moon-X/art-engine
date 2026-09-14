package com.art.config;

import com.art.context.SecurityContextHolder;
import com.art.properties.TenantProperties;
import com.mybatisflex.core.tenant.TenantFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 多租户配置类
 *
 * <p>注册全局唯一的 {@link TenantFactory}，MyBatis-Flex 会对带有
 * {@code @Column(tenantId = true)} 租户列的实体自动追加 {@code tenant_id} 过滤条件。</p>
 *
 * <ul>
 *     <li>多租户关闭（art.tenant.enable=false）时返回 null：不追加任何租户条件，行为与历史版本一致；</li>
 *     <li>多租户开启且线程上下文中存在租户ID时返回该租户ID；</li>
 *     <li>返回 null 时，新增数据的 tenant_id 保留实体上手动设置的值。</li>
 * </ul>
 *
 * @author Luminous.X
 * @since 2.0.0
 */
@Configuration
public class TenantConfiguration {

    /**
     * 全局租户工厂：从请求线程上下文获取当前生效租户ID
     *
     * @param tenantProperties 多租户配置
     * @return 租户工厂
     */
    @Bean
    public TenantFactory tenantFactory(TenantProperties tenantProperties) {
        return () -> {
            if (Boolean.FALSE.equals(tenantProperties.getEnable())) {
                // 未启用多租户：不进行租户过滤
                return null;
            }
            Long tenantId = SecurityContextHolder.getTenantId();
            if (tenantId == null) {
                // 无租户上下文（如登录等白名单请求）：不进行租户过滤
                return null;
            }
            return new Object[]{tenantId};
        };
    }
}