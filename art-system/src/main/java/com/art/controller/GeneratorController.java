package com.art.controller;

import com.art.art.common.HttpResult;
import com.art.domain.vo.CodeGenVO;
import com.art.domain.vo.DBTableVO;
import com.art.service.GeneratorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 代码生成器 控制器
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@RestController
@RequestMapping("/generator")
public class GeneratorController {
    /**
     * 代码生成器服务
     */
    private final GeneratorService generatorService;

    /**
     * 构造方法
     *
     * @param generatorService 代码生成器服务
     */
    public GeneratorController(GeneratorService generatorService) {
        this.generatorService = generatorService;
    }

    /**
     * 获取所有数据表信息
     *
     * @return 数据表信息
     */
    @GetMapping("tables")
    public HttpResult<List<DBTableVO>> tables() {
        return HttpResult.success(this.generatorService.tables());
    }

    /**
     * 生成代码
     *
     * @param codeGenVO 代码生成参数
     * @return 生成结果
     */
    @PostMapping("generateCode")
    public HttpResult<Boolean> generateCode(@RequestBody CodeGenVO codeGenVO) {
        return HttpResult.success(this.generatorService.generateCode(codeGenVO));
    }
}
