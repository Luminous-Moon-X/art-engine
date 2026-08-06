package com.art.domain.vo;

import com.art.annotation.Query;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租户VO对象
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
public class TenantVO {
    /**
     * 租户ID
     */
    private Long id;
    /**
     * 租户名称
     */
    @Query(type = Query.Type.LIKE)
    private String tenantName;
    /**
     * 过期时间
     */
    @Query(type = Query.Type.LE)
    private LocalDateTime expireTime;
    /**
     * 启用标志
     */
    @Query(type = Query.Type.EQ)
    private Integer enableFlag;
}
