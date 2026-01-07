package com.art.service.impl;

import com.art.domain.vo.DBTableVO;
import com.art.mapper.GeneratorMapper;
import com.art.service.GeneratorService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 代码生成服务实现类
 *
 * @author Luminous
 * @since 0.0.1-SNAPSHOT
 */
@Service
public class GeneratorServiceImpl implements GeneratorService {
    /**
     * 代码生成器Mapper
     */
    private final GeneratorMapper generatorMapper;
    /**
     * 构造方法
     *
     * @param generatorMapper 代码生成器Mapper
     */
    public GeneratorServiceImpl(GeneratorMapper generatorMapper) {
        this.generatorMapper = generatorMapper;
    }

    /**
     * 获取数据库所有表信息
     *
     * @return 数据库所有表信息
     */
    @Override
    public List<DBTableVO> tables() {
        return this.generatorMapper.getDBTables();
    }
}
