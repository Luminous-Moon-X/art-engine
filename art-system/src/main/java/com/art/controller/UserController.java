package com.art.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.art.annotation.ApiLog;
import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.common.TreeSelectVO;
import com.art.domain.User;
import com.art.domain.vo.UserInfoVO;
import com.art.domain.vo.UserVO;
import com.art.enums.ApiOperationType;
import com.art.service.SysUserService;
import com.art.service.UserService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户信息接口
 *
 * @author Luminous.X
 * @since 2025年12月15日
 */
@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {
    /**
     * 用户信息服务
     */
    private final SysUserService sysUserService;
    /**
     * 用户服务
     */
    private final UserService UserService;

    /**
     * 登录获取用户基本信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "登录获取用户基本信息", description = "登录获取用户基本信息")
    public HttpResult<UserInfoVO> info() {
        return HttpResult.success(sysUserService.info());
    }

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取用户信息", description = "根据ID获取用户信息")
    public HttpResult<User> getById(@PathVariable("id") Long id) {
        return HttpResult.success(this.UserService.selectById(id));
    }

    /**
     * 分页查询用户信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 分页数据对象
     */
    @PostMapping("/page")
    @SaCheckPermission("system:user:list")
    @Operation(summary = "分页查询用户信息", description = "分页查询用户信息")
    public HttpResult<Page<UserVO>> page(Page<UserVO> page, UserVO vo) {
        return HttpResult.success(this.UserService.queryPage(page, vo));
    }

    /**
     * 查询所有用户信息
     *
     * @return 用户信息列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询所有用户信息", description = "查询所有用户信息")
    @SaCheckPermission("system:user:list")
    public HttpResult<List<User>> list() {
        return HttpResult.success(this.UserService.selectList());
    }

    /**
     * 查询部门用户树
     *
     * @return 部门用户树
     */
    @GetMapping("/deptUserTree")
    @Operation(summary = "查询部门用户树", description = "查询部门用户树")
    public HttpResult<List<TreeSelectVO>> deptUserTree() {
        return HttpResult.success(this.UserService.deptUserTree());
    }

    /**
     * 查询用户树
     *
     * @return 用户树
     */
    @GetMapping("/userTree")
    @Operation(summary = "查询用户树", description = "查询用户树")
    public HttpResult<List<TreeSelectVO>> userTree() {
        return HttpResult.success(this.UserService.userTree());
    }

    /**
     * 新增用户
     *
     * @param vo 用户信息
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增用户", description = "新增用户")
    @ApiLog(module = "用户管理", operationType = ApiOperationType.INSERT, description = "新增用户")
    @SaCheckPermission("system:user:add")
    public HttpResult<Boolean> add(@RequestBody UserVO vo) {
        return HttpResult.success(UserService.add(vo));
    }

    /**
     * 编辑用户
     *
     * @param vo 用户信息
     * @return 编辑结果
     */
    @PutMapping("/edit")
    @Operation(summary = "编辑用户", description = "编辑用户")
    @ApiLog(module = "用户管理", operationType = ApiOperationType.UPDATE, description = "编辑用户")
    @SaCheckPermission("system:user:edit")
    public HttpResult<Boolean> edit(@RequestBody UserVO vo) {
        return HttpResult.success(UserService.edit(vo));
    }

    /**
     * 根据ID删除用户
     *
     * @param tableRowVO 表格行VO类
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除用户", description = "根据ID删除用户")
    @ApiLog(module = "用户管理", operationType = ApiOperationType.DELETE, description = "删除用户")
    @SaCheckPermission("system:user:delete")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        return HttpResult.success(this.UserService.delete(tableRowVO.getIdList()));
    }

    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     * @return 重置结果
     */
    @PutMapping("/resetDefaultPassword")
    @Operation(summary = "重置用户密码", description = "重置用户密码")
    @SaCheckPermission("system:user:resetPassword")
    @ApiLog(module = "用户管理", operationType = ApiOperationType.UPDATE, description = "重置用户密码")
    public HttpResult<Boolean> resetDefaultPassword(@RequestParam("userId") Long userId) {
        return HttpResult.success(this.UserService.resetDefaultPassword(userId));
    }

}
