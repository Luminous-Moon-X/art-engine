package com.art.controller;

import com.art.annotation.ApiLog;
import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.domain.vo.KnowledgeBaseVO;
import com.art.enums.ApiOperationType;
import com.art.service.KnowledgeBaseService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库管理控制器。
 *
 * @author Luminous.X
 * @since 1.3.1
 */
@RestController
@RequestMapping("/knowledge/base")
@Tag(name = "知识库管理", description = "知识库增删改查接口")
public class KnowledgeBaseController {

    /**
     * 知识库服务
     */
    private final KnowledgeBaseService knowledgeBaseService;

    /**
     * 构造函数。
     *
     * @param knowledgeBaseService 知识库服务
     */
    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    /**
     * 分页查询知识库信息。
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询知识库信息", description = "分页查询知识库信息")
    public HttpResult<Page<KnowledgeBaseVO>> page(Page<KnowledgeBaseVO> page, KnowledgeBaseVO vo) {
        return HttpResult.success(this.knowledgeBaseService.queryPage(page, vo));
    }

    /**
     * 新增知识库。
     *
     * @param vo 知识库信息
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增知识库", description = "新增知识库")
    @ApiLog(module = "知识库", operationType = ApiOperationType.INSERT, description = "新增知识库")
    public HttpResult<Boolean> add(@RequestBody KnowledgeBaseVO vo) {
        return HttpResult.success(this.knowledgeBaseService.add(vo));
    }

    /**
     * 编辑知识库。
     *
     * @param vo 知识库信息
     * @return 编辑结果
     */
    @PutMapping("/edit")
    @Operation(summary = "编辑知识库", description = "编辑知识库")
    @ApiLog(module = "知识库", operationType = ApiOperationType.UPDATE, description = "编辑知识库")
    public HttpResult<Boolean> edit(@RequestBody KnowledgeBaseVO vo) {
        return HttpResult.success(this.knowledgeBaseService.edit(vo));
    }

    /**
     * 删除知识库。
     *
     * @param tableRowVO 表格行VO
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除知识库", description = "删除知识库（知识库必须为空）")
    @ApiLog(module = "知识库", operationType = ApiOperationType.DELETE, description = "删除知识库")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        return HttpResult.success(this.knowledgeBaseService.delete(tableRowVO.getIdList()));
    }
}
