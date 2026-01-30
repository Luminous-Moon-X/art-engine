package com.art.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据表信息VO
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
public class DBTableVO {
    /**
     * 主键ID
     */
    private String id;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 表备注说明
     */
    private String tableDescription;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
