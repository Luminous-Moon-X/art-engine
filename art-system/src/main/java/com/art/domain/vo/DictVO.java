package com.art.domain.vo;

import com.art.annotation.Query;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典表VO
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Data
public class DictVO {
    /**
     * 字典ID
     */
    private Long id;
    /**
     * 字典名称
     */
    @Query(type = Query.Type.LIKE)
    private String dictName;
    /**
     * 字典编码
     */
    @Query(type = Query.Type.LIKE)
    private String dictCode;
    /**
     * 字典类型
     */
    private String dictType;
    /**
     * 启用标识
     */
    private Boolean enableFlag;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 备注
     */
    private String remark;
}
