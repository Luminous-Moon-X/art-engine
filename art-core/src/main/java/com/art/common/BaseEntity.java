package com.art.common;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 基础实体类
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Data
public class BaseEntity {
    /**
     * 主键
     */
    @Id
    @Column("id")
    private Long id;
    /**
     * 创建人
     */
    @Column("create_id")
    private String createId;
    /**
     * 创建时间
     */
    @Column("create_time")
    private LocalDateTime createTime;
    /**
     * 修改人
     */
    @Column("update_id")
    private String updateId;
    /**
     * 修改时间
     */
    @Column("update_time")
    private LocalDateTime updateTime;
    /**
     * 删除标识
     */
    @Column("delete_flag")
    private Boolean deleteFlag;
    /**
     * 租户ID
     */
    @Column(value = "tenant_id", tenantId = true)
    private Long tenantId;
}
