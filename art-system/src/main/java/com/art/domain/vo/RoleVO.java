package com.art.domain.vo;

import com.art.annotation.Query;
import lombok.Data;

/**
 * 角色VO对象
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
public class RoleVO {
    /**
     * 角色ID
     */
    private Long id;
    /**
     * 角色名称
     */
    @Query(type = Query.Type.LIKE)
    private String roleName;
    /**
     * 角色编码
     */
    @Query(type = Query.Type.LIKE)
    private String roleCode;
    /**
     * 角色描述
     */
    @Query(type = Query.Type.LIKE)
    private String roleDescription;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 是否启用
     */
    private Boolean enableFlag;
}
