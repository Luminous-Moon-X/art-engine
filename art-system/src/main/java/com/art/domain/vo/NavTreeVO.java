package com.art.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 导航树VO对象
 *
 * @author Luminous.X
 * @since 0.0.1
 */
@Data
@SuppressWarnings("unused")
@AllArgsConstructor
@NoArgsConstructor
public class NavTreeVO {
    @JsonProperty("index")
    private Long index;
    /**
     * 导航名称
     */
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
     * 子导航
     */
    @JsonProperty("children")
    private List<NavTreeVO> children;
}
