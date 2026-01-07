package com.art.mapper;

import com.art.domain.vo.DBTableVO;

import java.util.List;

/**
 * 代码生成器Mapper
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
public interface GeneratorMapper {
    /**
     * 获取数据库表信息
     *
     * @return 数据库表信息
     */
    List<DBTableVO> getDBTables();
}
