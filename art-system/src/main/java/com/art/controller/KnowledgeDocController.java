package com.art.controller;

import com.art.annotation.ApiLog;
import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.domain.vo.KnowledgeDocVO;
import com.art.enums.ApiOperationType;
import com.art.service.KnowledgeDocService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库文档管理控制器。
 *
 * @author Luminous.X
 * @since 1.3.1
 */
@RestController
@RequestMapping("/knowledge/doc")
@Tag(name = "知识库文档管理", description = "知识库文档上传、解析、向量处理接口")
public class KnowledgeDocController {

    /**
     * 知识库文档服务
     */
    private final KnowledgeDocService knowledgeDocService;

    /**
     * 构造函数。
     *
     * @param knowledgeDocService 知识库文档服务
     */
    public KnowledgeDocController(KnowledgeDocService knowledgeDocService) {
        this.knowledgeDocService = knowledgeDocService;
    }

    /**
     * 上传知识库文档。
     *
     * @param file 文档
     * @return 文档信息
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传知识库文档", description = "上传文档到对象存储并写入知识库文档表")
    @ApiLog(module = "知识库文档", operationType = ApiOperationType.INSERT, description = "上传知识库文档")
    public HttpResult<KnowledgeDocVO> upload(@RequestParam("file") MultipartFile file) {
        return HttpResult.success(this.knowledgeDocService.upload(file));
    }

    /**
     * 分页查询知识库文档信息。
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询知识库文档信息", description = "分页查询知识库文档信息")
    @ApiLog(module = "知识库文档", operationType = ApiOperationType.QUERY, description = "分页查询知识库文档信息")
    public HttpResult<Page<KnowledgeDocVO>> page(Page<KnowledgeDocVO> page, KnowledgeDocVO vo) {
        return HttpResult.success(this.knowledgeDocService.queryPage(page, vo));
    }

    /**
     * 解析文档。
     *
     * @param id 文档ID
     * @return 解析结果
     */
    @PostMapping("/parse/{id}")
    @Operation(summary = "解析文档", description = "下载文档并解析内容存储到文档内容表")
    @ApiLog(module = "知识库文档", operationType = ApiOperationType.UPDATE, description = "解析文档")
    public HttpResult<Boolean> parse(@PathVariable("id") Long id) {
        this.knowledgeDocService.parse(id);
        return HttpResult.success(true);
    }

    /**
     * 向量处理文档。
     *
     * @param id 文档ID
     * @return 处理结果
     */
    @PostMapping("/vector/{id}")
    @Operation(summary = "向量处理文档", description = "分割文档内容并调用向量库处理向量")
    @ApiLog(module = "知识库文档", operationType = ApiOperationType.UPDATE, description = "向量处理文档")
    public HttpResult<Boolean> vector(@PathVariable("id") Long id) {
        this.knowledgeDocService.vectorize(id);
        return HttpResult.success(true);
    }

    /**
     * 删除文档。
     *
     * @param tableRowVO 表格行VO
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除文档", description = "删除文档、文档内容与对象存储中的文件")
    @ApiLog(module = "知识库文档", operationType = ApiOperationType.DELETE, description = "删除文档")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        return HttpResult.success(this.knowledgeDocService.delete(tableRowVO.getIdList()));
    }
}
