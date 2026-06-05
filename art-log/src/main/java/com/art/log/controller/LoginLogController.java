package com.art.log.controller;

import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.log.domain.LoginLog;
import com.art.log.domain.vo.LoginLogVO;
import com.art.log.service.LoginLogService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 登录日志接口
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@RestController
@AllArgsConstructor
@RequestMapping("/log/login")
@Tag(name = "登录日志管理")
public class LoginLogController {
    /**
     * 登录日志服务
     */
    private final LoginLogService loginLogService;

    /**
     * 分页查询登录日志
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询登录日志", description = "分页查询登录日志")
    public HttpResult<Page<LoginLog>> page(Page<LoginLog> page, LoginLogVO vo) {
        return HttpResult.success(this.loginLogService.queryPage(page, vo));
    }

    /**
     * 根据ID删除登录日志
     *
     * @param tableRowVO 表格行VO类
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除登录日志", description = "根据ID删除登录日志")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        return HttpResult.success(this.loginLogService.removeByIds(tableRowVO.getIdList()));
    }
}
