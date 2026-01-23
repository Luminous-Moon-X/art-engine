package com.art.controller;

import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.domain.User;
import com.art.domain.vo.UserInfoVO;
import com.art.domain.vo.UserVO;
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
    public HttpResult<List<User>> list() {
        return HttpResult.success(this.UserService.selectList());
    }

    /**
     * 新增用户
     *
     * @param vo 用户信息
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增用户", description = "新增用户")
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
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        return HttpResult.success(this.UserService.delete(tableRowVO.getIdList()));
    }

}
