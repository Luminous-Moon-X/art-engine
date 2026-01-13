package com.art.controller;

import com.art.common.CommonDataVO;
import com.art.common.CommonSearchVO;
import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.kits.ConvertUtil;
import com.art.domain.vo.NavTreeVO;
import com.art.domain.vo.SystemNavVO;
import com.art.service.SysNavService;
import com.mybatisflex.core.paginate.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/nav")
public class NavController {
    /**
     * 导航表Service接口类
     */
    private final SysNavService sysNavService;

    /**
     * 构造器注入
     *
     * @param sysNavService 导航表Service接口类
     */
    public NavController(SysNavService sysNavService) {
        this.sysNavService = sysNavService;
    }

    /**
     * 根据ID获取导航信息
     *
     * @param id 导航ID
     * @return 导航信息
     */
    @GetMapping("/{id}")
    public HttpResult<SystemNavVO> getById(@PathVariable("id") Long id) {
        return HttpResult.success(ConvertUtil.convert(sysNavService.getById(id), SystemNavVO.class));
    }

    /**
     * 分页查询导航信息
     *
     * @param commonSearchVO 通用查询对象
     * @return 分页查询结果
     */
    @PostMapping("/queryPage")
    public HttpResult<Page<SystemNavVO>> queryPage(@RequestBody CommonSearchVO commonSearchVO) {
        return HttpResult.success(sysNavService.queryPage(commonSearchVO));
    }

    /**
     * 新增导航
     *
     * @param commonDataVO 通用数据类型
     * @return 新增结果
     */
    @PostMapping("/add")
    public HttpResult<Boolean> add(@RequestBody CommonDataVO commonDataVO) {
        return HttpResult.success(sysNavService.insertData(commonDataVO));
    }

    /**
     * 编辑导航
     *
     * @param commonDataVO 通用数据类型
     * @return 编辑结果
     */
    @PostMapping("/edit")
    public HttpResult<Boolean> edit(@RequestBody CommonDataVO commonDataVO) {
        return HttpResult.success(sysNavService.updateData(commonDataVO));
    }

    /**
     * 删除导航
     *
     * @param tableRowVO 表格行VO类
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        return HttpResult.success(sysNavService.removeByIds(tableRowVO.getIdList()));
    }

    /**
     * 根据父级ID查询子导航信息
     *
     * @param parentId 父级ID
     * @return 子导航集合
     */
    @GetMapping("/queryChildren")
    public HttpResult<List<SystemNavVO>> queryChildren(@RequestParam("parentId") String parentId) {
        return HttpResult.success(sysNavService.queryChildren(parentId));
    }

    /**
     * 获取当前用户导航信息
     *
     * @return 当前用户导航信息
     */
    @GetMapping("/navTree")
    public HttpResult<List<NavTreeVO>> navTree() {
        return HttpResult.success("success", sysNavService.navTree());
    }
}
