package com.art.controller;

import com.art.annotation.ApiLog;
import com.art.common.HttpResult;
import com.art.domain.vo.CodeGenVO;
import com.art.domain.vo.DBTableVO;
import com.art.enums.ApiOperationType;
import com.art.service.GeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 代码生成器 控制器
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@RestController
@RequestMapping("/generator")
@Tag(name = "代码生成器", description = "代码生成器相关接口")
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
    @Operation(summary = "获取数据表信息", description = "获取数据表，用于生成对应表的代码")
    public HttpResult<List<DBTableVO>> tables() {
        return HttpResult.success(this.generatorService.tables());
    }

    /**
     * 生成代码
     *
     * @param codeGenVO 代码生成参数
     * @return 生成结果
     */
    @PostMapping("generate")
    @Operation(summary = "生成代码", description = "生成对应数据表的代码")
    @ApiLog(module = "代码生成器", operationType = ApiOperationType.INSERT, description = "生成代码")
    public HttpResult<Boolean> generateCode(@RequestBody CodeGenVO codeGenVO) {
        return HttpResult.success(this.generatorService.generateCode(codeGenVO));
    }
}
