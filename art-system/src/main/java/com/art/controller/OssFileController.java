package com.art.controller;

import com.art.annotation.ApiLog;
import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.domain.OssFile;
import com.art.domain.vo.OssFileVO;
import com.art.enums.ApiOperationType;
import com.art.service.OssFileService;
import com.art.storage.ObjectStorageObject;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 对象存储文件控制器。
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@RestController
@RequestMapping("/oss/file")
@Tag(name = "对象存储文件管理", description = "对象存储上传、下载、删除接口")
public class OssFileController {

    /**
     * 文件服务
     */
    private final OssFileService ossFileService;

    /**
     * 构造函数。
     *
     * @param ossFileService 文件服务
     */
    public OssFileController(OssFileService ossFileService) {
        this.ossFileService = ossFileService;
    }

    /**
     * 上传文件。
     *
     * @param file      文件
     * @param directory 目录
     * @return 文件信息
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传文件", description = "上传文件到当前启用的对象存储")
    @ApiLog(module = "对象存储文件", operationType = ApiOperationType.INSERT, description = "上传文件")
    public HttpResult<OssFileVO> upload(@RequestParam("file") MultipartFile file,
                                        @RequestParam(value = "directory", required = false) String directory) {
        return HttpResult.success(this.ossFileService.upload(file, directory));
    }

    /**
     * 分页查询文件信息。
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询上传文件信息", description = "分页查询上传文件信息")
    public HttpResult<Page<OssFileVO>> page(Page<OssFileVO> page, OssFileVO vo) {
        return HttpResult.success(this.ossFileService.queryPage(page, vo));
    }

    /**
     * 下载文件。
     *
     * @param id 文件ID
     * @return 文件流
     */
    @GetMapping("/download/{id}")
    @Operation(summary = "下载文件", description = "根据文件ID下载文件")
    public ResponseEntity<InputStreamResource> download(@PathVariable("id") Long id) {
        OssFile file = this.ossFileService.getFileById(id);
        ObjectStorageObject object = this.ossFileService.downloadObject(id);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (object.contentType() != null) {
            try {
                mediaType = MediaType.parseMediaType(object.contentType());
            } catch (Exception ignored) {
            }
        }
        String encodedName = URLEncoder.encode(file.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .contentType(mediaType);
        if (object.contentLength() >= 0) {
            builder.contentLength(object.contentLength());
        }
        return builder.body(new InputStreamResource(object.inputStream()));
    }

    /**
     * 删除文件。
     *
     * @param tableRowVO 表格行VO
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除文件", description = "删除文件信息并删除对象存储中的文件")
    @ApiLog(module = "对象存储文件", operationType = ApiOperationType.DELETE, description = "删除文件")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        return HttpResult.success(this.ossFileService.delete(tableRowVO.getIdList()));
    }
}
