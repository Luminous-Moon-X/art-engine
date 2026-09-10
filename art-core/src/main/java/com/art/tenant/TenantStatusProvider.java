package com.art.tenant;

import java.time.LocalDate;

/**
 * 租户状态提供者
 *
 * <p>由 art-system 模块的租户服务实现，供 art-core 的请求拦截器在每个请求上
 * 校验当前生效租户是否仍然可用（启用且未到期）。实现方应做短时缓存，
 * 避免每个请求都查询数据库。</p>
 *
 * @author Luminous.X
 * @since 2.0.0
 */
@FunctionalInterface
public interface TenantStatusProvider {

    /**
     * 查询租户状态
     *
     * @param tenantId 租户ID
     * @return 租户状态（租户不存在时返回null）
     */
    TenantStatus getStatus(Long tenantId);

    /**
     * 租户状态快照
     *
     * @param enableFlag 启用标识（1启用 0禁用）
     * @param expireDate 到期日期（可为null，表示不限制）
     */
    record TenantStatus(Integer enableFlag, LocalDate expireDate) {

        /**
         * 租户是否已禁用
         *
         * @return 是否禁用
         */
        public boolean isDisabled() {
            return enableFlag != null && enableFlag != 1;
        }

        /**
         * 租户是否已到期
         *
         * @return 是否到期
         */
        public boolean isExpired() {
            return expireDate != null && expireDate.isBefore(LocalDate.now());
        }
    }
}
