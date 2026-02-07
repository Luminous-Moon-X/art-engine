package com.art.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜单操作权限VO类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuOperationPermissionVO {
    /**
     * 权限名称
     */
    private String title;
    /**
     * 权限标识
     */
    private String authMark;
}
