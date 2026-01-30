package com.art.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 菜单树VO对象
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuTreeVO {
    @JsonProperty("name")
    private String name;

    /**
     * 导航路径
     */
    @JsonProperty("path")
    private String path;
    /**
     * 组件路径
     */
    @JsonProperty("component")
    private String component;
    /**
     * 导航元数据
     */
    @JsonProperty("meta")
    private MenuMetaVO meta;
    /**
     * 下级菜单
     */
    @JsonProperty("children")
    private List<MenuTreeVO> children;
}
