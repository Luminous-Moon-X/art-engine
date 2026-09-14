package com.art.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.domain.MenuLog;
import com.art.domain.vo.MenuLogVO;
import com.art.service.MenuLogService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 菜单日志接口
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@RestController
@AllArgsConstructor
@RequestMapping("/log/menu")
@Tag(name = "菜单日志管理")
public class MenuLogController {
    /**
     * 菜单日志服务
     */
    private final MenuLogService menuLogService;

    /**
     * 分页查询菜单日志
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询菜单日志", description = "分页查询菜单日志")
    @SaCheckPermission("system:menu-log:list")
    public HttpResult<Page<MenuLog>> page(Page<MenuLog> page, MenuLogVO vo) {
        return HttpResult.success(this.menuLogService.queryPage(page, vo));
    }

    /**
     * 记录菜单日志
     *
     * @param menuName 菜单名称
     * @param menuPath 菜单路径
     * @return 记录结果
     */
    @PostMapping("/record")
    @Operation(summary = "记录菜单日志", description = "记录菜单点击日志")
    public HttpResult<Boolean> record(@RequestParam("menuName") String menuName,
                                      @RequestParam("menuPath") String menuPath) {
        this.menuLogService.recordMenuLog(menuName, menuPath);
        return HttpResult.success(true);
    }

    /**
     * 根据ID删除菜单日志
     *
     * @param tableRowVO 表格行VO类
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除菜单日志", description = "根据ID删除菜单日志")
    @SaCheckPermission("system:menu-log:delete")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        return HttpResult.success(this.menuLogService.removeByIds(tableRowVO.getIdList()));
    }
}
