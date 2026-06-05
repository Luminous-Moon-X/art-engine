package com.art.log.controller;

import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.log.domain.ApiLog;
import com.art.log.domain.vo.ApiLogVO;
import com.art.log.service.ApiLogService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 接口日志接口
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@RestController
@AllArgsConstructor
@RequestMapping("/log/api")
@Tag(name = "接口日志管理")
public class ApiLogController {
    /**
     * 接口日志服务
     */
    private final ApiLogService apiLogService;

    /**
     * 分页查询接口日志
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询接口日志", description = "分页查询接口日志")
    public HttpResult<Page<ApiLog>> page(Page<ApiLog> page, ApiLogVO vo) {
        return HttpResult.success(this.apiLogService.queryPage(page, vo));
    }

    /**
     * 根据ID删除接口日志
     *
     * @param tableRowVO 表格行VO类
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除接口日志", description = "根据ID删除接口日志")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        return HttpResult.success(this.apiLogService.removeByIds(tableRowVO.getIdList()));
    }
}
