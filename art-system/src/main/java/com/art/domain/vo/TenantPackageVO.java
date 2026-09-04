package com.art.domain.vo;

import com.art.annotation.Query;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户套餐VO（分页查询/新增/编辑）
 *
 * @author Luminous.X
 * @since 2.0.0
 */
@Data
public class TenantPackageVO {
    /**
     * 套餐ID
     */
    private Long id;
    /**
     * 套餐名称
     */
    @Query(type = Query.Type.LIKE)
    private String packageName;
    /**
     * 菜单权限标识集合（勾选的菜单，空=不限制菜单范围）
     */
    private List<String> permissionSigns;
    /**
     * 启用标识
     */
    private Integer enableFlag;
    /**
     * 备注
     */
    private String remark;
    /**
     * 勾选菜单数量（查询结果）
     */
    private Integer menuCount;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}