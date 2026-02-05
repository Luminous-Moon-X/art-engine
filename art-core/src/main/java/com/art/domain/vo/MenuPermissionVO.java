package com.art.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 菜单权限信息
 *
 * @author Luminous.X
 * @since 1.1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuPermissionVO {
    /**
     * 授权主体类型：role、dept、user
     */
    private String type;
    /**
     * 授权主体 ID
     */
    private Long id;
    /**
     * 菜单权限标识列表
     */
    private List<String> permissionSignList;
}
