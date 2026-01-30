package com.art.service.impl;

import com.art.CodeGenerator;
import com.art.domain.vo.CodeGenVO;
import com.art.domain.vo.DBTableVO;
import com.art.mapper.GeneratorMapper;
import com.art.service.GeneratorService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 代码生成服务实现类
 *
 * @author Luminous
 * @since 1.0.0
 */
@Service
public class GeneratorServiceImpl implements GeneratorService {
    /**
     * 代码生成器Mapper
     */
    private final GeneratorMapper generatorMapper;

    /**
     * 代码生成器
     */
    private final CodeGenerator codeGenerator;

    /**
     * 构造方法
     *
     * @param generatorMapper 代码生成器Mapper
     * @param codeGenerator   代码生成器
     */
    public GeneratorServiceImpl(GeneratorMapper generatorMapper, CodeGenerator codeGenerator) {
        this.generatorMapper = generatorMapper;
        this.codeGenerator = codeGenerator;
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

    /**
     * 生成代码
     *
     * @param codeGenVO 代码生成信息
     * @return 是否成功
     */
    @Override
    public Boolean generateCode(CodeGenVO codeGenVO) {
        return codeGenerator.generate(codeGenVO.getModuleName(), codeGenVO.getRootPackage(), codeGenVO.getTableNames(), codeGenVO.getAuthorName(), codeGenVO.getGenerateTypes());
    }
}
